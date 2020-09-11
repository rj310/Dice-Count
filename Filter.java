package edu.mccc.cos210.fp.dice;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.mccc.cos210.ds.Array;
import edu.mccc.cos210.ds.IArray;

public class Filter {
	// colors used in image detection
	public static final int WHITE = -1; //sobel detection
	public static final int RED = -65536; //surrounding dot
	public static final int ORANGE = -32768; //die corner
	public static final int BLACK = -16777216; //sobel background
	public static final int CYAN = -16711681; //center of dot
	public static final int GREEN = -16711936; //die edges
	// how much to scale down the image for the GUI
	private static final int SCALE_FACTOR = 11;
	// how many of each kind of die there is
	private static IArray<Integer> dotCounts = new Array<Integer>(6);
	static {
		for(int i = 0;i < 6;i++) {
			dotCounts.set(i, 0);
		}
	}
	// lookup table for converting greyscale sobel to black and white
	static byte[] lookup = new byte[256];
	static {
		for (int i = 0; i < lookup.length; i++) {
			if (i < 150) {
				lookup[i] = 0;
			} else {
				lookup[i] = (byte) 255;
			}
		}
	}
	// ----------- UTILITY FUNCTIONS ----------
	// make a deep copy of a buffered image
	public static BufferedImage deepCopyImage(BufferedImage bi) {
		BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(),
				BufferedImage.TYPE_INT_BGR);
		for(int i = 0;i < bi.getHeight();i++) {
			for(int j = 0;j < bi.getWidth();j++) {
				copy.setRGB(j, i, bi.getRGB(j, i));
			}
		}
		return copy;
	}
	// accessor to check how many of a certain die was found (how many 3s, etc)
	public static int getDiceCount(BufferedImage img, int dot_count) {
		return dotCounts.get(dot_count-1);
	}
	// creates a scaled version of an image
	public static BufferedImage getScaled(BufferedImage img) {
		BufferedImage biScale = new BufferedImage(
				img.getWidth() / SCALE_FACTOR,
				img.getHeight() / SCALE_FACTOR,
				BufferedImage.TYPE_3BYTE_BGR
			);
			Graphics2D g2d1 = biScale.createGraphics();
			AffineTransform at = AffineTransform.getScaleInstance(
				1.0 / SCALE_FACTOR,
				1.0 / SCALE_FACTOR
			);
			at.translate(0, 0);
			g2d1.drawRenderedImage(
				img,
				at
			);
			g2d1.dispose();
			return biScale;
	}
	// converting a color image into a sobel image with edge detection
	public static BufferedImage getSobel(BufferedImage biScaled) {
		BufferedImage bi = Filter.deepCopyImage(biScaled);
        IArray<Array<Integer>> sx1 = new Array<Array<Integer>>(3);
        Array<Integer> sx11 = new Array<Integer>(3);
        sx11.set(0, -1);
        sx11.set(1, 0);
        sx11.set(2, 1);
        Array<Integer> sx12 = new Array<Integer>(3);
        sx12.set(0, -1);
        sx12.set(1, 0);
        sx12.set(2, 1);
        Array<Integer> sx13 = new Array<Integer>(3);
        sx13.set(0, -1);
        sx13.set(1, 0);
        sx13.set(2, 1);
        sx1.set(0, sx11);
        sx1.set(1, sx12);
        sx1.set(2, sx13);
        IArray<Array<Integer>> sy1 = new Array<Array<Integer>>(3);
        Array<Integer> sy11 = new Array<Integer>(3);
        sy11.set(0, -1);
        sy11.set(1, -1);
        sy11.set(2, -1);
        Array<Integer> sy12 = new Array<Integer>(3);
        sy12.set(0, 0);
        sy12.set(1, 0);
        sy12.set(2, 0);
        Array<Integer> sy13 = new Array<Integer>(3);
        sy13.set(0, 1);
        sy13.set(1, 1);
        sy13.set(2, 1);
        sy1.set(0, sy11);
        sy1.set(1, sy12);
        sy1.set(2, sy13);
        int width = bi.getWidth();
        int height = bi.getHeight();
        IArray<Array<Integer>> sob1 = new Array<Array<Integer>>(width);
        for(int i = 0;i < width;i++) {
        	sob1.set(i, new Array<Integer>(height));
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
            	Color c = new Color(bi.getRGB(x, y));
                int pixel = bi.getRGB(x, y);
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();
                // calculate average
                int avg = (r+g+b)/3;
                sob1.get(x).set(y, avg);
                // replace RGB value with average
                pixel = (avg << 24) | (avg << 16) | (avg << 8) | avg;
                bi.setRGB(x, y, pixel);
            }
        }
        // sobel calculation
        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                int px = (sx1.get(0).get(0) * sob1.get(x-1).get(y-1)) 
                		+ (sx1.get(0).get(1) * sob1.get(x).get(y-1)) 
                		+ (sx1.get(0).get(2) * sob1.get(x+1).get(y-1))
                		+ (sx1.get(1).get(0) * sob1.get(x-1).get(y)) 
                		+ (sx1.get(1).get(1) * sob1.get(x).get(y)) 
                		+ (sx1.get(1).get(2) * sob1.get(x+1).get(y)) 
                		+ (sx1.get(2).get(0) * sob1.get(x-1).get(y+1)) 
                		+ (sx1.get(2).get(1) * sob1.get(x).get(y+1)) 
                		+ (sx1.get(2).get(2) * sob1.get(x+1).get(y+1));
                
                int py =  (sy1.get(0).get(0) * sob1.get(x-1).get(y-1)) 
                		+ (sy1.get(0).get(1) * sob1.get(x).get(y-1)) 
                		+ (sy1.get(0).get(2) * sob1.get(x+1).get(y-1)) 
                		+ (sy1.get(1).get(0) * sob1.get(x-1).get(y)) 
                		+ (sy1.get(1).get(1) * sob1.get(x).get(y))
                		+ (sy1.get(1).get(2) * sob1.get(x+1).get(y))
                		+ (sy1.get(2).get(0) * sob1.get(x-1).get(y+1)) 
                		+ (sy1.get(2).get(1) * sob1.get(x).get(y+1)) 
                		+ (sy1.get(2).get(2) * sob1.get(x+1).get(y+1));

                int pixel = (int) Math.sqrt((px * px) + (py * py));
                if (pixel>255) {
                    pixel = 255;
                } else if (pixel<0) {
                    pixel = 0;
                }
                Color pix = new Color(pixel,pixel,pixel);
                bi.setRGB(x, y, pix.getRGB());
            }
        }
        LookupOp lookUpOp = new LookupOp(new ByteLookupTable(0, lookup), null);
		BufferedImage biLookup = lookUpOp.filter(bi, null);
		BufferedImage colorEdge = makeColor(biLookup); 
		return (colorEdge);
	}
	// convert greyscale to the same image but with an available color index
	private static BufferedImage makeColor(BufferedImage grey) {
		BufferedImage colorImage = new BufferedImage(grey.getWidth(), 
				grey.getHeight(), BufferedImage.TYPE_INT_BGR);
		for (int y = 0; y < grey.getHeight(); y++) {
			for (int x = 0; x < grey.getWidth(); x++)	{
				int rgb = grey.getRGB(x, y);
				colorImage.setRGB(x, y, rgb);
			}
		}
		return colorImage;
	}
	// Merge two images taking the non-black or non-white pixel if possible
	public static BufferedImage mergeImages(BufferedImage dotImg, BufferedImage squareImg, boolean clearBackground) {
		
		BufferedImage biMerged = new BufferedImage(dotImg.getWidth(), 
				dotImg.getHeight(), BufferedImage.TYPE_INT_BGR);
		for (int y = 0; y < dotImg.getHeight(); y++) {
			for (int x = 0; x < dotImg.getWidth(); x++) {
				int dotColor = dotImg.getRGB(x, y);
				int squareColor = squareImg.getRGB(x, y);
				if(dotColor == RED) {
					biMerged.setRGB(x, y, RED);
				}
				else if(dotColor == CYAN) {
					biMerged.setRGB(x, y, CYAN);
				}
				else if(squareColor == ORANGE) {
					biMerged.setRGB(x, y, ORANGE);
				}
				else if(squareColor == GREEN) {
					biMerged.setRGB(x, y, GREEN);
				}
				else if(dotColor == WHITE || squareColor == WHITE) {
					// if clearBackground is true white area converted to black
					if(clearBackground == true) {
						biMerged.setRGB(x, y, BLACK);
					}
					else {
						biMerged.setRGB(x, y, WHITE);
					}
				}
				else {
					biMerged.setRGB(x, y, BLACK);
				}
			}
		}
		return biMerged;
	}
	// ------------- IMAGE ANALYSIS METHODS --------------
	// counts black pixels surrounded with white at 3-4 pixels distance (dots)
	public static Result circleCount(BufferedImage input) {
		int min_radius = 3;
		int max_radius = 4;
		int minimum_hits = 36;
		int min_range = (int)(max_radius*1.1);
		
		int dot_count = 0;

		for(int x = 0;x < input.getWidth();x++) {
			for(int y = 0; y < input.getHeight(); y++) {
				//anything immediately surrounded in white can't be a spot
				if(input.getRGB(x, y) == WHITE) {
					continue;
				}
				if(y > 0 && input.getRGB(x, y-1) == WHITE) {
					continue;
				}
				if(x > 0 && input.getRGB(x-1, y) == WHITE) {
					continue;
				}
				if(x < input.getWidth()-1 && input.getRGB(x+1, y) == WHITE) {
					continue;
				}
				boolean too_close = false;
				for(int q = - min_range; q <= min_range ; q++) {
					for(int p = - min_range; p <= min_range; p++) {
						double dist = Math.sqrt(p*p+q*q);
						
						if(dist <= max_radius * min_range) {
							int x_check = x + q;
							int y_check = y + p;
							if(x_check >= 0 && x_check < input.getWidth() 
									&& y_check >= 0 
									&& y_check < input.getHeight()) {
								if(input.getRGB(x_check, y_check) == CYAN) {
									too_close = true;
								}
							}
						}
					}
				}
				
				if(too_close == false) {							
					int white_count = 0;
					for(int deg = 0;deg < 360;deg+=10) {
						boolean found_white = false;
						for(int r = min_radius;r <= max_radius;r++) {
							double deg2rad = Math.PI/180.0;
							int x_check = (int)(x + r*Math.sin(deg*deg2rad));
							int y_check = (int)(y + r*Math.cos(deg*deg2rad));
							if(x_check >= 0 && x_check < input.getWidth() 
									&& y_check >= 0 
									&& y_check < input.getHeight()) {
								if(input.getRGB(x_check,y_check) == WHITE ) {
									found_white = true;
									break;
								}
							}
						}
						if(found_white) {
							white_count++;
						}
					}
					if(white_count >= minimum_hits) {
						input.setRGB(x, y, CYAN);
					}
				}
			}
		}
		for(int x = 0;x < input.getWidth();x++) {
			for(int y = 0;y < input.getHeight();y++) {
				if(input.getRGB(x, y) == CYAN) {
					dot_count++;
					for(int xoff = -4;xoff <= 4;xoff++) {
						for(int yoff = -4;yoff <= 4;yoff++) {
							double dist = Math.sqrt(xoff * xoff + yoff * yoff);
							if( dist < 4) {
								if(x + xoff >= 0 && x + xoff < input.getWidth()
										&& y+yoff >=0 
										&& y+yoff < input.getHeight()) {
									input.setRGB(x + xoff, y + yoff, RED);
								}
							}
						}
					}
					input.setRGB(x, y, CYAN);
				}
			}
		}
		Result cr = new Result(input, dot_count);
		return cr;
	}
	// looks for straight lines at 90 degree angles & counts "dots" found
	public static Result squareCount(BufferedImage input) {
		double minLength = 25;
		int ninetyDegreeAngleCount = 0;
		for(int i = 0;i < 6;i++) {
			dotCounts.set(i, 0);
		}
		double minLenSquared = minLength * minLength;
		for(int x = 0;x < input.getWidth();x++) {
			for(int y = 0;y < input.getHeight();y++) {
				double radMatch1 = -99;
				double radMatch2 = -99;
				double lenSquared1 = 0;
				double lenSquared2 = 0;
				for(double rads=Math.PI;rads<2*Math.PI-0.05;rads+=0.017453) {
					double checkX = x;
					double checkY = y;
					double stepX = Math.sin(rads);
					double stepY = Math.cos(rads);
					int skips = 3;
					while(true) {
						if((int)checkY < 0 
								|| (int)checkY >= input.getHeight() 
								|| (int)checkX >= input.getWidth() 
								|| (int)checkX < 0) {
							break;
						}
						if(input.getRGB((int)checkX, (int)checkY) != WHITE) {
							if(skips == 0) {
								break;
							}
							skips--;
							//break;
						}
						checkX += stepX;
						checkY += stepY;
					}
					double dx = checkX - x;
					double dy = checkY - y;
					double lenSquared = dx*dx+dy*dy;
					if(lenSquared > minLenSquared) {
						
						if(radMatch1 == -99) {
							radMatch1 = rads;
							lenSquared1 = lenSquared;
						}
						else if(radMatch2 == -99) {
							if(rads-radMatch1 > 1.4 && rads-radMatch1 < 1.74) {
								radMatch2 = rads;
								lenSquared2 = lenSquared;
							}
						}
					}
				}
				//found a 90 degree angle of white straight lines
				if(radMatch2 != -99) {
					int min_range = 7;
					int max_radius = 1;
					boolean too_close = false;
					for(int q = - min_range; q <= min_range ; q++) {
						for(int p = - min_range; p <= min_range; p++) {
							double dist = Math.sqrt(p*p+q*q);
							if(dist <= max_radius * min_range) {
								int x_check = x + q;
								int y_check = y + p;
								if(x_check >= 0 && x_check < input.getWidth()
										&& y_check >= 0 
										&& y_check < input.getHeight()) {
									int color = input.getRGB(x_check, y_check);
									if(color == ORANGE) {
										too_close = true;
									}
								}
							}
						}
					}
					if(too_close == false) {
						ninetyDegreeAngleCount++;
						double len2 = Math.sqrt(lenSquared2);
						for(int i = 0;i < len2;i++) {
							int reCheckX = (int)(x + i*Math.sin(radMatch2));
							int reCheckY = (int)(y + i*Math.cos(radMatch2));
							input.setRGB(reCheckX, reCheckY, GREEN);
						}
						double len1 = Math.sqrt(lenSquared1);
						for(int i = 0;i < len1;i++) {
							int reCheckX = (int)(x + i*Math.sin(radMatch1));
							int reCheckY = (int)(y + i*Math.cos(radMatch1));
							input.setRGB(reCheckX, reCheckY, GREEN);
						}
						input.setRGB(x, y, ORANGE);
						int dotDistance = 30;
						int dotCount = 0;
						for(int i = -dotDistance;i <= 0;i++) {
							for(int j = -dotDistance;j <= dotDistance;j++) {
								if(x+i >= 0 && x+i< input.getWidth() && y+j>=0 
										&& y+j<input.getHeight()) {
									if(input.getRGB(x+i, y+j) == CYAN) {
										dotCount++;
									}
								}
							}
						}
						if(dotCount > 6) {
							dotCount = 6;
						}
						if(dotCount < 1) {
							dotCount = 1;
						}
						dotCounts.set(dotCount-1, dotCounts.get(dotCount-1)+1);
					}
				}
			}
		}
		for(int x = 0;x < input.getWidth();x++) {
			for(int y = 0;y<input.getHeight();y++) {
				double radMatch1 = -99;
				double radMatch2 = -99;
				double lenSquared1 = 0;
				double lenSquared2 = 0;
				for(double rads=0;rads<Math.PI-0.05;rads+=0.017453) {
					double checkX = x;
					double checkY = y;
					double stepX = Math.sin(rads);
					double stepY = Math.cos(rads);
					int skips = 3;
					while(true) {
						if((int)checkY < 0 
								|| (int)checkY >= input.getHeight() 
								|| (int)checkX >= input.getWidth() 
								|| (int)checkX < 0) {
							break;
						}
						if(input.getRGB((int)checkX, (int)checkY) != WHITE) {
							if(skips == 0) {
								break;
							}
							skips--;
							//break;
						}
						checkX += stepX;
						checkY += stepY;
					}
					double dx = checkX - x;
					double dy = checkY - y;
					double lenSquared = dx*dx+dy*dy;
					if(lenSquared > minLenSquared) {
						
						if(radMatch1 == -99) {
							radMatch1 = rads;
							lenSquared1 = lenSquared;
						}
						else if(radMatch2 == -99) {
							if(rads-radMatch1 > 1.4 && rads-radMatch1 < 1.74) {
								radMatch2 = rads;
								lenSquared2 = lenSquared;
							}
						}
					}
				}
				//found a 90 degree angle of white straight lines
				if(radMatch2 != -99) {
					int min_range = 7;
					int max_radius = 1;
					boolean too_close = false;
					for(int q = - min_range; q <= min_range ; q++) {
						for(int p = - min_range; p <= min_range; p++) {
							double dist = Math.sqrt(p*p+q*q);
							if(dist <= max_radius * min_range) {
								int x_check = x + q;
								int y_check = y + p;
								if(x_check >= 0 && x_check < input.getWidth() 
										&& y_check >= 0 
										&& y_check < input.getHeight()) {
									int color = input.getRGB(x_check, y_check);
									if(color == ORANGE) {
										too_close = true;
									}
								}
							}
						}
					}
					
					if(too_close == false) {
						double len2 = Math.sqrt(lenSquared2);
						for(int i = 0;i < len2;i++) {
							int reCheckX = (int)(x + i*Math.sin(radMatch2));
							int reCheckY = (int)(y + i*Math.cos(radMatch2));
							input.setRGB(reCheckX, reCheckY, GREEN);
						}
						double len1 = Math.sqrt(lenSquared1);
						for(int i = 0;i < len1;i++) {
							int reCheckX = (int)(x + i*Math.sin(radMatch1));
							int reCheckY = (int)(y + i*Math.cos(radMatch1));
							input.setRGB(reCheckX, reCheckY, GREEN);
						}
						input.setRGB(x, y, ORANGE);
					}
				}
			}
		}
		Result cr = new Result(input, ninetyDegreeAngleCount);
		return cr;
	}
}