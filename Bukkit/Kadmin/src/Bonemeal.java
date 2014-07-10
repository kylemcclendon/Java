package net.kylemc.kadmin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


public final class Bonemeal implements Listener{
	//Work Like Old Bonemeal
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent event){
		//Click Event
		Player p = event.getPlayer();

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			//Right Click Block
			ItemStack item = event.getItem();
			Block block = event.getClickedBlock();

			if (item == null || block == null){
				//If Item in hand is null, ignore
				return;
			}

			if (item.getType() == Material.INK_SACK && item.getDurability() == 15){
				//If bonemeal
				if(block.getType().equals(Material.CROPS)){
					//If crops
					block.setData((byte) 7);
					if((p.getGameMode()) != GameMode.CREATIVE){
						//If Survival, remove 1 bonemeal
						item.setAmount(item.getAmount() - 1 );
						if(item.getAmount() == 0){
							//If run out of bonemeal, remove from inventory
							event.getPlayer().setItemInHand(null);
						}
					}
					return;
				}
				else{
					//Other kind of plant
					if((p.getGameMode()) != GameMode.CREATIVE){
						//If Survival, remove 1 bonemeal
						item.setAmount(item.getAmount() - 1 );
						if(item.getAmount() == 0){
							//If run out of bonemeal, remove from inventory
							event.getPlayer().setItemInHand(null);
						}
					}
					Material mat = block.getType();
					switch(mat){
					//Check type of plant
					case CARROT:
						//Grow carrots
						block.setData((byte) 7);
						return;
					case MELON_STEM:
						//Mature melon stem
						block.setData((byte) 7);
						return;
					case POTATO:
						//Grow potatoes
						block.setData((byte) 7);
						return;
					case PUMPKIN_STEM:
						//Mature pumpkin stem
						block.setData((byte) 7);
						return;
					case SAPLING:{
						//Grow tree
						Location l = block.getLocation();
						boolean bigtree = false;
						TreeType type = null;

						switch (block.getData()){
						//Check type of tree
						case 0:
							//Oak
							type = TreeType.TREE;
							if(((int)(Math.random() * 20)) == 10){
								type = TreeType.BIG_TREE;
							}
							break;
						case 1:
							//Redwood
							type = TreeType.REDWOOD;
							if(((int)(Math.random() * 20)) == 10){
								type = TreeType.TALL_REDWOOD;
							}

							if ((!(block.getRelative(BlockFace.NORTH) == null) && (block.getRelative(BlockFace.NORTH).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getData() == 1)) &&
									(!(block.getRelative(BlockFace.EAST) == null) && (block.getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.EAST).getData() == 1)) &&
									(!(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST) == null) &&
											(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getData() == 1))) {
								type = TreeType.MEGA_REDWOOD;
								block = block.getRelative(BlockFace.NORTH);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.NORTH) == null) && (block.getRelative(BlockFace.NORTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.NORTH).getData() == 1)) &&
									(!(block.getRelative(BlockFace.WEST) == null) && (block.getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.WEST).getData() == 1)) &&
									(!(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST) == null) &&
											(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).getData() == 1))) {
								type = TreeType.MEGA_REDWOOD;
								block = block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.SOUTH) == null) && (block.getRelative(BlockFace.SOUTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.SOUTH).getData() == 1)) &&
									(!(block.getRelative(BlockFace.WEST) == null) && (block.getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.WEST).getData() == 1)) &&
									(!(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST) == null) &&
											(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST).getData() == 1))) {
								type = TreeType.MEGA_REDWOOD;
								block = block.getRelative(BlockFace.WEST);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.SOUTH) == null) && (block.getRelative(BlockFace.SOUTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.SOUTH).getData() == 1)) &&
									(!(block.getRelative(BlockFace.EAST) == null) && (block.getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.EAST).getData() == 1)) &&
									(!(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST) == null) &&
											(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST).getData() == 1))) {
								type = TreeType.MEGA_REDWOOD;
								bigtree = true;
							}
							break;
						case 2:
							//Birch
							type = TreeType.BIRCH;
							if(((int)(Math.random() * 20)) == 10){
								type = TreeType.TALL_BIRCH;
							}
							break;
						case 3:
							//Jungle
							type = TreeType.SMALL_JUNGLE;

							if ((!(block.getRelative(BlockFace.NORTH) == null) && (block.getRelative(BlockFace.NORTH).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getData() == 3)) &&
									(!(block.getRelative(BlockFace.EAST) == null) && (block.getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.EAST).getData() == 3)) &&
									(!(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST) == null) &&
											(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getData() == 3))) {
								type = TreeType.JUNGLE;
								block = block.getRelative(BlockFace.NORTH);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.NORTH) == null) && (block.getRelative(BlockFace.NORTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.NORTH).getData() == 3)) &&
									(!(block.getRelative(BlockFace.WEST) == null) && (block.getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.WEST).getData() == 3)) &&
									(!(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST) == null) &&
											(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).getData() == 3))) {
								type = TreeType.JUNGLE;
								block = block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.SOUTH) == null) && (block.getRelative(BlockFace.SOUTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.SOUTH).getData() == 3)) &&
									(!(block.getRelative(BlockFace.WEST) == null) && (block.getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.WEST).getData() == 3)) &&
									(!(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST) == null) &&
											(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST).getData() == 3))) {
								type = TreeType.JUNGLE;
								block = block.getRelative(BlockFace.WEST);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.SOUTH) == null) && (block.getRelative(BlockFace.SOUTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.SOUTH).getData() == 3)) &&
									(!(block.getRelative(BlockFace.EAST) == null) && (block.getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.EAST).getData() == 3)) &&
									(!(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST) == null) &&
											(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST).getData() == 3))) {
								type = TreeType.JUNGLE;
								bigtree = true;
							}
							break;
						case 4:
							//Acacia
							type = TreeType.ACACIA;
							break;
						case 5:
							//Dark Oak
							if ((!(block.getRelative(BlockFace.NORTH) == null) && (block.getRelative(BlockFace.NORTH).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getData() == 5)) &&
									(!(block.getRelative(BlockFace.EAST) == null) && (block.getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.EAST).getData() == 5)) &&
									(!(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST) == null) &&
											(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).getData() == 5))){
								type = TreeType.DARK_OAK;
								block = block.getRelative(BlockFace.NORTH);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.NORTH) == null) && (block.getRelative(BlockFace.NORTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.NORTH).getData() == 5)) &&
									(!(block.getRelative(BlockFace.WEST) == null) && (block.getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.WEST).getData() == 5)) &&
									(!(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST) == null) &&
											(block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST).getData() == 5))) {
								type = TreeType.DARK_OAK;
								block = block.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.SOUTH) == null) && (block.getRelative(BlockFace.SOUTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.SOUTH).getData() == 5)) &&
									(!(block.getRelative(BlockFace.WEST) == null) && (block.getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.WEST).getData() == 5)) &&
									(!(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST) == null) &&
											(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST).getData() == 5))) {
								type = TreeType.DARK_OAK;
								block = block.getRelative(BlockFace.WEST);
								bigtree = true;
							}
							else if ((!(block.getRelative(BlockFace.SOUTH) == null) && (block.getRelative(BlockFace.SOUTH).getType().equals(Material.SAPLING))  && (block.getRelative(BlockFace.SOUTH).getData() == 5)) &&
									(!(block.getRelative(BlockFace.EAST) == null) && (block.getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.EAST).getData() == 5)) &&
									(!(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST) == null) &&
											(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST).getType().equals(Material.SAPLING)) && (block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST).getData() == 5))) {
								type = TreeType.DARK_OAK;
								bigtree = true;
							}
							else if(type == null){
								return;
							}
							break;
						default:
							return;  //we shouldn't hit this, something broke!
						}
						if(!bigtree){
							block.setType(Material.AIR);
						}
						else{
							block.setType(Material.AIR);
							block.getRelative(BlockFace.EAST).setType(Material.AIR);
							block.getRelative(BlockFace.SOUTH).setType(Material.AIR);
							block.getRelative(BlockFace.SOUTH_EAST).setType(Material.AIR);
						}
						l = block.getLocation();
						boolean grew = l.getWorld().generateTree(l, type);
						if(grew == false){
							if(bigtree){
								block.setTypeIdAndData(6, block.getData(), false);
								block.getRelative(BlockFace.EAST).setTypeIdAndData(6, block.getData(), false);
								block.getRelative(BlockFace.SOUTH).setTypeIdAndData(6, block.getData(), false);
								block.getRelative(BlockFace.SOUTH_EAST).setTypeIdAndData(6, block.getData(), false);
							}
							else{
								block.setTypeIdAndData(6, block.getData(), false);
							}
							return;
						}
						if(p.getGameMode() != GameMode.CREATIVE){
							item.setAmount(item.getAmount() - 1 );
							if(item.getAmount() == 0){
								event.getPlayer().setItemInHand(null);
							}
						}
						return;
					}
					default:
						return;
					}//End switch(mat)
				}//End else for other plants (not crops)
			}//End if bonemeal
		}//End right-click-block
	}//End onInteract
}//End class