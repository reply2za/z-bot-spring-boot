package hoursofza.commands.admin;


import hoursofza.commands.CommandHandler;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.Color;
import java.util.List;

public class Version implements CommandHandler {

    @Override
    public void execute(MessageEventLocal messageEvent) {
        Message m = messageEvent.getMessage();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("version")
                .setDescription("Command is not set up yet")
                .setColor(Color.GRAY);
        m.getChannel().sendMessage(MessageCreateData.fromEmbeds(embedBuilder.build())).queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("version", "v");
    }
}
