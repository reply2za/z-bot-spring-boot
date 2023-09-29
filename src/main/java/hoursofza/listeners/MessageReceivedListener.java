package hoursofza.listeners;

import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.commands.interfaces.CommandHandler;
import hoursofza.config.AppConfig;
import hoursofza.services.ProcessManagerService;
import hoursofza.store.CommandStore;
import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class MessageReceivedListener extends ListenerAdapter {

    private final CommandStore commandStore;
    private final DiscordUtils discordUtils;

    private final AppConfig appConfig;

    public MessageReceivedListener(
            CommandStore commandStore,
            DiscordUtils discordUtils,
            AppConfig appConfig
    ) {
        this.commandStore = commandStore;
        this.discordUtils = discordUtils;
        this.appConfig = appConfig;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        String botPrefix = ProcessManagerService.getPREFIX();
        if (!content.startsWith(botPrefix)) return;
        List<String> messageContents = List.of(message.getContentRaw().split("\\s+"));
        if (messageContents.isEmpty()) return;
        String statement = messageContents.get(0);
        statement = statement.substring(botPrefix.length()).toLowerCase();
        if (statement.isBlank()) return;
        MessageEventLocal messageEvent = new MessageEventLocal(message, statement, new HashMap<>(), messageContents.subList(1, messageContents.size()));
        CommandHandler commandHandler = commandStore.getCommand(messageEvent);
        if (commandHandler != null) {
            if (isInvalidInstanceAndPermission(commandHandler, message.getAuthor().getId())) return;
            log.info("executing {}", commandHandler.getClass().getName());
            commandHandler.execute(messageEvent);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        ClientCommandHandler clientCommand = commandStore.getClientCommand(event.getName());
        if (clientCommand != null) {
            if (isInvalidInstanceAndPermission(clientCommand, event.getUser().getId())) return;
            clientCommand.executeSlashCommand(event);
        } else {
            event.reply("this command is no longer supported").queue();
        }
    }

    /**
     * Determines whether the user has the required permissions to run the command. Also checks instance type.
     * The command should NOT be run if this method returns false.
     *
     * @param commandHandler The command to run.
     * @param userId         The user making the request.
     * @return Whether the command should be run.
     */
    private boolean isInvalidInstanceAndPermission(CommandHandler commandHandler, String userId) {
        boolean isAdmin = discordUtils.getAdmins().contains(userId);
        if (!ProcessManagerService.isACTIVE() && (!isAdmin || !commandHandler.isMultiProcessCommand())) return true;
        return appConfig.isDevMode() && !isAdmin;
    }
}


// Listener for message reactions
//    @Override
//    public void onMessageReactionAdd(MessageReactionAddEvent event) {
//        if (event.getUser() == null || event.getUser().isBot()) return;
//        Member member = event.getMember();
//        if (member != null) {
//            log.info("{} added a reaction", event.getMember().getUser().getName());
//        }
//    }