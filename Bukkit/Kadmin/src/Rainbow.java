package net.kylemc.kadmin;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Rainbow implements Listener 
{
	List<Byte> colors = Arrays.asList(new Byte[] {14, 6, 2, 10, 3, 9, 5, 4, 1});

	@SuppressWarnings("deprecation")
	@EventHandler
	public void brush(PlayerInteractEvent e)
	{
		Player player = e.getPlayer();
		ItemStack item = player.getItemInHand();
		
		if(item == null)
		{
			return;
		}
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && item.getType().equals(Material.DIAMOND_HOE) && player.isOp()) 
		{
			int radius = 0;
			boolean changeGrass = false;
			boolean randomColors = false;
			
			String itemName = null;
			
			if(item.getItemMeta() != null) //get name of hoe
			{
				itemName = item.getItemMeta().getDisplayName();
			}
			
			if(itemName == null) //no name, stop method.
			{
				return;
			}
			else
			{
				if(itemName.contains("grass"))
				{
					changeGrass = true;
				}
				if(itemName.contains("random"))
				{
					randomColors = true;
				}
				
				int spacepos = itemName.indexOf(" ");
				if(spacepos == -1)
				{
					if(itemName.matches("[0-9]+"))
					{
						radius = Integer.parseInt(itemName);
						if(radius > 30)
						{
							radius = 30;
						}
					}
					else
					{
						e.getPlayer().sendMessage(ChatColor.RED + "Name: <radius> [grass] [random]");
						return;
					}
				}			
				else if(itemName.substring(0, spacepos).matches("[0-9]+"))
				{
					radius = Integer.parseInt(itemName.substring(0, spacepos));
					if(radius > 30)
					{
						radius = 30;
					}
				}
				else
				{
					e.getPlayer().sendMessage(ChatColor.RED + "Name: <radius> [grass] [random]");
					return;
				}
			}
			
			Block block = e.getClickedBlock();
			Location loc = block.getLocation();

			double x = loc.getX();
			double y = loc.getY();
			double z = loc.getZ();
			World w = loc.getWorld();
			Location temp;
			
			for (int i = -2*radius; i < 2*radius + 1; i++)  //x direction
			{
				for (int k = -2*radius; k < 2*radius + 1; k++) //z direction
				{
					for (int j = -2*radius; j < 2*radius + 1; j++) //y direction
					{
						temp = new Location(w, x + i, y + j, z + k);
						block = temp.getBlock();
						Material m = block.getType();
						
						if (m.equals(Material.DIRT) || m.equals(Material.STONE) || m.equals(Material.WOOL) || (changeGrass == true && m.equals(Material.GRASS)))  //only overwrite naturals or other wools
						{
							boolean visible = false;
							if (block.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)
								|| block.getRelative(BlockFace.UP).getType().equals(Material.AIR)
								|| block.getRelative(BlockFace.EAST).getType().equals(Material.AIR)
								|| block.getRelative(BlockFace.WEST).getType().equals(Material.AIR)
								|| block.getRelative(BlockFace.NORTH).getType().equals(Material.AIR)
								|| block.getRelative(BlockFace.SOUTH).getType().equals(Material.AIR))
							{
								visible = true;
							}

							if (visible) //if it's visible, set to wool/color
							{
								block.setType(Material.WOOL);
								
								if(!randomColors) //rainbow pattern
								{
									block.setData(colors.get(Math.abs((block.getX()+block.getY()+block.getZ()))%9));
								}
								else //random colors
								{
									block.setData(colors.get((int)(Math.random()*(colors.size()-1))));
								}
							} //end if(visible)
						}//end if(dirt/stone/wool/grass)
					}//end for(y)
				}//end for(z)
			}//end for(x)
		}//end if(diamond-hoe & op)
	}//end void brush()
}
