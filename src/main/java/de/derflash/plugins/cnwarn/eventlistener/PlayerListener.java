package de.derflash.plugins.cnwarn.eventlistener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.derflash.plugins.cnwarn.model.Watch;
import de.derflash.plugins.cnwarn.services.ChatService;
import de.derflash.plugins.cnwarn.services.PermissionService;
import de.derflash.plugins.cnwarn.services.WarnService;
import de.derflash.plugins.cnwarn.services.WatchService;

public class PlayerListener implements Listener {
    private WarnService warnService;
    private WatchService watchService;
    private PermissionService permissionService;
    private ChatService chatService;

    public PlayerListener(WarnService warnService, WatchService watchService, PermissionService permissionService, ChatService chatService) {
        this.warnService = warnService;
        this.watchService = watchService;
        this.permissionService = permissionService;
        this.chatService = chatService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (warnService.hasUnacceptedWarnings(player.getName())) {
            warnService.addNotAccepted(player);
            chatService.showPlayerNewWarning(player);
        }

        Watch watchedPlayer = watchService.getWatchedPlayerByName(player.getName());
        if (watchedPlayer != null) {

            // inform admins
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (permissionService.hasPermission(onlinePlayer, "cnwarn.watch")) {
                    chatService.showStaffJoinWatchedPlayer(onlinePlayer, watchedPlayer);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        warnService.removeNotAccepted(playerName.toLowerCase());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (warnService.containsNotAccepted(player)) {
            event.setCancelled(true);
            if (event.isCancelled()) {
                player.teleport(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (warnService.containsNotAccepted(player)) {
            event.setCancelled(true);
            if (event.isCancelled()) {
                player.teleport(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (warnService.containsNotAccepted(player)) {
            chatService.showPlayerNewWarning(player);
        }
    }
}
