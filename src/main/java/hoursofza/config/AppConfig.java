package hoursofza.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("default")
public class AppConfig {

    private final String token;
    private final boolean devMode;
    private final String prefix;
    private final String version;

    @ConstructorBinding
    public AppConfig(String token, boolean devMode, String prefix, String version) {
        this.token = token;
        this.devMode = devMode;
        this.prefix = prefix;
        this.version = version;
    }

    public String getToken() {
        return token;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getVersion() {
        return version;
    }
}
