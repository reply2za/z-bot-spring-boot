package hoursofza.commands.client.playback;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.enums.UserProvidedType;
import hoursofza.handlers.AudioPlayerSendHandler;
import hoursofza.listeners.Audio;
import hoursofza.services.GuildService;
import hoursofza.services.ProcessManagerService;
import hoursofza.services.YoutubeSearchService;
import hoursofza.utils.MessageEventLocal;
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

    Audio audioListener;
    ProcessManagerService processManagerService;
    YoutubeSearchService youtubeSearchService;

    Play(Audio audioListener, ProcessManagerService processManagerService, YoutubeSearchService youtubeSearchService) {
        this.audioListener = audioListener;
        this.processManagerService = processManagerService;
        this.youtubeSearchService = youtubeSearchService;
    }

    @Override
    public void execute(MessageEventLocal event) {
        if (event.getArgs().size() < 1) {
            event.getMessage().getChannel().sendMessage("*no link provided*").queue();
            return;
        }
        String wordOrLink = String.join(" ", event.getArgs()).trim();
        UserProvidedType type = wordOrLink.contains(" ") || !wordOrLink.contains(".") ? UserProvidedType.LINK : UserProvidedType.WORDS;
        playCommand(event.getMessage().getMember(), event.getMessage().getChannel(), type, wordOrLink);
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
        return Commands.slash("play", "plays a link or searches YouTube and plays")
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
        member.getGuild().getAudioManager().openAudioConnection(voiceState.getChannel());
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        GuildService guildService = processManagerService.getServer(member.getGuild().getId());
        AudioPlayer player = guildService.getAudioPlayer();
        if (player == null) {
            player = playerManager.createPlayer();
            guildService.setAudioPlayer(player);
            player.addListener(audioListener);
        } else {
            if (player.isPaused()) {
                player.setPaused(false);
                channel.sendMessage("*playing*").queue();
                return;
            }
        }

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayerSendHandler audioPlayerSendHandler = new AudioPlayerSendHandler(player);
        member.getGuild().getAudioManager().setSendingHandler(audioPlayerSendHandler);
        String link = "";
        if (type == UserProvidedType.WORDS) {
            String videoId = youtubeSearchService.searchAndGetLink(userInput);
            if (videoId.isBlank()) {
                channel.sendMessage("*could not find video*").queue();
                return;
            }
            link = "https://www.youtube.com/watch?v=" + videoId;
        } else if (userInput.contains("spotify.com")) {
            channel.sendMessage("*spotify is not currently supported*").queue();
        } else {
            link = userInput;
        }
        if (link.isBlank()) {
            channel.sendMessage("there was an issue processing your request").queue();
            return;
        }

        try {
            playerManager.loadItem(link, new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    guildService.getAudioPlayer().playTrack(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {

                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException e) {

                }
            }).get();
        } catch (Exception ignored) {
            System.out.println("there was an exception");
        }
    }
}