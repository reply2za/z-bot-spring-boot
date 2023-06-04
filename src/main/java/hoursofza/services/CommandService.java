package hoursofza.services;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.commands.interfaces.CommandHandler;
import hoursofza.store.CommandStore;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommandService {

    public CommandService(
            Set<ClientCommandHandler> clientCommandClasses,
            Set<AdminCommandHandler> adminCommandClasses,
            @Value("${owners}") String admins,
            CommandStore commandStore
    ) {
        Set<String> adminsSet = new HashSet<>(Arrays.asList(admins.split(",")));
        ConcurrentMap<String, ClientCommandHandler> clientCommands = new ConcurrentHashMap<>();
        this.loadSpecificCommands(clientCommandClasses, clientCommands);
        ConcurrentMap<String, AdminCommandHandler> adminCommands = new ConcurrentHashMap<>();
        this.loadSpecificCommands(adminCommandClasses, adminCommands);
        String shadowedCommands = adminCommands.keySet().parallelStream()
                .filter(clientCommands::containsKey)
                .collect(Collectors.joining(", "));
        if (shadowedCommands.length() > 0) {
            log.warn("Admin commands will shadow client commands: " + shadowedCommands);
        }
        commandStore.setClientCommands(clientCommands).setAdminCommands(adminCommands).setAdmins(adminsSet);
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


}
