package hoursofza.utils;


import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import hoursofza.listeners.EventWaiterListenerWrapper;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

@Component
public class DiscordUtils {
    private final EventWaiter waiter;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Getter
    private final Set<String> admins;

    DiscordUtils(EventWaiterListenerWrapper eventWaiterListenerWrapper, @Value("${owners}") String admins) {
        this.waiter = eventWaiterListenerWrapper.getEventWaiter();
        this.admins = new HashSet<>(Arrays.asList(admins.split(",")));
    }

    /**
     * Awaits a reaction for a message.
     *
     * @param isValidReaction A Predicate that is given a MessageReactionAddEvent.
     *                        Returns whether the action should be executed.
     * @param action          A Predicate that performs the action. Returning true will keep the reaction alive.
     */
    public void awaitReaction(Message message,
                              int timeoutSeconds,
                              Predicate<MessageReactionAddEvent> isValidReaction,
                              Predicate<MessageReactionAddEvent> action,
                              Runnable timeoutAction
    ) {
        await(timeoutSeconds,
                (event) -> event.getMessageId().equals(message.getId()) && isValidReaction.test(event),
                action,
                timeoutAction,
                MessageReactionAddEvent.class
        );
    }


    /**
     * Awaits a reaction for a message.
     *
     * @param isValidMessage A Predicate that is given a MessageReceivedEvent.
     *                       Returns whether the action should be executed.
     * @param action         A Predicate that performs the action. Returning true will keep the event alive.
     */
    public void awaitMessage(Channel channel,
                             int timeoutSeconds,
                             Predicate<MessageReceivedEvent> isValidMessage,
                             Predicate<MessageReceivedEvent> action,
                             Runnable timeoutAction
    ) {
        await(timeoutSeconds,
                (event) -> event.getChannel().getId().equals(channel.getId()) && isValidMessage.test(event),
                action,
                timeoutAction,
                MessageReceivedEvent.class
        );
    }

    /**
     * Awaits a reaction for a message.
     *
     * @param isValidEvent A Predicate that is given a MessageReactionAddEvent.
     *                     Returns whether the action should be executed.
     * @param action       A Predicate that performs the action. Returning true will keep the event alive.
     */
    private <T extends GenericMessageEvent> void await(int timeoutSeconds,
                                                       Predicate<T> isValidEvent,
                                                       Predicate<T> action,
                                                       Runnable timeoutAction,
                                                       Class<T> awaitClass) {
        // boolean value is false when the timeout is complete.
        AtomicBoolean isActive = new AtomicBoolean(true);
        // purpose of timekeeping is to reduce the waitForEvent timeout
        AtomicLong startTimeMS = new AtomicLong(System.currentTimeMillis());
        AtomicLong elapsedTimeMS = new AtomicLong();
        AtomicInteger timeoutSecondsAtomic = new AtomicInteger(timeoutSeconds);
        // make the reaction no longer 'active' and perform the timeoutAction.
        Runnable onTimeoutCompletion = () -> {
            if (isActive.get()) {
                isActive.set(false);
                timeoutAction.run();
            }
        };
        executorService.schedule(
                onTimeoutCompletion,
                timeoutSeconds,
                TimeUnit.SECONDS
        );
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                waiter.waitForEvent(
                        awaitClass,
                        isValidEvent,
                        event -> {
                            if (isActive.get()) {
                                boolean continueWaiting = action.test(event);
                                if (continueWaiting) {
                                    elapsedTimeMS.set(System.currentTimeMillis() - startTimeMS.get());
                                    timeoutSecondsAtomic.set(timeoutSecondsAtomic.get() - (int) (elapsedTimeMS.get() / 1000));
                                    startTimeMS.set(System.currentTimeMillis());
                                    run();
                                } else isActive.set(false);
                            }
                        },
                        timeoutSecondsAtomic.get(),
                        TimeUnit.SECONDS,
                        () -> {/*executorService.schedule takes care of the timeout action*/}
                );
            }
        };
        runnable.run();
    }

}
