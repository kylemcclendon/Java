package net.kylemc.categorizedwarps;

import java.io.File;
import java.util.HashMap;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CategorizedWarps extends JavaPlugin{
	public static File newWarps;
	public static HashMap<String, String[]> warps;
	public static HashMap<String, String> categories;
	
	public String[] Hyrule = new String[] {"forest", "Sacred Forest Meadow", "Hyrule", "Hyrule hub", "light", "Temple of Time", "shadow", "Graveyard", "water", "Lake Hylia"};
	public String[] Tanith = new String[] {"Air", "Exziron", "Andy", "ICU27", "Ark", "Community Effort", "AstaShip", "Lioness", "Baragh", "Sundav", "Bloodlust", "oilsands5", "Bubble", "DD622", "Cabins", "ERROR372", "cube", "Sheep Farm", "DDBook", "DD622", "Donut", "DD622", "DPRanch", "DonPretzel", "Dragon", "DD622", "Fuzz", "DD622", "HolidayTown", "stella96", "HolOfSol", "Solsetur", "IceHut", "oilsands5", "Idaho", "Villager Farm", "iTower", "unknown", "Itsumoniyoru", "unknown", "Jehlan", "Myuufasa", "Kanto", "fatrat92", "LinkOverLook", "DD622", "Lotus", "DD622", "Maze", "DD622", "Metro", "quicksilver20, DD622, SnowLeopard, Paradoxigent", "Misteria", "ERROR372", "moo", "Sheep Farm", "MoonCrest", "ace714", "NewRussia", "Electricut2, meeko305, aesusure", "Otok", "Solsetur", "PortSerenity/ps", "quicksilver20, DD622, SnowLeopard448, Paradoxigent", "Riverside", "SnowLeopard448", "sheep", "Sheep Farm", "Ship", "DD622", "SkyCity", "Sundav", "SnowyCreek", "Brad + morganmc", "Tanith", "Main Spawn/Info", "Tardis", "quicksilver20", "Tenuto", "ERROR372 + DD622", "Terra", "idmb", "Travencal", "idmb", "Tulloch", "ERROR372", "TWC", "Tanith Warp Center", "TwoRivers", "Solsetur", "UFO", "DD622", "Windfish", "DD622", "Winterfell", "Sundav", "Winterland", "DD622", "Zion", "Exziron"};
	public String[] BigTowns = new String[] {"Fuzz","DD622","HolOfSol","Solsetur","Metro","quicksilver20, DD622, SnowLeopard, Paradoxagent","Misteria","ERROR372","PortSerenity/ps","quicksilver20, DD622, SnowLeopard, Paradoxagent","SkyCity","Sundav","Terra","idmb","Tulloch", "ERROR372", "Zion", "Exziron"};
	public String[] SmallTowns = new String[] {"Baragh", "Sundav", "Bubble", "DD622", "Cabins", "ERROR372", "HolidayTown", "stella96", "IceHut", "oilsands5", "Jehlan", "Myuufasa", "Mooncrest", "ace714", "NewRussia", "Electricut, meeko305, aesusure", "Otok", "Solsetur", "Riverside", "SnowLeopard448", "Tenuto", "ERROR372 + DD622", "TwoRivers", "Solsetur"};
	public String[] BigBuilds = new String[] {"Air", "Exziron", "Andy", "ICU27", "Ark", "Community Effort", "AstaShip", "Lioness", "Bloodlust", "oilsands5", "DDBook", "DD622", "Donut", "DD622", "DPRanch", "DonPretzel", "Dragon", "DD622", "hobbiton", "Sundav", "iTower", "unknown", "Itsumoniyoru", "unknown", "Kanto", "fatrat92", "LinkOverLook", "DD622", "Lotus", "DD622", "Maze", "DD622", "mirkwood", "Sundav", "Ship", "DD622", "Tanith", "quicksilver20 + DD622", "Tardis", "quicksilver20", "Travencal", "quicksilver20", "UFO", "DD622", "Windfish", "DD622", "Winterfell", "Sundav", "Winterland", "DD622"};
	public String[] Utilities = new String[] {"cube", "Sheep Farm", "Idaho", "Villager Farm", "moo", "Sheep Farm", "Tanith", "Main Spawn/Info", "TWC", "Tanith Warp Center"};
	public String[] New = new String[] {"BrianDesert", "brianwario's desert", "Cottage", "Valley", "Ludenwic", "New Town", "New", "New's Spawn", "PortPhasmatys", "Port Town", "Russia", "Melpy + AvaMike town", "SkyLoft", "Sky Town", "Torres", "New's Waypoint Hub"};
	public String[] Old = new String[] {"Arena", "PVP Arena", "BMB", "Beyond the Mysterious Beyond", "Cilly", "Great City of Old", "CTF", "Capture the Flag", "Exziron", "Capture the Flag", "Old", "Old's Spawn", "Rapture", "Old's Waypoint Hub", "RavineTown", "DD622's Old Town", "RenekTown", "Melpy's Old Town", "Zombie", "Nazi Zombies"};

	@Override
	public void onEnable(){
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new WarpsCommands(), this);

		File dFolder = getDataFolder();

		if(!dFolder.exists()){
			dFolder.mkdirs();
		}
		
		newWarps = new File(dFolder, "newWarps.yml");
				
		warps = new HashMap<String, String[]>();
		warps.put("hyrule", Hyrule);
		warps.put("tanith", Tanith);
		warps.put("bigtowns", BigTowns);
		warps.put("smalltowns", SmallTowns);
		warps.put("bigbuilds", BigBuilds);
		warps.put("utilities", Utilities);
		warps.put("new", New);
		warps.put("old", Old);
		
		categories = new HashMap<String, String>();
		categories.put("hyrule", "Hyrule");
		categories.put("tanith", "Tanith");
		categories.put("bigtowns", "BigTowns");
		categories.put("smalltowns", "SmallTowns");
		categories.put("bigbuilds", "BigBuilds");
		categories.put("utilities", "Utilities");
		categories.put("new", "New");
		categories.put("old", "Old");
		
		getCommand("cwarps").setExecutor(new WarpsCommands());
		getLogger().info("CategorizedWarps enabled:");
	}
	
	@Override
	public void onDisable(){
		getLogger().info("CategorizedWarps disabled:");
	}
}
