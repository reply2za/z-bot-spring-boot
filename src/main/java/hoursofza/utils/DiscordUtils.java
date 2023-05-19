package hoursofza.utils;


import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import hoursofza.listeners.EventWaiterListenerWrapper;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Component
public class DiscordUtils {
    private final EventWaiter waiter;
    DiscordUtils(EventWaiterListenerWrapper eventWaiterListenerWrapper) {
        this.waiter = eventWaiterListenerWrapper.getEventWaiter();
    }
    public void awaitReaction(Predicate<MessageReactionAddEvent> isValidReaction, Consumer<MessageReactionAddEvent> action) {
        AtomicBoolean continueWaiting = new AtomicBoolean(true);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                waiter.waitForEvent(
                        MessageReactionAddEvent.class,
                        isValidReaction,
                        event -> {
                            action.accept(event);
                            if (continueWaiting.get()) run();
                        },
                        30, TimeUnit.SECONDS,
                        () -> continueWaiting.set(false)
                );
            }
        };
        runnable.run();
    }

}
