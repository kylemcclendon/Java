package net.kylemc.kadmin;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class Vanish implements CommandExecutor, Listener
{
	private final Kadmin plugin;
	public static Set<String> players = new HashSet<String>();
	String[] creatures = {"Enderman", "Blaze", "MagmaCube", "CaveSpider", "Spider", "PigZombie","Ghast", "Silverfish"};
	Player player;

	public Vanish(Kadmin instance){
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("van")){
			if(!(sender instanceof Player)){
				sender.sendMessage("Command can only be done by players");
				return true;
			}
			else{
				player = (Player) sender;
				if(!player.hasPermission("kadmin.van")){
					player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
					return true;
				}
				players.add(player.getName().toLowerCase());
				for(Player p1 : sender.getServer().getOnlinePlayers()){
					if(!p1.isOp()){
						p1.hidePlayer(player);
					}
				}
				player.sendMessage(ChatColor.AQUA + "You have vanished.");
				return true;
			}
		}

		if(cmd.getName().equalsIgnoreCase("rea")){

			if(!(sender instanceof Player)){
				sender.sendMessage("Command can only be done by players");
				return true;
			}
			else{
				player = (Player) sender;
				if(!player.hasPermission("kadmin.van")){
					player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
					return true;
				}
				players.remove(player.getName().toLowerCase());
				for(Player p1 : sender.getServer().getOnlinePlayers()){
					p1.showPlayer(player);
				}
				player.sendMessage(ChatColor.AQUA + "You have reappeared.");
				return true;
			}
		}
		return false;
	}

	public final boolean isHidden(Player p){
		if(players.contains(p)){
			return true;
		}
		return false;
	}

	//EVENTS
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player p = event.getPlayer();
		if(Vanish.players.contains(p.getName().toLowerCase())){
			for(Player p1: p.getServer().getOnlinePlayers()){
				p1.showPlayer(p);
			}
			Vanish.players.remove(p);
			plugin.getLogger().info("Vanished Player quit, unvanishing them");
		}
		else{
			for(String p1: Vanish.players){
				p.showPlayer(Bukkit.getServer().getPlayer(p1));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player p = event.getPlayer();

		for(String p1 : Vanish.players){
			Player x = Bukkit.getServer().getPlayer(p1);
			if(!x.isOp()){
				p.hidePlayer(x);
			}
		}
	}

	@EventHandler
	public void itemPickup(PlayerPickupItemEvent event){
		Player p = event.getPlayer();
		if(Vanish.players.contains(p.getName().toLowerCase())){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void blockChanger(PlayerInteractEvent event){
		Player p = event.getPlayer();

		if(Vanish.players.contains(p.getName().toLowerCase()) && event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.CHEST)){
			/*Chest c = (Chest)event.getClickedBlock().getState();
			Inventory i = c.getBlockInventory();
			Inventory inv = Bukkit.getServer().createInventory(p, InventoryType.CHEST);
			inv = i;
			p.openInventory(inv);*/
			event.setCancelled(true);
			return;
		}
		else{
			if(event.hasItem() || event.hasBlock()){
				if(Vanish.players.contains(p.getName().toLowerCase())){
					event.setCancelled(true);
				}
				return;
			}
		}
	}

	@EventHandler
	public void onMobTarget(EntityTargetEvent event){
		if(event.getTarget() instanceof Player && (event.getEntity() instanceof Creature || contains(event.getEntityType().toString(), creatures))){
			Player p = (Player) event.getTarget();
			if(Vanish.players.contains(p.getName().toLowerCase())){
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void stopDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			Player p = (Player) event.getEntity();

			if(Vanish.players.contains(p.getName().toLowerCase())){
				event.setCancelled(true);
			}
		}
	}

	public final boolean contains(String type, String[] array){
		for(int i = 0; i < array.length; i++){
			if(type.equalsIgnoreCase(array[i])){
				return true;
			}
		}
		return false;
	}
}