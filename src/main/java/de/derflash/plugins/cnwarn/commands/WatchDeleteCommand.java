package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.model.Watch;
import de.derflash.plugins.cnwarn.services.WatchService;

public class WatchDeleteCommand {
    private WatchService watchService;
    private ChatService chatService;

    public WatchDeleteCommand(WatchService watchService, ChatService chatService) {
        this.watchService = watchService;
        this.chatService = chatService;
    }

    @Command(main = "watch", sub = { "delete", "remove" }, min = 1, max = 1, usage = "[Spieler/Id]", help = "Löscht den Spieler aus der Beobachtungsliste")
    @CommandPermissions("cubewarn.watch")
    public void deleteWatch(Player player, String playerName) {
        int id = -1;
        try {
            id = Integer.parseInt(playerName);
        } catch (Exception e) {
        }

        if (id != -1 && !playerName.equals(Integer.toString(id))) {
            id = -1;
        }

        Watch watchedPlayer = null;
        if (id != -1) {
            watchedPlayer = watchService.getWatchedPlayerById(id);
        } else {
            watchedPlayer = watchService.getWatchedPlayerByName(playerName);
        }

        if (watchedPlayer != null) {
            if (!watchService.deletePlayerWatch(watchedPlayer)) {
                chatService.one(player, "staff.watchDeletedFailed", watchedPlayer.getPlayerName());
                return;
            }

            chatService.one(player, "staff.watchDeleted", watchedPlayer.getPlayerName());
        } else {
            chatService.one(player, "staff.playerNotWatched");
        }
    }
}
