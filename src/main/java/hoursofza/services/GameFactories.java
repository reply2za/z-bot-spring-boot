package hoursofza.services;

import hoursofza.config.AppConfig;
import hoursofza.handlers.TwentyQuestionsGame;
import hoursofza.utils.DiscordUtils;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameFactories {
    @Autowired
    DiscordUtils discordUtils;
    @Autowired
    AppConfig appConfig;

    public TwentyQuestionsGame newTwentyQuestionsGame(User answerer, User guesser) {
        return new TwentyQuestionsGame(appConfig, discordUtils, answerer, guesser);
    }
}
