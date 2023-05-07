package hoursofza.utils;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Message;

import java.util.Map;

public class MessageEventLocal {
    private final Message message;
    private final String statement;

    public Map<String, Object> data;

    public MessageEventLocal(@Nonnull Message message, @Nonnull String statement, @Nonnull Map<String, Object> data) {
    this.message = message;
    this.statement = statement;
    this.data = data;
    }

    public Message getMessage() {
        return this.message;
    }

    public String getStatement() {
        return statement;
    }
}
