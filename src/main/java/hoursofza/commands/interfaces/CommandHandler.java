package hoursofza.commands.interfaces;

import hoursofza.utils.MessageEventLocal;

import java.util.List;

public interface CommandHandler {
    void execute(MessageEventLocal event);

    /**
     * @return A list of command names for the handler.
     */
    List<String> getNames();

}