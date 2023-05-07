package hoursofza.services;

import hoursofza.config.DiscordBotConfig;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class StartupService {
    private final DiscordBotConfig config;
    List<EventListener> eventListeners;
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupService.class);

    public StartupService(DiscordBotConfig config, List<EventListener> listeners) {
        this.config = config;
        this.eventListeners = listeners;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Initializing Discord bot");
        JDABuilder.createDefault(config.getToken())
                .addEventListeners(this.eventListeners.toArray())
                .setActivity(Activity.playing("Type !hello"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .build();
        LOGGER.info("Discord bot initialized");
    }
}
