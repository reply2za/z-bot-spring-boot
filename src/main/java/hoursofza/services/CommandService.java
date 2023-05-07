package hoursofza.services;

import hoursofza.commands.CommandHandler;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CommandService {
    private final Map<String, CommandHandler> commandHandlers;

    public CommandService(Set<CommandHandler> commandHandlers) {
        Map<String, CommandHandler> commandsMap = new HashMap<>();
        commandHandlers.forEach(commandHandler -> {
            commandHandler.getNames().forEach(alias -> {
                commandsMap.put(alias, commandHandler);
            });
        });
        this.commandHandlers = commandsMap;
    }

    @Nullable
    public CommandHandler getCommand(@Nonnull String commandName) {
        return this.commandHandlers.get(commandName);
    }
}
