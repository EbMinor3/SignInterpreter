package com.evanbelcher.KinectSEEInterpreter;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

class CompareThread extends Thread {
	
	private Thread t;
	private String threadName;
	private BufferedImage img1;
	private BufferedImage img2;
	private long diff;
	private Rectangle focus;
	
	CompareThread(String threadName, BufferedImage img1, BufferedImage img2, Rectangle focus) {
		this.threadName = threadName;
		this.img1 = img1;
		this.img2 = img2;
		this.focus = focus;
		// System.out.println("Creating" + threadName);
	}
	
	public void run() {
		// long startTime = System.nanoTime();
		// System.out.println("Running " + threadName);
		long diff = 0;
		for (int y = (int) focus.getMinY(); y <= focus.getMaxY(); y++) {
			for (int x = (int) focus.getMinX(); x <= focus.getMaxX(); x++) {
				int rgb1 = img1.getRGB(x, y);
				int rgb2 = img2.getRGB(x, y);
				int r1 = (rgb1 >> 16) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				diff += Math.abs(r1 - r2);
			}
		}
		this.diff = diff;
		// System.out.println(diff);
		// System.out.println("Thread " + threadName + " exiting.");
	}
	
	public void start() {
		// System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}
	
	public long getDiff() {
		return diff;
	}
	
}
