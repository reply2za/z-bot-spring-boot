package hoursofza.commands.client;


import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;

@Component
public class Version implements ClientCommandHandler {
    private final String appVersion;

    Version(@Value("${app.version}") String appVersion) {
        this.appVersion = appVersion;
    }
    @Override
    public void execute(MessageEventLocal messageEvent) {
        Message m = messageEvent.getMessage();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("version")
                .setDescription(appVersion)
                .setColor(Color.GRAY);
        m.getChannel().sendMessage(MessageCreateData.fromEmbeds(embedBuilder.build())).queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("version", "v");
    }
}
