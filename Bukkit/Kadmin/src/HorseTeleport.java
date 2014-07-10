package net.kylemc.kadmin;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class HorseTeleport implements Listener
{
	@EventHandler
	public void onHorseTP(PlayerCommandPreprocessEvent event){
		String input = event.getMessage();
		if((!event.getPlayer().isOp()) && (input.equalsIgnoreCase("/tp d") || input.equalsIgnoreCase("/tp dd") || input.equalsIgnoreCase("/tp dd6") || input.equalsIgnoreCase("/tp dd62") || input.equalsIgnoreCase("/tp dd622"))){
			event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to warp to this person.");
			event.setCancelled(true);
		}
		if(input.startsWith("/tp") || input.startsWith("/warp") || input.startsWith("/home") || input.startsWith("/spawn")){
			//If valid teleporting command
			Player p = event.getPlayer();
			if(p.isInsideVehicle() && p.getVehicle().getType() == EntityType.HORSE){
				//If player is on a horse
				Horse h = (Horse) p.getVehicle();
				if(h.isTamed()){
					//If the horse is tamed
					p.leaveVehicle();
					String message = event.getMessage().substring(1);
					World w = p.getWorld();
					p.performCommand(message);
					if(w == p.getWorld()){
						h.teleport(p);
					}
					event.setCancelled(true);
					return;
				}
			}
		}
		return;
	}
}
