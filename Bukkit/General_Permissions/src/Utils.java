package net.kylemc.generalpermissions;

import java.util.Arrays;

public final class Utils{
	public final static String OS = System.getProperty("os.name").contains("Windows") ? "\r\n" : "\n";

	private static String ranks = "";

	public final static void initRanks(){
		ranks = "";
		if(GeneralPermissions.groupNames.length < 1){
			return;
		}
		else{
			ranks += GeneralPermissions.groupNames[0];

			for(int i = 0; i < GeneralPermissions.groupNames.length; i++){
				ranks += ", " + GeneralPermissions.groupNames[i];
			}
		}
	}

	public final static String getRanks(){
		return ranks;
	}

	//Method to check if a string is in an array of strings
	public final static boolean contains(String type, String[] array){
		if(array.length > 39 && !array[0].equals("")){
			if(Arrays.binarySearch(array, type) != -1){
				return true;
			}
			return false;
		}
		else{
			for(int i = 0; i < array.length; i++){
				if(type.equalsIgnoreCase(array[i])){
					return true;
				}
			}
			return false;
		}
	}
}