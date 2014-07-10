package net.kylemc.generalpermissions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public final class GeneralGetPermissions{

	//HashMap to hold wildcard permissions for pseudo-caching
	private static HashMap<String, Set<String>> Wildcards = new HashMap<String, Set<String>>();
	//Set that holds all the server's permissions
	private static Set<Permission> serverPermissions = Bukkit.getPluginManager().getPermissions();

	//Collects permission nodes and returns the list of "positive" permissions
	public final static HashSet<String> collectPermissions(UUID pu, String world){
		HashSet<String> addPermissions = new HashSet<String>();
		HashSet<String> removePermissions = new HashSet<String>();
		HashSet<String> storedPermissions = new HashSet<String>();

		//Look in player's file
		File playersFolder = new File(GeneralPermissions.dFolder, "Players");
		try{
			File playerFile = new File(playersFolder, pu + ".txt");

			if(!playerFile.exists()){
				try{
					File p = new File(playersFolder, pu + ".txt");
					BufferedWriter out = new BufferedWriter(new FileWriter(p));
					out.write("Rank: " + GeneralPermissions.groupNames[0]);
					out.newLine();
					out.write("Name: " + Bukkit.getPlayer(pu).getName());
					out.newLine();
					out.newLine();
					out.write("Permissions:");
					out.newLine();
					out.write("+ permissions.build");
					out.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
			parseFiles(playerFile, addPermissions, removePermissions, storedPermissions);
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}

		//Get player's rank
		String rank = GeneralPermissionsCommands.getRank(pu);

		//If rank is not in groupNames or modNames, set rank to lowest rank (if available), and warn console
		if(rank.equals("") || (!Utils.contains(rank, GeneralPermissions.groupNames) && !Utils.contains(rank, GeneralPermissions.modNames))){
			if(!GeneralPermissions.groupNames[0].equals("")){
				System.out.println(pu + " has an invalid rank! Being set to lowest rank or null if not set!");
				GeneralPermissionsCommands.setRank(pu, rank, GeneralPermissions.groupNames[0]);
				rank = GeneralPermissions.groupNames[0];
			}
		}

		//Look in rank file
		if(rank.equals("")){
			//Assign no permissions for rank
			GeneralPermissions.prefixes.put(pu, "");
		}
		else{
			File groupsFolder = new File(GeneralPermissions.dFolder, "Groups");
			File groupFile = new File(groupsFolder, rank + ".txt");
			try{
				Scanner scan = new Scanner(groupFile);
				String line = scan.nextLine();
				String[] prefixLine = line.split(":");
				if(prefixLine.length == 1){
					GeneralPermissions.prefixes.put(pu, "");
				}
				else{
					String prefix = prefixLine[1].trim();
					if(prefix.contains("#")){
						GeneralPermissions.prefixes.put(pu, "");
					}
					else{
						GeneralPermissions.prefixes.put(pu, prefix);
					}
				}
				scan.close();
				parseFiles(groupFile, addPermissions, removePermissions, storedPermissions);
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}
		}

		//Look in world's rank file
		if(rank.equals("")){
			//Don't assign world permissions
		}
		else{
			try{
				File groupFile;
				File worldsFolder = new File(GeneralPermissions.dFolder, "Worlds");
				File worldFile = new File(worldsFolder, world);
				groupFile = new File(worldFile, rank + ".txt");
				parseFiles(groupFile, addPermissions, removePermissions, storedPermissions);
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}
		}

		//Remove any permissions in addPermissions that appear in removePermissions
		addPermissions.removeAll(removePermissions);

		for(String remainingAdd : addPermissions){
			storedPermissions.add("+"+remainingAdd);
		}
		for(String removal : removePermissions){
			storedPermissions.add("-"+removal);
		}

		GeneralPermissions.permissions.put(pu, storedPermissions);

		return addPermissions;
	}

	//Method to parse through the files for permission nodes
	private final static void parseFiles(File f, Set<String> addPermissions, Set<String> removePermissions, Set<String> storedPermissions) throws FileNotFoundException{
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		String text = "";
		String newLine = Utils.OS;

		try{
			//skip 4 lines to get to first permission node (if any)
			br.readLine();
			br.readLine();
			br.readLine();
			br.readLine();

			//Read in line, if not null, add to 'text'; repeat until EOF
			line = br.readLine();

			while(line != null){
				text += line + newLine;
				line = br.readLine();
			}
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}

		//Array holding each +/- permission
		String[] parsedPerms = text.split(newLine);
		String identifier = ""; //+ or -
		String node = ""; //worldedit.*

		//Loop through each permission, adding to sets
		for(String perm : parsedPerms){
			identifier = perm.substring(0,1); //+ or -
			node = perm.substring(2); //permission node

			if(node.contains("*")){
				//Permission has a wildcard
				Set<String> wP = wildcardPermissions(node);

				if(identifier.equals("+")){
					addPermissions.addAll(wP);
				}
				else{
					removePermissions.addAll(wP);
				}
			}
			else{
				//'Pure' permission
				if(identifier.equals("+")){
					addPermissions.add(node);
				}
				else{
					removePermissions.add(node);
				}
			}
		}
		return;
	}

	//Method to handle Wildcard Permissions (.* permissions)
	private final static Set<String> wildcardPermissions(String wild){
		if(Wildcards.containsKey(wild)){
			//This wildcard has been cached already
			return Wildcards.get(wild);
		}
		else{
			//Go through server permissions and create new Set of permissions
			Set<String> resolveWild = new HashSet<String>();
			int starIndex = wild.indexOf("*");
			String baseNode = wild.substring(0,starIndex); //'worldedit.biome.' part of worldedit.biome.*
			String permName = "";

			for(Permission p : serverPermissions){
				permName = p.getName();

				if(permName.contains(baseNode)){
					//If permission has the node part
					if(permName.contains("*")  && !(permName.equals(wild))){
						//Another wildcard found while iterating
						Set<String> internalWild = wildcardPermissions(permName); //Collect all permissions related to the wildcard
						resolveWild.addAll(internalWild); //Add all permissions found from the internal to the main
					}
					else{
						//Add the permission
						if(!(permName.contains("*"))){
							resolveWild.add(permName);
						}
					}
				}
			}

			//Add in wildcard permission to set if the set is empty
			if(resolveWild.isEmpty()){
				resolveWild.add(wild);
			}

			//Add new wildcard to HashMap for faster access
			Wildcards.put(wild, resolveWild);
			return resolveWild;
		}
	}
}