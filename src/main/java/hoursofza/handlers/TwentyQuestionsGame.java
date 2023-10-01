package hoursofza.handlers;

import hoursofza.config.AppConfig;
import hoursofza.enums.GameType;
import hoursofza.enums.ReactionEnum;
import hoursofza.handlers.interfaces.Game;
import hoursofza.utils.DiscordUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Slf4j
public class TwentyQuestionsGame implements Game {
    private final User answerer;
    private final User guesser;
    private String wordToGuess;
    private final DiscordUtils discordUtils;
    boolean guesserTurn = true;
    List<QuestionAnswer> questionAndAnswers = new ArrayList<>();
    private final int maxQuestions = 20;
    private boolean guesserHasWon = false;
    private static final List<String> VALID_ANSWERS;
    private boolean gameOver = false;
    private static final int REACT_TIMEOUT = 60 * 60 * 24 * 2;
    AppConfig appConfig;
    private static final String WIN_TXT = "you guessed it";
    private Message lastMessageGuesser;
    private Message lastMessageAnswerer;
    private static final int INIT_TIMEOUT = 120;

    static {
        VALID_ANSWERS = Stream.of("yes", "no", "sometimes", "rarely", WIN_TXT)
                .map(String::toLowerCase).toList();
    }


    public TwentyQuestionsGame(AppConfig appConfig, DiscordUtils discordUtils, User answerer, User guesser) {
        this.appConfig = appConfig;
        this.discordUtils = discordUtils;
        this.guesser = guesser;
        this.answerer = answerer;
    }

    @Override
    public void initialize(@Nullable MessageChannel channel) {
        answerer.openPrivateChannel().onSuccess(privateChannel -> {
            if (channel != null && !channel.getId().equals(privateChannel.getId())) {
                channel.sendMessage("*check your DMs*").queue();
            }
            privateChannel.sendMessage("what is the word?").onErrorMap(map -> {
                log.error(map.getMessage());
                return null;
            }).onSuccess((message) -> {
                discordUtils.awaitMessage(privateChannel, INIT_TIMEOUT,
                        (event) -> event.getMessage().getAuthor().getId().equals(answerer.getId()),
                        (event) -> {
                            wordToGuess = event.getMessage().getContentRaw();
                            event.getMessage().addReaction(ReactionEnum.CHECK_MARK.getEmoji()).queue();
                            event.getMessage().getChannel().sendMessage("waiting on other player...").queue();
                            updateGuesser(null);
                            return false;
                        },
                        () -> {
                            privateChannel.sendMessage("*no input provided, cancelled game*").queue();
                            gameOver = true;
                        });
            }).queue();
        }).queue();
    }


    @Override
    public boolean input(MessageChannel channel, User user, Message message) {
        String text = message.getContentRaw();
        if (guesserTurn) {
            questionAndAnswers.add(new QuestionAnswer().setQuestion(text));
            updateAnswerer(messageSuccessRunnable(message));
        } else {
            if (!VALID_ANSWERS.contains(text.trim().toLowerCase())) {
                channel.sendMessage("invalid answer").queue();
                return false;
            }
            if (text.equalsIgnoreCase(WIN_TXT)) {
                guesserHasWon = true;
                gameOver = true;
            } else if (questionAndAnswers.size() == maxQuestions) {
                gameOver = true;
            }
            if (!questionAndAnswers.isEmpty()) questionAndAnswers.get(questionAndAnswers.size() - 1).setAnswer(text);
            updateGuesser(messageSuccessRunnable(message));
        }
        return true;
    }

    @Override
    public GameType gameType() {
        return GameType.DM_ONLY;
    }

    @Override
    public List<User> getPlayers() {
        return List.of(guesser, answerer);
    }


    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    private String buildHistory() {
        StringBuilder sb = new StringBuilder("Questions asked " + questionAndAnswers.size() + "/" + maxQuestions + "\n");
        int i = 0;
        questionAndAnswers.forEach(x -> {
            sb.append(i).append(". ").append(x.question);
            if (!x.answer.isEmpty()) sb.append(" > ").append(x.answer);
            sb.append("\n");
        });
        return sb.toString();
    }

