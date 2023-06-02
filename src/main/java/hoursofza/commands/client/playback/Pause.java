package hoursofza.commands.client.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.services.GuildService;
import hoursofza.services.ProcessManagerService;
import hoursofza.utils.MessageEventLocal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Pause implements ClientCommandHandler {

    private final ProcessManagerService processManagerService;
    Pause(ProcessManagerService processManagerService) {
        this.processManagerService = processManagerService;
    }

    @Override
    public void execute(MessageEventLocal event) {
        GuildService guildService = processManagerService.getServer(event.getMessage().getGuild().getId());
        AudioPlayer audioPlayer = guildService.getAudioPlayer();
        if (audioPlayer == null || audioPlayer.isPaused()) {
            event.getMessage().getChannel().sendMessage("*nothing is playing right now*").queue();
        } else {
            audioPlayer.setPaused(true);
            event.getMessage().getChannel().sendMessage("*paused*").queue();
        }
    }

    @Override
    public List<String> getNames() {
        return List.of("pause");
    }
}
