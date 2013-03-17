package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.commandapi.annotation.Asynchron;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.services.ChatService;
import de.derflash.plugins.cnwarn.services.WarnService;

public class WarnDeleteCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnDeleteCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = "del", min = 1, max = 1, usage = "[Id]", help = "Löscht eine einzelne Verwarnung")
    @CommandPermissions("cubewarn.admin")
    @Asynchron
    public void deleteWarning(Player player, String amount) {
        Integer id;
        try {
            id = Integer.parseInt(amount);
        } catch (Exception e) {
            chatService.showStaffDelWarnCorrect(player);
            return;
        }

        warnService.deleteWarning(id, player);
    }

    @Command(main = "warn", sub = "delall", min = 1, max = 1, usage = "[Spieler]", help = "Löscht alle Verwarnungen des Spielers")
    @CommandPermissions("cubewarn.admin")
    @Asynchron
    public void deleteAllWarning(Player player, String playerName) {
        if (warnService.warnedPlayersContains(playerName)) {
            // delete all warnings if the player was warned
            warnService.deleteWarnings(playerName, player);
        } else {
            // nothing to delete, player has no warnings
            chatService.showStaffPlayerHasNoWarning(player, playerName);
        }
    }
}
