package hoursofza.commands.client.playback;

import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class End implements ClientCommandHandler {

    @Override
    public void execute(MessageEventLocal event) {
        endCommand(event.getMessage().getGuild());
    }

    @Override
    public List<String> getNames() {
        return List.of("end");
    }


    @Override
    public void executeSlashCommand(@NotNull SlashCommandInteractionEvent slashCommandEvent) {
        if (slashCommandEvent.getGuild() != null) {
            if (endCommand(slashCommandEvent.getGuild())) {
                slashCommandEvent.reply("ended session").queue();
            } else slashCommandEvent.reply("*you must be in a voice channel session to end*").queue();
        } else {
            slashCommandEvent.reply("you must be in a guild for this slash command").queue();
        }


    }

    private boolean endCommand(Guild guild) {
        if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
            return true;
        }
        return false;
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("end", "ends the session");
    }
}
