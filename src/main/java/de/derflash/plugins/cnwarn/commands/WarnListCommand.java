package de.derflash.plugins.cnwarn.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.model.Warn;
import de.derflash.plugins.cnwarn.services.WarnService;

public class WarnListCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnListCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = { "list", "info" }, min = 0, max = 0, help = "Zeigt dir deine Verwarnungen an.")
    public void listWarningPlayer(Player player) {
        warnService.clearExpired();

        String playerName = player.getName();
        if (warnService.isPlayersWarned(playerName)) {
            chatService.one(player, "player.warnHead", playerName);

            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm 'Uhr'");
            int ratingSum = 0;
            for (Warn warn : warnService.getWarnList(playerName)) {
                Date createDate = warn.getCreated();
                String created = formatter.format(createDate);
                ratingSum += warn.getRating();

                chatService.one(player, "player.warnEntry", warn.getId(), warn.getMessage(), warn.getRating(), created, warn.getStaffName(),
                        (warn.getAccepted() == null ? "Nein" : "Ja"));

                if (warn.getAccepted() != null) {
                    String accepted = formatter.format(warnService.calculateExpirationDate(warn));

                    chatService.one(player, "player.warnEntryAccepted", accepted);
                }
            }
            chatService.one(player, "player.warnFood", ratingSum);
        } else {
            chatService.one(player, "player.noWarn");
        }
    }

    @Command(main = "warn", sub = { "list", "info" }, min = 1, max = 1, usage = "[Spieler]", help = "Zeigt alle Verwarnungen des Spielers")
    @CommandPermissions("cubewarn.staff")
    public void listWarningAdmin(Player player, String playerName) {
        warnService.clearExpired();

        if (warnService.isPlayersWarned(playerName)) {
            chatService.one(player, "player.warnHead", playerName);

            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm 'Uhr'");
            int ratingSum = 0;
            for (Warn warn : warnService.getWarnList(playerName)) {
                Date createDate = warn.getCreated();
                String created = formatter.format(createDate);
                ratingSum += warn.getRating();

                chatService.one(player, "player.warnEntry", warn.getId(), warn.getMessage(), warn.getRating(), created, warn.getStaffName(),
                        (warn.getAccepted() == null ? "Nein" : "Ja"));

                if (warn.getAccepted() != null) {
                    String accepted = formatter.format(warnService.calculateExpirationDate(warn));

                    chatService.one(player, "player.warnEntryAccepted", accepted);
                }
            }
            chatService.one(player, "player.warnFood", ratingSum);
        } else {
            // player has no warnings
            chatService.one(player, "staff.searchNoWarn", playerName);
        }
    }
}
