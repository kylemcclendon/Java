package net.kylemc.kadmin;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public final class Kadmin extends JavaPlugin
{
	public static Kadmin plugin;
	private File settingsFile;
	public YamlConfiguration settings;
	
	private final AntiCreative ac = new AntiCreative();
	private final Bonemeal bm = new Bonemeal();
	private final FeatherClip fc = new FeatherClip();
	private final HorseTeleport ht = new HorseTeleport();
	private final Rainbow rb = new Rainbow();
	private final Restart restart = new Restart();
	private final Timber tim = new Timber();
	private final Vanish van = new Vanish(this);
	Thread t;
	
	@Override
	public void onEnable()
	{
		if(getServer().hasWhitelist())
		{
			new Maintenance(this);
		}

		PluginManager pm = getServer().getPluginManager();
		File dFolder = getDataFolder();

		if(!dFolder.exists())
		{
			dFolder.mkdirs();
		}

		settingsFile = new File(dFolder, "config.yml");
		if (!(settingsFile.exists())) 
		{
			settings = new YamlConfiguration();
			settings.set("AntiCreative", true);
			settings.set("Bonemeal", true);
			settings.set("Exploration", true);
			settings.set("Feather", true);
			settings.set("Flower", true);
			settings.set("HorseCraft", true);
			settings.set("HorseTeleport", true);
			settings.set("Leather", true);
			settings.set("Rainbow", true);
			settings.set("Restart", true);
			settings.set("Timber", true);
			settings.set("Vanish", true);
			saveSettings();
		}

		settings = YamlConfiguration.loadConfiguration(settingsFile);
		
		if(settings.getBoolean("AntiCreative"))
		{
			pm.registerEvents(ac, this);
		}
		if(settings.getBoolean("Bonemeal"))
		{
			pm.registerEvents(bm, this);
		}
		if(settings.getBoolean("Exploration"))
		{
			t = new Thread(new Exploration());
			t.start();
		}
		if(settings.getBoolean("Feather"))
		{
			pm.registerEvents(fc, this);
		}
		if(settings.getBoolean("Flower"))
		{
			getCommand("flower").setExecutor(new Flower());
		}
		if(settings.getBoolean("HorseCraft"))
		{
			final ShapedRecipe saddle = new ShapedRecipe(new ItemStack(Material.SADDLE, 1));
			saddle.shape("A A", "AAA", " B ");
			saddle.setIngredient('A', Material.LEATHER);
			saddle.setIngredient('B', Material.IRON_INGOT);
			getServer().addRecipe(saddle);
			saveSettings();

			final ShapelessRecipe nametag = new ShapelessRecipe(new ItemStack(Material.NAME_TAG, 1));
			nametag.addIngredient(Material.STRING);
			nametag.addIngredient(Material.WOOD);
			nametag.addIngredient(Material.INK_SACK);
			getServer().addRecipe(nametag);
		}
		if(settings.getBoolean("HorseTeleport"))
		{
			pm.registerEvents(ht, this);
		}
		if(settings.getBoolean("Leather"))
		{
			ItemStack flesh = new ItemStack(Material.LEATHER, 1);
			FurnaceRecipe leather = new FurnaceRecipe(flesh, Material.ROTTEN_FLESH);
			this.getServer().addRecipe(leather);
		}
		if(settings.getBoolean("Rainbow"))
		{
			pm.registerEvents(rb, this);
		}
		if(settings.getBoolean("Restart"))
		{
			getCommand("restart").setExecutor(restart);
			getCommand("ls").setExecutor(restart);
			getCommand("quartz").setExecutor(restart);
		}
		if(settings.getBoolean("Timber"))
		{
			pm.registerEvents(tim, this);
		}
		if(settings.getBoolean("Vanish"))
		{
			pm.registerEvents(van, this);
			getCommand("van").setExecutor(van);
			getCommand("rea").setExecutor(van);
		}	

		//Log info
		getLogger().info("Kadmin enabled:");
	}

	@Override
	public void onDisable()
	{
		t = null;
		for(String p : Vanish.players)
		{
			Player pl = Bukkit.getServer().getPlayer(p);

			for(Player player : Bukkit.getServer().getOnlinePlayers())
			{
				player.showPlayer(pl);
			}
		}
		getLogger().info("Kadmin disabled");
	}

	public boolean saveSettings() 
	{
		if (!settingsFile.exists()) 
		{
			settingsFile.getParentFile().mkdirs();
		}
		try
		{
			settings.save(settingsFile);
			return true;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return false;
	}
}