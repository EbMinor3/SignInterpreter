package com.evanbelcher.KinectSEEInterpreter;


import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class GetAllFrames {
	
	public static final double SECONDS_BETWEEN_FRAMES = (1 / 29.97);
	
	private static final String inputFilename = System.getenv("SystemDrive") + "/Documents/My Pictures/apteacher2.mp4";
	public static final String outputFilePrefix = System.getenv("SystemDrive") + "/Documents/My Pictures/Saved Pictures/frame";
	private static int left = Integer.MAX_VALUE , right = Integer.MIN_VALUE, top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE;
	private static final boolean everyFrame = true;
	private static int frameNum = 1;
	
	// The video stream index, used to ensure we display frames from one and
	// only one video stream from the media container.
	private static int mVideoStreamIndex = -1;
	
	// Time of last frame write
	private static long mLastPtsWrite = Global.NO_PTS;
	
	public static final long MICRO_SECONDS_BETWEEN_FRAMES = (long) (Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
	
	public static void main(String[] args) throws IOException {
		/*testThing(ImageIO.read(new File(System.getenv("SystemDrive") + "\\Documents\\My Pictures\\Saved Pictures\\Pony\\keyframe44.png")));
		System.exit(0);
		*/
		IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);
		
		// stipulate that we want BufferedImages created in BGR 24bit color space
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		
		mediaReader.addListener(new ImageSnapListener());
		
		// read out the contents of the media file and
		// dispatch events to the attached listener
		while (mediaReader.readPacket() == null);
		System.out.println(top + " " + bottom + " " + left + " " + right);
		//new KeyFrames2();
	}
	
	public static BufferedImage testThing(BufferedImage img){
		System.out.println(img.getWidth() + " " + img.getHeight());
		BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int rgb1 = img.getRGB(x, y);
				int val = (rgb1 >> 16) & 0xff;
				//System.out.println(val);
				int newval = val * 8;
				newval -= (img.getRGB((x > 0 && y > 0 ? x : x + 1) - 1, (x > 0 && y > 0 ? y : y + 1) - 1) >> 16) & 0xff;
				newval -= (img.getRGB(x, (y > 0 ? y : y + 1) - 1) >> 16) & 0xff;
				newval -= (img.getRGB((x < img.getWidth() - 1 && y > 0 ? x : x - 1) + 1,
						(x < img.getWidth() - 1 && y > 0 ? y : y + 1) - 1) >> 16) & 0xff;

				newval -= (img.getRGB((x > 0 ? x : x + 1) - 1, y) >> 16) & 0xff;
				newval -= (img.getRGB((x < img.getWidth() - 1 ? x : x - 1) + 1, y) >> 16) & 0xff;

				newval -= (img.getRGB((x > 0 && y < img.getHeight() - 1 ? x : x + 1) - 1,
						(x > 0 && y < img.getHeight() - 1 ? y : y - 1) + 1) >> 16) & 0xff;
				newval -= (img.getRGB(x, (y < img.getHeight() - 1 ? y : y - 1) + 1) >> 16) & 0xff;
				newval -= (img.getRGB((x < img.getWidth() - 1 && y < img.getHeight() - 1 ? x : x - 1) + 1,
						(x < img.getWidth() - 1 && y < img.getHeight() - 1 ? y : y - 1) + 1) >> 16) & 0xff;

				//System.out.println(newval);
				newval = Math.max(Math.min(newval, 255), 0);
				
				
				if(newval > 31){
					top = Math.min(top, y);
					bottom = Math.max(bottom, y);
					left = Math.min(left, x);
					right = Math.max(right, x);
				}
				
				img2.setRGB(x, y, ((newval & 0x0ff) << 16) | ((newval & 0x0ff) << 8) | (newval & 0x0ff));
			}
		}
		System.out.println(left + " " + right + " " + top + " " + bottom); 
		return img2;
	}
	
	private static class ImageSnapListener extends MediaListenerAdapter {
		
		public void onVideoPicture(IVideoPictureEvent event) {
			
			if (event.getStreamIndex() != mVideoStreamIndex) {
				// if the selected video stream id is not yet set, go ahead an
				// select this lucky video stream
				if (mVideoStreamIndex == -1)
					mVideoStreamIndex = event.getStreamIndex();
				// no need to show frames from this video stream
				else {
					System.out.println(mVideoStreamIndex);
					return;
					
				}
			}
			
			// if uninitialized, back date mLastPtsWrite to get the very first frame
			if (mLastPtsWrite == Global.NO_PTS)
				mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;
				
			// if it's time to write the next frame
			if (everyFrame) {
				String outputFilename = dumpImageToFile(event.getImage());
				
				// indicate file written
				double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
				System.out.printf("at elapsed time of %6.3f seconds wrote: %s\n", seconds, outputFilename);
			} else if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES) {
				
				String outputFilename = dumpImageToFile(event.getImage());
				
				// indicate file written
				double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
				System.out.printf("at elapsed time of %6.3f seconds wrote: %s\n", seconds, outputFilename);
				
				// update last write time
				mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
			}
			
		}
		
		private String dumpImageToFile(BufferedImage image) {
			BufferedImage grayImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			grayImg.getGraphics().drawImage(image, 0, 0, null);
			grayImg = outlinedImage(grayImg);
			try {
				String outputFilename = outputFilePrefix + (everyFrame ? frameNum++ : System.currentTimeMillis()) + ".png";
				ImageIO.write(grayImg, "png", new File(outputFilename));
				return outputFilename;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		public static BufferedImage outlinedImage(BufferedImage img) {
			BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int rgb1 = img.getRGB(x, y);
					int val = (rgb1 >> 16) & 0xff;
					//System.out.println(val);
					int newval = val * 8;
					newval -= (img.getRGB((x > 0 && y > 0 ? x : x + 1) - 1, (x > 0 && y > 0 ? y : y + 1) - 1) >> 16) & 0xff;
					newval -= (img.getRGB(x, (y > 0 ? y : y + 1) - 1) >> 16) & 0xff;
					newval -= (img.getRGB((x < img.getWidth() - 1 && y > 0 ? x : x - 1) + 1,
							(x < img.getWidth() - 1 && y > 0 ? y : y + 1) - 1) >> 16) & 0xff;

					newval -= (img.getRGB((x > 0 ? x : x + 1) - 1, y) >> 16) & 0xff;
					newval -= (img.getRGB((x < img.getWidth() - 1 ? x : x - 1) + 1, y) >> 16) & 0xff;

					newval -= (img.getRGB((x > 0 && y < img.getHeight() - 1 ? x : x + 1) - 1,
							(x > 0 && y < img.getHeight() - 1 ? y : y - 1) + 1) >> 16) & 0xff;
					newval -= (img.getRGB(x, (y < img.getHeight() - 1 ? y : y - 1) + 1) >> 16) & 0xff;
					newval -= (img.getRGB((x < img.getWidth() - 1 && y < img.getHeight() - 1 ? x : x - 1) + 1,
							(x < img.getWidth() - 1 && y < img.getHeight() - 1 ? y : y - 1) + 1) >> 16) & 0xff;

					//System.out.println(newval);
					newval = Math.max(Math.min(newval, 255), 0);
					
					if(newval > 230){
						top = Math.min(top, y);
						bottom = Math.max(bottom, y);
						left = Math.min(left, x);
						right = Math.max(right, x);
					}
					
					//img2.setRGB(x, y, ((newval & 0x0ff) << 16) | ((newval & 0x0ff) << 8) | (newval & 0x0ff));
					img2.setRGB(x, y, 233);
					//System.out.println(newval);
				}
			}
			return img2;
		}
		
	}
	
	private static BufferedImage getScaledImage(Image srcImg, int w, int h) {
	    BufferedImage resizedImg = new BufferedImage(w, h, Transparency.TRANSLUCENT);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();
	    return resizedImg;
	}
	
}