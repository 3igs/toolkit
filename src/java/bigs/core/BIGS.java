package bigs.core;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import bigs.api.exceptions.BIGSException;
import bigs.core.commands.Command;
import bigs.core.commands.Help;
import bigs.core.utils.Log;
import bigs.core.utils.Text;

public class BIGS {

    static Options options = new Options();
    public static File bigsBaseDir = null;
    public static String bigsRelease        = "@RELEASE@";
    public static String bigsFullName       = "@APPNAME@. @RELEASE-STRING@";
    
    public static BIGSProperties globalProperties = null;
    
	public final static String tableName = "bigs";
	public final static String[] columnFamilies = new String[]{"content"};

	public static void main (String[] args) throws Exception {

        // this is to ensure consistent formatting of numbers and dates
        Locale.setDefault(Locale.ENGLISH);

        // prints out BIGS info header
        System.out.println(Text.charString("-", bigsFullName.length()));
        System.out.println(bigsFullName);
        System.out.println(Text.charString("-", bigsFullName.length()));
        System.out.println("");
        
        // parses command line
        Option basedir   = new Option("b", "basedir",true, "basedir of this BIGS installation. Expert option use with care (default '.')");
    	Option help      = new Option("h", false, "shows complete help");
    	Option config    = new Option("c", "config",true, "BIGS config file (default 'bigs.properties')");
    	
    	options.addOption(basedir);
    	options.addOption(config);
    	options.addOption(help);
        CommandLineParser parser = new GnuParser();
        CommandLine line=null;

        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( exp.getMessage() );
            printCmdlineSyntax();
            System.exit(1);
        }

        // user asked for help
        if (line.hasOption("h")) {
            printCmdlineSyntax();
            new Help().run(new String[]{});
            System.exit(0);
        }

        // gets the base dir of this BIGS installation
        if (line.hasOption("b")) bigsBaseDir = new File(line.getOptionValue("b"));
        if (!bigsBaseDir.exists()) {
        	System.err.println("basedir "+bigsBaseDir.getAbsolutePath()+" does no exist");
        	System.exit(1);
        }
        
        // loads global properties file if specified in the command line args
        // or otherwise tries to find it in the default locations
        List<File> bigsPropertiesLocations = new ArrayList<File>();
        if (line.hasOption("c")) {
        	File f = new File (line.getOptionValue("c"));
        	if (f.exists()) bigsPropertiesLocations.add(f);
        }
        
        loadGlobalProperties(bigsPropertiesLocations);
                
        // the remaining args go the the command specied
        args = line.getArgs();
        if (args.length==0) {
            System.err.println("no command specified");
            printCmdlineSyntax();
            System.exit(1);
        }

        // launch the command
        String cmdName = args[0];
        args = Text.shift(args);
        Command cmd = Command.forName(cmdName);

        // exit if non existing command
        if (cmd==null) {
            System.err.println("invalid command '"+cmdName+"'\n");
            System.exit(1);
        }

        // check calling syntax
        if (!cmd.checkCallingSyntax(args)) {
        	System.err.println("invalid calling syntax for command '"+cmdName+"', syntax is:\n");
        	for (String s: cmd.getHelpString()) {
        		System.err.println("   "+s);
        	}
        	System.err.println();
        	System.exit(1);
        }
        
        cmd.run(args);

   }

   public static void printCmdlineSyntax() {
       HelpFormatter formatter = new HelpFormatter();
       formatter.printHelp( "bigs [options] [command] [command-options]", options );
   }
   
   /**
    * wrapper overloaded method with no arguments to allow directly loading global
    * properties only form default locations
    * @return true if properties could be loaded from some default location, false otherwise
    */
   public static Boolean loadGlobalProperties() {
	   return BIGS.loadGlobalProperties(null);
   }
   
   /**
    * Loads the global properties file by trying out locations specified as arguments and 
    * default ones
    * @param bigsPropertiesLocations a list of files specifying locations to try out to load global properties from
    * @return true if a properties file could be loaded from one of the specified and default locations, false otherwise
    */
   public static Boolean loadGlobalProperties(List<File> bigsPropertiesLocations) {
	   if (bigsPropertiesLocations == null) {
		   bigsPropertiesLocations = new ArrayList<File>();
	   }
       bigsPropertiesLocations.add(new File ("bigs.properties"));
       bigsPropertiesLocations.add(new File ("/etc/bigs.properties"));

       Boolean propertiesLoaded = false;
       for (File f: bigsPropertiesLocations) {	
    	   if (f.exists()) {
				try {
					globalProperties = new BIGSProperties();
					globalProperties.load(new FileReader(f));
				} catch (Exception e) {
					e.printStackTrace();
					throw new BIGSException("error loading global properties from "+f.getAbsolutePath()+", "+e.getMessage());
				}
				Log.setLogLevel(globalProperties.getProperty("bigs.loglevel"));
				Log.info("using global properties at "+f.getAbsolutePath());
				propertiesLoaded = true;
				break;
       	   }
       }
       
       if (!propertiesLoaded) {
    	   Log.info("no global properties loaded");
       }
       
       return propertiesLoaded;
   }

}
