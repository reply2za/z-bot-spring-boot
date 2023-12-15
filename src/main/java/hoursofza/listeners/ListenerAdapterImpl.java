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
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
@DependsOn("commandService")
@Slf4j
public class ListenerAdapterImpl extends ListenerAdapter {
    private final List<SlashCommandData> slashCommands;
    private final CommandStore commandStore;
    private final DiscordUtils discordUtils;

    private final AppConfig appConfig;

    ListenerAdapterImpl(CommandStore commandStore,
                        DiscordUtils discordUtils,
                        AppConfig appConfig) {
        this.commandStore = commandStore;
        this.discordUtils = discordUtils;
        this.appConfig = appConfig;
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

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        // civ channel & webhook bot
        if (event.getChannel().getId().equals("1183626115378589726") && event.getAuthor().getId().equals("1183626204801151068")) {
            notifyPlayer(event);
        }
//        gameService.parseUserResponse(event.getChannel(), event.getAuthor(), event.getMessage());
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

    private void notifyPlayer(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        String username = message.split(", it's your turn")[0].trim();
        String userId = null;
        switch (username.toLowerCase()) {
            case "machoherbivore9":
                userId = "378675087274016771";
            break;
            case "brownsycamore":
                userId = "268554823283113985";
                break;
            case "reply2za":
                userId = "443150640823271436";
                break;
            default:
                userId = "unknown user";
                break;
        }
        event.getMessage().getChannel().sendMessage("<@" + userId + "> it's your turn in civ 6").queue();
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

