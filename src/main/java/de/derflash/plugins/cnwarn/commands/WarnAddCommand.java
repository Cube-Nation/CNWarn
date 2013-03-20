package de.derflash.plugins.cnwarn.commands;

import java.util.Arrays;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.services.WarnService;

public class WarnAddCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnAddCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", min = 3, usage = "[Spieler] [Grund] [Bewertung]", help = "Verwarnt einen Spieler")
    @CommandPermissions("cubewarn.staff")
    public void addWarning(Player player, String[] args) {
        warnService.clearOld();

        LinkedList<String> argList = new LinkedList<String>(Arrays.asList(args));

        String playerName = argList.pollFirst();
        String rate = argList.pollLast();

        String message = "";
        Integer rating;

        // get the message
        for (String word : argList) {
            message += " " + word;
        }
        message = message.trim();

        // check if the message is at least 5 chars long
        if (message.length() <= 4) {
            chatService.one(player, "staff.warnDescToShort");
            return;
        }

        // get the rating
        try {
            rating = Integer.parseInt(rate);
            if (rating > 6 || rating <= 0) {
                chatService.one(player, "staff.warnRateWrong");
                return;
            }
        } catch (Exception e) {
            chatService.one(player, "staff.warnRateNoNum");
            return;
        }

        // get the warned player object and check if he is online
        Player warnPlayer = Bukkit.getServer().getPlayer(playerName);
        if (warnPlayer instanceof Player) {
            // check if the player tries to warn himself
            warnService.warnPlayer(warnPlayer.getName(), player, message, rating);
            chatService.one(warnPlayer, "player.warnJoinInfo", warnPlayer.getName());

        } else if (warnService.hasPlayedBefore(Bukkit.getOfflinePlayer(playerName))) {
            warnService.warnPlayer(Bukkit.getOfflinePlayer(playerName).getName(), player, message, rating);

        } else {
            chatService.one(player, "staff.playerNotJoinedBefore", playerName);
        }
    }
}
