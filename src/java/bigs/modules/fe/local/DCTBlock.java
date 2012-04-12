package bigs.modules.fe.local;

import ij.*;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.modules.fe.utils.DCT;
import bigs.modules.fe.utils.Image;


/**
 * DCTBlock.java
 * DescriptionClass
 * bigs
 * @created		30/03/2012
 * @author      javanegasr
 * @history
 * 11/01/2012	DCTBlock.java
 * @copyright 	Copyright 2012 (c) Bioingenium Research Group
 */
public class DCTBlock extends FeatureExtractionAlgorithm {

	@BIGSParam
    public Integer blocksSize = 8;
	
	@BIGSParam
	public Integer overlapSize = 4;
	
	@BIGSParam
	public Integer numberOfCoefficients = 21;
	
	@BIGSParam
	public Integer maxImageSize = 256;   
	
	@Override
	public List<List<Double>> extractFeatures(byte[] source) {
		return performExtraction(new Image(source));
	}
    
    /**
    *
    * @return dct blocks
    */
   public List<List<Double>> performExtraction(Image image) {
       ImagePlus ipl = image.getImageData();
       ImageProcessor ipr = ipl.getProcessor();

       int imgWidth = ipr.getWidth();
       int imgHeight = ipr.getHeight();
       float aspectRatio;

       int lower = (imgWidth < imgHeight) ? imgWidth : imgHeight;

       if (lower > maxImageSize) {
           int imgReWidth;
           int imgReHeight;
           aspectRatio = ((float) imgHeight) / ((float) imgWidth);
           if (imgWidth < imgHeight) {
               imgReWidth = maxImageSize;
               imgReHeight = (int) (maxImageSize * aspectRatio);
           } else {
               imgReHeight = maxImageSize;
               imgReWidth = (int) (maxImageSize / aspectRatio);
           }
           ipl.setImage(ipr.resize(imgReWidth, imgReHeight).createImage());
       }

       return getBlocks(ipl.getProcessor());
   }	
   
   /**
    * Perform DCT feature extraction from an image splitted in a regular grid
    * @param ip1
    * @return
    */
   public List<List<Double>> getBlocks(ImageProcessor ip) {
       

       int imgH = ip.getHeight();
       int imgW = ip.getWidth();

       int offsetX = (int)(((imgW - blocksSize) % overlapSize) / 2f);
       int offsetY = (int)(((imgH - blocksSize) % overlapSize) / 2f);
       
       DCT dct = new DCT(blocksSize);
       
       List<List<Double>> data = new ArrayList<List<Double>>();

       float[][][] block = new float[3][blocksSize][blocksSize];

       // image processing
       for (int y = offsetY; y <= imgH - blocksSize; y = y + overlapSize) {
           for (int x = offsetX; x <= imgW - blocksSize; x = x + overlapSize) {

               // block processing
               for (int j = 0; j < blocksSize; j++) {
                   for (int i = 0; i < blocksSize; i++) {
                       int[] rgb = new int[3];
                       ip.getPixel(i + x, j + y, rgb);
                       for (int channel = 0; channel < 3; channel++) {
                           block[channel][j][i] = rgb[channel]/255f;
                       }
                   }
               }

               //2D DCT transform for each color channel
               double[][][] dctMatrix = {
                   dct.forwardDCT(block[0]),
                   dct.forwardDCT(block[1]),
                   dct.forwardDCT(block[2])
               };

               double[][] coefficients = new double[3][blocksSize * blocksSize];
               double[][] tmp = new double[3][blocksSize * blocksSize];
               for (int channel = 0; channel < 3; channel++) {
                   for (int i = 0; i < blocksSize; i++){
                       for (int j = 0; j < blocksSize; j++){
                           coefficients[channel][j * blocksSize + i] = 
                                   dctMatrix[channel][i][j];
                           tmp[channel][j * blocksSize + i] = 
                                   dctMatrix[channel][i][j];
                       }                        
                   }
               }

               // use only the firsts 21 coefficients
               int blocksize2 = blocksSize * blocksSize;                   
               int testPoint = blocksize2 - numberOfCoefficients;
               
               for (int channel = 0; channel < 3; channel++) {
                   Arrays.sort(tmp[channel]);  
                   for (int i = 0; i < coefficients[0].length; i++) {
                       if (coefficients[channel][i] < 
                               tmp[channel][testPoint]) {
                           coefficients[channel][i] = 0;
                       }
                   }
               }

               // join all channels in just one feature vector
               double[] features = new double[3 * blocksize2];
               for (int channel = 0; channel < 3; channel++) {
                   System.arraycopy(coefficients[channel], 0, 
                           features, blocksize2 * channel, blocksize2);
               }
               
       		List<Double> descriptor = new ArrayList<Double>();

               for(int i = 0 ; i < features.length ; i++)
                   descriptor.add((double)features[i]);		
               data.add(descriptor);
               
           }
       }

       return data;
   }

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
