package bigs.modules.fe.global;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.ImageProcessor;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
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
 * LireHSVColorHistogram.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	LireHSVColorHistogram.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class LireHSVColorHistogram extends FeatureExtractionAlgorithm {
	
	private float q_h = 32;
    private float q_s = 8;
    private float q_v = 1;
    private int[] pixel; 
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		return sb.toString();
	}	
	
	@Override
	public List<List<Double>> extractFeatures(byte[] source) {			
		Image image = new Image(source); 
		
		List<List<Double>> data = new ArrayList<List<Double>>();
		
		List<Double> descriptor = new ArrayList<Double>();
		
		pixel = new int[3];
        int values[] = (new int[(int) (q_h * q_s * q_v)]);
				
		BufferedImage bimage = image.getBufferedImage();
        if (bimage.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
            throw new UnsupportedOperationException("Color space not supported. Only RGB.");
        WritableRaster raster = bimage.getRaster();
        int count = 0;
        for (int x = 0; x < bimage.getWidth() - 40; x++) {
            for (int y = 0; y < bimage.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
                values[quant(pixel)]++;
                count++;
            }
        }	 
        
        for(int i=0 ; i<values.length ; i++)
            descriptor.add((double)values[i]);
        
        data.add(descriptor);
		return data;
	}
	
	private int quant(int[] pixel) {
	//      int qH = (int) Math.floor((pixel[0] * 64f) / 360f);    // more granularity in color
	//      int qS = (int) Math.floor((pixel[2] * 8f) / 100f);
	//      return qH * 7 + qS;
	  int qH = (int) Math.floor((pixel[0] * q_h) / 360f);    // more granularity in color
	  int qS = (int) Math.floor((pixel[2] * q_s) / 100f);
	  int qV = (int) Math.floor((pixel[1] * q_v) / 100f);
	  if (qH == q_h) qH = (int) (q_h - 1);
	  if (qS == q_s) qS = (int) (q_s - 1);
	  if (qV == q_v) qV = (int) (q_v - 1);
	  return (qH) * (int) (q_v * q_s) + qS * (int) q_v + qV;
	}

	public static void rgb2hsv(int r, int g, int b, int hsv[]) {
	
	  int min;    //Min. value of RGB
	  int max;    //Max. value of RGB
	  int delMax; //Delta RGB value
	
	  min = Math.min(r, g);
	  min = Math.min(min, b);
	
	  max = Math.max(r, g);
	  max = Math.max(max, b);
	
	  delMax = max - min;
	
	//  System.out.println("hsv = " + hsv[0] + ", " + hsv[1] + ", "  + hsv[2]);
	
	  float H = 0f, S = 0f;
	  float V = max / 255f;
	
	  if (delMax == 0) {
	      H = 0f;
	      S = 0f;
	  } else {
	      S = delMax / 255f;
	      if (r == max) {
	          if (g >= b) {
	              H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60;
	          } else {
	              H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60 + 360;
	          }
	      } else if (g == max) {
	          H = (2 + (b / 255f - r / 255f) / (float) delMax / 255f) * 60;
	      } else if (b == max) {
	          H = (4 + (r / 255f - g / 255f) / (float) delMax / 255f) * 60;
	      }
	  }
	//  System.out.println("H = " + H);
	  hsv[0] = (int) (H);
	  hsv[1] = (int) (S * 100);
	  hsv[2] = (int) (V * 100);
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
