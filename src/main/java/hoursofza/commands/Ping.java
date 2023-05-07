package hoursofza.commands;

import hoursofza.utils.MessageEventLocal;

import java.util.List;

public class Ping implements CommandHandler{

    @Override
    public void execute(MessageEventLocal event) {
        event.getMessage().getChannel().sendMessage("Pong! - ja").queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("ping");
    }
}
