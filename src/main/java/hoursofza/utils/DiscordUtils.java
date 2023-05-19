package hoursofza.utils;


import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import hoursofza.listeners.EventWaiterListenerWrapper;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Component
public class DiscordUtils {
    private final EventWaiter waiter;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    DiscordUtils(EventWaiterListenerWrapper eventWaiterListenerWrapper) {
        this.waiter = eventWaiterListenerWrapper.getEventWaiter();
    }

    /**
     * Awaits a reaction for a message.
     * @param isValidReaction A Predicate that is given a MessageReactionAddEvent.
     *                        Returns whether the action should be executed.
     * @param action A Predicate that performs the action. Returning true will keep the reaction alive.
     */
    public void awaitReaction(Message message, int timeoutSeconds,
                              Predicate<MessageReactionAddEvent> isValidReaction,
                              Predicate<MessageReactionAddEvent> action,
                              Consumer<?> timeoutAction
                              ) {

        // boolean value is true when the reaction timeout is complete.
        AtomicBoolean isActiveReaction = new AtomicBoolean(true);
        // purpose of timekeeping is to reduce the waitForEvent timeout
        AtomicLong startTimeMS = new AtomicLong(System.currentTimeMillis());
        AtomicLong elapsedTimeMS = new AtomicLong();
        AtomicInteger timeoutSecondsAtomic = new AtomicInteger(timeoutSeconds);
        Runnable onTimeoutCompletion = () -> {
            if (isActiveReaction.get()) {
                isActiveReaction.set(false);
                timeoutAction.accept(null);
            }
        };
        // make the reaction no longer 'active' and perform the timeoutAction.
        executorService.schedule(
                onTimeoutCompletion,
                timeoutSeconds,
                TimeUnit.SECONDS
        );
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                waiter.waitForEvent(
                        MessageReactionAddEvent.class,
                        event -> event.getMessageId().equals(message.getId()) && isValidReaction.test(event),
                        event -> {
                            if (isActiveReaction.get()) {
                                boolean continueWaiting = action.test(event);
                                if (continueWaiting) {
                                    elapsedTimeMS.set(System.currentTimeMillis() - startTimeMS.get());
                                    timeoutSecondsAtomic.set(timeoutSecondsAtomic.get() - (int)(elapsedTimeMS.get()/1000));
                                    startTimeMS.set(System.currentTimeMillis());
                                    run();
                                }
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
