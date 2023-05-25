package hoursofza.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class MockMessageSetup {

    @Mock
    public Message message;

    @Mock
    public User user;

    @Mock
    public MessageChannelUnion channel;

    @Mock
    public MessageCreateAction messageAction;

    @Mock
    public RestAction<Void> reactionAction;

    @Mock
    public AuditableRestAction<Void> auditableRestAction;

    public MockMessageSetup() throws Exception {
            AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this);
            setupMocks();
    }

    public void setupMocks() {
        when(message.getAuthor()).thenReturn(user);
        when(user.getAsMention()).thenReturn("@user");
        when(message.getChannel()).thenReturn(channel);
        when(channel.sendMessage(any(MessageCreateData.class))).thenReturn(messageAction);
        when(message.addReaction(any(Emoji.class))).thenReturn(reactionAction);
        when(message.removeReaction(any(Emoji.class), any(User.class))).thenReturn(auditableRestAction);
    }
}
