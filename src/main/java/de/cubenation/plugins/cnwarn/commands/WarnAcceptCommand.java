package de.cubenation.plugins.cnwarn.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Player;

import de.cubenation.plugins.cnwarn.model.Warn;
import de.cubenation.plugins.cnwarn.model.exception.WarnsNotFoundException;
import de.cubenation.plugins.cnwarn.services.WarnService;
import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;

public class WarnAcceptCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnAcceptCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = "accept", max = 0, help = "Damit aktzeptierst du eine Verwarnung.")
    public void acceptWarning(Player player) throws WarnsNotFoundException {
        String playerName = player.getName();
        if (warnService.isPlayersWarned(playerName)) {
            if (warnService.hasPlayerNotAcceptedWarns(playerName)) {
                warnService.acceptWarns(playerName);

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
                chatService.one(player, "player.warnAccepted");
            } else {
                chatService.one(player, "player.warnAlreadyAccepted");
            }
        } else {
            chatService.one(player, "player.noWarn");
        }
    }
}
