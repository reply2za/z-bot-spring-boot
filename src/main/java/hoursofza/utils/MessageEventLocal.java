package hoursofza.utils;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MessageEventLocal {
    private final Message message;
    private final String statement;

    private final List<String> args;

    public Map<String, Object> data;

    public MessageEventLocal(@NotNull Message message, @NotNull String statement, @NotNull Map<String, Object> data, @NotNull List<String> args) {
        this.message = message;
        this.statement = statement;
        this.data = data;
        this.args = args;
    }

    public Message message() {
        return message;
    }

    public String statement() {
        return statement;
    }

    public List<String> args() {
        return args;
    }

    public Map<String, Object> data() {
        return data;
    }
}