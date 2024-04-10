package hoursofza.services;


import hoursofza.config.AppConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ProcessManagerService {
    @Getter
    private static boolean ACTIVE;
    @Getter
    private static String PREFIX;
    private static final Map<String, GuildService> SERVERS = new HashMap<>();
    @Getter
    private static JDA BOT;


    public ProcessManagerService(AppConfig appConfig) {
        PREFIX = appConfig.getPrefix();
        ACTIVE = true;
        log.info("process startup - active");
    }

    public static void setActive(boolean b) {
        ACTIVE = b;
        log.info(b ? "process is now active" : "process is now inactive");
    }

    public static GuildService getServer(String id) {
        return SERVERS.computeIfAbsent(id, GuildService::new);
    }

    public static void setBot(JDA bot) {
        if (BOT != null) throw new RuntimeException("cannot reset bot");
        BOT = bot;
    }
}
