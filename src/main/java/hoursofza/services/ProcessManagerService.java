package hoursofza.services;


import hoursofza.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessManagerService {
    boolean isActive;
    private final String prefix;
    public ProcessManagerService(AppConfig appConfig) {
        prefix = appConfig.isDevMode() ? appConfig.getPrefix().dev() : appConfig.getPrefix().prod();
        this.isActive = true;
        log.info("process startup - active");
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
}
