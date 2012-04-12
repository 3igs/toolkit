package bigs.modules.fe.utils;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * @author: Ricardo Guerra.
 * @clase:  UtilConvertidores.java  
 * @descripción: Clase POJO para el manejo de conversiones de distintos objetos JAVA.
 * @author_web:  http://frameworksjava2008.blogspot.com - http://viviendoconjavaynomoririntentandolo.blogspot.com 
 * @author_email: cesarricardo_guerra19@hotmail.com.
 * @fecha_de_creación: 05-08-2009.
 * @fecha_de_ultima_actualización: 20-03-2009.
 * @versión: 3.0
 */
public class UtilConvertidores implements Serializable{

	private static final long serialVersionUID = -2454627055596665003L;

    /**
     * bufferedImageToImage convierte un dato de tipo 'BufferedImage' a 'Image'.
     * @return Image
     */
	public Image bufferedImageToImage( BufferedImage grafico ){
		Image  imagen = Toolkit.getDefaultToolkit().createImage( grafico.getSource() );
		return imagen;
	}

    /**
     * arrayBytesToImage convierte un dato de tipo 'arrayBytes' a 'Image'.
     * @return Image
     */
	public Image arrayBytesToImage( byte[] grafico ){
		Image  imagen = Toolkit.getDefaultToolkit().createImage( grafico );
		return imagen;
	}
	
    /**
     * arrayBytesToBufferedImage convierte un dato de tipo 'byte[]' a 'BufferedImage'.
     * @return BufferedImage
     */
	public BufferedImage arrayBytesToBufferedImage( byte[] imagenBytes ){
		
		BufferedImage bufferedImage = null;
		
		try{
			if( (imagenBytes != null) && (imagenBytes.length > 0) ){
				bufferedImage = ImageIO.read( new ByteArrayInputStream( imagenBytes ) );
			}
		} 
		catch( Exception e ){
			e.printStackTrace();
		}
		
		return bufferedImage;
	}
	
