package bigs.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import bigs.api.core.Algorithm;
import bigs.api.core.Configurable;
import bigs.api.core.BIGSParam;
import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.core.BIGS;
import bigs.core.BIGSProperties;
import bigs.core.exceptions.BIGSPropertyNotFoundException;
import bigs.core.utils.Text;


public class Core {
	
	/**
	 * Unique number to identify this JavaVM in databases, scheduling, etc.
	 */
	public final static String myUUID = UUID.randomUUID().toString(); 
	
	public final static Boolean USECACHE = true;
	public final static Boolean NOCACHE  = false;

	/**
	 * creates an configurable object and sets its field annotated with BIGSParam according to values specified in a properties object
	 * @param classPropertyName the name of the property containing the class name for which the object will be instantiated
	 * @param parentClass the parent class or interface that the object must inherit or implement.
	 *                    This is the returning class of the object. If the class specified in <b>classPropertyName</b>
	 *                    does not implement or does not inherit from this class, an exception will be thrown
	 * @param properties the properties object containing the class name and the values to configure the fields
	 * @param propertiesPrefix a prefix to add to the properties names for the class name and field values
	 * @return the configured object
	 * @throws BIGSPropertyNotFoundException 
	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <T extends Configurable> T getConfiguredObject(String classPropertyName, Class<T> parentClass, Properties properties, String propertiesPrefix) throws BIGSPropertyNotFoundException {

 		String propertyName = propertiesPrefix+"."+classPropertyName;
		String configurableObjectClassName = properties.getProperty(propertyName);		
		if (configurableObjectClassName==null) {
			throw new BIGSPropertyNotFoundException("property "+propertyName);
		}
		configurableObjectClassName = configurableObjectClassName.trim();
		try {
			if (configurableObjectClassName==null) {
				throw new BIGSException("definition for "+propertiesPrefix+"."+classPropertyName+" not found");
			}
			
			Class<?> configurableObjectClass = Class.forName(configurableObjectClassName);
			T configurableObject = (T)configurableObjectClass.newInstance();
			
			for (Field field: configurableObjectClass.getFields()) {
				if (field.isAnnotationPresent(BIGSParam.class)) {
					// sets the value from the properties
					String propName = propertiesPrefix+"."+configurableObjectClass.getSimpleName()+"."+field.getName();
					String paramSValue = properties.getProperty(propName);
					BIGSParam annotation = field.getAnnotation(BIGSParam.class);
					if (paramSValue==null && annotation.isMandatory()) {
						throw new BIGSException("no values defined for param "+field.getName()+ " in configuration for class "+configurableObjectClassName+" through property name "+propName);
					}
					if (paramSValue!=null) {
						Class<?> fieldClass = field.getType();
						Object value = Text.parseObject(paramSValue, fieldClass);
						field.set(configurableObject, value);
					}
				}				
			}
			return configurableObject;
		} catch (ClassNotFoundException e) {
			throw new BIGSException("class "+configurableObjectClassName+" (or its configuration) not found");
		} catch (InstantiationException e) {
			throw new BIGSException("instantiation exception for "+configurableObjectClassName+" (or its configuration, check it has an empty constructor) and parent class "+parentClass.getSimpleName()+". "+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new BIGSException("illegal access exception for "+configurableObjectClassName+" (or its configuration). "+e.getMessage());
		}
 	}
	
 	/**
 	 * overloaded method to obtain a configured instance from a Map containing <fieldName, value> pairs
 	 * @param className
 	 * @param parentClass
 	 * @param fieldValues
 	 * @return
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <T extends Configurable> T getConfiguredObject(String className, Class<T> parentClass, Map<String, String> fieldValues) {
 		 
		try {
			if (className==null) {
				throw new BIGSException("cannot create an instance from a NULL class name");
			}
			
			Class<?> configurableObjectClass = Class.forName(className.trim());
			T configurableObject = (T)configurableObjectClass.newInstance();
			
			for (Field field: configurableObjectClass.getFields()) {
				if (field.isAnnotationPresent(BIGSParam.class)) {
					// sets the value from the Map
					String paramSValue = fieldValues.get(field.getName());
					BIGSParam annotation = field.getAnnotation(BIGSParam.class);
					if (paramSValue==null && annotation.isMandatory()) {
						throw new BIGSException("no values defined for param "+field.getName()+ " in configuration for class "+className);
					}
					if (paramSValue!=null) {
	 					Class<?> fieldClass = field.getType();
						Object value = Text.parseObject(paramSValue, fieldClass);
						field.set(configurableObject, value);
					}
				}				
			}
			return configurableObject;
		} catch (ClassNotFoundException e) {
			throw new BIGSException("class "+className+" (or its configuration) not found");
		} catch (InstantiationException e) {
			throw new BIGSException("instantiation exception for "+className+" (or its configuration) and parent class "+parentClass.getSimpleName()+". "+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new BIGSException("illegal access exception for "+className+" (or its configuration). "+e.getMessage());
		}
 	}

	
	/**
	 * creates an object list and sets their BIGSParam annotated fields according to all possible value combinations specified in a properties object
	 * @param classPropertyName the name of the property containing the class name for which the objects will be instantiated
	 * @param parentClass the parent class or interface that the objects must inherit or implement.
	 *                    This is the returning class of the objects list. If the class specified in <b>classPropertyName</b>
	 *                    does not implement or does not inherit from this class, an exception will be thrown
	 * @param fieldAnnotation the annotation class used to signal the fields that are configurable through user properties
	 *                        in the actual objects class  (the one specified in <b>classPropertyName</b>)
	 * @param properties the properties object containing the class name and the values to configure the fields
	 * @param propertiesPrefix a prefix to add to the properties names for the class name and field values
	 * @return the configured objects list
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	static public <T extends Configurable> List<T> getConfiguredObjectList(String classPropertyName, Class<T> parentClass, Properties properties, String propertiesPrefix) {
		String className = properties.getProperty(propertiesPrefix+"."+classPropertyName).trim();

		if (className==null) {
			throw new BIGSException("definition for "+propertiesPrefix+"."+classPropertyName+" not found");
		}
		try {
			Class<?> c = Class.forName(className);
			List<T> configuredInstances = new ArrayList<T>();
			Boolean isAnyFieldAnnotated = false;
			for (Field field: c.getFields()) {
				if (field.isAnnotationPresent(BIGSParam.class)) {
					isAnyFieldAnnotated = true;					
					// gets the value list for this field from the properties
					String propName = propertiesPrefix+"."+c.getSimpleName()+"."+field.getName();
					String paramList = properties.getProperty(propName);
					BIGSParam annotation = field.getAnnotation(BIGSParam.class);
					if (paramList==null && annotation.isMandatory()) {
						throw new BIGSException("no values defined for param "+field.getName()+ " in class "+className);
					}
					
					if (paramList!=null) {
						// parse it into the type declared by the field
						Class<?> fieldClass = field.getType();
						List<?> values = Text.parseObjectList(paramList, ":", fieldClass);
						
						// if empty insert an initial empty object
						if (configuredInstances.isEmpty()) {
							T initialDummy = (T)c.newInstance();	
							configuredInstances.add(initialDummy);
						}
						// clone and create instances and inject the field value
						List<T> newConfiguredInstances = new ArrayList<T>();
						for (T instance: configuredInstances) {
							for (Object v: values) {
								T clonedInstance = (T)c.newInstance();
								for (Field f: c.getFields()) {
									if (!Modifier.isStatic(f.getModifiers())) {
										f.set(clonedInstance, f.get(instance));
									}
								}
								field.set(clonedInstance, v);
								newConfiguredInstances.add(clonedInstance);
							}
						}
						
						configuredInstances = newConfiguredInstances;
					}
				}
				
			}
			
			// if no field has been annotated, this means that the algorithm has no
			// configuration parameters and needs no configuration. In this case 
			// simply add a new instance to the list
			if (!isAnyFieldAnnotated) {
				configuredInstances.add((T)c.newInstance());
			}
			
			return configuredInstances;
			
		} catch (ClassNotFoundException e) {
			throw new BIGSException("class not found "+className);
		} catch (InstantiationException e) {
			throw new BIGSException("instantiation exception for "+className+" and parent class "+parentClass.getSimpleName()+". "+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new BIGSException("illegal access exception for "+className+". "+e.getMessage());
		}		
	}
	
	/**
	 * returns a map with <fieldName, fieldValue> pairs for all fields in the object 
	 * passed as parameter that have been marked with the given annotation
	 * @param obj the object to obtain values for
	 * @param fieldAnnotation the annotation to select the fields from which to get the values
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public Map<String, String> getObjectAnnotatedFieldsAsString (Configurable obj, Class fieldAnnotation) {
		try {
			Map<String, String> r = new HashMap<String, String>();
			Class<?> c = obj.getClass();
			for (Field field: c.getFields()) {
				if (field.isAnnotationPresent(fieldAnnotation)) {
					String value = field.get(obj).toString();
					r.put(field.getName(), value);
				}
			}
			return r;
		} catch (IllegalAccessException e) {
			throw new BIGSException("illegal access exception for "+obj.getClass()+". "+e.getMessage());
		}		
		
	}

	/**
	 * waits a certain amount o miliseconds
	 * @param milisecs
	 */
	public static void sleep(Long milisecs) {
       try {
               Thread.sleep(milisecs);
       } catch (InterruptedException e) {
               e.printStackTrace();
               System.exit(-1);
       }
	}
	
