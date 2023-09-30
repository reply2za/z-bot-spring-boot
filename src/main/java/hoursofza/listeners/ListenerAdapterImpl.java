package hoursofza.listeners;

import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.commands.interfaces.CommandHandler;
import hoursofza.config.AppConfig;
import hoursofza.services.ProcessManagerService;
import hoursofza.store.CommandStore;
import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
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

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // only interpret reactions to bot messages
        if (event.getUser() == null) return;
        ProcessManagerService.awaitingReactions.get(event.getUser().getId());
        Member member = event.getMember();
        if (member != null) {
            log.info("{} added a reaction", event.getMember().getUser().getName());
        }
    }
}


/**
 *
 *
 *
 * User initiates game (.start game)
 * asked (which game would you like to start [1-n]
 * response 1
 * asked followup game setup questions
 *
 *
 * dms the other party
 *
 *
 */