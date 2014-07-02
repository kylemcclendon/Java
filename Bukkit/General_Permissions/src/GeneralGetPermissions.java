package net.kylemc.generalpermissions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class GeneralGetPermissions{

	//Collects permission nodes and returns the list of "positive" permissions
	public static ArrayList<String> collectPermissions(UUID pu, String world){
		ArrayList<String> addPermissions = new ArrayList<String>();
		ArrayList<String> removePermissions = new ArrayList<String>();
		ArrayList<String> storedPermissions = new ArrayList<String>();
		File playersFolder = new File(GeneralPermissions.dFolder, "Players");

		//Look in player's file
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
					out.write("--------------------------------------------------");
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
		else{
			//Continue to assigning rank permissions
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
		for(String removePerm : removePermissions){
			if(addPermissions.contains(removePerm)){
				addPermissions.remove(removePerm);
			}
		}

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
	private static void parseFiles(File f, ArrayList<String> addPermissions, ArrayList<String> removePermissions, ArrayList<String> storedPermissions) throws FileNotFoundException{
		Set<Permission> allPerms = Bukkit.getPluginManager().getPermissions();
		Scanner scan;
		scan = new Scanner(f);

		//4 nextLines() to get to first permission node (if any)
		scan.nextLine();
		scan.nextLine();
		scan.nextLine();
		scan.nextLine();

		while(scan.hasNextLine()){
			String line = scan.nextLine();
			String identifier = line.substring(0, 1); //+ or -
			String permission = line.substring(2); //permission node

			if(identifier.equals("+")){
				//+ permissions
				if(permission.equals("*")){
					//Give all permissions to player

					for(Permission perm : allPerms){
						//For each permission in pluginmanager permissions, add permission
						String permName = perm.getName();
						if(!addPermissions.contains(permName)){
							//Prevent duplicates
							addPermissions.add(permName);
						}
					}
				}
				else if(permission.contains("*")){
					//All permissions of a specific node (i.e. worldedit.biome.*)

					int position = permission.indexOf("*");
					String wildcardperm = permission.substring(0, position);

					for(Permission perm : allPerms){
						String permname = perm.getName();
						if(permname.contains(wildcardperm) && !permname.equals(permission)){
							if(!addPermissions.contains(permname)){
								addPermissions.add(permname);
							}
						}
					}
				}
				else if(!(addPermissions.contains(permission))){
					addPermissions.add(permission);
				}
			}
			else{
				//- permissions
				if(permission.contains("*")){
					int position = permission.indexOf("*");
					String wildcardperm = permission.substring(0, position);

					for(Permission perm : allPerms){
						String permname = perm.getName();
						if(permname.contains(wildcardperm) && !permname.equals(permission)){
							removePermissions.add(permname);
						}
					}
				}
				else if(!(removePermissions.contains(permission))){
					removePermissions.add(permission);
				}
			}
		}
		scan.close();
		return;
	}
}