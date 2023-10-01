package hoursofza.handlers.interfaces;

import hoursofza.enums.GameType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nullable;
import java.util.List;

public interface Game {

    void initialize(@Nullable MessageChannel channel);

    /**
     * Returns whether the input was valid.
     * @param channel
     * @param user
     * @param message
     * @return
     */
    boolean input(MessageChannel channel, User user, Message message);

    GameType gameType();

    List<User> getPlayers();

    /**
     * @return Whether the game has ended
     */
    boolean isGameOver();

    /**
     * Request that a game be ended.
     * @param initiator The user making the request.
     */
    void requestEndGame(User initiator);

}
