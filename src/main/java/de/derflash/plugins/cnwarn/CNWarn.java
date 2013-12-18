package de.derflash.plugins.cnwarn;

import java.util.List;

import org.bukkit.event.Listener;

import de.cubenation.plugins.utils.pluginapi.BasePlugin;
import de.cubenation.plugins.utils.pluginapi.CommandSet;
import de.derflash.plugins.cnwarn.commands.WarnAcceptCommand;
import de.derflash.plugins.cnwarn.commands.WarnAddCommand;
import de.derflash.plugins.cnwarn.commands.WarnDeleteCommand;
import de.derflash.plugins.cnwarn.commands.WarnListCommand;
import de.derflash.plugins.cnwarn.commands.WarnSearchCommand;
import de.derflash.plugins.cnwarn.commands.WatchAddCommand;
import de.derflash.plugins.cnwarn.commands.WatchDeleteCommand;
import de.derflash.plugins.cnwarn.commands.WatchInfoCommand;
import de.derflash.plugins.cnwarn.commands.WatchListCommand;
import de.derflash.plugins.cnwarn.eventlistener.PlayerListener;
import de.derflash.plugins.cnwarn.model.Warn;
import de.derflash.plugins.cnwarn.model.Watch;
import de.derflash.plugins.cnwarn.services.WarnService;
import de.derflash.plugins.cnwarn.services.WatchService;

public class CNWarn extends BasePlugin {
    // local services
    private WarnService warnService;
    private WatchService watchService;

    @Override
    protected void initialCustomServices() {
        warnService = new WarnService(getDatabase(), getLogger());
        watchService = new WatchService(getDatabase());
    }

    @Override
    protected void startCustomServices() {
        warnService.setExpirationDays(getConfig().getInt("warn_expiration_days", 30));
    }

    @Override
    protected void registerCustomEventListeners(List<Listener> list) {
        list.add(new PlayerListener(warnService, watchService, chatService));
    }

    @Override
    protected void registerCommands(List<CommandSet> list) {
        list.add(new CommandSet(WarnAcceptCommand.class, warnService, chatService));
        list.add(new CommandSet(WarnAddCommand.class, warnService, chatService));
        list.add(new CommandSet(WarnDeleteCommand.class, warnService, chatService));
        list.add(new CommandSet(WarnListCommand.class, warnService, chatService));
        list.add(new CommandSet(WarnSearchCommand.class, warnService, chatService));
        list.add(new CommandSet(WatchAddCommand.class, watchService, chatService));
        list.add(new CommandSet(WatchDeleteCommand.class, watchService, chatService));
        list.add(new CommandSet(WatchInfoCommand.class, watchService, chatService));
        list.add(new CommandSet(WatchListCommand.class, watchService, chatService));
    }

    @Override
    protected void registerDatabaseModel(List<Class<?>> list) {
        list.add(Warn.class);
        list.add(Watch.class);
    }

    /**
     * Return the plugin local used service for warn issues.
     * 
     * @return
     * 
     * @since 1.2
     */
    public final WarnService getWarnService() {
        return warnService;
    }

    /**
     * Return the plugin local used service for player watch notes.
     * 
     * @return
     * 
     * @since 1.2
     */
    public final WatchService getWatchService() {
        return watchService;
    }
}
