package net.kylemc.generalpermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

public class GeneralPermissionsCommands implements CommandExecutor{
	private final GeneralPermissions plugin;
	public static File playersFolder = new File(GeneralPermissions.dFolder, "Players");
	//public static HashMap<UUID, String> ranks = new HashMap<UUID, String>();
	private static HashMap<UUID, Boolean> spamDelay = new HashMap<UUID, Boolean>();

	//GeneralPermissionsCommands Constructor
	public GeneralPermissionsCommands(GeneralPermissions instance){
		plugin = instance;
	}

	//Handles commands
	@Override
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("permissions")){
			//help, ranks, reload, and reloadALL commands
			if(args.length != 1){
				//Invalid number of arguments
				sender.sendMessage(ChatColor.RED + "/permissions [help,ranks,reload]");
				return true;
			}
			else{
				if(args[0].equalsIgnoreCase("help")){
					if(sender.hasPermission("permissions.moderator")){
						sender.sendMessage(ChatColor.RED + "/permissions [ranks,reload,reloadAll]\npromote <member>\n/demote <member>\n/setrank <player> <rank>\n/ranks");
					}
					else{
						sender.sendMessage(ChatColor.RED + "/permissions reload\n/permissions ranks");
					}
					return true;
				}

				if(args[0].equalsIgnoreCase("ranks")){
					if(Utils.ranks.equals("")){
						sender.sendMessage(ChatColor.GOLD + "No ranks");
						return true;
					}

					sender.sendMessage(ChatColor.GOLD + Utils.ranks);
					return true;
				}

				if((args[0].equalsIgnoreCase("reloadAll") || args[0].equalsIgnoreCase("ra")) && sender.hasPermission("permissions.moderator")){
					for(Player p : sender.getServer().getOnlinePlayers()){
						reloadPlayer(p, p.getWorld().getName());
					}
					sender.sendMessage(ChatColor.GOLD + "All permissions successfully reloaded!");
					return true;
				}

				if((args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r"))){
					if(!(sender instanceof Player)){
						return true;
					}
					Player self = (Player) sender;
					final UUID pu = self.getUniqueId();
					if(!spamDelay.containsKey(pu)){
						spamDelay.put(pu, true);
					}
					else{
						if(spamDelay.get(pu) == true){
							self.sendMessage(ChatColor.RED + "You can only reload permissions every 5 minutes!");
							return true;
						}
						spamDelay.put(pu, true);
					}
					reloadPlayer(self, self.getWorld().getName());
					sender.sendMessage(ChatColor.GOLD + "Permissions successfully reloaded!");

					//5 minutes later, allow player to use command again
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable()
					{
						@Override
						public void run(){
							spamDelay.put(pu, false);
						}
					}, 300*20);

					return true;
				}

				//If we get here, no correct /permissions command
				sender.sendMessage(ChatColor.RED + "/permissions [help,ranks,reload]");
				return true;
			}
		}
		//		/promote <name>
		if(cmd.getName().equalsIgnoreCase("promote")){
			if(sender instanceof Player && !sender.hasPermission("permissions.moderator")){
				//No permission to promote players
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			else if(args.length == 1){
				UUID pu = Bukkit.getPlayer(args[0]).getUniqueId();
				File playerFile = new File(playersFolder, pu + ".txt");
				if(!playerFile.exists()){
					sender.sendMessage(ChatColor.RED + "Player does not exist");
					return true;
				}
				else{
					String orank = getRank(pu).toLowerCase();
					if(Utils.contains(orank, GeneralPermissions.modNames)){
						sender.sendMessage(ChatColor.RED + "Cannot promote mod team in game. Contact the server owner!");
						return true;
					}
					if(GeneralPermissions.groupNames[0].equals("")){
						sender.sendMessage(ChatColor.RED + "No groups have been specified)");
						return true;
					}
					if(orank.equals("")){
						setRank(pu, orank, GeneralPermissions.groupNames[0].toLowerCase());
						sender.sendMessage(args[0] + "set to lowest rank");
						Player p = Bukkit.getPlayer(pu);
						if(!p.isOnline()){
							return true;
						}
						else{
							String worldName = p.getWorld().getName();
							reloadPlayer(p, worldName);
							return true;
						}
					}

					int position = GeneralPermissions.groupNames.length - 1;
					for(int i = 0; i < GeneralPermissions.groupNames.length; i++){
						if(GeneralPermissions.groupNames[i].equals(orank)){
							position = i;
							break;
						}
					}
					if(position == GeneralPermissions.groupNames.length - 1){
						sender.sendMessage(ChatColor.RED + args[0] + " is fully promoted.");
						return true;
					}
					else{
						Player p = Bukkit.getPlayer(pu);
						setRank(pu, orank, GeneralPermissions.groupNames[position + 1].toLowerCase());
						sender.sendMessage(ChatColor.AQUA + args[0] + " promoted to: " + GeneralPermissions.groupNames[position + 1]);
						if(p == null){
							return true;
						}
						else{
							String worldName = p.getWorld().getName();
							reloadPlayer(p, worldName);
							return true;
						}
					}
				}
			}
			else{
				sender.sendMessage(ChatColor.RED + "Usage: /promote <player>");
				return true;
			}
		}
		//		/demote <player>
		if(cmd.getName().equalsIgnoreCase("demote")){
			if(sender instanceof Player && !sender.hasPermission("permissions.moderator")){
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			else if(args.length == 1){
				String playerName = args[0].toLowerCase();
				UUID pu = Bukkit.getPlayer(playerName).getUniqueId();
				File playerFile = new File(playersFolder, pu + ".txt");
				if(!playerFile.exists()){
					sender.sendMessage(ChatColor.RED + "Player does not exist");
					return true;
				}
				else{
					String orank = getRank(pu).toLowerCase();
					if(Utils.contains(orank, GeneralPermissions.modNames)){
						sender.sendMessage(ChatColor.RED + "Cannot demote mod team in game. Contact server owner!");
						return true;
					}
					if(GeneralPermissions.groupNames[0].equals("")){
						sender.sendMessage(ChatColor.RED + "No groups have been specified)");
						return true;
					}

					int position = 0;
					for(int i = 0; i < GeneralPermissions.groupNames.length; i++){
						if(GeneralPermissions.groupNames[i].equals(orank)){
							position = i;
							break;
						}
					}
					if(position == 0){
						sender.sendMessage(ChatColor.RED + args[0] + " is fully demoted.");
						return true;
					}
					else{
						setRank(pu, orank, GeneralPermissions.groupNames[position - 1].toLowerCase());
						sender.sendMessage(ChatColor.AQUA + args[0] + " demoted to: " + GeneralPermissions.groupNames[position - 1]);
						Player p = sender.getServer().getPlayer(pu);
						if(p == null){
							return true;
						}
						else{
							String worldName = p.getWorld().getName();
							reloadPlayer(p, worldName);
							return true;
						}
					}
				}
			}
			else{
				sender.sendMessage(ChatColor.RED + "Usage: /demote <player>");
			}
			return true;
		}

		//		/setrank <player> <rank>
		if(cmd.getName().equalsIgnoreCase("setrank")){
			if(sender instanceof Player && !sender.hasPermission("permissions.moderator")){
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			else if(args.length == 2){
				UUID pu = Bukkit.getPlayer(args[0]).getUniqueId();
				File playerFile = new File(playersFolder, pu + ".txt");
				//check for player
				if(!playerFile.exists()){
					sender.sendMessage(ChatColor.RED + args[0] + " does not exist");
					return true;
				}
				String orank = getRank(pu);
				String nrank = args[1].toLowerCase();
				if(Utils.contains(orank, GeneralPermissions.modNames) || Utils.contains(nrank, GeneralPermissions.modNames)){
					sender.sendMessage(ChatColor.RED + "Cannot change mod team ranks in game. Contact server owner!");
					return true;
				}
				for(int i = 0; i < GeneralPermissions.groupNames.length; i++){
					if(nrank.equals(GeneralPermissions.groupNames[i])){
						Player p = Bukkit.getPlayer(pu);
						setRank(pu, orank, nrank);
						sender.sendMessage(ChatColor.AQUA + args[0] + " set to " + nrank);
						if(p == null){
							return true;
						}
						reloadPlayer(p, p.getWorld().getName());
						return true;
					}
				}
				sender.sendMessage(ChatColor.RED + "Invalid new rank");
				return true;
			}
			else{
				sender.sendMessage(ChatColor.RED + "Usage: /setrank <player> <rank>");
				return true;
			}
		}

		if(cmd.getName().equalsIgnoreCase("getID")){
			if(sender.hasPermission("permissions.moderator")){
				if(args.length == 1){
					OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
					sender.sendMessage(ChatColor.AQUA + p.getName() + "'s UUID: " + p.getUniqueId());
					return true;
				}
				else{
					sender.sendMessage(ChatColor.RED + "Usage: /getID <playername>");
					return true;
				}
			}
			else{
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this");
				return true;
			}
		}

		return false;
	}

	//Reloads a player's permissions. Called after /promote, /demote, and /setrank
	private void reloadPlayer(Player player, String world){
		//Removes existing permissions and prefix
		UUID pu = player.getUniqueId();
		player.removeAttachment(GeneralPermissions.players.get(pu));
		GeneralPermissions.players.remove(pu);
		GeneralPermissions.prefixes.remove(pu);

		//Adds new permissions
		PermissionAttachment attachment2 = player.addAttachment(plugin);
		Set<String> newperms = GeneralGetPermissions.collectPermissions(pu, world);
		for(String perm : newperms){
			attachment2.setPermission(perm, true);
		}
		GeneralPermissions.players.put(pu, attachment2);
	}

	//Parses player file and acquires the assigned rank
	public static String getRank(UUID pu){
		File playerFile = new File(playersFolder, pu + ".txt");
		String rest = "";
		if (playerFile.exists()){
			Scanner scan;
			try{
				scan = new Scanner(playerFile);
				String rank = scan.nextLine();
				scan.close();
				int i = rank.indexOf(' ');
				if(i != -1){
					rest = rank.substring(i + 1);
				}
			}
			catch (FileNotFoundException e){
				e.printStackTrace();
			}
		}
		rest = rest.toLowerCase();

		return rest;
	}

	//Used to set a new rank. Called with /promote, /demote, and /setrank
	public static void setRank(UUID player, String oldRank, String newRank){
		try{
			File playerFile = new File(playersFolder, player + ".txt");
			BufferedReader reader = new BufferedReader(new FileReader(playerFile));
			String line = "", oldtext = "";
			while((line = reader.readLine()) != null){
				oldtext += line + Utils.OS;
			}
			reader.close();
			String newtext;
			//To replace a line in a file
			if(oldRank.equals("")){
				newtext = oldtext.replaceFirst("Rank:" + oldRank, "Rank: " + newRank);
			}
			else{
				newtext = oldtext.replaceFirst("Rank: " + oldRank, "Rank: " + newRank);
			}
			FileWriter writer = new FileWriter(playerFile);
			writer.write(newtext);
			writer.close();
		}
		catch (IOException ioe){
			ioe.printStackTrace();
		}
		return;
	}
}