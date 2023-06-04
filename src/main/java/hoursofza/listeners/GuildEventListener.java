package hoursofza.listeners;

import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.store.CommandStore;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@DependsOn("commandService")
public class GuildEventListener extends ListenerAdapter {

    private final List<SlashCommandData> slashCommands;

    GuildEventListener(CommandStore commandStore) {
        this.slashCommands = commandStore.getClientCommands().stream().map(ClientCommandHandler::getSlashCommand).filter(Objects::nonNull).toList();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        slashCommands.parallelStream().filter(Objects::nonNull).forEach(
                slashCommand -> event.getGuild().updateCommands().addCommands(slashCommand).queue()
        );
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent guildReadyEvent) {
        guildReadyEvent.getGuild().updateCommands().addCommands(slashCommands).queue();
    }

}
