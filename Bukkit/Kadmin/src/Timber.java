package net.kylemc.kadmin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public final class Timber implements Listener
{
	@EventHandler
	public void treeBreak(BlockBreakEvent event){
		List<Block> treeBlocks = new ArrayList<Block>();
		Player p = event.getPlayer();
		Block b = event.getBlock();
		ItemStack item = p.getItemInHand();
		int logs = 0; //kim's variable, how many logs were in our tree?

		if(item.getType().equals(Material.GOLD_AXE) && (b.getType().equals(Material.LOG) || b.getType().equals(Material.LOG_2))){
			if (treeWalk(treeBlocks, b, Integer.MAX_VALUE - 1))  //the int is how many blocks we'll break, at max
			{
				for(Block bl : treeBlocks){
					if (bl.getType().equals(Material.LOG) || bl.getType().equals(Material.LOG_2)){
						logs++;
					}
					bl.breakNaturally();
				}
			}

			if(!(p.getGameMode() == GameMode.CREATIVE)){
				item.setDurability((short)(item.getDurability() + ((logs/20) + 1)));  //decrement durability based on tree size
				if(item.getDurability() >= (short) 33){
					p.setItemInHand(null);
				}
			}
		}
		return;
	}

	public final boolean treeWalk(List<Block> blocks, Block base, int max){
		//max is how many blocks we're gonna break, not a radius
		int count = 0;
		int radius = 5;
		boolean imatree = false;
		Queue<Block> myQueue = new LinkedList<Block>();
		myQueue.add(base);

		int x = base.getX(), z = base.getZ();
		while (!(myQueue.isEmpty()) && count < max){
			//loop until we're out of blocks or beyond the max
			count++;
			base = myQueue.poll();   //get the next block from the queue
			blocks.add(base);       //put it in the list
			if (base.getType().equals(Material.LEAVES) || base.getType().equals(Material.LEAVES_2)){
				//if i'm a leaf, we're a tree
				imatree = true;
			}

			//add children to the queue if we haven't seen them yet
			if (Math.abs(base.getX() - x) > radius || Math.abs(base.getZ() - z) > radius){
				continue;
			}
			if (isTreePiece(base, BlockFace.UP) && !(blocks.contains(base.getRelative(BlockFace.UP))) && !(myQueue.contains(base.getRelative(BlockFace.UP)))){
				myQueue.add(base.getRelative(BlockFace.UP));
			}
			if (Math.abs(base.getX() - x) > radius || Math.abs(base.getZ() - z) > radius){
				continue;
			}
			if (isTreePiece(base, BlockFace.NORTH) && !(blocks.contains(base.getRelative(BlockFace.NORTH))) && !(myQueue.contains(base.getRelative(BlockFace.NORTH)))){
				myQueue.add(base.getRelative(BlockFace.NORTH));
			}
			if (Math.abs(base.getX() - x) > radius || Math.abs(base.getZ() - z) > radius){
				continue;
			}
			if (isTreePiece(base, BlockFace.SOUTH) && !(blocks.contains(base.getRelative(BlockFace.SOUTH))) && !(myQueue.contains(base.getRelative(BlockFace.SOUTH)))){
				myQueue.add(base.getRelative(BlockFace.SOUTH));
			}
			if (Math.abs(base.getX() - x) > radius || Math.abs(base.getZ() - z) > radius){
				continue;
			}
			if (isTreePiece(base, BlockFace.EAST) && !(blocks.contains(base.getRelative(BlockFace.EAST))) && !(myQueue.contains(base.getRelative(BlockFace.EAST)))){
				myQueue.add(base.getRelative(BlockFace.EAST));
			}
			if (Math.abs(base.getX() - x) > radius || Math.abs(base.getZ() - z) > radius){
				continue;
			}
			if (isTreePiece(base, BlockFace.WEST) && !(blocks.contains(base.getRelative(BlockFace.WEST))) && !(myQueue.contains(base.getRelative(BlockFace.WEST)))){
				myQueue.add(base.getRelative(BlockFace.WEST));
			}
			if (Math.abs(base.getX() - x) > radius || Math.abs(base.getZ() - z) > radius){
				continue;
			}
			if (isTreePiece(base, BlockFace.DOWN) && !(blocks.contains(base.getRelative(BlockFace.DOWN))) && !(myQueue.contains(base.getRelative(BlockFace.DOWN)))){
				myQueue.add(base.getRelative(BlockFace.DOWN));
			}
		}
		return imatree;
	}

	private final boolean isTreePiece(Block base, BlockFace f){
		if(!(base.getRelative(f) == null) && ((base.getRelative(f).getType().equals(Material.LOG)) || (base.getRelative(f).getType().equals(Material.LOG_2)) || (base.getRelative(f).getType().equals(Material.LEAVES)) || (base.getRelative(f).getType().equals(Material.LEAVES_2)))){
			return true;
		}
		return false;
	}
}