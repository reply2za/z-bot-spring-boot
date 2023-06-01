package hoursofza.utils;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MessageEventLocal {
    private final Message message;
    private final String statement;

    public Map<String, Object> data;

    public MessageEventLocal(@NotNull Message message, @NotNull String statement, @NotNull Map<String, Object> data) {
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
