package de.cubenation.plugins.cnwarn.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.cnwarn.model.exception.WarnNoLocationException;
import de.cubenation.plugins.cnwarn.model.exception.WarnNotFoundException;
import de.cubenation.plugins.cnwarn.services.WarnService;
import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;

public class WarnTPCommand {
    private WarnService warnService;
    private ChatService chatService;

    public WarnTPCommand(WarnService warnService, ChatService chatService) {
        this.warnService = warnService;
        this.chatService = chatService;
    }

    @Command(main = "warn", sub = "tp", min = 1, max = 1, usage = "[Id]", help = "TP zu einer Verwarnung")
    @CommandPermissions("cubewarn.admin")
    public void tpWarning(Player player, String warnIdStr) throws WarnNotFoundException {
        int id;
        try {
            id = Integer.parseInt(warnIdStr);
        } catch (Exception e) {
            chatService.one(player, "staff.warnTPWarnId");
            return;
        }

        try {
			if (warnService.tpToWarn(player, id)) {
			    chatService.one(player, "staff.warnTPed", id);
			}
		} catch (WarnNoLocationException e) {
		    chatService.one(player, "exception.warnNoLocation", id);
		}
    }

}
