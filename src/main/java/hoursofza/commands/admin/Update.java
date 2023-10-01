package hoursofza.commands.admin;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.utils.MessageEventLocal;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Component
public class Update implements AdminCommandHandler {

    @Override
    public void execute(MessageEventLocal event) {
        Process process = null;
        BufferedReader reader = null;
        try {
            event.message().getChannel().sendMessage("updating...").queue();
            process = Runtime.getRuntime().exec("bash pm2/start.bash");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            event.message().getChannel().sendMessage("`".concat(output.append("`").toString())).queue();
        } catch (IOException e) {
            event.message().getChannel().sendMessage("there was an error updating").queue();
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Override
    public List<String> getNames() {
        return List.of("update");
    }
}
