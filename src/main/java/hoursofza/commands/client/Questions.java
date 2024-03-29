package hoursofza.commands.client;

import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.handlers.TwentyQuestionsGame;
import hoursofza.services.GameFactories;
import hoursofza.services.GameService;
import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Questions implements ClientCommandHandler {

    @Autowired
    GameService gameService;
    @Autowired
    DiscordUtils discordUtils;
    @Autowired
    GameFactories gameFactories;
    private static final String PLAYER_NOT_FOUND_TXT = "could not find player\n" +
            "searching is done in the guild that command was initiated in";

    @Override
    public void execute(MessageEventLocal event) {
        String parameter = "";
        if (!event.args().isEmpty()) parameter = event.args().get(0);
        if (parameter.equalsIgnoreCase("end")) {
            event.message().getChannel().sendMessage("sending end game request").queue();
            gameService.requestEndGame(event.message().getAuthor(), TwentyQuestionsGame.class.getSimpleName());
            return;
        }
        if (!gameService.getActiveGames(event.message().getAuthor().getId()).isEmpty()) {
            event.message().getChannel().sendMessage("there can only be one game of this type, " +
                    "add the suffix 'end' to this command to end this game"
            ).queue();
        } else {
            String finalParameter = parameter;
            event.message().getChannel().sendMessage("name the username you would like to play with").onSuccess((message) -> {
                discordUtils.awaitMessage(event.message().getChannel(), 90,
                        (e) -> event.message().getAuthor().getId().equals(e.getAuthor().getId()),
                        (e) -> {
                            List<Member> matchingUsers = new ArrayList<>();
                            String userToFind = e.getMessage().getContentRaw();
                            try {
                                event.message().getGuildChannel();
                            } catch (Exception ignored) {
                                event.message().getChannel().sendMessage(PLAYER_NOT_FOUND_TXT).queue();
                                return false;
                            }
                            event.message().getGuild().loadMembers().onSuccess(members -> {
                                members.forEach(member -> {
                                    if (member.getUser().getName().equalsIgnoreCase(userToFind))
                                        matchingUsers.add(member);
                                });
                                if (matchingUsers.size() > 1) {
                                    event.message().getChannel().sendMessage("too many players with that username exist").queue();
                                } else if (matchingUsers.isEmpty()) {
                                    event.message().getChannel().sendMessage(PLAYER_NOT_FOUND_TXT).queue();
                                } else {
                                    Integer num = null;
                                    if (!finalParameter.isBlank()) {
                                        try {
                                            num = Integer.parseInt(finalParameter);
                                        } catch (Exception ignored) {
                                            num = -1;
                                        }
                                    }
                                    if (num != null && num < 1) {
                                        event.message().getChannel().sendMessage("invalid question count").queue();
                                        return;
                                    }
                                    event.message().getChannel().sendMessage("starting game...").queue();
                                    gameService.startGame(
                                            gameFactories.newTwentyQuestionsGame(
                                                    event.message().getAuthor(),
                                                    matchingUsers.get(0).getUser(),
                                                    num
                                            ),
                                            event.message().getChannel()
                                    );
                                }

                            });
                            return false;
                        },
                        () -> {
                            event.message().getChannel().sendMessage("cancelled").queue();
                        }
                );
            }).queue();
        }

    }

    @Override
    public List<String> getNames() {
        return List.of("question", "questions");
    }
}
