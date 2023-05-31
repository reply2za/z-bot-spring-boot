package hoursofza.services;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.commands.interfaces.CommandHandler;
import hoursofza.utils.MessageEventLocal;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class CommandService {
    private static final String KEY_SEPARATOR = ", ";
    private final ConcurrentMap<String, ClientCommandHandler> clientCommands = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AdminCommandHandler> adminCommands = new ConcurrentHashMap<>();
    private final Set<String> admins;

    public CommandService(Set<ClientCommandHandler> clientCommandClasses, Set<AdminCommandHandler> adminCommandClasses, @Value("${owners}") String admins) {
        this.admins = new HashSet<>(Arrays.asList(admins.split(",")));
        this.loadSpecificCommands(clientCommandClasses, this.clientCommands);
        this.loadSpecificCommands(adminCommandClasses, this.adminCommands);
        StringBuilder shadowedCommands = new StringBuilder();
        this.adminCommands.keySet().stream().parallel()
                .filter(this.clientCommands::containsKey)
                .forEach(key -> shadowedCommands.append(key).append(KEY_SEPARATOR));
        if (shadowedCommands.length() > 0) {
            log.warn("Admin commands will shadow client commands: " +
                    shadowedCommands.substring(0, shadowedCommands.length() - KEY_SEPARATOR.length()));
        }
    }

    private <T extends CommandHandler> void loadSpecificCommands(Collection<T> commandClasses, Map<String, T> commands) {
        commandClasses.stream().parallel().forEach(commandHandler ->
                commandHandler.getNames().stream().parallel().forEach(alias -> {
                    alias = alias.toLowerCase();
                    if (commands.get(alias) != null) {
                        throw new RuntimeException("Command aliases must be unique. Duplicate value: '" +
                                alias + "'. Exists in classes '" + commandHandler.getClass().getName()
                                + "' and '" + commands.get(alias).getClass().getName() + "'");
                    } else {
                        commands.put(alias, commandHandler);
                    }
                })
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
