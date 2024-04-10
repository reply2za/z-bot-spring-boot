package hoursofza.services;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import hoursofza.listeners.AudioEventListener;
import hoursofza.services.playback.QueueItem;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;


public class GuildService {
    @Setter
    private AudioPlayer audioPlayer;

    @Setter
    @Getter
    private AudioEventListener audioEventListener;

    @Setter
    @Getter
    private AudioPlayerManager playerManager;
    @Getter
    private final String id;

    @Getter
    private final ArrayDeque<QueueItem> queue;


    public GuildService(String id) {
        this.id = id;
        queue = new ArrayDeque<>();
    }

    @Nullable
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }


}
