package net.kylemc.kadmin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Exploration implements Runnable
{
	@Override
	public void run() 
	{
		while(true)
		{
			try
			{
				Thread.sleep(10000);
			}
			catch(InterruptedException ex)
			{
				ex.printStackTrace();
			}
			// check all players:
			for (Player player : Bukkit.getServer().getOnlinePlayers()) 
			{
				if(!player.isOp())
				{
					// if not in bounds:
					Location location = player.getLocation();
					double x = location.getX();
					double z = location.getZ();
					int newX;
					int newZ;

					if(x > 8000 || x < -13000 || z > 13200 || z < -7200)
					{
						if(x > 8000)
						{
							newX = 8000;
						}
						else if(x < -13000)
						{
							newX = -13000;
						}
						else
						{
							newX = (int)Math.floor(x);
						}

						if(z < -7200)
						{
							newZ = -7200;
						}
						else if(z > 13000)
						{
							newZ = 13000;
						}
						else
						{
							newZ = (int)Math.floor(z);
						}

						// to be sure..
						Entity e = null;
						if(player.isInsideVehicle())
						{
							e = player.getVehicle();
						}
						player.leaveVehicle();


						// determine where the player shall go:
						World world = player.getWorld();
						int newY = world.getHighestBlockYAt(newX, newZ);

						//woosh
						player.teleport(new Location(world, newX, newY, newZ, location.getYaw(), location.getPitch()));
						if(e != null)
						{
							e.teleport(player);
						}
						player.sendMessage(ChatColor.RED + "You've reached the edge of the explorable world!");
					}
				}
			}
		}
	}
}
