package net.kylemc.kadmin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class Exploration implements Runnable{
	//Checks if players are out of bounds of the map
	boolean OOB = false;

	@Override
	public void run(){
		//Thread
		while(true){
			//Run every 10 seconds
			try{
				Thread.sleep(10000);
			}
			catch(InterruptedException ex){
				ex.printStackTrace();
			}

			for (Player player : Bukkit.getServer().getOnlinePlayers()){
				//Check each player
				if(!player.isOp()){
					// if not in bounds:
					Location location = player.getLocation();
					double x = location.getX();
					double z = location.getZ();
					int newX = (int) x;
					int newZ = (int) z;

					if(location.getWorld().getName().equals("Hyrule")){
						//In Hyrule
						if(x > 510 || x < -880 || z > 990 || z < -400){
							//Out of bounds

							OOB = true;
							if(x > 510){
								newX = 510;
							}
							else if(x < -880){
								newX = -880;
							}

							if(z > 990){
								newZ = 990;
							}
							else if(z < -400){
								newZ = -400;
							}
						}
					}
					else if(x > 15000 || x < -15000 || z > 15000 || z < -15000){
						//Different world besides Hyrule and out of bounds

						OOB = true;
						if(x > 15000){
							newX = 15000;
						}
						else if(x < -15000){
							newX = -15000;
						}

						if(z < -15000){
							newZ = -15000;
						}
						else if(z > 15000){
							newZ = 15000;
						}
					}

					if(OOB){
						//If out of bounds
						Entity e = null;
						if(player.isInsideVehicle()){
							//Store vehicle (cart, horse, etc) in e
							e = player.getVehicle();
						}
						player.leaveVehicle();


						// determine where the player shall go:
						World world = player.getWorld();
						int newY = world.getHighestBlockYAt(newX, newZ);

						//woosh
						player.teleport(new Location(world, newX, newY, newZ, location.getYaw(), location.getPitch()));

						if(e != null){
							//Teleport entity to player
							e.teleport(player);
						}
						player.sendMessage(ChatColor.RED + "You've reached the edge of the explorable world!");
						OOB = false;
					}
				}
			}
		}
	}
}