package hoursofza.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import hoursofza.listeners.AudioEventListener;
import hoursofza.services.GuildService;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.function.Consumer;

public class AudioLoadHandler implements AudioLoadResultHandler {
    private final GuildService guildService;
    private final MessageChannel channel;
    private final Consumer<String> playCommand;
    private final AudioEventListener audioListener;

    public AudioLoadHandler(GuildService guildService, MessageChannel channel, AudioEventListener audioListener,
                            Consumer<String> playCommand) {
        this.guildService = guildService;
        this.channel = channel;
        this.audioListener = audioListener;
        this.playCommand = playCommand;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        System.out.println("loaded: " + audioTrack.getInfo().title);
        if (guildService.getAudioPlayer() == null) {
            throw new IllegalStateException("Expected audio player to exist");
        }
        audioListener.load(audioTrack, playCommand);
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {

    }

    @Override
    public void noMatches() {

    }

    @Override
    public void loadFailed(FriendlyException e) {
        channel.sendMessage("error playing link: " + guildService.getQueue().getFirst()).queue();
        guildService.getQueue().remove();
        if (guildService.getQueue().size() > 1) {
            playCommand.accept(guildService.getQueue().getFirst().getPlayableLink());
        }
    }
}

