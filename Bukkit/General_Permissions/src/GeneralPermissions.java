package net.kylemc.generalpermissions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.wepif.PermissionsProvider;

public final class GeneralPermissions extends JavaPlugin implements PermissionsProvider{
	private File settingsFile;
	public static File namesFile;
	public YamlConfiguration settings;
	public static YamlConfiguration namesSettings;
	public static HashMap<UUID, PermissionAttachment> players = new HashMap<UUID, PermissionAttachment>();
	public static HashMap<UUID, String> prefixes = new HashMap<UUID, String>();
	public static HashMap<UUID, HashSet<String>> permissions = new HashMap<UUID, HashSet<String>>();
	public static HashMap<String, UUID> uuids = new HashMap<String, UUID>();
	private final GeneralPermissionsReloadEvents gpre = new GeneralPermissionsReloadEvents(this);
	public static String[] groupNames;
	public static String[] modNames;
	public static String[] bbl;
	public static String[] bwl;
	public static File dFolder;
	private boolean enabled = false;

	//Called when GeneralPermissions loads
	@Override
	public void onEnable(){
		for(Player p: Bukkit.getServer().getOnlinePlayers()){
			uuids.put(p.getName(), p.getUniqueId());
		}

		dFolder = getDataFolder();
		final Plugin x = this;

		//Creates Permissions Folder
		if(!dFolder.exists()){
			dFolder.mkdirs();
		}

		settingsFile = new File(dFolder, "config.yml");
		namesFile = new File(dFolder, "names.yml");

		if(!namesFile.exists()){
			namesSettings = new YamlConfiguration();
			namesSettings.set("#", "Example");
			try {
				namesSettings.save(namesFile);
			} catch (IOException e){
				e.printStackTrace();
			}
		}

		//Set default groups in config file if config.yml was missing
		if (!(settingsFile.exists())) {
			settings = new YamlConfiguration();
			settings.set("groups", "#EXAMPLE: guest,member,veteran");
			settings.set("mods", "#EXAMPLE: op,admin");
			settings.set("block-blacklist","#EXAMPLE: 1,2,3,4");
			settings.set("block-whitelist","#EXAMPLE: 1,2,3,4");
			saveSettings();
		}

		settings = YamlConfiguration.loadConfiguration(settingsFile);
		saveSettings();

		//checks to make sure the groups list doesn't contain any illegal characters
		checkSettings();

		//checks to see if groups are enabled. If not, warn the admin.
		if(groupNames[0].equals("") && modNames[0].equals("")){
			//No groups specified
			getLogger().warning("Permissions not enabled, no groups/mods specified!");
		}
		else{
			//Groups have been specified
			Utils.initRanks(); //Initialize ranks string in Utils (prevents computation of in-game ranks when /permissions ranks is done)
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvents(gpre, this);

			File worldsFolder = new File(dFolder, "Worlds");
			File groupsFolder = new File(dFolder,  "Groups");
			File playersFolder = new File(dFolder, "Players");

			//Creates Worlds folder within Permissions folder
			if(!worldsFolder.exists()){
				worldsFolder.mkdir();
			}

			//Creates Groups folder within Permissions folder
			if(!groupsFolder.exists()){
				groupsFolder.mkdir();
			}

			//Creates Players folder within Permissions folder
			if(!playersFolder.exists()){
				playersFolder.mkdir();
			}

			//Loads server worlds into serverWorlds list
			List<String> worldNames = new ArrayList<String>();

			//Load world names into worldNames list
			for(World w : getServer().getWorlds()){
				worldNames.add(w.getName());
			}

			if(createWorldFolders(worldNames) && createGroupFiles()){
				//If World folders were created and Group files created, enable

				enabled = true;
				getServer().getLogger().info("Permissions Enabled!");
				GeneralPermissionsCommands pc = new GeneralPermissionsCommands(this);
				getCommand("permissions").setExecutor(pc);
				getCommand("promote").setExecutor(pc);
				getCommand("demote").setExecutor(pc);
				getCommand("setrank").setExecutor(pc);
				getCommand("getid").setExecutor(pc);

				//Handles a /reload command. Delays re-assigning permissions until reload is complete.
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run(){
						for(Player p : getServer().getOnlinePlayers()){
							UUID pu = p.getUniqueId();
							String world = p.getWorld().getName();
							PermissionAttachment attachment = p.addAttachment(x);
							players.put(pu, attachment);

							Set<String> perms = GeneralGetPermissions.collectPermissions(pu, world);

							for(String permission : perms){
								attachment.setPermission(permission, true);
							}
						}

						System.out.println("All permissions reloaded");
					}
				}, 2*20);
			}
			else{
				getServer().getLogger().warning("Permissions Not Enabled!");
			}
		}
	}

	//Called when GeneralPermissions is disabled
	@Override
	public void onDisable(){
		//Removes all permissions from online players and removes prefixes from HashMap prefixes

		uuids.clear();
		if(enabled){
			enabled = false;
			for(Player p : getServer().getOnlinePlayers()){
				UUID pu = p.getUniqueId();
				PermissionAttachment attachment = players.get(pu);
				players.remove(pu);
				p.removeAttachment(attachment);
				prefixes.remove(pu);
				permissions.remove(pu);
			}
		}
		getServer().getLogger().info("Permissions Disabled!");
	}

	//Saves settings file
	private boolean saveSettings(){
		if (!settingsFile.exists()){
			settingsFile.getParentFile().mkdirs();
		}
		try{
			settings.save(settingsFile);
			return true;
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return false;
	}

	//Fills in global arrays
	private void checkSettings(){
		if( settings.getString("groups") == null || settings.getString("groups").contains("#")){
			groupNames = new String[] {""};
		}
		else{
			groupNames = settings.getString("groups").split("\\s*,\\s*");
		}

		if( settings.getString("mods") == null || settings.getString("mods").contains("#")){
			modNames = new String[] {""};
		}
		else{
			modNames = settings.getString("mods").split("\\s*,\\s*");
		}

		if(settings.getString("block-blacklist") == null || settings.getString("block-blacklist").contains("#")){
			bbl = new String[] {""};
		}
		else{
			bbl = settings.getString("block-blacklist").split("\\s*,\\s*");
			Arrays.sort(bbl);
		}

		if(settings.getString("block-whitelist") == null || settings.getString("block-whitelist").contains("#")){
			bwl = new String[] {""};
		}
		else{
			bwl = settings.getString("block-whitelist").split("\\s*,\\s*");
			Arrays.sort(bwl);
		}
	}

	//Creates each world folder the server has
	private boolean createWorldFolders(List<String> worldnames){
		File worldsFolder = new File(dFolder, "Worlds");
		File newWorld;
		for(String world : worldnames){
			newWorld = new File(worldsFolder, world);
			if(!newWorld.exists()){
				newWorld.mkdir();
				if(newWorld.exists()){
					//Writes the default groups (if any specified) inside each folder
					writeGroups(newWorld);
				}
			}
			else{
				//Add groups to world folder
				writeGroups(newWorld);
			}
		}
		return true;
	}

	//Creates each group file from the groupNames and modNames lists, if any specified
	private boolean createGroupFiles(){
		File groupsFolder = new File(getDataFolder(),  "Groups");
		File newGroup;
		for(int i = 0; i < groupNames.length; i++){
			newGroup = new File (groupsFolder, groupNames[i] + ".txt");
			if(groupNames[i].equals("")){
				break;
			}

			if(!newGroup.exists()){
				try{
					newGroup.createNewFile();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				writeDefaultGroup(newGroup);
			}
		}
		for(int i = 0; i < modNames.length; i++){
			newGroup = new File (groupsFolder, modNames[i] + ".txt");
			if(modNames[i].equals("")){
				break;
			}
			if(!newGroup.exists()){
				try{
					newGroup.createNewFile();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				writeDefaultGroup(newGroup);
			}
		}
		return true;
	}

	//Creates default group files for each group in Groups directory.
	private void writeDefaultGroup(File file){
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			//BufferedWriter output = new BufferedWriter(new FileWriter(file));
			writer.write("Prefix: #[&4G&f]");
			writer.newLine();
			writer.newLine();
			writer.write("Permissions:");
			writer.newLine();
			writer.newLine();
			writer.write("+ permissions.build");
			writer.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	//Creates default group files for each world directory
	private void writeGroups(File file){
		File group;
		for(String newGroup : groupNames){
			if(newGroup.equals("")){
				break;
			}
			try{
				group = new File(file, newGroup + ".txt");
				if(!group.exists()){
					BufferedWriter output = new BufferedWriter(new FileWriter(group));
					output.write("#Add all permissions after the 'Permissions:' line. Do not Modify any other lines");
					output.newLine();
					output.newLine();
					output.write("Permissions:");
					output.newLine();
					output.newLine();
					output.write("+ permissions.build");
					output.close();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		for(String modGroup : modNames){
			if(modGroup.equals("")){
				break;
			}
			try{
				group = new File(file, modGroup + ".txt");
				if(!group.exists()){
					BufferedWriter output = new BufferedWriter(new FileWriter(group));
					output.write("#Add all permissions after the 'Permissions:' line. Do not Modify any other lines");
					output.newLine();
					output.newLine();
					output.write("Permissions:");
					output.newLine();
					output.newLine();
					output.write("+ permissions.build");
					output.close();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	//All sk89q hasPermission methods default here
	@Override
	public boolean hasPermission(String player, String permission){
		System.out.println(permission);
		UUID pu = uuids.get(player);
		boolean has = false;
		String[] full = permission.split("\\.");
		String combine = full[0];
		String star = "*";

		if(permissions.get(pu) == null){
			return false;
		}

		Set<String> playerPerms = permissions.get(pu);

		/*System.out.println(permission);
		for(String per : playerPerms){
			System.out.println(per);
		}*/

		if(combine.equals("+ *") || playerPerms.contains("+"+combine+".*")){
			has = true;
		}
		if(!playerPerms.contains("-" + star) && !playerPerms.contains("-" + combine)){
			for(int i = 1; i < (full.length); i++){
				combine = combine + "." + full[i];
				if(full[i].equals("*") || i == full.length-1){
					if(playerPerms.contains("+"+combine)){
						has = true;
					}
					if(playerPerms.contains("-"+combine)){
						has = false;
						break;
					}
				}
				else{
					if(playerPerms.contains("+"+combine+".*")){
						has = true;
					}
					if(playerPerms.contains("-"+combine+".*")){
						has = false;
						break;
					}
				}
			}
		}
		else{
			has = false;
		}
		return has;
	}

	//sk89q method to check permission in a world
	@Override
	public boolean hasPermission(String worldName, String player, String permission){
		return hasPermission(player, permission);
	}

	//sk89q method to check if a player is in a group
	@Override
	@SuppressWarnings("deprecation")
	public boolean inGroup(String player, String group){
		UUID pu = Bukkit.getOfflinePlayer(player).getUniqueId();
		String rank = GeneralPermissionsCommands.getRank(pu).toLowerCase();
		if(rank.equals(group.toLowerCase())){
			return true;
		}
		return false;
	}

	//sk89q method to get the groups a player is part of
	@Override
	public String[] getGroups(String player){
		//String rank = GeneralPermissionsCommands.getRank(pu).toLowerCase();
		return new String[] {"None"};
	}

	//sk89q method to check if a player has permission
	@Override
	public boolean hasPermission(OfflinePlayer player, String permission){
		//Offline player, we don't care
		return hasPermission(player.getPlayer().getName(), permission);
	}

	//sk89q method to check if a player has permission
	@Override
	public boolean hasPermission(String worldName, OfflinePlayer player, String permission){
		//Offline player, we don't care
		return hasPermission(player.getPlayer().getName(), permission);
	}

	//sk89q method to check if a player is in a group
	@Override
	public boolean inGroup(OfflinePlayer player, String group){
		//Offline player, we don't care
		return false;
	}

	//sk89q method to get the groups a player is a part of
	@Override
	public String[] getGroups(OfflinePlayer player){
		//Offline player, we don't care
		return null;
	}

}