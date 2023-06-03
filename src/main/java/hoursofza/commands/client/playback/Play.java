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
import hoursofza.handlers.AudioPlayerSendHandler;
import hoursofza.listeners.Audio;
import hoursofza.services.GuildService;
import hoursofza.services.ProcessManagerService;
import hoursofza.services.YoutubeSearchService;
import hoursofza.utils.MessageEventLocal;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
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
        Member member = event.getMessage().getMember();
        if (member == null) {
            return;
        }
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            event.getMessage().getChannel().sendMessage("*must be in a voice channel*").queue();
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
                event.getMessage().getChannel().sendMessage("*playing*").queue();
                return;
            }
        }
        if (event.getArgs().size() < 1) {
            event.getMessage().getChannel().sendMessage("*no link provided*").queue();
            return;
        }
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayerSendHandler audioPlayerSendHandler = new AudioPlayerSendHandler(player);
        member.getGuild().getAudioManager().setSendingHandler(audioPlayerSendHandler);
        String linkOrWord = String.join(" ", event.getArgs());
        String link = "";
        if (linkOrWord.contains(" ") || !linkOrWord.contains(".")) {
            String videoId = youtubeSearchService.searchAndGetLink(linkOrWord);
            if (videoId.isBlank()) {
                event.getMessage().getChannel().sendMessage("*could not find video*").queue();
                return;
            }
            link = "https://www.youtube.com/watch?v=" + videoId;
            System.out.println(link);
        } else if (linkOrWord.contains("spotify.com")) {
            event.getMessage().getChannel().sendMessage("*spotify is not currently supported*").queue();
        } else {
            link = linkOrWord;
        }
        if (link.isBlank()) {
            event.getMessage().getChannel().sendMessage("there was an issue processing your request").queue();
            return;
        };
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

    @Override
    public List<String> getNames() {
        return List.of("play");
    }
}
