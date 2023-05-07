package hoursofza.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordBotConfig {


    @Value("${DISCORD_BOT_TOKEN}")
    private String token;

    public String getToken() {
        return token;
    }
}
