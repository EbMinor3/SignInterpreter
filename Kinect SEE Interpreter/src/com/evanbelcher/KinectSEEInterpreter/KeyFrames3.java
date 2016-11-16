package com.evanbelcher.KinectSEEInterpreter;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class KeyFrames3 implements Runnable {
	
	private static BufferedImage img2 = null;
	private static final String FRAME_FILEPATH_PREFIX = GetAllFrames.outputFilePrefix;
	private static String keyframe_filename_prefix = System.getenv("SystemDrive") + "/Documents/My Pictures/Saved Pictures/";
	
	// To configure
	public static final double LOWER_THRESHOLD = 0.01;// discard anything below
														// this
	public static final double UPPER_THRESHOLD = 0.05;// new key frame if above
														// this
														//
														
	LinkedList<BufferedImage> images;
	Rectangle focus;
	
	public KeyFrames3(LinkedList<BufferedImage> images, Rectangle focus) {
		this.images = images;
		this.focus = focus;
		System.out.println(focus);
	}
	
	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		System.out.print("Word: ");
		String word = sc.nextLine();
		sc.close();
		keyframe_filename_prefix += word.trim();// + "/keyframe";
		new File(keyframe_filename_prefix).mkdirs();
		keyframe_filename_prefix += "/keyframe";
		
		long startTime = System.nanoTime();
		
		ArrayList<BufferedImage> img1 = new ArrayList<BufferedImage>();
		
		for(int i = 0; i < images.size() - 1; i++){
			img1.add(images.get(i));
			img2 = images.get(i + 1);
			
			ArrayList<Long> diff = new ArrayList<Long>();
			ArrayList<CompareThread> threads = new ArrayList<CompareThread>();
			for (BufferedImage img : img1) {
				threads.add(new CompareThread("" + i, img, img2, focus));
				threads.get(threads.size() - 1).start();
			}
			try {
				Thread.sleep(480);
				// System.out.println(i + " done.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (CompareThread t : threads) {
				diff.add(t.getDiff());
			}
			
			double n = focus.getWidth()*focus.getHeight() * 3;
			double p = sum(diff) / n / 255.0;
			// System.out.println(sum(diff));
			if (p > UPPER_THRESHOLD) {
				File outputfile = new File(keyframe_filename_prefix + (i - img1.size() + 1) + ".png");
				try {
					ImageIO.write(img1.get(0), "png", outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				img1.clear();
			}
			if(i == images.size()-2){
				File outputfile = new File(keyframe_filename_prefix + (i - img1.size() + 1) + ".png");
				try {
					ImageIO.write(img1.get(0), "png", outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println((System.nanoTime() - startTime) / 1000000000);
		System.exit(0);
	}
	
	public static long sum(ArrayList<Long> a) {
		long sum = 0;
		for (long l : a)
			sum += l;
		return sum;
	}
	
	static int getFileCount(File dir) {
		int count = dir.listFiles().length;
		for (File file : dir.listFiles())
			if (file.isDirectory())
				count--;
		return count;
	}
	
	static void cleanDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (!file.isDirectory())
				file.delete();
		}
	}
	
}

