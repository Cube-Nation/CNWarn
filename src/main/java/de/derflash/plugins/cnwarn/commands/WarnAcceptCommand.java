package de.derflash.plugins.cnwarn.commands;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.derflash.plugins.cnwarn.model.Warn;
import de.derflash.plugins.cnwarn.services.WarnService;

public class WarnAcceptCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnAcceptCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = "accept", max = 0, help = "Damit aktzeptierst du eine Verwarnung.")
    public void acceptWarning(Player player) {
        String playerName = player.getName();
        if (warnService.warnedPlayersContains(playerName)) {
            if (warnService.hasUnacceptedWarnings(playerName)) {
                warnService.acceptWarnings(playerName);

                chatService.one(player, "player.warnHead", playerName);

                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy 'um' HH:mm 'Uhr'");
                int ratingSum = 0;
                for (Warn warn : warnService.getWarnList(playerName)) {
                    Date createDate = warn.getCreated();
                    String created = formatter.format(createDate);
                    ratingSum += warn.getRating();

                    chatService.one(player, "player.warnEntry", warn.getId(), warn.getMessage(), warn.getRating(), created, warn.getStaffname(),
                            (warn.getAccepted() == null ? "Nein" : "Ja"));

                    if (warn.getAccepted() != null) {
                        GregorianCalendar acceptedDate = new GregorianCalendar();
                        acceptedDate.setTime(warn.getAccepted());
                        acceptedDate.add(Calendar.DAY_OF_MONTH, 30);
                        String accepted = formatter.format(acceptedDate.getTime());

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
