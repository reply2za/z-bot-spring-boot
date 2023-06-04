package hoursofza.listeners;

import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.services.ProcessManagerService;
import hoursofza.store.CommandStore;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class GuildEventListener extends ListenerAdapter {

    private final List<GuildReadyEvent> allGuildReadyEvents;
    CommandStore commandStore;

    GuildEventListener(CommandStore commandStore, ProcessManagerService processManagerService) {
        this.commandStore = commandStore;
        this.allGuildReadyEvents = processManagerService.getAllGuildReadyEvents();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        commandStore.getClientCommands().parallelStream().map(ClientCommandHandler::getSlashCommand).filter(Objects::nonNull).forEach(
                slashCommand -> event.getGuild().updateCommands().addCommands(slashCommand).queue()
        );
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        allGuildReadyEvents.add(event);
    }

}
