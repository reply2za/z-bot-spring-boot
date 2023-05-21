package hoursofza.commands.interfaces;

import hoursofza.utils.MessageEventLocal;

import java.util.List;

public interface CommandHandler {
    void execute(MessageEventLocal event);

    /**
     * @return A list of callable names for the command handler.
     */
    List<String> getNames();

    default boolean isMultiProcessCommand() {
        return false;
    }

}