package hoursofza.commands.client;


import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.enums.Unicode;
import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;

@Component
public class Help implements ClientCommandHandler {

    private final DiscordUtils discordUtils;
    Help(DiscordUtils discordUtils) {
        this.discordUtils =  discordUtils;
    }
    @Override
    public void execute(MessageEventLocal messageEvent) {
        Message m = messageEvent.getMessage();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Hello!")
                .setDescription("Hi, " + m.getAuthor().getAsMention() + "! I'm a simple Discord bot built with Spring Boot and JDA.")
                .setColor(Color.decode("#469963"));
        m.getChannel().sendMessage(MessageCreateData.fromEmbeds(embedBuilder.build())).queue();
        List<Emoji> reacts = List.of(Unicode.THUMBS_UP.getEmoji(), Unicode.GEAR.getEmoji());
        reacts.forEach(e ->  m.addReaction(e).queue());
        discordUtils.awaitReaction(
                m,
                30,
                event -> {
                    if (event.getUser() != null) {
                        return event.getUser().getId().equals(m.getAuthor().getId());
                    }
                    return false;
                    },
                event -> {
                    if (event.getUser() != null) {
                        reacts.forEach(emoji -> m.removeReaction(emoji, event.getUser()).queue());
                    }
                    return true;
                },
                x -> reacts.forEach(emoji -> m.clearReactions(emoji).queue())
        );
    }

    @Override
    public List<String> getNames() {
        return List.of("help", "h");
    }
}
