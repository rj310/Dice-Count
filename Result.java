package edu.mccc.cos210.fp.dice;

import java.awt.image.BufferedImage;

public class Result {
	public int theCount;
	public BufferedImage theImage;
	
	public Result(BufferedImage bi, int c) {
		theCount = c;
		theImage = bi;
	}
}