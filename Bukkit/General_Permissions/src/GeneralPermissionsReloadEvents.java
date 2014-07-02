package net.kylemc.generalpermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

public class GeneralPermissionsReloadEvents implements Listener{
	private final GeneralPermissions plugin;
	private final String[] creatures = {"MAGMA_CUBE","GHAST"};

	//GeneralPermissionsReloadEvents Constructor
	public GeneralPermissionsReloadEvents(GeneralPermissions instance){
		plugin = instance;
	}

	//Handles when a player joins
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		//Assign permissions to player when they join
		Player p = event.getPlayer();
		UUID pu = p.getUniqueId();
		String world = p.getWorld().getName();

		GeneralPermissions.uuids.put(p.getName(), pu);

		if(p.isOp()){
			p.setAllowFlight(true);
		}

		if(world.equalsIgnoreCase("Hyrule")){
			p.setWalkSpeed((float)0.3);
		}
		else{
			p.setWalkSpeed((float)0.2);
		}

		PermissionAttachment attachment = p.addAttachment(plugin);

		ArrayList<String> perms = GeneralGetPermissions.collectPermissions(pu, world);

		for(String permission : perms)
		{
			attachment.setPermission(permission, true);
		}

		GeneralPermissions.players.put(pu, attachment);

		YamlConfiguration YC = YamlConfiguration.loadConfiguration(GeneralPermissions.namesFile);
		String name = YC.getString(pu.toString());

		if(name == null || !(name.equals(p.getName()))){
			YC.set(pu.toString(), p.getName());
			try {
				YC.save(GeneralPermissions.namesFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try{
				File playerFile = new File(GeneralPermissionsCommands.playersFolder, pu + ".txt");
				BufferedReader reader = new BufferedReader(new FileReader(playerFile));
				String line = "", text = "";

				text += reader.readLine() + Utils.OS; //Rank
				text += "Name: " + p.getName() + Utils.OS;
				reader.readLine(); //Name, which is replaced
				text += reader.readLine() + Utils.OS; //blank line
				text += reader.readLine() + Utils.OS; //"Permissions:"
				text += reader.readLine() + Utils.OS; //dotted line

				while((line = reader.readLine()) != null){
					//Each permission
					text += line + Utils.OS;
				}
				reader.close();

				FileWriter writer = new FileWriter(playerFile);
				writer.write(text);
				writer.close();
			}
			catch (IOException ioe){
				ioe.printStackTrace();
			}
		}
	}

	//Handles when a player quits
	public void onPlayerQuit(PlayerQuitEvent event){
		removePlayer(event);
	}

	//Handles when a player is kicked
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event){
		removePlayer(event);
	}

	//Handles when a player changes worlds
	@EventHandler
	public void onSwitchWorlds(PlayerChangedWorldEvent event)
	{
		Player p = event.getPlayer();
		UUID pu = p.getUniqueId();

		//Removes permissions from player
		PermissionAttachment attachment1 = GeneralPermissions.players.get(pu);
		p.removeAttachment(attachment1);
		GeneralPermissions.players.remove(pu);
		GeneralPermissions.permissions.remove(pu);


		String world = p.getWorld().getName();
		if(world.equalsIgnoreCase("hyrule")){
			p.setWalkSpeed((float) 0.3);
		}
		else{
			p.setWalkSpeed((float) 0.2);
		}

		//Adds permissions to player
		PermissionAttachment attachment2 = p.addAttachment(plugin);
		GeneralPermissions.players.put(pu, attachment2);

		ArrayList<String> perms = GeneralGetPermissions.collectPermissions(pu, world);

		for(String permission : perms)
		{
			attachment2.setPermission(permission, true);
		}
	}

	//Handles prefix additions when chatting
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		if(event.isCancelled()){
			return;
		}

