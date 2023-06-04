package hoursofza.store;

import hoursofza.commands.interfaces.AdminCommandHandler;
import hoursofza.commands.interfaces.ClientCommandHandler;
import hoursofza.commands.interfaces.CommandHandler;
import hoursofza.utils.MessageEventLocal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Service
public class CommandStore {

    private Map<String, ClientCommandHandler> clientCommands;
    private Map<String, AdminCommandHandler> adminCommands;
    private Set<String> admins;


    @Nullable
    public CommandHandler getCommand(@NotNull MessageEventLocal messageEventLocal) {
        if (this.admins.contains(messageEventLocal.getMessage().getAuthor().getId())) {
            CommandHandler adminCmd = this.adminCommands.get(messageEventLocal.getStatement());
            if (adminCmd != null) {
                return adminCmd;
            }
        }
        return this.clientCommands.get(messageEventLocal.getStatement());
    }
    @Nullable
    public ClientCommandHandler getClientCommand(@NotNull String name) {
        return this.clientCommands.get(name);
    }

    public Collection<ClientCommandHandler> getClientCommands() {
        return this.clientCommands.values().parallelStream().distinct().toList();
    }

    /**
     * Sets the client commands. Can only be called once.
     * @param clientCommands
     * @return
     */
    public CommandStore setClientCommands(ConcurrentMap<String, ClientCommandHandler> clientCommands) {
        if (this.clientCommands != null) {
            throw new IllegalCallerException("cannot call method after being finalized");
        }
        this.clientCommands = Collections.unmodifiableMap(clientCommands);
        return this;
    }

    /**
     * Sets the admin commands. Can only be called once.
     * @param adminCommands
     * @return
     */
    public CommandStore setAdminCommands(ConcurrentMap<String, AdminCommandHandler> adminCommands) {
        if (this.adminCommands != null) {
            throw new IllegalCallerException("cannot call method after being finalized");
        }
        this.adminCommands = Collections.unmodifiableMap(adminCommands);
        return this;
    }

    /**
     * Sets the admins. Can only be called once.
     * @param admins
     * @return
     */
    public CommandStore setAdmins(Set<String> admins) {
        if (this.admins != null) {
            throw new IllegalCallerException("cannot call method after being finalized");
        }
        this.admins = Collections.unmodifiableSet(admins);
        return this;
    }
}


