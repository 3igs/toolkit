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
import bigs.modules.fe.global.lire.fcth.FCTHQuant;
import bigs.modules.fe.global.lire.fcth.Fuzzy10Bin;
import bigs.modules.fe.global.lire.fcth.Fuzzy24Bin;
import bigs.modules.fe.global.lire.fcth.FuzzyFCTHpart;
import bigs.modules.fe.global.lire.fcth.RGB2HSV;
import bigs.modules.fe.global.lire.fcth.WaveletMatrixPlus;
import bigs.modules.fe.utils.*;


/**
 * LireFCTH.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	LireFCTH.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class LireFCTH extends FeatureExtractionAlgorithm {
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
		
		BufferedImage bimage = image.getBufferedImage();
        Fuzzy10Bin Fuzzy10 = new Fuzzy10Bin(false);
        Fuzzy24Bin Fuzzy24 = new Fuzzy24Bin(false);
        FuzzyFCTHpart FuccyFCTH = new FuzzyFCTHpart();


        double[] Fuzzy10BinResultTable = new double[10];
        double[] Fuzzy24BinResultTable = new double[24];
        double[] FuzzyHistogram192 = new double[192];


        int Method = 2;
        int width = bimage.getWidth();
        int height = bimage.getHeight();


        for (int R = 0; R < 192; R++) {
            FuzzyHistogram192[R] = 0;

        }


        RGB2HSV HSVConverter = new RGB2HSV();
        int[] HSV = new int[3];

        WaveletMatrixPlus Matrix = new WaveletMatrixPlus();


        double[][] ImageGrid = new double[width][height];
        int[][] ImageGridRed = new int[width][height];
        int[][] ImageGridGreen = new int[width][height];
        int[][] ImageGridBlue = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bimage.getRGB(x, y);
                ImageGridRed[x][y] = (pixel >> 16) & 0xff;
                ImageGridGreen[x][y] = (pixel >> 8) & 0xff;
                ImageGridBlue[x][y] = (pixel) & 0xff;

                int mean = (int) (0.114 * ImageGridBlue[x][y] + 0.587 * ImageGridGreen[x][y] + 0.299 * ImageGridRed[x][y]);
                ImageGrid[x][y] = mean;
            }
        }


        int NumberOfBlocks = 1600;
        int Step_X = (int) Math.floor(width / Math.sqrt(NumberOfBlocks));
        int Step_Y = (int) Math.floor(height / Math.sqrt(NumberOfBlocks));

        if ((Step_X % 2) != 0) {
            Step_X = Step_X - 1;
        }
        if ((Step_Y % 2) != 0) {
            Step_Y = Step_Y - 1;
        }


        if (Step_Y < 4) Step_Y = 4;
        if (Step_X < 4) Step_X = 4;
        ///
        // Filter

        for (int y = 0; y < height - Step_Y; y += Step_Y) {
            for (int x = 0; x < width - Step_X; x += Step_X) {
                //int[][] BinaryBlock = new int[4][4];
                double[][] Block = new double[4][4];
                int[][] BlockR = new int[4][4];
                int[][] BlockG = new int[4][4];
                int[][] BlockB = new int[4][4];
                int[][] BlockCount = new int[4][4];

                int[] CororRed = new int[Step_Y * Step_X];
                int[] CororGreen = new int[Step_Y * Step_X];
                int[] CororBlue = new int[Step_Y * Step_X];

                int[] CororRedTemp = new int[Step_Y * Step_X];
                int[] CororGreenTemp = new int[Step_Y * Step_X];
                int[] CororBlueTemp = new int[Step_Y * Step_X];

                int MeanRed = 0;
                int MeanGreen = 0;
                int MeanBlue = 0;
                int CurrentPixelX = 0;
                int CurrentPixelY = 0;
                for (int i = 0; i < 4; i++) {

                    for (int j = 0; j < 4; j++) {
                        Block[i][j] = 0;
                        BlockCount[i][j] = 0;
                    }
                }
                //#endregion

                int TempSum = 0;
                for (int i = 0; i < Step_X; i++) {
                    for (int j = 0; j < Step_Y; j++) {
                        CurrentPixelX = 0;
                        CurrentPixelY = 0;

                        if (i >= (Step_X / 4)) CurrentPixelX = 1;
                        if (i >= (Step_X / 2)) CurrentPixelX = 2;
                        if (i >= (3 * Step_X / 4)) CurrentPixelX = 3;

                        if (j >= (Step_Y / 4)) CurrentPixelY = 1;
                        if (j >= (Step_Y / 2)) CurrentPixelY = 2;
                        if (j >= (3 * Step_Y / 4)) CurrentPixelY = 3;

                        Block[CurrentPixelX][CurrentPixelY] += ImageGrid[x + i][y + j];
                        BlockCount[CurrentPixelX][CurrentPixelY]++;

                        BlockR[CurrentPixelX][CurrentPixelY] = ImageGridRed[x + i][y + j];
                        BlockG[CurrentPixelX][CurrentPixelY] = ImageGridGreen[x + i][y + j];
                        BlockB[CurrentPixelX][CurrentPixelY] = ImageGridBlue[x + i][y + j];

                        CororRed[TempSum] = BlockR[CurrentPixelX][CurrentPixelY];
                        CororGreen[TempSum] = BlockG[CurrentPixelX][CurrentPixelY];
                        CororBlue[TempSum] = BlockB[CurrentPixelX][CurrentPixelY];

                        CororRedTemp[TempSum] = BlockR[CurrentPixelX][CurrentPixelY];
                        CororGreenTemp[TempSum] = BlockG[CurrentPixelX][CurrentPixelY];
                        CororBlueTemp[TempSum] = BlockB[CurrentPixelX][CurrentPixelY];


                        TempSum++;
                    }
                }


                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        Block[i][j] = Block[i][j] / BlockCount[i][j];
                    }
                }

                Matrix = singlePassThreshold(Block, 1);


                for (int i = 0; i < (Step_Y * Step_X); i++) {
                    MeanRed += CororRed[i];
                    MeanGreen += CororGreen[i];
                    MeanBlue += CororBlue[i];
                }

                MeanRed = (int) (MeanRed / (Step_Y * Step_X));
                MeanGreen = (int) (MeanGreen / (Step_Y * Step_X));
                MeanBlue = (int) (MeanBlue / (Step_Y * Step_X));


                HSV = HSVConverter.ApplyFilter(MeanRed, MeanGreen, MeanBlue);

                if (Compact == false) {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], Method);
                    Fuzzy24BinResultTable = Fuzzy24.ApplyFilter(HSV[0], HSV[1], HSV[2], Fuzzy10BinResultTable, Method);
                    FuzzyHistogram192 = FuccyFCTH.ApplyFilter(Matrix.F3, Matrix.F2, Matrix.F1, Fuzzy24BinResultTable, Method, 24);

                } else {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], Method);
                    FuzzyHistogram192 = FuccyFCTH.ApplyFilter(Matrix.F3, Matrix.F2, Matrix.F1, Fuzzy10BinResultTable, Method, 10);

                }

            }


        }

        // end of the filter
        double TotalSum = 0;

        for (int i = 0; i < 192; i++) {


            TotalSum += FuzzyHistogram192[i];


        }

        for (int i = 0; i < 192; i++) {


            FuzzyHistogram192[i] = FuzzyHistogram192[i] / TotalSum;


        }

        FCTHQuant Quant = new FCTHQuant();
        FuzzyHistogram192 = Quant.Apply(FuzzyHistogram192);

        for(int i=0 ; i<FuzzyHistogram192.length ; i++)
            descriptor.add((double)FuzzyHistogram192[i]);		 
		
        data.add(descriptor);
		return data;
	}
	
	private WaveletMatrixPlus singlePassThreshold(double[][] inputMatrix, int level) {

        WaveletMatrixPlus TempMatrix = new WaveletMatrixPlus();
        level = (int) Math.pow(2.0, level - 1);

        //GETLENGTH*************
        double[][] resultMatrix = new double[inputMatrix.length][inputMatrix[0].length];

        int xOffset = inputMatrix.length / 2 / level;

        int yOffset = inputMatrix[0].length / 2 / level;

        int currentPixel = 0;

        //double size = inputMatrix.length * inputMatrix[0].length;

        double multiplier = 0;


        for (int y = 0; y < inputMatrix[0].length; y++) {

            for (int x = 0; x < inputMatrix.length; x++) {

                if ((y < inputMatrix[0].length / 2 / level) && (x < inputMatrix.length / 2 / level)) {

                    currentPixel++;

                    resultMatrix[x][y] = (inputMatrix[2 * x][2 * y] + inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] + inputMatrix[2 * x + 1][2 * y + 1]) / 4;

                    double vertDiff = (-inputMatrix[2 * x][2 * y] - inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] + inputMatrix[2 * x + 1][2 * y + 1]);

                    double horzDiff = (inputMatrix[2 * x][2 * y] - inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] - inputMatrix[2 * x + 1][2 * y + 1]);

                    double diagDiff = (-inputMatrix[2 * x][2 * y] + inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] - inputMatrix[2 * x + 1][2 * y + 1]);


                    resultMatrix[x + xOffset][y] = (int) (byte) (multiplier + Math.abs(vertDiff));

                    resultMatrix[x][y + yOffset] = (int) (byte) (multiplier + Math.abs(horzDiff));

                    resultMatrix[x + xOffset][y + yOffset] = (int) (byte) (multiplier + Math.abs(diagDiff));


                } else {

                    if ((x >= inputMatrix.length / level) || (y >= inputMatrix[0].length / level))

                    {
                        resultMatrix[x][y] = inputMatrix[x][y];
                    }

                }

            }

        }

        double Temp1 = 0;
        double Temp2 = 0;
        double Temp3 = 0;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Temp1 += 0.25 * Math.pow(resultMatrix[2 + i][j], 2);
                Temp2 += 0.25 * Math.pow(resultMatrix[i][2 + j], 2);
                Temp3 += 0.25 * Math.pow(resultMatrix[2 + i][2 + j], 2);
            }

        }

        //double[] MatrixResults = new double[4];

        TempMatrix.F1 = Math.sqrt(Temp1);
        TempMatrix.F2 = Math.sqrt(Temp2);
        TempMatrix.F3 = Math.sqrt(Temp3);

        TempMatrix.Entropy = 0;

        return TempMatrix;

    }

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
