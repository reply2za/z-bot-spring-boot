package hoursofza.handlers.interfaces;

import hoursofza.enums.GameType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nullable;
import java.util.List;

public interface Game {

    /**
     * Returns whether the game is over.
     * @param channel
     * @param user
     * @param text
     * @return
     */
    boolean input(MessageChannel channel, User user, Message message);

    GameType gameType();

    List<User> getPlayers();

    void initialize(@Nullable MessageChannel channel);

    boolean isGameOver();

}
