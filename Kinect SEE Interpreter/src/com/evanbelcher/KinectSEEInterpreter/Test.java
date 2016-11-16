package com.evanbelcher.KinectSEEInterpreter;
import java.io.File;

public class Test {

	public static void main(String[] args){
		//System.out.println("H:/Documents/My Pictures/Saved Pictures/frame".substring(0,"H:/Documents/My Pictures/Saved Pictures/frame".lastIndexOf("/")));
		cleanDirectory(new File("H:/Documents/My Pictures/Saved Pictures"));
	}
	
	static void cleanDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (!file.isDirectory())
				file.delete();
		}
	}
	
}
