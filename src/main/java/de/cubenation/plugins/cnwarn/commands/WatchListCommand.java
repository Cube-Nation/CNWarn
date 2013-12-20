package de.cubenation.plugins.cnwarn.commands;

import java.util.List;

import org.bukkit.entity.Player;

import de.cubenation.plugins.cnwarn.model.Watch;
import de.cubenation.plugins.cnwarn.services.WatchService;
import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;

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

        StringBuffer playerList = new StringBuffer();

        for (Watch watch : watchedPlayers) {
            if (playerList.length() > 0) {
                playerList.append(", ");
            }
            playerList.append(watch.getPlayerName());
        }

        if (playerList.length() > 0) {
            chatService.one(player, "staff.watchedPlayer", playerList.toString());
        } else {
            chatService.one(player, "staff.noWatchPlayers");
        }
    }
}
