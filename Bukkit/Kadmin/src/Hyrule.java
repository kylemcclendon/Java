package net.kylemc.kadmin;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftArrow;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public final class Hyrule implements Listener{

	Plugin plugin;
	float pitch = 0;
	float yaw = 0;

	public Hyrule(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void RSChest(final BlockRedstoneEvent e){
		final Location loc = e.getBlock().getLocation();
		if(!(loc.getWorld().getName().toString().equalsIgnoreCase("Hyrule"))){
			return;
		}
		Player closest = null;
		double distance = 999.99;

		//Find closest player
		for(Player player: loc.getWorld().getPlayers()){
			Location playerLoc = player.getLocation();
			float temp = (float) Math.sqrt(Math.pow(playerLoc.getX()-loc.getX(), 2) + Math.pow(playerLoc.getY()-loc.getY(), 2) + Math.pow(playerLoc.getZ()-loc.getZ(), 2));
			if(temp < distance){
				closest = player;
				distance = temp;
			}
		}

		if(closest == null){
			return;
		}

		loc.setY(loc.getY()-3);
		if((loc.getBlock().getType() == Material.CHEST || loc.getBlock().getType() == Material.TRAPPED_CHEST) && (e.getNewCurrent() > e.getOldCurrent())){
			Chest c = (Chest) loc.getBlock().getState();
			Inventory inv = c.getBlockInventory();
			if(inv.contains(Material.WRITTEN_BOOK)){
				ItemMeta im = inv.getItem(0).getItemMeta();
				BookMeta bm = (BookMeta) im;
				if(bm != null){
					Resolve(bm.getPage(1), closest, bm);
				}
			}
		}
	}

	public final static int contains(String[] array, String type){
		for(int i = 0; i < array.length; i++){
			if(array[i] == null){
				return -1;
			}
			else if(array[i].equals(type)){
				return i;
			}
		}
		return -1;
	}

	public final void Resolve(String condition, Player p, BookMeta b){

		String[] parts = condition.split("\n");

		if(parts[0].equalsIgnoreCase("Quest")){
			/*Quest           parts[0]
			  Item|Item|Item  parts[1]
			  Name|Name|Name  parts[2]
			  Item|Item|Item  parts[3]
			  DamV|DamV|DamV  parts[4]
			  Name|Name|Name  parts[5]
			 */

			if(parts[1].equals("Nothing")){
				//Starting Quest
				for(ItemStack check : p.getInventory().getContents()){
					//Check entire inventory for quest items
					if(check != null){
						//Safety check
						if(Kadmin.quest.contains(check.getItemMeta().getDisplayName())){
							p.sendMessage(ChatColor.GOLD + "You have already started this quest");
							//If quest item was found, stop immediately
							return;
						}
					}
				}

				//If player doesn't have a quest item, give them the starting item
				ItemStack i = new ItemStack(Material.FEATHER, 1);
				ItemMeta nothing = i.getItemMeta();
				nothing.setDisplayName("Cucco");
				i.setItemMeta(nothing);
				p.setItemInHand(i);
				p.sendMessage(ChatColor.AQUA + "Please take this Cucco and make him happy. Waking a heavy sleeper will make him very happy.\n *You borrowed a Pocket Cucco! Be sure to make it very happy!");
			}
			else{
				//Quest Extensions

				Material[] qTakes = new Material[3]; //Max 3 materials per quest extension
				String[] qTNames = new String[3]; //Material Names
				Material[] qGives = new Material[3]; //3 replacement materials
				int[] damV = new int[3]; //replacement damage values
				String[] qGNames = new String[3]; //replacement names

				//Item|Item|Item  parts[1]
				int i = 0;
				for(String x : parts[1].split(",")){
					qTakes[i] = Material.getMaterial(x.toUpperCase());
					i++;
				}

				//Name|Name|Name  parts[2]
				i = 0;
				for(String x : parts[2].split(",")){
					qTNames[i] = x;
					i++;
				}

				//Item|Item|Item  parts[3]
				i = 0;
				for(String x : parts[3].split(",")){
					qGives[i] = Material.getMaterial(x.toUpperCase());
					i++;
				}

				//DamV|DamV|DamV  parts[4]
				i = 0;
				for(String x : parts[4].split(",")){
					damV[i] = Integer.parseInt(x);
					i++;
				}

				//Name|Name|Name  parts[5]
				i = 0;
				for(String x : parts[5].split(",")){
					qGNames[i] = x;
					i++;
				}

				int index = 0;

				if(p.getItemInHand().getType() == Material.AIR || p.getItemInHand().getItemMeta().getDisplayName() == null || contains(qTNames, p.getItemInHand().getItemMeta().getDisplayName()) == -1){
					p.sendMessage(ChatColor.GOLD + "You need: ");
					for(int print = 0; print < qTNames.length; print++){
						if(qTNames[print] != null){
							p.sendMessage(ChatColor.GOLD + qTNames[print]);
						}
					}
					return;
				}
				else{
					/*Name|Name|Name  parts[2]
					  Item|Item|Item  parts[3]
					  DamV|DamV|DamV  parts[4]
					  Name|Name|Name  parts[5]
					 */
					index = contains(qTNames, p.getItemInHand().getItemMeta().getDisplayName());
					if(p.getItemInHand().getItemMeta().getDisplayName() != null && p.getItemInHand().getItemMeta().getDisplayName().equals(qTNames[index])){
						//continue
						ItemStack give = new ItemStack(qGives[index], 1, (short) damV[index]);
						ItemMeta im = give.getItemMeta();
						im.setDisplayName(qGNames[index]);
						give.setItemMeta(im);

						if(qGNames[index].equals("Biggoron's Sword")){
							give.addEnchantment(Enchantment.DAMAGE_ALL, 5);
							give.addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
						}

						p.sendMessage(ChatColor.AQUA + b.getPage(index+2));
						p.setItemInHand(give);
					}
				}
			}
		}
		else if(parts[0].equals("Has")){
			/*Has             parts[0]
			  Item|Item|Item  parts[1]
			  Name|Name|Name  parts[2]
			  Item|Item|Item  parts[3]
			  DamV|DamV|DamV  parts[4]
			  Name|Name|Name  parts[5]
			 */

			Material[] qTakes = new Material[3]; //Max 3 materials per quest extension
			String[] qTNames = new String[3]; //Material Names
			Material[] qGives = new Material[3]; //3 replacement materials
			int[] damV = new int[3]; //replacement damage values
			String[] qGNames = new String[3]; //replacement names

			//Item|Item|Item  parts[1]
			int i = 0;
			for(String x : parts[1].split(",")){
				qTakes[i] = Material.getMaterial(x.toUpperCase());
				i++;
			}

			//Name|Name|Name  parts[2]
			i = 0;
			for(String x : parts[2].split(",")){
				qTNames[i] = x;
				i++;
			}

			//Item|Item|Item  parts[3]
			i = 0;
			for(String x : parts[3].split(",")){
				qGives[i] = Material.getMaterial(x.toUpperCase());
				i++;
			}

			//DamV|DamV|DamV  parts[4]
			i = 0;
			for(String x : parts[4].split(",")){
				damV[i] = Integer.parseInt(x);
				i++;
			}

			//Name|Name|Name  parts[5]
			i = 0;
			for(String x : parts[5].split(",")){
				qGNames[i] = x;
				i++;
			}

			if(p.getItemInHand().getType() == Material.AIR || p.getItemInHand().getItemMeta().getDisplayName() == null || contains(qTNames, p.getItemInHand().getItemMeta().getDisplayName()) == -1){
				p.sendMessage(ChatColor.GOLD + "You need: ");
				for(int print = 0; print < qTNames.length; print++){
					if(qTNames[print] != null){
						p.sendMessage(ChatColor.GOLD + qTNames[print]);
					}
				}
				return;
			}
			else{
				/*Name|Name|Name  parts[2]
				  Item|Item|Item  parts[3]
				  DamV|DamV|DamV  parts[4]
				  Name|Name|Name  parts[5]
				 */
				int index = contains(qTNames, p.getItemInHand().getItemMeta().getDisplayName());
				if(p.getItemInHand().getItemMeta().getDisplayName() != null && p.getItemInHand().getItemMeta().getDisplayName().equals(qTNames[index])){
					//continue
					ItemStack give = new ItemStack(qGives[index], 1, (short) damV[index]);
					ItemMeta im = give.getItemMeta();
					im.setDisplayName(qGNames[index]);
					give.setItemMeta(im);

					p.setItemInHand(give);

					//Get page[index+2] and send to player
					p.sendMessage(ChatColor.AQUA + b.getPage(index+2));
				}
			}
		}
		else if(parts[0].equals("Give")){
			/*Give		      parts[0]
			  Item            parts[1]
			  DamVal          parts[2]
			  Enchantment,... parts[3]
			  EnchantVal,...  parts[4]
			  Name            parts[5]
			 */

			Material mat = Material.getMaterial(parts[1].toUpperCase());

			if(mat == null){
				return;
			}

			short DamVal = Short.parseShort(parts[2]);
			String[] enchants = parts[3].split(",");
			String[] eVals = parts[4].split(",");
			String name = parts[5];

			ItemStack i = new ItemStack(mat, 1, DamVal);
			ItemMeta im = i.getItemMeta();
			im.setDisplayName(name);
			i.setItemMeta(im);

			for(int k = 0; k < enchants.length; k++){
				if(enchants[k].equals("")){
					break;
				}
				Enchantment enc = Enchantment.getByName(enchants[k].toUpperCase());
				if(enc != null){
					i.addEnchantment(enc, Integer.parseInt(eVals[k]));
				}
			}

			p.setItemInHand(i);
			p.sendMessage(ChatColor.AQUA + b.getPage(2));
		}
		else{
			//For future commands
		}
	}

	@EventHandler
	public void ShootHookshot(PlayerInteractEvent e){
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(e.getItem() != null && e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getDisplayName() != null && (e.getItem().getItemMeta().getDisplayName().equals("Longshot") || e.getItem().getItemMeta().getDisplayName().equals("Hookshot")) ){
				e.setCancelled(true);
			}
		}
		else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK){
			if(e.getItem() != null && e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getDisplayName() != null && (e.getItem().getItemMeta().getDisplayName().equals("Longshot") || e.getItem().getItemMeta().getDisplayName().equals("Hookshot"))){
				Player player = e.getPlayer();
				player.setWalkSpeed((float) 0.3);
				Block b = getTarget(e.getPlayer(), 50);
				Location bloc = b.getLocation();
				Location ploc = player.getLocation();
				pitch = ploc.getPitch();
				yaw = ploc.getYaw();

				ploc.setY(ploc.getY()+1.620);
				bloc.setX(bloc.getX()+0.5);
				bloc.setY(bloc.getY()+0.5);
				bloc.setZ(bloc.getZ()+0.5);

				double dx = bloc.getX() - ploc.getX();
				double dy = bloc.getY() - ploc.getY();
				double dz = bloc.getZ() - ploc.getZ();

				double d = Math.sqrt((dx*dx) + (dy*dy) + (dz*dz));

				Vector v = new Vector((dx/d)*4.0, (dy/d)*4.0, (dz/d)*4.0);

				Arrow a = e.getPlayer().getWorld().spawnArrow(ploc, v, (float)4.0, (float)0.0);
				a.setShooter(e.getPlayer());
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void Hooked(final ProjectileHitEvent e){
		if(e.getEntity().getShooter() instanceof Player && e.getEntity().getWorld().getName().equalsIgnoreCase("Hyrule")){
			Player p = (Player) e.getEntity().getShooter();
			ItemStack i = p.getItemInHand();
			if(i == null ||
					i.getItemMeta() == null ||
					i.getItemMeta().getDisplayName() == null){
				return;
			}

			if(i.getItemMeta().getDisplayName().equalsIgnoreCase("hookshot") || i.getItemMeta().getDisplayName().equalsIgnoreCase("longshot")){
				if(e.getEntity() instanceof Arrow){
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							try {

								net.minecraft.server.v1_7_R3.EntityArrow entityArrow = ((CraftArrow) e.getEntity()).getHandle();
								Field fieldX = net.minecraft.server.v1_7_R3.EntityArrow.class.getDeclaredField("d");
								Field fieldY = net.minecraft.server.v1_7_R3.EntityArrow.class.getDeclaredField("e");
								Field fieldZ = net.minecraft.server.v1_7_R3.EntityArrow.class.getDeclaredField("f");

								fieldX.setAccessible(true);
								fieldY.setAccessible(true);
								fieldZ.setAccessible(true);

								int x = fieldX.getInt(entityArrow);
								int y = fieldY.getInt(entityArrow);
								int z = fieldZ.getInt(entityArrow);

								if (isValidBlock(y)) {
									Block block = e.getEntity().getWorld().getBlockAt(x, y, z);
									Bukkit.getServer().getPluginManager().callEvent(new ArrowHitBlockEvent((Arrow) e.getEntity(), block));
								}

							} catch (NoSuchFieldException e1) {
								e1.printStackTrace();
							} catch (SecurityException e1) {
								e1.printStackTrace();
							} catch (IllegalArgumentException e1) {
								e1.printStackTrace();
							} catch (IllegalAccessException e1) {
								e1.printStackTrace();
							}
						}
					});

					e.getEntity().remove();
				}
			}
		}
	}

	private final boolean isValidBlock(int y) {
		return y != -1;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void Stick(ArrowHitBlockEvent e){
		Material mat = e.getBlock().getType();
		if((mat == Material.SMOOTH_BRICK && e.getBlock().getData() == (byte)3) || mat == Material.HAY_BLOCK || mat == Material.CHEST || mat == Material.TRAPPED_CHEST || (mat == Material.LOG_2 && e.getBlock().getData() == (byte)1) || (mat == Material.STAINED_CLAY && e.getBlock().getData() == (byte)14) || (mat == Material.STAINED_CLAY && e.getBlock().getData() == (byte)11) || mat == Material.HOPPER){
			Player p = (Player)e.getArrow().getShooter();
			Location aloc = e.getArrow().getLocation();
			aloc.setYaw(yaw);
			aloc.setPitch(pitch);
			p.teleport(aloc);
			e.getArrow().remove();
		}
	}

	public static final Block getTarget(Player player, Integer range) {
		BlockIterator iter = new BlockIterator(player, range);
		Block lastBlock = iter.next();
		while (iter.hasNext()) {
			lastBlock = iter.next();
			if (lastBlock.getType() == Material.AIR) {
				continue;
			}
			break;
		}
		return lastBlock;
	}
}

class ArrowHitBlockEvent extends BlockEvent {

	private static final HandlerList handlers = new HandlerList();

	private final Arrow arrow;

	public ArrowHitBlockEvent(Arrow arrow, Block block) {
		super(block);
		this.arrow = arrow;
	}

	public Arrow getArrow() {
		return arrow;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}