package hoursofza.commands.interfaces;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public interface ClientCommandHandler extends CommandHandler {

    default SlashCommandData getSlashCommand() {
        return null;
    }

    default void executeSlashCommand(@NotNull SlashCommandInteractionEvent slashCommandEvent) {
        slashCommandEvent.reply("this command is still under development").queue();

    }


}
