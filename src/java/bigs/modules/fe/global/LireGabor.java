package bigs.modules.fe.global;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.Blitter;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
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
import bigs.modules.fe.global.lire.ImageUtils;
import bigs.modules.fe.utils.*;


/**
 * LireGabor.java
 * DescriptionClass
 * bigs
 * @created		Created on 10 March of 2012
 * @author		aacruzr
 * @version 	%I%, %G%
 * @since 		1.5
 * @history
 * 06/03/2012	LireGabor.java
 * @copyright 	Copyright 2007-2012 (c) BioIngenium Research Group - Universidad Nacional de Colombia
 */

public class LireGabor extends FeatureExtractionAlgorithm {
	
	private double[][][][][] gaborWavelet = null;
    private double U_H = .4;
    private double U_L = .05;
    private int M = 5;
    private int N = 6;
    private double A = Math.pow((U_H / U_L), 1. / (M - 1));
    private int MAX_IMG_WIDTH = 64;
    private int MAX_IMG_HEIGHT = 64;
    private double[] sigma_x = new double[M];
    private double[] sigma_y = new double[M];
    private int S = 4;
    private int T = 4;
    private double[] theta = new double[N];
    private double[] modulationFrequency = new double[M];
    private double[][][][][] selfSimilarGaborWavelets = new double[S][T][M][N][2];
    private final double LOG2 = Math.log(2);
    
    public LireGabor() {
        for (int i = 0; i < N; i++) {
            theta[i] = i * Math.PI / N;
        }
        for (int i = 0; i < M; i++) {
            modulationFrequency[i] = Math.pow(A, i) * U_L;
            sigma_x[i] = (A + 1) * Math.sqrt(2 * LOG2) / (2 * Math.PI * Math.pow(A, i) * (A - 1) * U_L);
            sigma_y[i] = 1 / (2 * Math.PI * Math.tan(Math.PI / (2 * N)) * Math.sqrt(Math.pow(U_H, 2) / (2 * LOG2) - Math.pow(1 / (2 * Math.PI * sigma_x[i]), 2)));
        }
        double[] selfSimilarGaborWavelet;
        for (int s = 0; s < S; s++) {
            for (int t = 0; t < T; t++) {
                for (int m = 0; m < M; m++) {
                    for (int n = 0; n < N; n++) {
                        selfSimilarGaborWavelet = selfSimilarGaborWavelet(s, t, m, n);
                        this.selfSimilarGaborWavelets[s][t][m][n][0] = selfSimilarGaborWavelet[0];
                        this.selfSimilarGaborWavelets[s][t][m][n][1] = selfSimilarGaborWavelet[1];
                    }
                }
            }
        }
        //data.getTypeFeature().setId(7);
        //data.getTypeFeature().setDimension(N*M*2);
        //ImageRepresentation.addFeature(data);
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
		int width = image.getColorImage().getWidth();
		int height = image.getColorImage().getHeight();		
		
		List<List<Double>> data = new ArrayList<List<Double>>();
				
		List<Double> descriptor = new ArrayList<Double>();
		//TODO
		BufferedImage bimg =  image.getBufferedImage();
        double values[] = getNormalizedFeature(bimg);
        
        for(int i=0 ; i<values.length ; i++)
            descriptor.add((double)values[i]);
		
        data.add(descriptor);
		return data;
	}
	
	public double[] getNormalizedFeature(BufferedImage image) {
        return normalize(getFeature(image));
    }

