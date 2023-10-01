package hoursofza.commands.client;


import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.enums.ReactionEnum;
import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;

@Component
public class Help implements ClientCommandHandler {

    private final DiscordUtils discordUtils;
    Help(DiscordUtils discordUtils) {
        this.discordUtils =  discordUtils;
    }
    @Override
    public void execute(MessageEventLocal messageEvent) {
        Message message = messageEvent.message();
        String mention = getMention(message.getMember(), message.getAuthor());
        message.getChannel().sendMessage(computation(message, mention)).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandInteractionEvent slashCommandEvent) {
        String mention = getMention(slashCommandEvent.getMember(), slashCommandEvent.getUser());
        slashCommandEvent.reply(computation(null, mention)).queue();
    }

    private String getMention(Member member, User author) {
        if (member != null) {
            return member.getAsMention();
        } else {
            return author.getName();
        }
    }

    private MessageCreateData computation(@Nullable Message messageToReactTo, @Nullable String mention) {
//        if (mention == null) mention = "friend";
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Hello!")
//                .setDescription("Hi, " + mention + "! I'm a simple Discord bot built with Spring Boot and JDA.")
                .setDescription("question (num): play 20-questions or specify a number\n" +
                        "question [end]: quit all 20-question games")
                .setColor(Color.decode("#469963"));
        if (messageToReactTo != null) {
            List<Emoji> reacts = List.of(ReactionEnum.THUMBS_UP.getEmoji(), ReactionEnum.GEAR.getEmoji());
            reacts.forEach(e -> messageToReactTo.addReaction(e).queue());
            discordUtils.awaitReaction(
                    messageToReactTo,
                    30,
                    event -> {
                        if (event.getUser() != null) {
                            return event.getUser().getId().equals(messageToReactTo.getAuthor().getId());
                        }
                        return false;
                    },
                    event -> {
                        if (event.getUser() != null) {
                            reacts.forEach(emoji -> messageToReactTo.removeReaction(emoji, event.getUser()).queue());
                        }
                        return true;
                    },
                    () -> reacts.forEach(emoji -> messageToReactTo.clearReactions(emoji).queue())
            );
        }
        return MessageCreateData.fromEmbeds(embedBuilder.build());
    }


    @Override
    public List<String> getNames() {
        return List.of("help", "h");
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("help", "displays the help list");
    }
}