    /**
     * inputStreamToBufferedImage convierte un dato de tipo 'InputStream' a 'BufferedImage'.
     * @return BufferedImage
     */
	public BufferedImage inputStreamToBufferedImage( InputStream inputStream ){
        
		BufferedImage bufferedImage = null;
		
		try{
			bufferedImage = javax.imageio.ImageIO.read( inputStream );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
        return bufferedImage;
	}
	
    /**
     * byteArrayOutputStreamToBufferedImage convierte un dato de tipo 'ByteArrayOutputStream' a 'BufferedImage'.
     * @return BufferedImage
     */
	public BufferedImage byteArrayOutputStreamToBufferedImage( ByteArrayOutputStream baos ){
		
		BufferedImage bufferedImage = null;
			
		try{
			byte[] bytes = baos.toByteArray();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( bytes );
			bufferedImage = ImageIO.read( byteArrayInputStream );

		}
		catch( Exception e ){
			e.printStackTrace();
		}
		
		return bufferedImage;
	}
		
    /**
     * imageToBufferedImage convierte un dato de tipo 'Image' a 'BufferedImage'.
     * @return BufferedImage
     */
	public BufferedImage imageToBufferedImage( Image image ){

		if( image instanceof BufferedImage ){
			return (BufferedImage)image;
		}

		image = new ImageIcon(image).getImage();

		boolean validator = this.getValidator(image);

		BufferedImage bufferedImage = null; 
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		try{
			int transparency = Transparency.OPAQUE;

			if( validator ){
				transparency = Transparency.BITMASK;
			}

			GraphicsDevice        gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bufferedImage = gc.createCompatibleImage( image.getWidth(null), image.getHeight(null), transparency );  
		} 
		catch( Exception e ){
			e.printStackTrace();
		}

		if( bufferedImage == null ){
			
			int type = BufferedImage.TYPE_INT_RGB;

			if( validator ){
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bufferedImage = new BufferedImage( image.getWidth(null), image.getHeight(null), type );
		}

		Graphics g = bufferedImage.createGraphics();

		g.drawImage( image, 0, 0, null );
		g.dispose();

		return bufferedImage;
	}
	
    /**
     * getValidator valida un dao 'image'.
     * @return boolean
     */
	private boolean getValidator( Image image ){

		if( image instanceof BufferedImage ){
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		PixelGrabber pg = new PixelGrabber( image, 0, 0, 1, 1, false );

		try{
			pg.grabPixels();
		} 
		catch( InterruptedException e ){
			e.printStackTrace();
		}

		ColorModel cm = pg.getColorModel();
		
		return cm.hasAlpha();
	}
	
    /**
     * bufferedImageToArrayBytes convierte un dato de tipo 'bufferedImage' a 'byte[]'.
     * @return byte[]
     */
	public byte[] bufferedImageToArrayBytes( BufferedImage bufferedImage ){
		
		try{
			if( bufferedImage != null ){
				BufferedImage          image = bufferedImage;
				ByteArrayOutputStream  baos  = new ByteArrayOutputStream();
				
				try{
					ImageIO.write( image, "jpg", baos );
				} 
				catch( IOException e ){
					throw new IllegalStateException(e.toString());
				}
				byte[] b = baos.toByteArray();
				
				return b;
			} 
		}	
		catch( Exception e ){
			e.printStackTrace();
		}
		
		return new byte[0];
	}
	
    /**
     * inputStreamToArrayBytes convierte un dato de tipo 'inputStream' a 'byte[]'.
     * @return byte[]
     */
	public byte[] inputStreamToArrayBytes( InputStream inputStream ){
        
		System.out.println( "DENTRO...!!!" );
		
		ByteArrayOutputStream out= null;
		
		try{
			out = new ByteArrayOutputStream(1024);
			byte[] buffer = new byte[1024];
			int len;
	
			while((len = inputStream.read(buffer)) >= 0)
			out.write(buffer, 0, len);
	
			inputStream.close();
			out.close();
		}	
		catch( Exception e ){
			e.printStackTrace();
		}
		
		return out.toByteArray();
	} 
	
    /**
     * intToArrayBytes convierte un dato de tipo 'int' a 'byte[]'.
     * @return byte[]
     */
	public byte[] intToArrayBytes( int numero ){
        byte[] arrayBytes = new byte[4];
        
        for( int i=0; i<4; i++ ){
             int offset = (arrayBytes.length - 1 - i) * 8;
             arrayBytes[i] = (byte) ((numero >>> offset) & 0xFF);
        }
        
        return arrayBytes;
    }
	
    /**
     * stringToArrayBytes convierte un dato de tipo 'String' a 'byte[]'.
     * @return byte[]
     */
	public byte[] stringToArrayBytes( String cadena ){
	   
		byte[] arrayBytes = null;
	    
	    try{
		    arrayBytes = cadena.getBytes();
		    //arrayBytes = cadena.getBytes( "UTF-8" );
	    } 
	    catch( Exception e ){
	        e.printStackTrace();
	    }
	    
		return arrayBytes;
    }
	
    /**
     * objectToArrayBytes convierte un dato de tipo 'Object' a 'byte[]'.
     * @return byte[]
     */
	public byte[] objectToArrayBytes( Object objeto ){
		
		byte[] data = null;
		
		try{
	        ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	        ObjectOutputStream    salida = new ObjectOutputStream( stream );  
	      
	        salida.writeObject( objeto );
	        salida.flush();
	        
	        salida.close(); 
	        stream.close();
	      
	        data = stream.toByteArray();
		}
		catch( Exception e ){
		       e.printStackTrace();
		}
		
	    return data;
	}
	
    /**
     * inputStreamToOutputStream convierte un dato de tipo 'InputStream' a 'OutputStream'.
     * @return OutputStream
     */
	public OutputStream inputStreamToOutputStream( InputStream inputStream ){
		
		OutputStream outputStream = null;
		
		try{
			byte[] streamBytes = new byte[1024];
			int    ch          = 0;
			while( (ch = inputStream.read( streamBytes ) ) != -1 ){
			    outputStream.write( streamBytes, 0, ch );
			}
			outputStream.flush();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		
		return outputStream;	
	}
	
    /**
     * bufferedImageToByteArrayInputStream convierte un dato de tipo 'BufferedImage' a 'InputStream'.
     * @return InputStream
     */
	public InputStream bufferedImageToByteArrayInputStream( BufferedImage bufferedImage ){
		
		ByteArrayInputStream byteArrayInputStream = null;
		
		try{
			String formatName = "jpeg";
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write( bufferedImage, formatName, byteArrayOutputStream );
			byte[] imageBytes = byteArrayOutputStream.toByteArray();
			byteArrayInputStream = new ByteArrayInputStream( imageBytes );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		return byteArrayInputStream;
	}
	
    /**
     * arrayBytesToLong convierte un dato de tipo 'arrayBytes' a 'long'.
     * @return long
     */
	public long arrayBytesToLong( byte[] arrayBytes ){
	    long l = 0;
	    l |= arrayBytes[0] & 0xFF;
	    l <<= 8;
	    l |= arrayBytes[1] & 0xFF;
	    l <<= 8;
	    l |= arrayBytes[2] & 0xFF;
	    l <<= 8;
	    l |= arrayBytes[3] & 0xFF;
	    
	    return l;
	}
    
	/**
     * arrayBytesToInteger convierte un dato de tipo 'arrayBytes' a 'Integer'.
     * @return Integer
     */
	public Integer arrayBytesToInteger( byte[] arrayBytes ){
		Integer i = 0;
	    i |= arrayBytes[0] & 0xFF;
	    i <<= 8;
	    i |= arrayBytes[1] & 0xFF;
	    return i;
	}
	
	/**
     * arrayBytesToString convierte un dato de tipo 'arrayBytes' a 'String'.
     * @return String
     */
	public String arrayBytesToString( byte[] arrayBytes ){
	   	   
	   String cadena  = new String( arrayBytes );
	   
	   return cadena;	
	}
	
	/**
     * arrayBytesToObject convierte un dato de tipo 'arrayBytes' a 'Object'.
     * @return Object
     */
	public Object arrayBytesToObject( byte[] arrayBytes ){
		
		Object objeto = null;
		
		try{
			objeto = new java.io.ObjectInputStream( new java.io.ByteArrayInputStream( arrayBytes ) ).readObject();
		}
		catch( java.io.IOException e ){
			   java.util.logging.Logger.global.log( java.util.logging.Level.SEVERE, e.getMessage() );
		}
		catch( java.lang.ClassNotFoundException e ){ 
			   java.util.logging.Logger.global.log( java.util.logging.Level.SEVERE, e.getMessage() );
		}
			
		return objeto;
	}

	/**
     * listObjectToArrayBytes  convierte un dato de tipo List<MiObjeto> a byte[]
     * @param  listObject
     * @return byte[]
     */
	public byte[] listObjectToArrayBytes( Object listObject ) throws IOException{
		
		ByteArrayOutputStream  stream     = new ByteArrayOutputStream(); 
		ObjectOutputStream     salida     = new ObjectOutputStream( stream ); 
		byte[]                 arrayBytes = null;
		
		salida.writeObject( listObject ); 
		arrayBytes = stream.toByteArray();
		salida.close();
		
		System.out.println( "List Serializada" );
		
		return arrayBytes ; 
	}
	
	/**
     * arrayListObjectToArrayBytes  convierte un dato de tipo ArrayList<MiObjeto> a byte[]
     * @param  arrayListObject
     * @return byte[]
     */
	public byte[] arrayListObjectToArrayBytes( Object arrayListObject ) throws IOException{
		
		ByteArrayOutputStream  stream     = new ByteArrayOutputStream(); 
		ObjectOutputStream     salida     = new ObjectOutputStream( stream ); 
		byte[]                 arrayBytes = null;
		
		salida.writeObject( arrayListObject ); 
		arrayBytes = stream.toByteArray();
		salida.close();
		
		System.out.println( "Array List Serializada" );
		
		return arrayBytes ; 
	}
	
	/**
     * vectorObjectToArrayBytes  convierte un dato de tipo Vector<MiObjeto> a byte[]
     * @param  vectorObject
     * @return byte[]
     */
	public byte[] vectorObjectToArrayBytes( Object vectorObject ) throws IOException{
		
		ByteArrayOutputStream  stream     = new ByteArrayOutputStream(); 
		ObjectOutputStream     salida     = new ObjectOutputStream( stream ); 
		byte[]                 arrayBytes = null;
		
		salida.writeObject( vectorObject ); 
		arrayBytes = stream.toByteArray();
		salida.close();
		
		System.out.println( "Array List Serializada" );
		
		return arrayBytes ; 
	}
	
	/**
     * arrayBytesToListObject  al momento de recepcionar tendra que ser parcesado a List<MiObjeto>
     * @param  bytes 
     * @return Object
     */
	public Object arrayBytesToListObject( byte[] bytes ){
		
		Object listaObjetos = null;
		
		try{
			ByteArrayInputStream entrada = new ByteArrayInputStream( bytes );
		    ObjectInputStream    salida  = new ObjectInputStream( entrada );
		    
			listaObjetos = (Object)salida.readObject(); 
			
			salida.close(); 
			entrada.close(); 
	    } 
		catch( IOException e ){
			   e.printStackTrace();
		} 
		catch( ClassNotFoundException e ){
			   e.printStackTrace();
		}
		
		return listaObjetos;
	}
	
	/**
     * arrayBytesToListObject  al momento de recepcionar tendra que ser parcesado a ArrayList<MiObjeto>
     * @param  bytes 
     * @return Object
     */
	public Object arrayBytesToArrayListObject( byte[] bytes ){
		
		Object arrayListObject = null;
		
		try{
			ByteArrayInputStream entrada = new ByteArrayInputStream( bytes );
		    ObjectInputStream    salida  = new ObjectInputStream( entrada );
		    
		    arrayListObject = (Object)salida.readObject(); 
			
			salida.close(); 
			entrada.close(); 
	    } 
		catch( IOException e ){
			   e.printStackTrace();
		} 
		catch( ClassNotFoundException e ){
			   e.printStackTrace();
		}
		
		return arrayListObject;
	}
	
	/**
     * arrayBytesToVectorObject  al momento de recepcionar tendra que ser parcesado a Vector<MiObjeto>
     * @param  bytes 
     * @return Object
     */
	public Object arrayBytesToVectorObject( byte[] bytes ){
		
		Object vectorObject = null;
		
		try{
			ByteArrayInputStream entrada = new ByteArrayInputStream( bytes );
		    ObjectInputStream    salida  = new ObjectInputStream( entrada );
		    
		    vectorObject = (Object)salida.readObject(); 
			
			salida.close(); 
			entrada.close(); 
	    } 
		catch( IOException e ){
			   e.printStackTrace();
		} 
		catch( ClassNotFoundException e ){
			   e.printStackTrace();
		}
		
		return vectorObject;
	}
	
	/**
	 * hashMapToArrayList  convierte un hashMap a un ArrayList y la lista que retorna es en base 
	 * al 'Key' o 'Value'.
	 * 
	 * @param mapa        es un objeto de tipo HashMap.
	 * @param datoRetorno es un dato de tipo String. El dato ingresado puede ser 'Key' o 'Value'.
	 **/
	public List<Object> hashMapToArrayList( Map<Object, Object> mapa, String datoRetorno ){
		
		List<Object> lista = null;
		
		if( datoRetorno.equalsIgnoreCase( "Value" ) ){			
			lista = new ArrayList<Object>( mapa.values() );   		
		}
		else if( datoRetorno.equalsIgnoreCase( "Key" ) ){	
			lista = new ArrayList<Object>( mapa.keySet() );
		}
		
		return lista;
	}
	
	public static long getSerialVersionUID(){
		return serialVersionUID;
	}
	
 }
