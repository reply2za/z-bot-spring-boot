package hoursofza.commands.client.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.enums.UserProvidedType;
import hoursofza.handlers.AudioLoadHandler;
import hoursofza.handlers.AudioPlayerSendHandler;
import hoursofza.listeners.AudioEventListener;
import hoursofza.services.GuildService;
import hoursofza.services.ProcessManagerService;
import hoursofza.services.YoutubeSearchService;
import hoursofza.services.playback.QueueItem;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Play implements ClientCommandHandler {

    private final ProcessManagerService processManagerService;
    private final YoutubeSearchService youtubeSearchService;
    private final Resume resume;

    Play(ProcessManagerService processManagerService, YoutubeSearchService youtubeSearchService, Resume resume) {
        this.processManagerService = processManagerService;
        this.youtubeSearchService = youtubeSearchService;
        this.resume = resume;
    }

    @Override
    public void execute(MessageEventLocal event) {
        if (event.args().size() < 1) {
            event.message().getChannel().sendMessage("*no link provided*").queue();
            return;
        }
        String wordOrLink = String.join(" ", event.args()).trim();
        UserProvidedType type = wordOrLink.contains(" ") || !wordOrLink.contains(".") ? UserProvidedType.WORDS : UserProvidedType.LINK;
        playCommand(event.message().getMember(), event.message().getChannel(), type, wordOrLink);
    }

    @Override
    public List<String> getNames() {
        return List.of("play", "p");
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandInteractionEvent slashCommandEvent) {
        if (slashCommandEvent.getOptions().size() < 1) {
            slashCommandEvent.reply("search or link option is required").queue();
            return;
        }
        OptionMapping optionMapping = slashCommandEvent.getOptions().get(0);
        UserProvidedType type = UserProvidedType.valueOf(optionMapping.getName().toUpperCase());
        playCommand(slashCommandEvent.getMember(), slashCommandEvent.getChannel(), type, optionMapping.getAsString());
        slashCommandEvent.reply("*playing (" + (slashCommandEvent.getMember() != null ?
                slashCommandEvent.getMember().getNickname() : slashCommandEvent.getUser().getName()) +
                ")*").queue();
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return Commands.slash("play", "plays a link or searches YouTube to play")
                .addOption(OptionType.STRING, "words", "words for search")
                .addOption(OptionType.STRING, "link", "link to play");
    }

    private void playCommand(Member member, @NotNull MessageChannel channel, UserProvidedType type, String userInput) {
        if (member == null) {
            return;
        }
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            channel.sendMessage("*must be in a voice channel*").queue();
            return;
        }
        GuildService guildService = ProcessManagerService.getServer(member.getGuild().getId());
        AudioPlayer player = guildService.getAudioPlayer();
        if (!member.getGuild().getAudioManager().isConnected()) {
            guildService.getQueue().clear();
        }
        if (player != null && resume.resumeCommand(member.getGuild())) {
            player.setPaused(false);
            channel.sendMessage("*resumed*").queue();
            return;
        }
        boolean queueWasEmpty = guildService.getQueue().isEmpty();
        String link = "";
        if (type == UserProvidedType.WORDS) {
            String videoId = youtubeSearchService.searchAndGetLink(userInput);
            if (videoId.isBlank()) {
                channel.sendMessage("*could not find video*").queue();
                return;
            }
            link = "https://www.youtube.com/watch?v=" + videoId;
            guildService.getQueue().add(new QueueItem(link));
        } else if (userInput.contains("spotify.com")) {
            channel.sendMessage("*spotify is not currently supported*").queue();
        } else {
            link = userInput;
            guildService.getQueue().add(new QueueItem(link));
        }
        if (link.isBlank()) {
            channel.sendMessage("there was an issue processing your request").queue();
            return;
        }
        if (!queueWasEmpty) {
            channel.sendMessage("added to queue").queue();
            return;
        }
        playLink(member.getGuild(), guildService, channel, voiceState, link);
    }

    public void playLink(Guild guild, GuildService guildService, MessageChannel channel, GuildVoiceState voiceState, String link) {
        AudioPlayer player = guildService.getAudioPlayer();
        AudioPlayerManager playerManager = guildService.getPlayerManager();
        AudioEventListener audioEventListener = guildService.getAudioEventListener();

        guild.getAudioManager().openAudioConnection(voiceState.getChannel());
        if (playerManager == null) {
            playerManager = new DefaultAudioPlayerManager();
            guildService.setPlayerManager(playerManager);
        }
        if (player != null) player.destroy();
        player = playerManager.createPlayer();
        guildService.setAudioPlayer(player);

        audioEventListener = new AudioEventListener(player, guildService);
        guildService.setAudioEventListener(audioEventListener);
        player.addListener(audioEventListener);
        AudioPlayerSendHandler audioPlayerSendHandler = new AudioPlayerSendHandler(player);
        guild.getAudioManager().setSendingHandler(audioPlayerSendHandler);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioLoadHandler audioLoadHandler = new AudioLoadHandler(guildService, channel, audioEventListener,
                (nextLink) -> playLink(guild, guildService, channel, voiceState, nextLink));
        try {
            System.out.println("loading...");
            playerManager.loadItem(link, audioLoadHandler).get();
        } catch (Exception ignored) {
            System.out.println("there was an exception");
        }
    }

}




