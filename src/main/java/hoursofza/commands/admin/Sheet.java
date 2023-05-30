package hoursofza.commands.admin;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.services.DatabaseService;
import hoursofza.utils.MessageEventLocal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Sheet implements AdminCommandHandler {

    private final DatabaseService databaseService;

    Sheet(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void execute(MessageEventLocal event) {
        try {
            // Demonstration of updating the sheet database
            this.databaseService.update("Hello world");
            event.getMessage().getChannel().sendMessage("updated sheet!").queue();
        } catch (Exception ignored) {
            event.getMessage().getChannel().sendMessage("there was an error").queue();
        }

    }

    @Override
    public List<String> getNames() {
        return List.of("sheet");
    }
}
