package hoursofza.commands.interfaces;

import hoursofza.utils.MessageEventLocal;
import hoursofza.utils.NoMessageEventLocal;

import java.util.List;

public interface CommandHandler {
    void execute(MessageEventLocal event);

    default void executeNoMessage(NoMessageEventLocal event) {
        MessageEventLocal m = new MessageEventLocal(null, event.getStatement(), event.getData(), event.getArgs());
        execute(m);
    }

    /**
     * @return A list of callable names for the command handler. Is case-insensitive.
     */
    List<String> getNames();

    default boolean isMultiProcessCommand() {
        return false;
    }

    /**
     * Will be run on app startup.
     */
    default void startup() {}

}