    public double[] normalize(double[] featureVector) {
        int dominantOrientation = 0;
        double orientationVectorSum = 0;
        double orientationVectorSum2 = 0;
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                orientationVectorSum2 += Math.sqrt(Math.pow(featureVector[m * N + n * 2], 2) + Math.pow(featureVector[m * N + n * 2 + 1], 2));
            }
            if (orientationVectorSum2 > orientationVectorSum) {
                orientationVectorSum = orientationVectorSum2;
                dominantOrientation = m;
            }
        }

        double[] normalizedFeatureVector = new double[featureVector.length];
        for (int m = dominantOrientation, k = 0; m < M; m++, k++) {
            for (int n = 0; n < N; n++) {
                normalizedFeatureVector[k * N + n * 2] = featureVector[m * N + n * 2];
                normalizedFeatureVector[k * N + n * 2 + 1] = featureVector[m * N + n * 2 + 1];
            }
        }
        for (int m = 0, k = M - dominantOrientation; m < dominantOrientation; m++, k++) {
            for (int n = 0; n < N; n++) {
                normalizedFeatureVector[k * N + n * 2] = featureVector[m * N + n * 2];
                normalizedFeatureVector[k * N + n * 2 + 1] = featureVector[m * N + n * 2 + 1];
            }
        }

        return normalizedFeatureVector;
    }
    
    public double[] getFeature(BufferedImage image) {
        image = ImageUtils.scaleImage(image, MAX_IMG_HEIGHT);
        Raster imageRaster = image.getRaster();
        int[][] grayLevel = new int[imageRaster.getWidth()][imageRaster.getHeight()];
        int[] tmp = new int[3];
        for (int i = 0; i < imageRaster.getWidth(); i++) {
            for (int j = 0; j < imageRaster.getHeight(); j++) {
                grayLevel[i][j] = imageRaster.getPixel(i, j, tmp)[0];
            }
        }

        double[] featureVector = new double[M * N * 2];
        double[][] magnitudes = computeMagnitudes(grayLevel);
        int imageSize = image.getWidth() * image.getHeight();
        double[][] magnitudesForVariance = new double[M][N];

        if (this.gaborWavelet == null) {
            precomputeGaborWavelet(grayLevel);
        }

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                featureVector[m * N + n * 2] = magnitudes[m][n] / imageSize;
                for (int i = 0; i < magnitudesForVariance.length; i++) {
                    for (int j = 0; j < magnitudesForVariance[0].length; j++) {
                        magnitudesForVariance[i][j] = 0.;
                    }
                }
                for (int x = S; x < image.getWidth(); x++) {
                    for (int y = T; y < image.getHeight(); y++) {
                        magnitudesForVariance[m][n] += Math.pow(Math.sqrt(Math.pow(this.gaborWavelet[x - S][y - T][m][n][0], 2) + Math.pow(this.gaborWavelet[x - S][y - T][m][n][1], 2)) - featureVector[m * N + n * 2], 2);
                    }
                }

                featureVector[m * N + n * 2 + 1] = Math.sqrt(magnitudesForVariance[m][n]) / imageSize;
            }
        }
        this.gaborWavelet = null;

        return featureVector;
    }

    private double[][] computeMagnitudes(int[][] image) {
        double[][] magnitudes = new double[M][N];
        for (int i = 0; i < magnitudes.length; i++) {
            for (int j = 0; j < magnitudes[0].length; j++) {
                magnitudes[i][j] = 0.;
            }
        }

        if (this.gaborWavelet == null) {
            precomputeGaborWavelet(image);
        }

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                for (int x = S; x < image.length; x++) {
                    for (int y = T; y < image[0].length; y++) {
                        magnitudes[m][n] += Math.sqrt(Math.pow(this.gaborWavelet[x - S][y - T][m][n][0], 2) + Math.pow(this.gaborWavelet[x - S][y - T][m][n][1], 2));

                    }
                }
            }
        }
        return magnitudes;
    }

    private void precomputeGaborWavelet(int[][] image) {
        this.gaborWavelet = new double[image.length - S][image[0].length - T][M][N][2];
        double[] gaborWavelet;
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                for (int x = S; x < image.length; x++) {
                    for (int y = T; y < image[0].length; y++) {
                        gaborWavelet = gaborWavelet(image, x, y, m, n);
                        this.gaborWavelet[x - S][y - T][m][n][0] = gaborWavelet[0];
                        this.gaborWavelet[x - S][y - T][m][n][1] = gaborWavelet[1];
                    }
                }
            }
        }
    }

    // returns 2 doubles representing the real ([0]) and imaginary ([1]) part of the mother wavelet
    private double[] gaborWavelet(int[][] img, int x, int y, int m, int n) {
        double re = 0;
        double im = 0;
        for (int s = 0; s < S; s++) {
            for (int t = 0; t < T; t++) {
                re += img[x][y] * selfSimilarGaborWavelets[s][t][m][n][0];
                im += img[x][y] * -selfSimilarGaborWavelets[s][t][m][n][1];
            }
        }

        return new double[]{re, im};
    }


    private double[] selfSimilarGaborWavelet(int x, int y, int m, int n) {
        double[] motherWavelet = computeMotherWavelet(x_tilde(x, y, m, n), y_tilde(x, y, m, n), m, n);
        return new double[]{
                Math.pow(A, -m) * motherWavelet[0],
                Math.pow(A, -m) * motherWavelet[1]};
    }

    private double[] computeMotherWavelet(double x, double y, int m, int n) {
        return new double[]{
                1 / (2 * Math.PI * sigma_x[m] * sigma_y[m]) *
                        Math.exp(-1 / 2 * (Math.pow(x, 2) / Math.pow(sigma_x[m], 2) + Math.pow(y, 2) / Math.pow(sigma_y[m], 2))) *
                        Math.cos(2 * Math.PI * modulationFrequency[m] * x),
                1 / (2 * Math.PI * sigma_x[m] * sigma_y[m]) *
                        Math.exp(-1 / 2 * (Math.pow(x, 2) / Math.pow(sigma_x[m], 2) + Math.pow(y, 2) / Math.pow(sigma_y[m], 2))) *
                        Math.sin(2 * Math.PI * modulationFrequency[m] * x)};
    }

    private double x_tilde(int x, int y, int m, int n) {
        return Math.pow(A, -m) * (x * Math.cos(theta[n]) + y * Math.sin(theta[n]));
    }

    private double y_tilde(int x, int y, int m, int n) {
        return Math.pow(A, -m) * (-x * Math.sin(theta[n] + y * Math.cos(theta[n])));
    }

	@Override
	public Integer outputDataRowkeyPrefix() {
		return Algorithm.ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE;
	}

}
