package com.evanbelcher.KinectSEEInterpreter;
import java.io.File;
import java.util.ArrayList;

public class Translate {

	public static void main(String[] args) {
		String root = GetAllFrames.outputFilePrefix.substring(0, GetAllFrames.outputFilePrefix.lastIndexOf("/"));
		ArrayList<String> words = indexDirectory(new File(root));
		System.out.println(words);
	}
	
//	static void cleanDirectory(File dir) {
//		for (File file : dir.listFiles()) {
//			if (!file.isDirectory())
//				file.delete();
//		}
//	}
	
	static ArrayList<String> indexDirectory(File dir){
		ArrayList<String> words = new ArrayList<String>();
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				words.add(file.list().length + "|" + file.getName());
			}
		}
		return words;
	}

}
