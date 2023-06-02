package hoursofza.commands.client;

import hoursofza.utils.DiscordUtils;
import hoursofza.utils.MessageEventLocal;
import hoursofza.utils.MockMessageSetup;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HelpTest {

    @Mock
    private DiscordUtils discordUtils;
    @Mock
    private MessageEventLocal messageEvent;
    @InjectMocks
    private Help help;
    @InjectMocks
    private MockMessageSetup mockMessageSetup;
    private AutoCloseable autoCloseable;

    @BeforeEach
    public void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        when(messageEvent.getMessage()).thenReturn(mockMessageSetup.message);
        mockMessageSetup.setupMocks();
    }

    @AfterEach
    public void breakDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void testExecute() {
        ArgumentCaptor<MessageCreateData> messageCaptor = ArgumentCaptor.forClass(MessageCreateData.class);

        help.execute(messageEvent);

        verify(messageEvent, times(1)).getMessage();
        verify(mockMessageSetup.message, times(1)).getAuthor();
        verify(mockMessageSetup.user, times(1)).getAsMention();
        verify(mockMessageSetup.message, times(1)).getChannel();
        verify(mockMessageSetup.channel, times(1)).sendMessage(messageCaptor.capture());
        verify(mockMessageSetup.messageAction, times(1)).queue();
        verify(mockMessageSetup.message, times(2)).addReaction(any(Emoji.class));
        verify(mockMessageSetup.reactionAction, times(2)).queue();
        verify(discordUtils, times(1)).awaitReaction(any(Message.class), anyInt(), any(), any(), any());

        MessageCreateData capturedMessage = messageCaptor.getValue();
        Assertions.assertTrue(capturedMessage.getEmbeds().get(0).getDescription().contains("I'm a simple"));
    }
}
