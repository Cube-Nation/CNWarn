package de.derflash.plugins.cnwarn.eventlistener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubenation.plugins.utils.chatapi.ChatService;
import de.derflash.plugins.cnwarn.model.Watch;
import de.derflash.plugins.cnwarn.services.WarnService;
import de.derflash.plugins.cnwarn.services.WatchService;

public class PlayerListener implements Listener {
    private WarnService warnService;
    private WatchService watchService;
    private ChatService chatService;

    public PlayerListener(WarnService warnService, WatchService watchService, ChatService chatService) {
        this.warnService = warnService;
        this.watchService = watchService;
        this.chatService = chatService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (warnService.hasPlayerNotAcceptedWarns(player.getName())) {
            warnService.cacheNotAcceptedWarns(player.getName());
            chatService.one(player, "player.warnJoinInfo", player.getName());
        }

        Watch watchedPlayer = watchService.getWatchedPlayerByName(player.getName());
        if (watchedPlayer != null) {

            // inform admins
            chatService.allPerm("staff.watchJoinInfo", "cnwarn.watch", watchedPlayer.getPlayername(), watchedPlayer.getCreated(), watchedPlayer.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        warnService.removeCachedNotAcceptedWarns(player.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (warnService.hasPlayerNotAcceptedWarnsCached(player.getName())) {
            // player moves back to originally position by bukkit
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (warnService.hasPlayerNotAcceptedWarnsCached(player.getName())) {
            // player moves back to originally position by bukkit
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (warnService.hasPlayerNotAcceptedWarnsCached(player.getName())) {
            chatService.one(player, "player.warnJoinInfo", player.getName());
        }
    }
}
