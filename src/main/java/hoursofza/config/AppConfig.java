package hoursofza.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("default")
public class AppConfig {

    private final String token;
    private final boolean devMode;
    private final Prefix prefix;
    private final String version;

    @ConstructorBinding
    public AppConfig(String token, boolean devMode, Prefix prefix, String version) {
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

    public Prefix getPrefix() {
        return prefix;
    }

    public record Prefix(String prod, String dev) {}

    public String getVersion() {
        return version;
    }
}
