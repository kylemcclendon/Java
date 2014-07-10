package net.kylemc.kadmin;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class Maintenance implements Listener{
	Logger log = Logger.getLogger("Minecraft");
	private final Kadmin plugin;

	public Maintenance(Kadmin instance){
		this.plugin = instance;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void login(PlayerLoginEvent event){
		Player p = event.getPlayer();
		if(Bukkit.hasWhitelist() && !p.isOp()){
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Server Under Maintenance.\n Watch from direct.kylemc.net/map");
		}
		return;
	}
}