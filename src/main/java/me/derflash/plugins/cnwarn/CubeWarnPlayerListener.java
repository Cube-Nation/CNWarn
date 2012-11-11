package me.derflash.plugins.cnwarn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class CubeWarnPlayerListener implements Listener {
	private final CNWarn plugin;	
	
	public CubeWarnPlayerListener(CNWarn instance) {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }	
	
	@EventHandler(priority = EventPriority.HIGHEST)
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
		
        Watch watchedUser = plugin.getDatabase().find(Watch.class).setMaxRows(1).where().ieq("playerName", player.getName()).findUnique();
        if (watchedUser != null) {
        	
    		// inform admins
    		if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
    		    PermissionManager permissions = PermissionsEx.getPermissionManager();
    		    
    			for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
    			    if(permissions.has(onlinePlayer, "cnwarn.watch")) {
    			    	onlinePlayer.sendMessage(ChatColor.DARK_RED + "[CNWarn] " + ChatColor.AQUA + watchedUser.getPlayername() + " steht auf der Watchlist!");
    		    		onlinePlayer.sendMessage(ChatColor.AQUA + "Erstellt: " + watchedUser.getCreated());
    		    		onlinePlayer.sendMessage(ChatColor.AQUA + "Beschreibung: " + watchedUser.getMessage());
    			    }
    			}
    		}
        }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();

		plugin.notAccepted.remove(playerName.toLowerCase());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (plugin.notAccepted.contains(player)) {
			event.setCancelled(true);
	        if (event.isCancelled()) player.teleport(event.getFrom());

		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.notAccepted.contains(player)) {
	        event.setCancelled(true);
	        if (event.isCancelled()) player.teleport(event.getFrom());
	    }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		if (plugin.notAccepted.contains(player)) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() {
				player.sendMessage(ChatColor.DARK_RED + "!!! ACHTUNG !!! " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_RED + " DU WURDEST VERWARNT !!!");
	    		player.sendMessage(ChatColor.DARK_RED + "Du kannst dich jetzt nicht mehr bewegen,");
	    		player.sendMessage(ChatColor.DARK_RED + "bis du die Verwarnung aktzeptiert hast.");
	    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn info" + ChatColor.DARK_RED + " kannst du dir den Grund ansehen.");
	    		player.sendMessage(ChatColor.DARK_RED + "Mit " + ChatColor.GREEN  + "/warn accept" + ChatColor.DARK_RED + " aktzeptierst du die Verwarnung.");;
			}}, 0L);

		}
	}
}
