package hoursofza.utils;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
@Data
public class NoMessageEventLocal {
    private final boolean isAdmin;
    private final String statement;

    private final List<String> args;

    public Map<String, Object> data;

    public NoMessageEventLocal(boolean isAdmin, @NotNull String statement, @NotNull Map<String, Object> data, @NotNull List<String> args) {
        this.isAdmin = isAdmin;
        this.statement = statement;
        this.data = data;
        this.args = args;
    }
}