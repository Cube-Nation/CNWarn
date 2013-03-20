package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.services.WarnService;

public class WarnSearchCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnSearchCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = { "search", "check" }, max = 1, usage = "[Spieler]", help = "Nach einem Spieler suchen.")
    @CommandPermissions("cubewarn.staff")
    public void checkWarning(Player player, String[] args) {
        warnService.clearOld();

        if (args.length < 1 || args[0].length() < 3) {
            chatService.one(player, "staff.checkWarnNotCorrect");
        } else {
            warnService.showSuggestions(args[0], player);
        }
    }
}
