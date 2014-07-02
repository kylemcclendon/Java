package net.kylemc.kadmin;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Restart implements CommandExecutor 
{
	Logger log = Logger.getLogger("Minecraft");


	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("restart"))
		{
			if(sender instanceof Player && !sender.hasPermission("kadmin.restart"))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			else
			{
				log.info("Restarting...");
				for(Player p1 : sender.getServer().getOnlinePlayers())
				{
					p1.kickPlayer("Server Is Restarting");
				}
				sender.getServer().shutdown();
				return true;
			}
		}
		else if(cmd.getName().equalsIgnoreCase("ls"))
		{
			String online = "Online Players (";
			String players = "";
			int numPlayers = 0;

			for(int i = 0; i < Bukkit.getOnlinePlayers().length; i++)
			{
				numPlayers++;
				if(i == Bukkit.getOnlinePlayers().length - 1)
				{
					players += Bukkit.getOnlinePlayers()[i].getName();
				}
				else
				{
					players += Bukkit.getOnlinePlayers()[i].getName() + ",";
				}						
			}
			online += numPlayers + "/" + Bukkit.getMaxPlayers() + "): " + players;
			sender.sendMessage(online);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("quartz"))
		{
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				if(p.getItemInHand() == null)
				{
					p.sendMessage(ChatColor.RED + "You must be holding NetherRack to use this command");
				}
				else
				{
					boolean hasroom = false;
					if(p.getItemInHand().getType().equals(Material.NETHERRACK) && p.getInventory().getItemInHand().getAmount() > 9)
					{
						for(ItemStack item : p.getInventory().getContents())
						{
							if(item == null || (item.getType().equals(Material.QUARTZ) && item.getAmount() < 64))
							{
								hasroom = true;
								break;
							}
						}

						if(hasroom)
						{
							ItemStack i = new ItemStack(Material.QUARTZ, 1);
							ItemStack h = p.getItemInHand();
							p.getInventory().addItem(i);
							h.setAmount(h.getAmount() - 10);

							if(h.getAmount() == 0)
							{
								p.setItemInHand(null);
							}
							else
							{
								p.setItemInHand(h);
							}
							p.sendMessage(ChatColor.GOLD + "10 NetherRack removed, 1 Quartz added");
						}
						else
						{
							p.sendMessage(ChatColor.RED + "You don't have room for any quartz!");
						}
					}
					else
					{
						p.sendMessage(ChatColor.RED + "You need to hold at least 10 NetherRack to use this command");
					}
				}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Command can only be used by players");
			}
			return true;
		}
		return false;
	}
}