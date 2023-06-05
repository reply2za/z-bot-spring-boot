package hoursofza.services;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import hoursofza.listeners.AudioEventListener;
import hoursofza.services.playback.QueueItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;


public class GuildService {
    private AudioPlayer audioPlayer;

    private AudioEventListener audioEventListener;

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public void setPlayerManager(AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    private AudioPlayerManager playerManager;
    private final String id;

    private final ArrayDeque<QueueItem> queue;


    public GuildService(String id) {
        this.id = id;
        queue = new ArrayDeque<>();
    }

    public String getId() {
        return id;
    }

    @Nullable
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }


    public AudioEventListener getAudioEventListener() {
        return audioEventListener;
    }

    public ArrayDeque<QueueItem> getQueue() {
        return queue;
    }

    public void setAudioPlayer(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    public void setAudioEventListener(AudioEventListener audioEventListener) {
        this.audioEventListener = audioEventListener;
    }
}
