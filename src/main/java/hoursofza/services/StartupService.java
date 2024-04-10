package hoursofza.services;

import hoursofza.config.AppConfig;
import hoursofza.listeners.EventWaiterListenerWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class StartupService {
    private final AppConfig appConfig;
    private final List<EventListener> eventListeners;

    public StartupService(AppConfig appConfig, List<ListenerAdapter> listeners,
                          EventWaiterListenerWrapper eventWaiterListenerWrapper) {
        this.appConfig = appConfig;
        this.eventListeners = new ArrayList<>(listeners);
        this.eventListeners.add(eventWaiterListenerWrapper.getEventWaiter());
    }

    @PostConstruct
    private void init() {
        log.info("Initializing Discord bot");
        ProcessManagerService.setBot(JDABuilder.createDefault(this.appConfig.getToken())
                .addEventListeners(this.eventListeners.toArray())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING
                )
                .build());
        log.info("Mode: {}", this.appConfig.isDevMode() ? "Development" : "Production");
    }

}
