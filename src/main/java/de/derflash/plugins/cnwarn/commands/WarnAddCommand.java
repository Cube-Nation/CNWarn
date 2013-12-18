package de.derflash.plugins.cnwarn.commands;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
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

        String message = StringUtils.join(argList, " ");
        Integer rating;

        // check if the message is at least 5 chars long
        if (message.length() <= 4) {
            chatService.one(player, "staff.warnDescToShort");
            return;
        }

        if (!warnService.hasPlayedBefore(playerName)) {
            chatService.one(player, "staff.playerNotJoinedBefore", playerName);
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

        Boolean wasWarned = warnService.isPlayersWarned(playerName);
        String existsWarnCount = "0";
        String existsWarnRatingSum = "0";
        if (wasWarned) {
            existsWarnCount = warnService.getWarnCount(playerName).toString();
            existsWarnRatingSum = warnService.getRatingSum(playerName).toString();
        }

        if (!warnService.warnPlayer(playerName, player.getName(), message, rating)) {
            chatService.one(player, "staff.newWarnFailed");
            return;
        }

        chatService.one(player, "staff.newWarn", playerName, message, rating.toString());

        if (wasWarned) {
            chatService.one(player, "staff.warnExists", playerName, existsWarnCount, existsWarnRatingSum);
        }

        // inform play, if online
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            chatService.one(onlinePlayer, "player.warnJoinInfo", playerName);
        }
    }
}
