package me.derflash.plugins.cnwarn;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class CubeWarnPlayerListener extends PlayerListener{
	private final CNWarn plugin;	
	
	public CubeWarnPlayerListener(CNWarn instance) {
        plugin = instance;
    }	
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if(plugin.hasUnacceptedWarnings(player.getName())) {
			plugin.notAccepted.add(player);
    		player.sendMessage(ChatColor.DARK_RED + "!!! ACHTUNG !!! " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_RED + " DU WURDEST VERWARNT !!!");
    		player.sendMessage(ChatColor.DARK_RED + "Du kannst dich jetzt nicht mehr bewegen,");
    		player.sendMessage(ChatColor.DARK_RED + "bis du die Verwarnung aktzeptiert hast.");
    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn info" + ChatColor.DARK_RED + " kannst du dir den Grund ansehen.");
    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn accept" + ChatColor.DARK_RED + " aktzeptierst du die Verwarnung.");;
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();

		plugin.notAccepted.remove(playerName.toLowerCase());
	}

	
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (plugin.notAccepted.contains(player)) {
			event.setCancelled(true);
	        if (event.isCancelled()) player.teleport(event.getFrom());

		}
	}
	
	public void onPlayerTeleport(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.notAccepted.contains(player)) {
	        event.setCancelled(true);
	        if (event.isCancelled()) player.teleport(event.getFrom());
	    }
	}

	
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		if (plugin.notAccepted.contains(player)) {
    		player.sendMessage(ChatColor.DARK_RED + "!!! ACHTUNG !!! " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_RED + " DU WURDEST VERWARNT !!!");
    		player.sendMessage(ChatColor.DARK_RED + "Du kannst dich jetzt nicht mehr bewegen,");
    		player.sendMessage(ChatColor.DARK_RED + "bis du die Verwarnung aktzeptiert hast.");
    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn info" + ChatColor.DARK_RED + " kannst du dir den Grund ansehen.");
    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn accept" + ChatColor.DARK_RED + " aktzeptierst du die Verwarnung.");;
		}
	}
}
