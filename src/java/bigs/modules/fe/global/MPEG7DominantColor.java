package bigs.modules.fe.global;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.ImageProcessor;

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
import bigs.modules.fe.global.mpeg7.DominantColor;
import bigs.modules.fe.utils.*;


/**
 * MPEG7DominantColor.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	MPEG7DominantColor.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class MPEG7DominantColor extends FeatureExtractionAlgorithm {
	
	private int SpatialCoherency;
	
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
				
		DominantColor DC = new DominantColor(image.getBufferedImage());
        SpatialCoherency = DC.getSpatialCoherency();
        int cv[];
        int pc[];
        pc = new int[DC.getSize()];
        
        for (int i = 0; i < DC.getSize(); i++) {
            pc[i] = DC.getPercentage(i);
            cv = DC.getColorValue(i);

            for(int j=0 ; j<cv.length ; j++)
                descriptor.add((double)cv[j]);
        }
        data.add(descriptor);
        return data;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
