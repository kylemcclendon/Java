package net.kylemc.categorizedwarps;

import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class WarpsCommands implements CommandExecutor, Listener{
	String cats = ChatColor.GOLD + "Categories: hyrule, tanith, bigtowns, smalltowns, bigbuilds, utilities, new, old";
	String use = ChatColor.RED + "Usage: /cwarps [category] [page number]";
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("cwarps")){
			if(args.length == 0){
				sender.sendMessage(cats);//Add categories
				sender.sendMessage(use);
				return true;
			}
			else{
				if(args.length == 1){
					String cat = args[0].toLowerCase();
					
					if(CategorizedWarps.warps.containsKey(cat)){
						String[] h = CategorizedWarps.warps.get(cat);
						String category = CategorizedWarps.categories.get(cat);
						sender.sendMessage(ChatColor.GOLD + "---------------" + category + " Pages: " + (int)Math.ceil((double)h.length/10.0) + "---------------");
						sender.sendMessage(ChatColor.GOLD + "----------Format: Warp - Builder(s)/Description----------");
						for(int i = 1; i < 10; i+=2){
							if(i >= h.length){
								return true;
							}
							sender.sendMessage(ChatColor.AQUA + h[i-1] + " - " + h[i]);
						}
					}
					else{
						sender.sendMessage(ChatColor.RED + "Invalid Category");
					}
				}
				else if(args.length == 2){
					//cwarps <category> <page_number>
					if(args[0].equalsIgnoreCase("info") && sender.getName().equals("@")){
						String x = args[1];
						Player p = Bukkit.getServer().getPlayer(x);
						onCommand((CommandSender) p, cmd,"",new String[0]); 
						//p.sendMessage(cats);
						//p.sendMessage(use);
						return true;
					}
					else if(args[1].matches("[0-9]+")){
						try{
							int num = Integer.parseInt(args[1]);
							
							int pnum = (num-1)*10;
							String cat = args[0].toLowerCase();
	
							if(CategorizedWarps.warps.containsKey(cat)){
								String[] h = CategorizedWarps.warps.get(cat);
								String category = CategorizedWarps.categories.get(cat);
								sender.sendMessage(ChatColor.GOLD + "---------------" + category + " Page: " + num + "/" + (int)Math.ceil((double)h.length/10.0) + "---------------");
								sender.sendMessage(ChatColor.GOLD + "----------Format: Warp - Builder(s)/Description----------");
								for(int i = 0; i < 10; i+=2){
									if((pnum + i) >= h.length){
										return true;
									}
									sender.sendMessage(ChatColor.AQUA + h[pnum+i] + " - " + h[pnum+(i+1)]);
								}
							}
							else{
								sender.sendMessage(ChatColor.RED + "Invalid Category");
							}
						}
						catch(NumberFormatException e){
							sender.sendMessage(ChatColor.RED + "Number is too large!");
							return true;
						}
					}
					else{
						sender.sendMessage(ChatColor.RED + "Invalid Number");
					}
				}
				else{
					sender.sendMessage(ChatColor.RED + "Usage: /cwarps [category] [page number]");
				}
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public final void setWarpListen(PlayerCommandPreprocessEvent event){
		String msg = event.getMessage();
		String[] parts = msg.split(" ");
		if(event.getPlayer().isOp() && parts.length == 2 && parts[0].equalsIgnoreCase("/setwarp")){
			String warp = parts[1];
			FileWriter fStream;
	        try{
	            fStream = new FileWriter(CategorizedWarps.newWarps, true);
	            fStream.append(warp);
	            fStream.append(System.getProperty("line.separator"));
	            fStream.flush();
	            fStream.close();
	        }
	        catch (IOException ex){
	            System.out.println("Could not write to text file!");
	        }
		}
	}
}