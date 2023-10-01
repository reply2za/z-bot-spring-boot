package hoursofza.commands.client.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.services.GuildService;
import hoursofza.services.ProcessManagerService;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
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
        event.message().getChannel().sendMessage(
                pauseCommand(event.message().getGuild(), event.message().getChannel())
        ).queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("pause");
    }


    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("pause", "pauses the audio stream");
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandInteractionEvent slashCommandEvent) {
        if (slashCommandEvent.getGuild() == null) {
            slashCommandEvent.reply("*must be in a guild for this command*").queue();
            return;
        }
        slashCommandEvent.reply(pauseCommand(slashCommandEvent.getGuild(), slashCommandEvent.getChannel())).queue();
    }

    /**
     * Executes the pause command to pause a playing stream.
     *
     * @param guild   The guild.
     * @param channel The channel.
     * @return The response to be sent to the user.
     */
    private String pauseCommand(Guild guild, MessageChannel channel) {
        GuildService guildService = ProcessManagerService.getServer(guild.getId());
        AudioPlayer audioPlayer = guildService.getAudioPlayer();
        if (audioPlayer == null || audioPlayer.isPaused()) {
            return "*nothing is playing right now*";
        } else {
            audioPlayer.setPaused(true);
            return "*paused*";
        }
    }
}