    /**
     * extracts a resource from the ClassLoader (any jar file in the classpath)
     *
     * @param resourcePath the path of the resource within any jar in the CLASSPATH
     * @param destFile the destination file where the resource is to be extracted
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void extractResource(String resourcePath, File destFile) 
    		throws FileNotFoundException, IOException {

        URL rURL = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        System.out.println("URL "+rURL);

        InputStream is = rURL.openStream();
        OutputStream os = new FileOutputStream(destFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len=is.read(buf))>0) {
            os.write(buf, 0, len);
        }
        is.close();
        os.close();
    }	
    
    /**
     * extracts the contents of a resource from the ClassLoader (a jar file in the classpath)
     *
     * @param resourcePath the path of the resource within any jar in the CLASSPATH
     * @returns a string with the contents of the resource
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String extractResourceAsString(String resourcePath)
    		throws IOException {
        URL rURL = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (rURL==null) return null;
        
        InputStream is = rURL.openStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len=is.read(buf))>0) {
            os.write(buf, 0, len);
        }
        is.close();
        os.close();
        
        String r = new String(os.toByteArray());
        return r;    	
    }

    /**
     * encrypts a string with default pass-phrase
     * @param source
     * @return
     */
    public static String encrypt(String source) {
    		DESEncrypter encrypter = new DESEncrypter(DESEncrypter.defaultPassphrase);
    		return encrypter.encrypt(source);	   
    }
    