    private void updateGuesser(@Nullable Runnable successCallback) {
        if (gameOver) {
            guesser.openPrivateChannel().onSuccess(privateChannel -> {
                String endGameTxt;
                if (guesserHasWon) {
                    endGameTxt = "you've won, the word is: " + wordToGuess + "\n" +
                            "tries: " + questionAndAnswers.size() + "/" + maxQuestions;
                } else {
                    endGameTxt = buildHistory() + "\nGame Over. The word was: **" + wordToGuess + "**";
                }
                privateChannel.sendMessage(endGameTxt).queue();
            }).queue();
            return;
        }
        String description = "";
        description += "**React to ask a question or guess the word**\n" + buildHistory();
        guesserTurn = true;
        updatePlayer(guesser, description, (message) -> {
            if (successCallback != null) successCallback.run();
            if (lastMessageGuesser != null) lastMessageGuesser.delete().queue();
            lastMessageGuesser = message;
        });
    }

    private void updateAnswerer(@Nullable Runnable successCallback) {
        guesserTurn = false;
        String description = "**React to answer the question**\n" +
                "*Questions*\n" + buildHistory() + getValidAnswers();
        updatePlayer(answerer, description, (message) -> {
            if (successCallback != null) successCallback.run();
            if (lastMessageAnswerer != null) lastMessageAnswerer.delete().queue();
            lastMessageAnswerer = message;
        });
    }

    private void updatePlayer(User user, String description, Consumer<Message> messageConsumer) {
        actionWrapper(user.openPrivateChannel(), privateChannel -> {
            actionWrapper(privateChannel.sendMessage(description), message -> {
                messageConsumer.accept(message);
                actionWrapper(message.addReaction(ReactionEnum.ENVELOPE.getEmoji()),
                        (reaction) -> awaitReactionAndInput(message, user, ReactionEnum.ENVELOPE));
            });
        });
    }

    private <T> void actionWrapper(RestAction<T> cacheRestAction, Consumer<T> onSuccess) {
        cacheRestAction.onSuccess(onSuccess).onErrorMap(throwable -> {
            log.error(throwable.getMessage());
            return null;
        }).queue();
    }

    /**
     * Awaits a reaction for an official game message that requires an input response.
     *
     * @param message
     * @param responder
     * @param reaction
     */
    private void awaitReactionAndInput(Message message, User responder, ReactionEnum reaction) {
        discordUtils.awaitReaction(message, REACT_TIMEOUT, (event) ->
                        event.getUser() != null && !event.getUser().isBot() && event.getReaction().getEmoji().equals(reaction.getEmoji())
                                && event.getUser().getId().equals(responder.getId()),
                (event) -> {
                    event.getChannel().sendMessage("input: ").queue();
                    discordUtils.awaitMessage(message.getChannel(), INIT_TIMEOUT,
                            (e) ->
                                    event.getUser() != null && e.getAuthor().getId().equals(responder.getId()),
                            (e) -> !input(e.getChannel(), e.getAuthor(), e.getMessage()),
                            () -> {
                                message.getChannel().sendMessage("no input provided, react when you are ready").queue();
                                awaitReactionAndInput(message, responder, reaction);
                            });
                    return false;
                },
                () -> {
                    message.getChannel().sendMessage(
                            "no input provided, please reply with " + appConfig.getPrefix() + "input when you are ready"
                    ).queue();
                }
        );
    }

    private static String getValidAnswers() {
        return "you can say: " + VALID_ANSWERS.stream().map(answer -> "**" + answer + "**").collect(Collectors.joining(", "));
    }

    private Runnable messageSuccessRunnable(Message message) {
        return () -> {
            message.addReaction(ReactionEnum.THUMBS_UP.getEmoji()).queue();
            discordUtils.awaitReaction(message, 120,
                    (e) -> e.getUser() != null && message.getAuthor().getId().equals(e.getUser().getId()),
                    (e) -> {
                        message.getChannel().sendMessage("your message has been sent to the other player").queue();
                        return false;
                    },
                    () -> {
                    }
            );
        };
    }

    private static class QuestionAnswer {
        private String question = "";
        @Setter
        private String answer = "";

        public QuestionAnswer setQuestion(String question) {
            this.question = question;
            return this;
        }
    }

}
