package de.derflash.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.utils.commandapi.annotation.Asynchron;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;
import de.derflash.plugins.cnwarn.services.WarnService;

public class WarnConfirmCommand {
    private WarnService warnService;

    public WarnConfirmCommand(WarnService warnService) {
        this.warnService = warnService;
    }

    @Command(main = "warn", sub = "confirm", max = 0, help = "Bestätigt die Verwarnung eines offline Spielers")
    @CommandPermissions("cubewarn.staff")
    @Asynchron
    public void confirmWarning(Player player) {
        warnService.confirmOfflinePlayerWarning(player);
    }
}
