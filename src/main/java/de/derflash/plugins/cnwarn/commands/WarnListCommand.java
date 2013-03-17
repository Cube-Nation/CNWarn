package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.commandapi.annotation.Asynchron;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.services.ChatService;
import de.derflash.plugins.cnwarn.services.WarnService;

public class WarnListCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnListCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = { "list", "info" }, min = 0, max = 0, help = "Zeigt dir deine Verwarnungen an.")
    @Asynchron
    public void listWarningPlayer(Player player) {
        warnService.clearOld();

        String playerName = player.getName();
        if (warnService.warnedPlayersContains(playerName)) {
            chatService.showWarnList(player, playerName, warnService.getWarnList(playerName));
        } else {
            chatService.showPlayerNoWarning(player);
        }
    }

    @Command(main = "warn", sub = { "list", "info" }, min = 1, max = 1, usage = "[Spieler]", help = "Zeigt alle Verwarnungen des Spielers")
    @CommandPermissions("cubewarn.staff")
    @Asynchron
    public void listWarningAdmin(Player player, String playerName) {
        warnService.clearOld();

        if (warnService.warnedPlayersContains(playerName)) {
            chatService.showWarnList(player, playerName, warnService.getWarnList(playerName));
        } else {
            // player has no warnings
            chatService.showStaffNoWarningForPlayer(player, playerName);
        }
    }
}
