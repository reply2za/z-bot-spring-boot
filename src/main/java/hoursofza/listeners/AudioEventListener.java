package hoursofza.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import hoursofza.services.GuildService;

import java.util.function.Consumer;

public class AudioEventListener extends AudioEventAdapter {

    private final AudioPlayer player;
    private final GuildService guildService;

    private Consumer<String> playCommand;

    public AudioEventListener(AudioPlayer player, GuildService guildService) {
        this.player = player;
        this.guildService = guildService;
    }

    public void load(AudioTrack audioTrack, Consumer<String> playCommand) {
        this.playCommand = playCommand;
        System.out.println("made it");
//        player.stopTrack();
        player.startTrack(audioTrack, true);
        player.setPaused(false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            System.out.println("ended");
            guildService.getQueue().remove();
            if (!guildService.getQueue().isEmpty()) {
                playCommand.accept(guildService.getQueue().getFirst().getPlayableLink());
            }
        }
    }
}
