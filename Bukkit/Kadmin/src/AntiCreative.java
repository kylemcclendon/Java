package net.kylemc.kadmin;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public final class AntiCreative implements Listener{
	//Prevents Creative In Survival Worlds
	@EventHandler
	public void onGMChange(PlayerGameModeChangeEvent event){
		//Check GameMode change
		Player player = event.getPlayer();
		if(player.isOp()){
			//Ignore if player is Op
			return;
		}
		else{
			//If world is a "Survival" world, prevent them from being Creative'd
			String worldName = player.getWorld().getName();
			if(event.getNewGameMode() == GameMode.CREATIVE){
				//Prevent Creative
				if(worldName.equals("Tanith") || worldName.equals("New") || worldName.equals("Temp")){
					//Survival World, Stop
					player.sendMessage("This is a survival world. No creative allowed!");
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}