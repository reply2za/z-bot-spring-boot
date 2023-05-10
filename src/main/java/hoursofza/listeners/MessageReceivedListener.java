package hoursofza.listeners;

import hoursofza.commands.CommandHandler;
import hoursofza.services.CommandService;
import hoursofza.utils.MessageEventLocal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;

@Slf4j
public class MessageReceivedListener extends ListenerAdapter {

    String messagePrefix;

    @Autowired
    CommandService commandService;

    public MessageReceivedListener(@Value("${default.prefix}") String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (!content.substring(0,1).equals(this.messagePrefix)) return;
        String statement = message.getContentRaw().split("\\s+")[0];
        statement = statement.substring(1);
        if (statement.isBlank()) return;
        MessageEventLocal messageEvent = new MessageEventLocal(message, statement, new HashMap<>());
        CommandHandler commandHandler = commandService.getCommand(messageEvent);
        if (commandHandler != null) {
            log.info("executing {}", commandHandler.getClass().getName());
            commandHandler.execute(messageEvent);
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        Member member = event.getMember();
        if (member != null) {
            log.info("{} added a reaction", event.getMember().getUser().getName());
        }
    }
}

