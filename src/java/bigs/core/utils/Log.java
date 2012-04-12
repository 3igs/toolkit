package bigs.core.utils;

import java.util.Enumeration;


import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class Log {
    public static Logger logger = Logger.getLogger("bigs");
    static String thismachine = Network.getHostName();

    static {
    	
    	// Removes logging from certain classes
		for (Enumeration en = Logger.getRootLogger().getAllAppenders(); en.hasMoreElements(); ) {
			Appender ap = (Appender)en.nextElement();
			Filter filter = new Filter() {
				public int decide(LoggingEvent event) {
					if (event.getLoggerName().contains("jmimemagic")) return Filter.DENY;
					return Filter.ACCEPT;
				}
			};
			ap.addFilter(filter);
		}
    	
    	// this is commented out. seems other library already initalizes a logger
//        PatternLayout pl = new PatternLayout ("%-5p [%d{dd.MM.yy HH:mm:ss}] %m%n");
//        ConsoleAppender ca = new ConsoleAppender (pl);
//        logger.addAppender(ca);
    }

    static String lastLog= "";
    public static String getLastLogMessage() { return lastLog; }
    
    public static synchronized void info(String msg)  { logger.info(thismachine+" "+msg); lastLog = msg; }
    public static synchronized void debug(String msg) { logger.debug(thismachine+" "+msg); lastLog = msg; }
    public static synchronized void warn(String msg)  { logger.warn(thismachine+" "+msg); lastLog = msg;}
    public static synchronized void error(String msg) { logger.error(thismachine+" "+msg); lastLog = msg;}
    public static synchronized void fatal(String msg) { logger.fatal(thismachine+" "+msg); lastLog = msg;}

    public static void setLogLevel(String level) {
    	if (level==null) level="INFO";
        if (level.equalsIgnoreCase("INFO")) logger.setLevel(Level.INFO);
        else if (level.equalsIgnoreCase("DEBUG")) logger.setLevel(Level.DEBUG);
        else if (level.equalsIgnoreCase("ERROR")) logger.setLevel(Level.ERROR);
        else if (level.equalsIgnoreCase("FATAL")) logger.setLevel(Level.FATAL);
        else if (level.equalsIgnoreCase("WARN")) logger.setLevel(Level.WARN);

        else {
            error("unkown log level "+level+". Using default 'INFO' log level");
            logger.setLevel(Level.INFO);
        }
    }


}
