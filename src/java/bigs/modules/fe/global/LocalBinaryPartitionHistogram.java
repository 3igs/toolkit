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
import java.util.Hashtable;
import java.util.List;

import bigs.api.core.Algorithm;
import bigs.api.core.BIGSParam;
import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.api.utils.TextUtils;
import bigs.modules.fe.utils.*;


/**
 * LocalBinaryPartitionHistogram.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author 		jccaicedo
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	LocalBinaryPartitionHistogram.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class LocalBinaryPartitionHistogram extends FeatureExtractionAlgorithm {
	
	private Hashtable<String, MyInteger> countingTable;
	private int width;
    private int height;
	static private String[] allKeys = {"00000000", "00000001", "00000010", "00000011", "00000100", "00000101", "00000110", "00000111", "00001000", "00001001", "00001010", "00001011", "00001100", "00001101", "00001110", "00001111", "00010000", "00010001", "00010010", "00010011", "00010100", "00010101", "00010110", "00010111", "00011000", "00011001", "00011010", "00011011", "00011100", "00011101", "00011110", "00011111", "00100000", "00100001", "00100010", "00100011", "00100100", "00100101", "00100110", "00100111", "00101000", "00101001", "00101010", "00101011", "00101100", "00101101", "00101110", "00101111", "00110000", "00110001", "00110010", "00110011", "00110100", "00110101", "00110110", "00110111", "00111000", "00111001", "00111010", "00111011", "00111100", "00111101", "00111110", "00111111", "01000000", "01000001", "01000010", "01000011", "01000100", "01000101", "01000110", "01000111", "01001000", "01001001", "01001010", "01001011", "01001100", "01001101", "01001110", "01001111", "01010000", "01010001", "01010010", "01010011", "01010100", "01010101", "01010110", "01010111", "01011000", "01011001", "01011010", "01011011", "01011100", "01011101", "01011110", "01011111", "01100000", "01100001", "01100010", "01100011", "01100100", "01100101", "01100110", "01100111", "01101000", "01101001", "01101010", "01101011", "01101100", "01101101", "01101110", "01101111", "01110000", "01110001", "01110010", "01110011", "01110100", "01110101", "01110110", "01110111", "01111000", "01111001", "01111010", "01111011", "01111100", "01111101", "01111110", "01111111", "10000000", "10000001", "10000010", "10000011", "10000100", "10000101", "10000110", "10000111", "10001000", "10001001", "10001010", "10001011", "10001100", "10001101", "10001110", "10001111", "10010000", "10010001", "10010010", "10010011", "10010100", "10010101", "10010110", "10010111", "10011000", "10011001", "10011010", "10011011", "10011100", "10011101", "10011110", "10011111", "10100000", "10100001", "10100010", "10100011", "10100100", "10100101", "10100110", "10100111", "10101000", "10101001", "10101010", "10101011", "10101100", "10101101", "10101110", "10101111", "10110000", "10110001", "10110010", "10110011", "10110100", "10110101", "10110110", "10110111", "10111000", "10111001", "10111010", "10111011", "10111100", "10111101", "10111110", "10111111", "11000000", "11000001", "11000010", "11000011", "11000100", "11000101", "11000110", "11000111", "11001000", "11001001", "11001010", "11001011", "11001100", "11001101", "11001110", "11001111", "11010000", "11010001", "11010010", "11010011", "11010100", "11010101", "11010110", "11010111", "11011000", "11011001", "11011010", "11011011", "11011100", "11011101", "11011110", "11011111", "11100000", "11100001", "11100010", "11100011", "11100100", "11100101", "11100110", "11100111", "11101000", "11101001", "11101010", "11101011", "11101100", "11101101", "11101110", "11101111", "11110000", "11110001", "11110010", "11110011", "11110100", "11110101", "11110110", "11110111", "11111000", "11111001", "11111010", "11111011", "11111100", "11111101", "11111110", "11111111"};
	
	public LocalBinaryPartitionHistogram() {        
        countingTable = new Hashtable<String, MyInteger>();
        for (int x = 0; x < 256; x++) {
            countingTable.put(allKeys[x], new MyInteger());
        }
    }
	
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
					
		resetCountingTable();

        width = image.getGrayImage().getWidth();
        height = image.getGrayImage().getHeight();
        byte[] pixels = (byte[]) image.getGrayImage().getPixels();

        int offset, index;
        String key;
        for (int y = 1; y < height - 1; y++) {
            offset = y * width;
            for (int x = 1; x < width - 1; x++) {
                index = offset + x;
                key = analyzeTexture(pixels, x, y, index);
                countingTable.get(key).increment(1);
            }
        }
        List<Double> descriptor = getTextureHistogram();		 
		        
        data.add(descriptor);
		return data;
	}
	
	/**
     * Transforms the hash table in a single array of integers
     * @return An object of class LocalBinaryPartition, that is a Feature of an Image
     */
    public List<Double> getTextureHistogram() {
    	List<Double> descriptor = new ArrayList<Double>();
        //Histogram lbp = new Histogram();
        int[] values = new int[256];
        for (int x = 0; x < 256; x++) {
            values[x] = countingTable.get(allKeys[x]).getValue();
        }
        
        for(int i=0 ; i<values.length ; i++)
            descriptor.add((double)values[i]);
        
        return descriptor;
    }
	
	/**
     * It check the neighborhood of a pixel and build a binary string in which each position has
     * 1 if its value is greater than the pixel and 0 if its value is lesser than the pixel
     * @param pixels The original array of pixels (Gray scale)
     * @param x The x coordinate of the pixel to be analyzed
     * @param y The y coordinate of the pixel to be analyzed
     * @param index The index position of the pixel in the array of pixels
     * @return
     */
	public String analyzeTexture(byte[] pixels, int x, int y, int index) {
        //Take into account  boundary conditions...
        int[] neighbors = {
            x - 1 + width * (y - 1),
            x + width * (y - 1),
            x + 1 + width * (y - 1),
            x - 1 + width * (y),
            x + 1 + width * (y),
            x - 1 + width * (y + 1),
            x + width * (y + 1),
            x + 1 + width * (y + 1)
        };

        int localValue = pixels[index];
        String binary = "";
        int temp;

        for (int pos = 0; pos < neighbors.length; pos++) {
            temp = pixels[neighbors[pos]];
            if (temp > localValue) {
                binary += "1";
            } else {
                binary += "0";
            }
        }
        return binary;
    }
	
	/**
     * Initialize the hashtable in order to reuse it
     * */
    public void resetCountingTable() {
        for (int x = 0; x < 255; x++) {
            countingTable.get(allKeys[x]).setValue(0);
        }
    }

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
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
