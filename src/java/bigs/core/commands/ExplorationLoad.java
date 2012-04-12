package bigs.core.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import bigs.api.exceptions.BIGSException;
import bigs.core.explorations.Exploration;
import bigs.core.utils.Log;


public class ExplorationLoad extends Command {

	@Override
	public String getCommandName() {		
		return "exploration.load";
	}

	@Override
	public String[] getHelpString() {
		return new String[]{"'bigs "+this.getCommandName()+" explorationFile'"};
	}

    @Override
    public Boolean checkCallingSyntax(String[] args) {
    	return (args.length==1);
    }

    @Override
	public void run(String[] args) {
		File props = new File(args[0]);
		ExplorationLoad.loadExploration(props);
	}
    
    public static void loadExploration(File explorationFile) {
		try {
			Log.info("using exploration properties file at "+explorationFile.toString());

			Exploration expl = new Exploration();
			expl.load(new FileReader(explorationFile));
			expl.save();
			Log.info("saved exploration "+expl.toString());
			
		} catch (FileNotFoundException e) {
			throw new BIGSException("file "+explorationFile.toString()+" not found");
		} 
    	
    }

	@Override
	public String getDescription() {
		return "processes and loads an exploration into BIGS";
	}

}
