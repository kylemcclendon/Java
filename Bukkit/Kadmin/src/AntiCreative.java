package net.kylemc.kadmin;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class AntiCreative implements Listener
{
	@EventHandler
	public void onGMChange(PlayerGameModeChangeEvent event)
	{
		Player player = event.getPlayer();
		if(player.isOp())
		{
			return;
		}
		else
		{
			String worldName = player.getWorld().getName();
			if(event.getNewGameMode() == GameMode.CREATIVE)
			{
				if(worldName.equals("Tanith") || worldName.equals("New") || worldName.equals("Temp"))
				{
					player.sendMessage("This is a survival world. No creative allowed!");
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}
