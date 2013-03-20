package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.model.Watch;
import de.derflash.plugins.cnwarn.services.WatchService;

public class WatchInfoCommand {
    private WatchService watchService;
    private ChatService chatService;

    public WatchInfoCommand(WatchService watchService, ChatService chatService) {
        this.watchService = watchService;
        this.chatService = chatService;
    }

    @Command(main = "watch", sub = "info", min = 1, max = 1, usage = "[Spieler/Id]", help = "Gibt alle Infos zum Spieler aus")
    @CommandPermissions("cubewarn.watch")
    public void infoWatch(Player player, String playerName) {
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
            chatService.one(player, "staff.warnInfo", watchedPlayer.getPlayername(), watchedPlayer.getStaffname(), watchedPlayer.getCreated(),
                    watchedPlayer.getMessage());
        } else {
            chatService.one(player, "staff.playerNotWatched");
        }
    }
}
