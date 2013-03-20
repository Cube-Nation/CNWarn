package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.derflash.plugins.cnwarn.services.ChatService;
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
                chatService.showPlayerAcceptedWarning(player, playerName, warnService.getWarnList(playerName));
            } else {
                chatService.showPlayerAlreadyAcceptedWarning(player);
            }
        } else {
            chatService.showPlayerNoWarning(player);
        }
    }
}
