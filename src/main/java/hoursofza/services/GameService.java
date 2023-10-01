package hoursofza.services;

import hoursofza.handlers.interfaces.Game;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GameService {
    // userId to games
    private final Map<String, List<Game>> allGames = new HashMap<>();


    public void startGame(Game game, @Nullable MessageChannel channel) {
        game.getPlayers().forEach(player -> {
            if (allGames.containsKey(player.getId())) {
                allGames.get(player.getId()).add(game);
            } else {
                List<Game> playerGames = new ArrayList<>();
                playerGames.add(game);
                allGames.put(player.getId(), playerGames);
            }
        });
        game.initialize(channel);
    }

    public List<Game> getActiveGames(String userId) {
        List<Game> userGames = allGames.get(userId);
        if (userGames == null) return List.of();
        List<Game> activeGames = userGames.stream().filter(game -> !game.isGameOver()).collect(Collectors.toList());
        allGames.put(userId, activeGames);
        return activeGames;
    }

    public boolean requestEndGame(User initiator, String name) {
        List<Game> games = allGames.get(initiator.getId());
        if (games == null) return false;
        games.forEach(game -> {
            if (game.getClass().getSimpleName().equalsIgnoreCase(name)) {
                game.requestEndGame(initiator);
            }
        });
        return false;
    }

}
