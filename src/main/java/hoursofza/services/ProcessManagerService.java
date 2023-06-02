package hoursofza.services;


import hoursofza.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ProcessManagerService {
    boolean isActive;
    private final String prefix;
    private final Map<String, GuildService> servers;
    public ProcessManagerService(AppConfig appConfig) {
        prefix = appConfig.isDevMode() ? appConfig.getPrefix().dev() : appConfig.getPrefix().prod();
        this.isActive = true;
        log.info("process startup - active");
        this.servers = new HashMap<>();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setActive(boolean b) {
        this.isActive = b;
        log.info(b ? "process is now active" : "process is now inactive");
    }

    public boolean isActive() {
        return this.isActive;
    }

    public GuildService getServer(String id) {
        GuildService guildService = this.servers.get(id);
        if (guildService == null) {
            guildService = new GuildService(id);
            this.servers.put(id, guildService);
        }
        return guildService;
    }
}
