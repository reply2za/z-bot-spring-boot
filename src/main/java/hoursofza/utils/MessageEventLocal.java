package hoursofza.utils;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record MessageEventLocal(Message message, String statement, Map<String, Object> data, List<String> args) {
    public MessageEventLocal(@NotNull Message message, @NotNull String statement, @NotNull Map<String, Object> data, @NotNull List<String> args) {
        this.message = message;
        this.statement = statement;
        this.data = data;
        this.args = args;
    }

}
