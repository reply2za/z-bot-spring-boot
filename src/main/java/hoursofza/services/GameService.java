package hoursofza.services;

import hoursofza.handlers.interfaces.Game;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GameService {
    @Getter
    // userId to games
    private final Map<String, List<Game>> activeGames = new HashMap<>();


    public void startGame(Game game, @Nullable MessageChannel channel) {
        game.getPlayers().forEach(player -> {
            if (activeGames.containsKey(player.getId())){
                activeGames.get(player.getId()).add(game);
            } else {
                List<Game> playerGames = new ArrayList<>();
                playerGames.add(game);
                activeGames.put(player.getId(), playerGames);
            }
        });
        game.initialize(channel);
    }

    public void parseUserResponse(MessageChannel channel, User author, Message message) {
        // todo add game over logic
        List<Game> games = activeGames.get(author.getId());
        if (games != null && !games.isEmpty()) {
            games.get(0).input(channel, author, message);
        }
    }

    
}
