package bigs.web;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import bigs.api.exceptions.BIGSException;
import bigs.core.BIGS;
import bigs.core.utils.Log;



public class Web {
	
	public static Color COLOR_TRAIN = Color.BLUE;
	public static Color COLOR_VALIDATION = Color.RED;
	public static Color COLOR_TEST = Color.green;
	
	public static String KIND_REPEAT = "Repeat";
	public static String KIND_CONFIG = "Config";
	public static String KIND_RUN = "Run";
	
	public static String ALL_ENGINES = null;
	
	public static String htmlColorLegend = "color legend<br/>" +
			"<b>" +
			"<span style='color: blue'>train</span> " +
			"<span style='color: red'>validation</span> " +
			"<span style='color: green'>test</span> " +
			"</b>";
	
	static String thisHost = "";
	static Integer thisPort = 0;
	
	/**
	 * this method was put here (from StartWebCommand) since its presence in
	 * StartWebCommand caused the class loader to look to jetty classes, even if
	 * they were not used, such as when invoking 'bigs worker'. This was not convenient
	 * because it forced to include jetty jars within the Java Web Start bigs worker jnlp
	 * making the client download jars that was not going to use.
	 */
	public static void startWeb(Integer port) {
        try {
        	
            Log.info("starting BIGS web server");

            thisHost = java.net.InetAddress.getLocalHost().getHostAddress().toString();
            thisPort = port;
            // configures server connection
            Server server = new Server(thisPort);

            // configures regular file handler to serve files under $BIGSHOME/web
            ResourceHandler filehandler = new ResourceHandler();
            filehandler.setDirectoriesListed(true);
            filehandler.setWelcomeFiles(new String[]{ "index.html" });
            Log.info("BIGS web server using html root at "+BIGS.bigsBaseDir+"/web");
            filehandler.setResourceBase(BIGS.bigsBaseDir+"/web");            
            
            // add handler for summary
            ServletHandler servlets = new ServletHandler();
            servlets.addServletWithMapping(GetJNLPServlet.class, WebConstants.JNLP_SERVLET_NAME);
                                    
            // adds all handlers to web server
            HandlerList handlers = new HandlerList();
            handlers.addHandler(servlets);
            handlers.addHandler(filehandler);
            server.setHandler(handlers);
            

            Log.info("web server listening at http://"+thisHost+":"+thisPort);
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BIGSException("cannot start BIGS web server. "+e.getMessage());
        }		
	}
	
}