		UUID pu = event.getPlayer().getUniqueId();
		String msg = event.getMessage();
		String prefix = GeneralPermissions.prefixes.get(pu);
		String alteredPrefix = ChatColor.translateAlternateColorCodes('&',prefix);
		String eventFormat = "<" + alteredPrefix + event.getPlayer().getName() + "> " + msg;
		event.setFormat(eventFormat);
	}

	//Handles right and left clicks of blocks
	@SuppressWarnings("deprecation")
	@EventHandler
	public void blockChanger(PlayerInteractEvent event){
		//Event for block interaction, placement, and removal
		if(event.getClickedBlock() == null){
			//Air,Lava,Water block so it doesn't matter
			return;
		}

		Player p = event.getPlayer();

		//Permission to allow interaction with a block even if black-listed.
		String perm = "interact." + event.getClickedBlock().getTypeId();

		//Check player's interaction permission
		if(p.hasPermission("permissions.nointeract") && Utils.contains(Integer.toString(event.getClickedBlock().getTypeId()), GeneralPermissions.bbl)){
			//If block is blacklisted
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
				//If block is right-clicked
				if(p.hasPermission(perm) && !p.isSneaking()){
					//If player has block-specific interaction permission and as long as player isn't sneaking, allow action
					return;
				}
			}
			event.setCancelled(true);
			return;
		}

		//Check player's build permission
		if(!p.hasPermission("permissions.build")){
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
				if(event.getItem() == null){
					//If nothing is in the hand, they can't place anything, so we don't care about this case
					return;
				}
				else{
					if((p.hasPermission(perm) || Utils.contains(Integer.toString(event.getClickedBlock().getTypeId()), GeneralPermissions.bwl)) && !p.isSneaking()){
						//If player has interact specific permissions or block is whitelisted, and player isn't sneaking, allow right-click
						return;
					}
					event.setCancelled(true);
					return;
				}
			}
			else{
				//Left click, don't break
				if(event.getAction() == Action.LEFT_CLICK_BLOCK){
					event.setCancelled(true);
				}
			}
		}

		//If we get here, player is allowed to build and interact without restriction
		return;
	}

	//Stop players from picking up items
	@EventHandler
	public void itemPickup(PlayerPickupItemEvent event){
		if(event.getPlayer().hasPermission("permissions.nopickup")){
			event.setCancelled(true);
		}
	}

	//Stop players from dropping their items
	@EventHandler
	public void itemDrop(PlayerDropItemEvent event){
		if(event.getPlayer().hasPermission("permissions.nodrop")){
			event.setCancelled(true);
		}
	}

	//Stop players from getting hungry
	@EventHandler
	public void noHunger(FoodLevelChangeEvent event){
		if(event.getEntity() instanceof Player){
			if(((Player) event.getEntity()).hasPermission("permissions.nohunger")){
				event.setCancelled(true);
			}
		}
	}

	//Stop all damage to players
	@EventHandler
	public void noDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player)	{
			if(((Player) event.getEntity()).hasPermission("permissions.nodamage")){
				event.setCancelled(true);
			}
		}
	}

	//Stop mobs from targeting players
	@EventHandler
	public void noFollow(EntityTargetEvent event){
		if(event.getTarget() instanceof Player){
			Player p = (Player) event.getTarget();
			if((event.getEntity() instanceof Creature) || Utils.contains(event.getEntity().getType().toString().toUpperCase(), creatures)){
				if(p.hasPermission("permissions.nofollow")){
					event.setTarget(null);
				}
			}
		}
	}

	//Stop players from inserting/removing items into/from chests
	@EventHandler
	public void noChestRemoval(InventoryClickEvent event){
		if(event.getWhoClicked() instanceof Player){
			if(((Player) event.getWhoClicked()).hasPermission("permissions.noremove")){
				if(event.getInventory().getType() == InventoryType.CHEST){
					event.setCancelled(true);
				}
			}
		}
	}

	//Remove permissions and prefixes from player
	private void removePlayer(Event event){
		Player p = ((Player) event).getPlayer();
		UUID pu = GeneralPermissions.uuids.get(p.getName());
		GeneralPermissions.uuids.remove(p.getName());
		PermissionAttachment attachment = GeneralPermissions.players.get(pu);
		GeneralPermissions.players.remove(pu);
		p.removeAttachment(attachment);
		GeneralPermissions.prefixes.remove(pu);
		GeneralPermissions.permissions.remove(pu);
	}
}