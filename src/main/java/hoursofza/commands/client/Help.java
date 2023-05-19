package hoursofza.commands.client;


import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

@Component
public class Help implements ClientCommandHandler {

    private final DiscordUtils discordUtils;
    Help(DiscordUtils discordUtils) {
        this.discordUtils =  discordUtils;
    }
    @Override
    public void execute(MessageEventLocal messageEvent) {
        Emoji thumbsUp = Emoji.fromUnicode("\uD83D\uDC4D");
        Message m = messageEvent.getMessage();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Hello!")
                .setDescription("Hi, " + m.getAuthor().getAsMention() + "! I'm a simple Discord bot built with Spring Boot and JDA.")
                .setColor(Color.decode("#469963"));
        m.getChannel().sendMessage(MessageCreateData.fromEmbeds(embedBuilder.build())).queue();
        m.addReaction(thumbsUp).queue();
        discordUtils.awaitReaction(
                m,
                10,
                event -> {
                    if (Objects.nonNull(event.getUser())) {
                        return event.getUser().getId().equals(m.getAuthor().getId());
                    }
                    return false;
                    },
                event -> {
                    if (Objects.nonNull(event.getUser())) {
                        m.removeReaction(thumbsUp, event.getUser()).queue();
                    }
                    return true;
                },
                x -> m.clearReactions(thumbsUp).queue()
        );
    }

    @Override
    public List<String> getNames() {
        return List.of("help", "h");
    }
}
