package hoursofza.commands.client.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.services.GuildService;
import hoursofza.services.ProcessManagerService;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Resume implements ClientCommandHandler {

    private final ProcessManagerService processManagerService;

    Resume(ProcessManagerService processManagerService) {
        this.processManagerService = processManagerService;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("resume", "resumes playing if paused");
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandInteractionEvent slashCommandEvent) {
        String response = runResumeCommandAndSendResponse(slashCommandEvent.getGuild());
        slashCommandEvent.reply(response).queue();

    }

    @Override
    public void execute(MessageEventLocal event) {
        String response = runResumeCommandAndSendResponse(event.getMessage().getGuild());
        event.getMessage().getChannel().sendMessage(response).queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("resume");
    }

    private String runResumeCommandAndSendResponse(Guild guild) {
        if (guild != null) {
            if (resumeCommand(guild)) {
                return "*resumed*";
            } else {
                return "*nothing is playing right now*";
            }
        } else {
            return "*must be in a guild for this command*";
        }
    }

    public boolean resumeCommand(Guild guild) {
        GuildService guildService = processManagerService.getServer(guild.getId());
        AudioPlayer player = guildService.getAudioPlayer();
        if (player != null && player.isPaused()) {
            player.setPaused(false);
            return true;
        }
        return false;
    }
}
