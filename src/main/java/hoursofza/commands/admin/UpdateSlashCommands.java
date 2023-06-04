package hoursofza.commands.admin;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.services.ProcessManagerService;
import hoursofza.store.CommandStore;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class UpdateSlashCommands implements AdminCommandHandler {

    CommandStore commandStore;
    ProcessManagerService processManagerService;

    UpdateSlashCommands(ProcessManagerService processManagerService, CommandStore commandStore) {
        this.processManagerService = processManagerService;
        this.commandStore = commandStore;
    }

    @Override
    public void execute(MessageEventLocal event) {
        if (event.getArgs().size() < 1) {
            event.getMessage().getChannel().sendMessage("expected command names").queue();
            return;
        }
        List<String> updatedSlashCommands = new ArrayList<>();

        Consumer<? super Throwable> failResponse = o -> event.getMessage().getChannel().sendMessage("there was an error executing the update").queue();
        for (String arg : event.getArgs()) {
            Consumer<? super List<Command>> successResponse = o -> updatedSlashCommands.add(arg);
            if (updateSlashCommands(arg, successResponse, failResponse)) {
                updatedSlashCommands.add(arg);
            }
        }
        if (updatedSlashCommands.size() > 0)
        event.getMessage().getChannel().sendMessage("updated slash commands for: " + String.join(", ", updatedSlashCommands)).queue();
        else
            event.getMessage().getChannel().sendMessage("no slash commands were updated").queue();

    }

    private boolean updateSlashCommands(String name, Consumer<? super List<Command>> success, Consumer<? super Throwable> fail) {
        ClientCommandHandler commandHandler = commandStore.getClientCommand(name);
        if (commandHandler != null) {
            SlashCommandData slashCommandData = commandHandler.getSlashCommand();
            if (slashCommandData != null) {
                processManagerService.getAllGuildReadyEvents().forEach(guildReadyEvent -> guildReadyEvent.getGuild().updateCommands().addCommands(slashCommandData).queue(success, fail));
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getNames() {
        return List.of("updateSlashCommand", "slash");
    }
}
