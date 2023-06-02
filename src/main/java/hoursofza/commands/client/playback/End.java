package hoursofza.commands.client.playback;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.utils.MessageEventLocal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class End implements AdminCommandHandler {

    @Override
    public void execute(MessageEventLocal event) {
        event.getMessage().getGuild().getAudioManager().closeAudioConnection();
    }

    @Override
    public List<String> getNames() {
        return List.of("end");
    }
}
