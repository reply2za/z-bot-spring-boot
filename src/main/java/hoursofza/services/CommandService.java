package hoursofza.services;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.commands.interfaces.CommandHandler;
import hoursofza.utils.MessageEventLocal;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommandService {
    private final ConcurrentMap<String, ClientCommandHandler> clientCommands = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AdminCommandHandler> adminCommands = new ConcurrentHashMap<>();
    private final Set<String> admins;

    public CommandService(
            Set<ClientCommandHandler> clientCommandClasses,
            Set<AdminCommandHandler> adminCommandClasses,
            @Value("${owners}") String admins
    ) {
        this.admins = new HashSet<>(Arrays.asList(admins.split(",")));
        this.loadSpecificCommands(clientCommandClasses, this.clientCommands);
        this.loadSpecificCommands(adminCommandClasses, this.adminCommands);
        String shadowedCommands = this.adminCommands.keySet().parallelStream()
                .filter(this.clientCommands::containsKey)
                .collect(Collectors.joining(", "));
        if (shadowedCommands.length() > 0) {
            log.warn("Admin commands will shadow client commands: " + shadowedCommands);
        }
    }

    private <T extends CommandHandler> void loadSpecificCommands(Collection<T> commandClasses, Map<String, T> commands) {
        commandClasses.parallelStream().forEach(commandHandler ->
                commandHandler.getNames().parallelStream().map(String::toLowerCase).forEach(alias -> {
                    if (commands.get(alias) == null) {
                        commands.put(alias, commandHandler);
                    } else {
                        throw new RuntimeException("Command aliases must be unique. Duplicate value: '" +
                                alias + "'. Exists in classes '" + commandHandler.getClass().getName()
                                + "' and '" + commands.get(alias).getClass().getName() + "'");
                    }
                })
        );
    }

    @Nullable
    public CommandHandler getCommand(@NotNull MessageEventLocal messageEventLocal) {
        if (this.admins.contains(messageEventLocal.getMessage().getAuthor().getId())) {
            CommandHandler adminCmd = this.adminCommands.get(messageEventLocal.getStatement());
            if (adminCmd != null) {
                return adminCmd;
            }
        }
        return this.clientCommands.get(messageEventLocal.getStatement());
    }
}
