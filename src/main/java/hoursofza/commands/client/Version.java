package hoursofza.commands.client;


import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.config.AppConfig;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;

@Component
public class Version implements ClientCommandHandler {
    private final AppConfig appConfig;

    Version(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void execute(MessageEventLocal messageEvent) {
        Message m = messageEvent.message();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("version")
                .setDescription(appConfig.getVersion())
                .setColor(Color.GRAY);
        m.getChannel().sendMessage(MessageCreateData.fromEmbeds(embedBuilder.build())).queue();
    }

    @Override
    public List<String> getNames() {
        return List.of("version", "v");
    }

}
