package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.commandapi.annotation.Asynchron;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.services.ChatService;
import de.derflash.plugins.cnwarn.services.WatchService;

public class WatchAddCommand {
    private WatchService watchService;
    private ChatService chatService;

    public WatchAddCommand(WatchService watchService, ChatService chatService) {
        this.watchService = watchService;
        this.chatService = chatService;
    }

    @Command(main = "watch", min = 1, usage = "[Spieler] [Beschreibung]", help = "Beobachtet diesen Spieler")
    @CommandPermissions("cubewarn.watch")
    @Asynchron
    public void addWatch(Player player, String[] args) {
        String playerName = args[0];

        if (!watchService.isPlayerInWatchList(playerName)) {
            String description = "";
            for (int i = 1; i < args.length; i++) {
                description += (" " + args[i]);
            }
            description = description.trim();

            watchService.addWatch(playerName, description, player.getName());
            chatService.showStaffAddWatch(player, playerName);
        } else {
            chatService.showStaffAlreadyAddedWatch(player, playerName);
        }
    }
}
