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
import bigs.modules.fe.utils.*;


/**
 * SobelHistogram.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author 		jccaicedo
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	SobelHistogram.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class SobelHistogram extends FeatureExtractionAlgorithm {
	
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
		int width = image.getColorImage().getWidth();
		int height = image.getColorImage().getHeight();		
		
		List<List<Double>> data = new ArrayList<List<Double>>();
				
		ImagePlus iPlus = NewImage.createImage("SobelImage", width, height, 1, 24, NewImage.FILL_BLACK);
        ImageProcessor imageProcessor = iPlus.getProcessor();
        imageProcessor.copyBits(image.getColorImage(), 0, 0, Blitter.COPY);
        //The following method uses a Sobel operator (according to ImageJ documentation)
        imageProcessor.findEdges();
        //if (isShowingResult()) {
        //    iPlus.show();
        //}
        
        List<Double> descriptor = RGBHistogram.calculateHistogram((int[]) imageProcessor.getPixels(), width, height, rangeSize);		 
		
        data.add(descriptor);
		return data;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
