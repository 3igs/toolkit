package bigs.modules.fe.global;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
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
import bigs.modules.fe.global.mpeg7.ColorLayout;
import bigs.modules.fe.utils.*;


/**
 * MPEG7ColorLayout.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	MPEG7ColorLayout.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class MPEG7ColorLayout extends FeatureExtractionAlgorithm {
	// TODO: aacruzr: This feature is not ready to use it
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
		
		List<Double> YCoeff = new ArrayList<Double>();
		List<Double> CbCoeff = new ArrayList<Double>();
		List<Double> CrCoeff = new ArrayList<Double>();
        ColorLayout CL = new ColorLayout(image.getBufferedImage());
        
        for(int i=0 ; i<CL.getYCoeff().length ; i++)
        	YCoeff.add((double)CL.getYCoeff()[i]);
        for(int i=0 ; i<CL.getCbCoeff().length ; i++)
        	CbCoeff.add((double)CL.getCbCoeff()[i]);
        for(int i=0 ; i<CL.getCrCoeff().length ; i++)
        	CrCoeff.add((double)CL.getCrCoeff()[i]);        
        
        data.add(YCoeff);
        data.add(CbCoeff);
        data.add(CrCoeff);
        
        //BufferedImage imgaux = ImageUtil.scaleToSize(100, 100, CL.getColorLayoutImage());
        //data.setImageMatrix(imgaux);        
		return data;
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