    /**
     * decrypts a string with default pass-phrase
     * @param source
     * @return
     */
    public static String decrypt(String source) {
   		DESEncrypter encrypter = new DESEncrypter(DESEncrypter.defaultPassphrase);
   		return encrypter.decrypt(source);	   
   } 
    
    /**
     * returns the stack trace of an exception as a string
     * @param e the exception from where to retrieve the stack trace
     * @return the stack trace as string
     */
    public static String getStackTrace(Throwable e) {
        StringBuffer sb=new StringBuffer();
        sb.append(e.getClass().getName()+" "+e.getCause()+" "+e.getMessage()+"\n");
        for (StackTraceElement se: e.getStackTrace()) {
            sb.append("      at "+se.toString()).append("\n");
        }
        return sb.toString();
    }    

    /**
     * returns the hierarchy of super classes of a class
     * @param initialClass
     * @return a list of classes from which the initialClass inherits
     */
    public static List<Class<?>> getSuperclasses(Class<?> initialClass) {
    	List<Class<?>> r = new ArrayList<Class<?>>();
    	
    	Class<?> currentClass = initialClass;
    	while (currentClass!=null && currentClass!=Object.class) {
    		currentClass = currentClass.getSuperclass();
    		r.add(currentClass);
    	}
    	return r;
    }

    /**
     * returns a list of classes that inherit directly or indirectly from
     * the class passed as argument
     * @param initialClass the class to retrieve its subclasses for
     * @return the list of subclasses
     */
    public static <T> List<Class<? extends T>> getAllSubclasses(Class<T> initialClass) {
    	Reflections refl = new Reflections(
  	          new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader())
                .setScanners(new SubTypesScanner()));
  	
	  	Set<Class<? extends T>> s = refl.getSubTypesOf(initialClass);
	  	List<Class<? extends T>> r = new ArrayList<Class<? extends T>>();
	  	for (Class<? extends T> c: s) {
	  		r.add(c);
	  	}    	
	  	return r;
    }
    
    
    static Long timeOffset = null;

    /**
     * calibrates this machine's time with respect to a data source
     * @param dataSource the data source from where to get the time reference
     * @return
     */
    public static Long calibrateTime(DataSource dataSource) {
		Long date = dataSource.getTime();
		timeOffset = new Date().getTime() - date;
		Log.debug("offset to time reference is "+Text.timeToString(timeOffset));
		return date;    	
    }
    
    /**
     * returns this machine time. if calibration with respect to a data source happened before this
     * call, returns the corrected time with respect to that data source. Otherwise returns this
     * machine local time
     * 
     * @return
     */
    public static Long getTime() {
    	if (timeOffset == null) {
    		Log.error("time has not been previously calibrated. using this machine local time");
    		return new Date().getTime();
    	} else {
        	return new Date().getTime() - timeOffset;
    	}
    }
        
}
