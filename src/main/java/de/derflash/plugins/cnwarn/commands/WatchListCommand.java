package de.derflash.plugins.cnwarn.commands;

import java.util.List;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.model.Watch;
import de.derflash.plugins.cnwarn.services.ChatService;
import de.derflash.plugins.cnwarn.services.WatchService;

public class WatchListCommand {
    private WatchService watchService;
    private ChatService chatService;

    public WatchListCommand(WatchService watchService, ChatService chatService) {
        this.watchService = watchService;
        this.chatService = chatService;
    }

    @Command(main = "watch", sub = "list", max = 0, help = "Listet alle beobachteten Spieler auf")
    @CommandPermissions("cubewarn.watch")
    public void listWatch(Player player) {
        List<Watch> watchedPlayers = watchService.getAllWatches();
        if (watchedPlayers.size() > 0) {
            chatService.showStaffAllWatchedPlayers(player, watchedPlayers);
        } else {
            chatService.showStaffNoWatchedPlayers(player);
        }
    }
}
