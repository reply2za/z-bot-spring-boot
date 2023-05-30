package hoursofza.services;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.commands.interfaces.CommandHandler;
import hoursofza.utils.MessageEventLocal;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class CommandService {
    private final Map<String, ClientCommandHandler> clientCommands = new HashMap<>();
    private final Map<String, AdminCommandHandler> adminCommands = new HashMap<>();
    private final Set<String> admins;

    public CommandService(Set<ClientCommandHandler> clientCommandClasses, Set<AdminCommandHandler> adminCommandClasses, @Value("${owners}") String admins) {
        this.loadSpecificCommands(clientCommandClasses, this.clientCommands);
        this.loadSpecificCommands(adminCommandClasses, this.adminCommands);
        this.admins = new HashSet<>(Arrays.asList(admins.split(",")));
    }

    private <T extends CommandHandler> void loadSpecificCommands(Collection<T> commandClasses, Map<String, T> commands) {
        commandClasses.stream().parallel().forEach(commandHandler ->
                commandHandler.getNames().stream().parallel().forEach(alias ->
                                commands.put(alias, commandHandler)
                )
        );
    }

    @Nullable
    public CommandHandler getCommand(@Nonnull MessageEventLocal messageEventLocal) {
        if (this.admins.contains(messageEventLocal.getMessage().getAuthor().getId())) {
            CommandHandler adminCmd = this.adminCommands.get(messageEventLocal.getStatement());
            if (adminCmd != null) {
                return adminCmd;
            }
        }
        return this.clientCommands.get(messageEventLocal.getStatement());
    }
}
