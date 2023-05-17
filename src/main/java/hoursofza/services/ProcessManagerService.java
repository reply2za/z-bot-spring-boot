package hoursofza.services;


import hoursofza.config.AppConfig;
import org.springframework.stereotype.Service;

@Service
public class ProcessManagerService {
    private final String prefix;
    public ProcessManagerService(AppConfig appConfig) {
        prefix = appConfig.isDevMode() ? appConfig.getPrefix().dev() : appConfig.getPrefix().prod();
    }

    public String getPrefix() {
        return prefix;
    }
}
