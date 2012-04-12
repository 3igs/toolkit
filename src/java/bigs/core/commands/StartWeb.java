package bigs.core.commands;

import bigs.api.exceptions.BIGSException;
import bigs.core.BIGS;
import bigs.web.Web;

public class StartWeb extends Command {

	@Override
	public String getCommandName() {
		return "web";
	}

	@Override
	public String[] getHelpString() {
		return new String[]{ "'bigs "+getCommandName() };
	}

	@Override
	public void run(String[] args) throws Exception {		
		
		
        Integer port = 9090;
        if (args.length==1) port = new Integer(args[0]);
                
        if (BIGS.bigsBaseDir==null) {
        	throw new BIGSException("BIGS installation directory not set. cannot start web server");
        }
        
        Web.startWeb(port);	

	}

	@Override
	public String getDescription() {
		return "starts web server";
	}

	@Override
	public Boolean checkCallingSyntax(String[] args) {
		if (args.length>1) return false;
		return true;
	}

}
