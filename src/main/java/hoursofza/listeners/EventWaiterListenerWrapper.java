package hoursofza.listeners;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import org.springframework.stereotype.Component;

@Component
public class EventWaiterListenerWrapper {
    private final EventWaiter waiter;

    EventWaiterListenerWrapper() {
        waiter = new EventWaiter();
    }

    public EventWaiter getEventWaiter() {
        return waiter;
    }
}
