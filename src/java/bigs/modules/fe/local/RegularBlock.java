package bigs.modules.fe.local;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.modules.fe.utils.*;


/**
 * RegularBlock.java
 * DescriptionClass
 * bigs
 * @created		Created on 26 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 26/03/2012	RegularBlock.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class RegularBlock extends FeatureExtractionAlgorithm {

	@BIGSParam	  
    public Integer blockSize = 9;
	
	@BIGSParam
    public Integer overlapSize = 0;   
	
	@BIGSParam
	public Integer maxImageSize = 512;
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		return sb.toString();
	}	
	
	@Override
	public List<List<Double>> extractFeatures(byte[] source) {			
		Image image = new Image(source); 				
		
        ImageProcessor ip1 = image.getImageData().getProcessor().convertToFloat();
        ImagePlus ip = image.getImageData();

        int imgWidth = ip1.getWidth();
        int imgHeight = ip1.getHeight();
        int imgReWidth = 0;
        int imgReHeight = 0;
        int mayor = 0;
        float aspectRatio;

        if (imgWidth > imgHeight) {
            mayor = imgWidth;
        } else {
            mayor = imgHeight;
        }
        
        // Rescaling whether is image is bigger than maxImageSize
        if (mayor != maxImageSize) {
            aspectRatio = ((float) imgHeight) / ((float) imgWidth);
            if (imgWidth > imgHeight) {
                imgReWidth = maxImageSize;
                imgReHeight = (int) (maxImageSize * aspectRatio);
            } else {
                imgReHeight = maxImageSize;
                imgReWidth = (int) (maxImageSize / aspectRatio);
            }

            ip.setImage(ip1.resize(imgReWidth, imgReHeight).createImage());
        }        

        return getBlocks(ip.getProcessor());
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}
	
	public List<List<Double>> getBlocks(ImageProcessor ip1) {
		List<Double> vector = new ArrayList<Double>();
		List<List<Double>> matrix = new ArrayList<List<Double>>();

        int imgH = ip1.getHeight();
        int imgW = ip1.getWidth();
        int aux[] = new int[blockSize * blockSize];
        int nblocks = 0;

        int y = 0;
        
        while (y < imgH) {
            int x = 0;
            while (x < imgW) {
                int it = 0;
                for (int j = y; j < y + blockSize; j++) {
                    for (int i = x; i < x + blockSize; i++) {
                        aux[it] = ip1.getPixel(i, j);
                        it++;
                    }
                }                
                
                vector.clear();
                
                /*int min = 255;
                int max = 0;
                for (int i = 0; i < it; i++) {
                    if (aux[i] < min) {
                        min = aux[i];
                    }
                    if (aux[i] > max) {
                        max = aux[i];
                    }                    
                }*/
                
                // 
                for (int i = 0; i < it; i++) {
                	vector.add((double)aux[i]);
                }
                
                matrix.add(vector);
                nblocks++;
                x = x + blockSize + overlapSize;
            }
            y = y + blockSize + overlapSize;
        }

        return matrix;
    }
	
	
}
