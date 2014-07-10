package net.kylemc.kadmin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Flower implements CommandExecutor
{
	@Override
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{

		Player player;

		if(sender instanceof Player){
			//If command user is a player in game
			player = (Player) sender;
			if(cmd.getName().equalsIgnoreCase("flower")){
				//If player does /flower <radius>
				if(!player.hasPermission("kadmin.flower")){
					//If has permission
					player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
					return true;
				}
				else if(args.length == 0 || args.length > 1){
					//if incorrect number of arguments
					player.sendMessage(ChatColor.RED + "Usage: /flower <radius>");
					return true;
				}
				else{
					//Actual flowering
					Location playerLoc = player.getLocation();
					double pX = playerLoc.getX();
					double pY = playerLoc.getY();
					double pZ = playerLoc.getZ();
					int radius = 0;

					try{
						//Change radius to int
						radius = Integer.parseInt(args[0]);
					}
					catch(NumberFormatException e){
						//Bad number for radius
						player.sendMessage(ChatColor.RED + "Usage: /flower <radius>");
						return true;
					}

					for (int x = -(radius); x <= radius; x ++){
						for (int y = -(radius); y <= radius; y ++){
							for (int z = -(radius); z <= radius; z ++){
								Block b = player.getWorld().getBlockAt((int)pX+x, (int)pY+y, (int)pZ+z);
								if(b.getType() == Material.GRASS && b.getRelative(BlockFace.UP).getType() == Material.AIR && b.getRelative(BlockFace.UP).getLightLevel() > 7){
									//if block is grass and above it there's no block and light level is more than 7
									Block c = b.getRelative(BlockFace.UP);
									int r = (int)(75*Math.random());

									if(r == 1){
										//Sunflower, Lilac, Tallgrass, fern, rose bush, peony
										c.setType(Material.DOUBLE_PLANT);
										byte r2 = (byte)(5*Math.random());
										c.setData(r2);
									}
									else if(r == 7 || r == 15){
										//Poppy, Orchid, Alllium, Bluet, Tulip, Daisy
										byte r2 = (byte)(8*Math.random());

										c.setType(Material.RED_ROSE);
										c.setData(r2);
									}
									else if(r == 13){
										int yellow = (int)(1*Math.random());
										if(yellow == 0){
											c.setType(Material.YELLOW_FLOWER);
										}
										else{
											c.setType(Material.AIR);
										}
									}
									else if (r % 2 == 0){
										c.setType(Material.AIR);
									}
									else{
										//grass
										c.setType(Material.LONG_GRASS);
										c.setData((byte) 1);
									}
								}
							}
						}
					}
					sender.sendMessage(ChatColor.AQUA + "Flowered!");
				}
			}
		}
		else{
			sender.sendMessage(ChatColor.RED + "Command can only be used by a player!");
		}
		return true;
	}
}
