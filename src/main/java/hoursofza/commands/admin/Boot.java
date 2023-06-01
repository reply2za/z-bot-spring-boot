package hoursofza.commands.admin;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.config.AppConfig;
import hoursofza.services.ProcessManagerService;
import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import hoursofza.enums.Unicode;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Boot implements AdminCommandHandler {

    final ProcessManagerService processManagerService;
    final DiscordUtils discordUtils;
    final AppConfig appConfig;
    final String pid;
    private final static Emoji GEAR = Unicode.GEAR.getEmoji();

    Boot(ProcessManagerService processManagerService, DiscordUtils discordUtils, AppConfig appConfig) {
        this.pid = String.valueOf(ProcessHandle.current().pid());
        this.processManagerService = processManagerService;
        this.discordUtils = discordUtils;
        this.appConfig = appConfig;
    }

    @Override
    public void execute(MessageEventLocal event) {
        event.getMessage().getChannel().sendMessage(this.getStatus()).queue(message -> {
            message.addReaction(GEAR).queue();
            discordUtils.awaitReaction(message, 60,
                    (messageEvent) -> discordUtils.getAdmins().contains(messageEvent.getUserId()),
                    (messageEvent) -> {
                        processManagerService.setActive(!processManagerService.isActive());
                        if (messageEvent.getUser() != null)
                            message.removeReaction(GEAR, messageEvent.getUser()).queue();
                        message.editMessage(this.getStatus()).queue();
                        return true;
                    },
                    (x) -> message.clearReactions(GEAR).queue()
            );
        });

    }

    @Override
    public List<String> getNames() {
        return List.of("boot");
    }

    @Override
    public boolean isMultiProcessCommand() {
        return true;
    }

    private String getActiveText() {
        return processManagerService.isActive() ? "**active**" : "inactive";
    }

    private String getStatus() {
        return this.getActiveText() + ": " + pid + " (v" + appConfig.getVersion() + ")";
    }
}
