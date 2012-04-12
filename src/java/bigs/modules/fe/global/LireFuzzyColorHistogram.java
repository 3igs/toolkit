package bigs.modules.fe.global;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.ImageProcessor;

import java.awt.Color;
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
 * LireFuzzyColorHistogram.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	LireFuzzyColorHistogram.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class LireFuzzyColorHistogram extends FeatureExtractionAlgorithm {
	
	protected Color[] binColors;
	
	protected final int SIZE = 5;
    protected final int SIZE3 = SIZE * SIZE * SIZE;

    public boolean Compact = false;
	
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
		
		BufferedImage bimg = image.getBufferedImage();
        binColors = new Color[SIZE3];

        int counter = 0;
        for (int k = 0; k < SIZE; k++) {
            for (int j = 0; j < SIZE; j++) {
                for (int i = 0; i < SIZE; i++) {
                    binColors[counter] = getColorForBin(i, j, k);
                    counter++;
                }
            }
        }

        double[] histogramA = new double[SIZE3];

        int width = bimg.getWidth();
        int height = bimg.getHeight();

        WritableRaster raster = bimg.getRaster();
        int[] pixel = new int[3];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                raster.getPixel(i, j, pixel);
                int r = pixel[0];
                int g = pixel[1];
                int b = pixel[2];


                for (int k = 0; k < SIZE3; k++) {
                    double rDiff = (double) (binColors[k].getRed()) - r;
                    double gDiff = (double) (binColors[k].getGreen()) - g;
                    double bDiff = (double) (binColors[k].getBlue()) - b;
                    double rdist = rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
                    histogramA[k] += (10.0 / Math.sqrt(rdist + 1));
                }
            }
        }

        double maxA = 0;
        for (int k = 0; k < SIZE3; k++) {
            if (histogramA[k] > maxA)
                maxA = histogramA[k];
        }

        //data.setValues(new int[SIZE3]);
        for (int k = 0; k < SIZE3; k++)
        	descriptor.add((double)((int) (histogramA[k] / maxA * 255)));
        
        data.add(descriptor);
		return data;
	}
	
	protected Color getColorForBin(int rBin, int gBin, int bBin) {
        // work out the width of each bin and place the color in the middle
        int binWidth = 256 / SIZE;
        int offset = binWidth / 2;

        return new Color(rBin * binWidth + offset, gBin * binWidth + offset, bBin * binWidth + offset);
    }

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
