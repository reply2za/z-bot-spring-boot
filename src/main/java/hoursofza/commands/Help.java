package hoursofza.commands;


import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.Color;
import java.util.List;

public class Help implements CommandHandler {

    @Override
    public void execute(MessageEventLocal messageEvent) {
        Message m = messageEvent.getMessage();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Hello!")
                .setDescription("Hi, " + m.getAuthor().getAsMention() + "! I'm a simple Discord bot built with Spring Boot and JDA.")
                .setColor(Color.decode("#469963"));
        m.getChannel().sendMessage(MessageCreateData.fromEmbeds(embedBuilder.build())).queue();
        m.addReaction(Emoji.fromUnicode("\uD83D\uDC4D")).queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("help", "h");
    }
}
