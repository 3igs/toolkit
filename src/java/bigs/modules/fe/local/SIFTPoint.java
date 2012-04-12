package bigs.modules.fe.local;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import mpi.cbg.fly.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.util.Hashtable;
import java.awt.Polygon;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.modules.fe.utils.Image;


/**
 * SIFTPoint.java
 * DescriptionClass
 * bigs
 * @created		Created on 30 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 30/03/2012	SIFTPoint.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class SIFTPoint extends FeatureExtractionAlgorithm {
	
	@BIGSParam
    public Integer steps = 1;   
	
	@BIGSParam
	public Integer maxImageSize = 512;
	
	private Float initial_sigma = 1.6f; // initial sigma
    
	@BIGSParam
    public Integer fdsize = 4;	// feature descriptor size
    
	@BIGSParam
    public Integer fdbins = 8;	// feature descriptor orientation bins
    
	@BIGSParam
    public Integer min_size = 64; // size restrictions for scale octaves, use octaves < max_size and > min_size only
	
	@BIGSParam
    public Integer max_size = 512; // size restrictions for scale octaves, use octaves < max_size and > min_size only 
	
    /**
     * Set true to double the size of the image by linear interpolation to
     * ( with * 2 + 1 ) * ( height * 2 + 1 ).  Thus we can start identifying
     * DoG extrema with $\sigma = INITIAL_SIGMA / 2$ like proposed by
     * \citet{Lowe04}.
     *
     * This is useful for images scmaller than 1000px per side only.
     */
    public Boolean upscale = true;
    //private static float scale = 1.0f;
    //private static boolean existFeatures = false;
    
    private Hashtable<String, MyInteger> countingTable;
    
    /**
     * It is better to load all keys in memory rather than overcharging the processor
     * calculating them everytime that a new object is constructed
     */
    static private String[] allKeys = {"00000000", "00000001", "00000010", "00000011", "00000100", "00000101", "00000110", "00000111", "00001000", "00001001", "00001010", "00001011", "00001100", "00001101", "00001110", "00001111", "00010000", "00010001", "00010010", "00010011", "00010100", "00010101", "00010110", "00010111", "00011000", "00011001", "00011010", "00011011", "00011100", "00011101", "00011110", "00011111", "00100000", "00100001", "00100010", "00100011", "00100100", "00100101", "00100110", "00100111", "00101000", "00101001", "00101010", "00101011", "00101100", "00101101", "00101110", "00101111", "00110000", "00110001", "00110010", "00110011", "00110100", "00110101", "00110110", "00110111", "00111000", "00111001", "00111010", "00111011", "00111100", "00111101", "00111110", "00111111", "01000000", "01000001", "01000010", "01000011", "01000100", "01000101", "01000110", "01000111", "01001000", "01001001", "01001010", "01001011", "01001100", "01001101", "01001110", "01001111", "01010000", "01010001", "01010010", "01010011", "01010100", "01010101", "01010110", "01010111", "01011000", "01011001", "01011010", "01011011", "01011100", "01011101", "01011110", "01011111", "01100000", "01100001", "01100010", "01100011", "01100100", "01100101", "01100110", "01100111", "01101000", "01101001", "01101010", "01101011", "01101100", "01101101", "01101110", "01101111", "01110000", "01110001", "01110010", "01110011", "01110100", "01110101", "01110110", "01110111", "01111000", "01111001", "01111010", "01111011", "01111100", "01111101", "01111110", "01111111", "10000000", "10000001", "10000010", "10000011", "10000100", "10000101", "10000110", "10000111", "10001000", "10001001", "10001010", "10001011", "10001100", "10001101", "10001110", "10001111", "10010000", "10010001", "10010010", "10010011", "10010100", "10010101", "10010110", "10010111", "10011000", "10011001", "10011010", "10011011", "10011100", "10011101", "10011110", "10011111", "10100000", "10100001", "10100010", "10100011", "10100100", "10100101", "10100110", "10100111", "10101000", "10101001", "10101010", "10101011", "10101100", "10101101", "10101110", "10101111", "10110000", "10110001", "10110010", "10110011", "10110100", "10110101", "10110110", "10110111", "10111000", "10111001", "10111010", "10111011", "10111100", "10111101", "10111110", "10111111", "11000000", "11000001", "11000010", "11000011", "11000100", "11000101", "11000110", "11000111", "11001000", "11001001", "11001010", "11001011", "11001100", "11001101", "11001110", "11001111", "11010000", "11010001", "11010010", "11010011", "11010100", "11010101", "11010110", "11010111", "11011000", "11011001", "11011010", "11011011", "11011100", "11011101", "11011110", "11011111", "11100000", "11100001", "11100010", "11100011", "11100100", "11100101", "11100110", "11100111", "11101000", "11101001", "11101010", "11101011", "11101100", "11101101", "11101110", "11101111", "11110000", "11110001", "11110010", "11110011", "11110100", "11110101", "11110110", "11110111", "11111000", "11111001", "11111010", "11111011", "11111100", "11111101", "11111110", "11111111"};
    
    
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

        return getSIFTFeatures(ip.getProcessor());
	}

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}	
	
    public List<List<Double>> getSIFTFeatures(ImageProcessor ip1) {
    	List<Double> vector = new ArrayList<Double>();
		List<List<Double>> siftHistogram = new ArrayList<List<Double>>();
		
        Vector<mpi.cbg.fly.Feature> fs1;

        FloatArray2DSIFT sift = new FloatArray2DSIFT((int)fdsize, (int)fdbins);
        //ImagePlus ip.setImage(ip1.)                
        FloatArray2D fa = ImageArrayConverter.ImageToFloatArray2D(ip1);
        mpi.cbg.fly.Filter.enhance(fa, 1.0f);

        if (upscale) {
            FloatArray2D fat = new FloatArray2D(fa.width * 2 - 1, fa.height * 2 - 1);
            FloatArray2DScaleOctave.upsample(fa, fat);
            fa = fat;
            fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 1.0));
        } else {
            fa = Filter.computeGaussianFastMirror(fa, (float) Math.sqrt(initial_sigma * initial_sigma - 0.25));
        }
        sift.init(fa, (int) steps, (float) initial_sigma, (int) min_size, (int) max_size);
        fs1 = sift.run(max_size);
        Collections.sort(fs1);

        float positionsX[] = new float[fs1.size()];
        float positionsY[] = new float[fs1.size()];
        float scales[] = new float[fs1.size()];
        float orientations[] = new float[fs1.size()];


        int i = 0;
        //existFeatures = false;
        int nfea = 0;

        for (mpi.cbg.fly.Feature f : fs1) {
            nfea = nfea + 1;
            if (i < fs1.size()) {
                positionsX[i] = f.location[ 0];	//x
                positionsY[i] = f.location[ 1];	//y
                scales[i] = f.scale;			//scale
                orientations[i] = f.orientation;	//orientation

                vector.clear();
                for (int j=0;j<f.descriptor.length;j++){
                	vector.add((double)f.descriptor[j]);
                }
                
                siftHistogram.add(vector);
                i++;
            }
        }
        
        return siftHistogram;
    }
	
    static void drawSquare(ImageProcessor ip, double[] o, double scale, double orient) {
        scale /= 2;

        double sin = Math.sin(orient);
        double cos = Math.cos(orient);

        int[] x = new int[6];
        int[] y = new int[6];


        x[ 0] = (int) (o[ 0] + (sin - cos) * scale);
        y[ 0] = (int) (o[ 1] - (sin + cos) * scale);

        x[ 1] = (int) o[ 0];
        y[ 1] = (int) o[ 1];

        x[ 2] = (int) (o[ 0] + (sin + cos) * scale);
        y[ 2] = (int) (o[ 1] + (sin - cos) * scale);
        x[ 3] = (int) (o[ 0] - (sin - cos) * scale);
        y[ 3] = (int) (o[ 1] + (sin + cos) * scale);
        x[ 4] = (int) (o[ 0] - (sin + cos) * scale);
        y[ 4] = (int) (o[ 1] - (sin - cos) * scale);
        x[ 5] = x[ 0];
        y[ 5] = y[ 0];

        ip.drawPolygon(new Polygon(x, y, x.length));
    }
    
    /**
     * Transforms the hash table in a single array of integers
     * @return An object of class SIFT, that is a Feature of an Image
     */
    /**
     * Initialize the hashtable in order to reuse it
     * */
    public void resetCountingTable() {
        for (int x = 0; x < 128; x++) {
            countingTable.get(allKeys[x]).setValue(0);
        }
    }

    /**
     * This class was created because the original class Integer hasn't an add or increment method When I access this numbers in the Hashtable, I need to increment it directly, I don't want to extract it, to increment it alone and to put it again overcalculating hash functions...
     */
    class MyInteger {

        private int value;

        public MyInteger() {
            value = 0;
        }

        public void increment(int rate) {
            value += rate;
        }

        /**
         * @return  the value
         * @uml.property  name="value"
         */
        public int getValue() {
            return value;
        }

        /**
         * @param value  the value to set
         * @uml.property  name="value"
         */
        public void setValue(int i) {
            value = i;
        }
    }
	
}
