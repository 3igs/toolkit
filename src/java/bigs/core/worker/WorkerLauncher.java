package bigs.core.worker;

import java.awt.GraphicsEnvironment;

import bigs.api.exceptions.BIGSException;
import bigs.core.BIGS;
import bigs.core.BIGSProperties;
import bigs.core.utils.Core;
import bigs.core.utils.Log;


/**
 * launches a worker accepting an encrypted configuration file as argument.
 * If we are running in a headless system (no X) this launches and text based worker.
 * Otherwise it launches a Worker Window.
 * @author rrp03
 *
 */
public class WorkerLauncher {
	
	public static void main (String args[]) {
		
		GraphicsEnvironment.getLocalGraphicsEnvironment(); 
		if (!GraphicsEnvironment.isHeadless())  {
			Log.info("launching worker window");
			WorkerWindow.main(args);
		} else {
			if (args.length>0) {
				Log.info("launching worker headless");
				BIGS.globalProperties = BIGSProperties.fromString(Core.decrypt(args[0]));
				Worker worker = new Worker();
				try {
					worker.start();
				} catch (BIGSException e) {
					Log.error(Core.getStackTrace(e));
				}				
			}			
		}				
	}
}
