package de.cubenation.plugins.cnwarn.commands;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import de.cubenation.plugins.cnwarn.services.WarnService;
import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;

public class WarnSearchCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnSearchCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = { "search", "check" }, min = 1, max = 1, usage = "[Spieler]", help = "Nach einem Spieler suchen.")
    @CommandPermissions("cubewarn.staff")
    public void checkWarning(Player player, String playerName) {
        warnService.clearExpired();

        if (playerName.length() < 3) {
            chatService.one(player, "staff.checkWarnNotCorrect");
        } else {
            chatService.one(player, "staff.searchWarnedPlayers", playerName);

            Collection<String> found = warnService.searchPlayerWithWarns(playerName);

            if (found.isEmpty()) {
                chatService.one(player, "staff.noSearchEntries");
            } else {
                chatService.one(player, "staff.searchEntries", StringUtils.join(found, ", "));
            }
        }
    }
}
