package bigs.modules.fe.utils;

import ij.*;
import ij.process.ImageProcessor;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

import java.awt.image.*;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * @author  aacruzr
 */
public class Image 
{

    private String name;
    private String path;
    private ImagePlus imageData;
    private ImageProcessor colorImage;
    private ImageProcessor grayImage;
    private long systemId;
    private String thumbnailLocation;
    private String classification;
	
	public static int STANDAR_DIMENSION = 256;
	
	public Image(byte[] imagenBytes){
		
		imageData = new ImagePlus();
		UtilConvertidores uc = new UtilConvertidores();
		
		BufferedImage bufferedImage = uc.arrayBytesToBufferedImage(imagenBytes);
		
		java.awt.Image imgtmp = bufferedImage.getScaledInstance(bufferedImage.getWidth(), bufferedImage.getHeight(), java.awt.Image.SCALE_DEFAULT);	
		
		imageData.setImage(imgtmp);
		this.setImageData(imageData);
	}
	
	/**
	 * @param name  the name to set
	 * @uml.property  name="name"
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return  the name
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPath(){
		return path;
	}

	/**
	 * @param imageData  the imageData to set
	 * @uml.property  name="imageData"
	 */
	public void setImageData(ImagePlus imageData) 
	{
		this.imageData = imageData;
		ImageProcessor ip1 = this.imageData.getProcessor().convertToRGB();
		ImageProcessor ip2 = this.imageData.getProcessor().convertToByte(true);
		colorImage = ip1.resize(STANDAR_DIMENSION, STANDAR_DIMENSION);
		grayImage = ip2.resize(STANDAR_DIMENSION, STANDAR_DIMENSION);
	}
	/**
	 * @return  the imageData
	 * @uml.property  name="imageData"
	 */
	public ImagePlus getImageData() 
	{
		return imageData;
	}
	
	public BufferedImage getBufferedImage()	//TODO: its necesary more validations
	{
            BufferedImage bi = new BufferedImage(imageData.getImage().getWidth(null),imageData.getImage().getHeight(null),BufferedImage.TYPE_INT_RGB);
	    Graphics bg = bi.getGraphics();
	    bg.drawImage(imageData.getImage(), 0, 0, null);
	    bg.dispose();
	    return bi;
	}

	/**
	 * @return  the features
	 * @uml.property  name="features"
	 */
	/*public Hashtable<String, Feature> getFeatures()
	{
		return features;
	}*/

	/**
	 * @param colorImage  the colorImage to set
	 * @uml.property  name="colorImage"
	 */
	public void setColorImage(ImageProcessor colorImage) {
		this.colorImage = colorImage;
	}

	/**
	 * @return  the colorImage
	 * @uml.property  name="colorImage"
	 */
	public ImageProcessor getColorImage() {
		return colorImage;
	}

	/**
	 * @param grayImage  the grayImage to set
	 * @uml.property  name="grayImage"
	 */
	public void setGrayImage(ImageProcessor grayImage) {
		this.grayImage = grayImage;
	}

	/**
	 * @return  the grayImage
	 * @uml.property  name="grayImage"
	 */
	public ImageProcessor getGrayImage() {
		return grayImage;
	}
	
	/**
	 * @param systemId  the systemId to set
	 * @uml.property  name="systemId"
	 */
	public void setSystemId(long systemId) {
		this.systemId = systemId;
	}

	/**
	 * @return  the systemId
	 * @uml.property  name="systemId"
	 */
	public long getSystemId() {
		return systemId;
	}

	/**
	 * @param thumbnailLocation  the thumbnailLocation to set
	 * @uml.property  name="thumbnailLocation"
	 */
	public void setThumbnailLocation(String thumbnailLocation) {
		this.thumbnailLocation = thumbnailLocation;
	}

	/**
	 * @return  the thumbnailLocation
	 * @uml.property  name="thumbnailLocation"
	 */
	public String getThumbnailLocation() {
		return thumbnailLocation;
	}

	/**
	 * @param classification the classification to set
	 */
	public void setClassification(String classification) {
		this.classification = classification;
	}

	/**
	 * @return the classification
	 */
	public String getClassification() {
		return classification;
	}

}
