package hoursofza.listeners;


import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
//        log.info("Bot is ready");
//        VoiceChannel voiceChannel = event.getJDA().getVoiceChannelById("827425831365640246");
//        if (voiceChannel != null) {
//            voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
//        } else {
//            log.warn("no voice channel found");
//        }
    }

}
