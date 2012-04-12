package bigs.modules.fe.global;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.api.utils.TextUtils;
import bigs.modules.fe.utils.*;


/**
 * RGBHistogram.java
 * DescriptionClass
 * bigs
 * @created		Created on 7 March of 2012
 * @author 		jccaicedo
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 07/03/2012	RGBHistogram.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class RGBHistogram extends FeatureExtractionAlgorithm {

	@BIGSParam
	public Integer rangeSize=8;	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		return sb.toString();
	}	
	
	@Override
	public List<List<Double>> extractFeatures(byte[] source) {			
		Image image = new Image(source); 
		int[] pixels = (int[]) image.getColorImage().getPixels();
		
		List<List<Double>> data = new ArrayList<List<Double>>(); 
		
		List<Double> descriptor = calculateRGBHistogram(pixels, image.getColorImage().getWidth(), image.getColorImage().getHeight(), rangeSize);		
		
        data.add(descriptor);
		return data;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}
	
	private List<Double> calculateRGBHistogram(int[] pixels, int w, int h, int range) {
		List<Double> descriptor = new ArrayList<Double>();
		
        int c, r, g, b;
        int roiY = 0;
        int roiX = 0;
        int roiWidth = w;
        int roiHeight = h;
        int[][] histogram = new int[3][256];
        int[] rangedHistogram = new int[range * range * range];
        int unitsPerRange = 256 / range;
        int rx, ry, rz;               

        for (int y = roiY; y < (roiY + roiHeight); y++) {
            int i = y * roiWidth + roiX;
            for (int x = roiX; x < (roiX + roiWidth); x++) {
                c = pixels[i++];
                r = (c & 0xff0000) >> 16;
                g = (c & 0xff00) >> 8;
                b = (c & 0xff);
                //HSV Space
                //v = (int)(r*0.299 + g*0.587 + b*0.114 + 0.5);
                //Conventional histogram counting
                histogram[0][r]++;
                histogram[1][g]++;
                histogram[2][b]++;
                //Ranged histogram counting
                rx = (int) Math.ceil(r / unitsPerRange);
                ry = (int) Math.ceil(g / unitsPerRange);
                rz = (int) Math.ceil(b / unitsPerRange);
                rangedHistogram[rx + ry * range + rz * range * range]++;
            }
        }
        for(int i=0 ; i<rangedHistogram.length ; i++)
          descriptor.add((double)rangedHistogram[i]);
        
        return descriptor;
    }
	
	public static List<Double> calculateHistogram(int[] pixels, int w, int h, int range) {//TODO: aacruzr: I know that this not its the solution, then I will fix.
		List<Double> descriptor = new ArrayList<Double>();
		
        int c, r, g, b;
        int roiY = 0;
        int roiX = 0;
        int roiWidth = w;
        int roiHeight = h;
        int[][] histogram = new int[3][256];
        int[] rangedHistogram = new int[range * range * range];
        int unitsPerRange = 256 / range;
        int rx, ry, rz;

        for (int y = roiY; y < (roiY + roiHeight); y++) {
            int i = y * roiWidth + roiX;
            for (int x = roiX; x < (roiX + roiWidth); x++) {
                c = pixels[i++];
                r = (c & 0xff0000) >> 16;
                g = (c & 0xff00) >> 8;
                b = (c & 0xff);
                //HSV Space
                //v = (int)(r*0.299 + g*0.587 + b*0.114 + 0.5);
                //Conventional histogram counting
                histogram[0][r]++;
                histogram[1][g]++;
                histogram[2][b]++;
                //Ranged histogram counting
                rx = (int) Math.ceil(r / unitsPerRange);
                ry = (int) Math.ceil(g / unitsPerRange);
                rz = (int) Math.ceil(b / unitsPerRange);
                rangedHistogram[rx + ry * range + rz * range * range]++;
            }
        }
        for(int i=0 ; i<rangedHistogram.length ; i++)
            descriptor.add((double)rangedHistogram[i]);
        
        return descriptor;

    }

}
