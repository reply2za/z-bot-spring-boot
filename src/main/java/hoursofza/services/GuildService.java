package hoursofza.services;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import org.jetbrains.annotations.Nullable;


public class GuildService {
    private AudioPlayer audioPlayer;
    private final String id;

    public GuildService(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public void setAudioPlayer(AudioPlayer audioPlayer) {
        if (this.audioPlayer != null) {
            audioPlayer.destroy();
        }
        this.audioPlayer = audioPlayer;
    }
}
