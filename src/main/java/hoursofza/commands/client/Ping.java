package hoursofza.commands.client;

import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.utils.MessageEventLocal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Ping implements ClientCommandHandler {

    @Override
    public void execute(MessageEventLocal event) {
        event.message().getChannel().sendMessage("Pong! - java").queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("ping");
    }

}
