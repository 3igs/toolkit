package bigs.modules.fe.utils;

/**
 * DCT.java
 * 2D DTC transformation to a nxn matrix
 * InfoRMed
 * @created	12/01/2012
 * @author      javanegasr
 * @history
 * 12/01/2012	DCT.java
 * @copyright 	Copyright 2012 (c) Bioingenium Research Group
 */
public class DCT {

    // DCT Block Size - default 8
    private int n = 8;
    // Cosines matrix
    double c[][];

    /**
     *  Initialise the nxn cosines matrix
     * @param dctBlockSize 
     */
    public DCT(int dctBlockSize) {
        n = dctBlockSize;
        initMatrix();
    }
    
    public DCT() {
        initMatrix();
    }

    /**
     * Build the nxn cosines matrix
     */
    private void initMatrix() {
        
        c = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == 0) {
                    c[i][j] = (1d / Math.sqrt(n));
                } else {
                    c[i][j] = (Math.sqrt(2d / n) * Math.cos((double)
                            (2.0 * j + 1) * (double) i * Math.PI / (2d * n)));
                }
            }
        }
    }

    /**
     * Perform the DTC transformation to a nxn matrix
     * @param input input matrix
     * @return dct transformation result of input matrix
     */
    public double[][] forwardDCT(float input[][]) {
        
        double output[][] = new double[n][n];
        double aux[][] = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    aux[i][j] += c[i][k] * input[k][j];
                }
            }
        }
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    output[i][j] += aux[i][k] * c[k][j];
                }
            }
        }

        return output;
    }
}
