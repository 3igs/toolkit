package bigs.core.commands;

import java.lang.reflect.Modifier;
import java.util.List;

import bigs.api.featureextraction.FeatureExtractionAlgorithm;
import bigs.core.utils.Core;
import bigs.core.utils.Log;


public class ShowFeatureExtractors  extends Command {

	@Override
	public String getCommandName() {
		return "show.fe";
	}

	@Override
	public String[] getHelpString() {
		return new String[]{ "'bigs "+getCommandName() };
	}

	@Override
	public void run(String[] args) throws Exception {		
		
		Log.info("scanning libraries ...");
    	List<Class<? extends FeatureExtractionAlgorithm>> l = Core.getAllSubclasses(FeatureExtractionAlgorithm.class);
    	for (Class<? extends FeatureExtractionAlgorithm> c: l) {
    		if (!Modifier.isAbstract(c.getModifiers())) {
    			FeatureExtractionAlgorithm alg = c.newInstance();
    			for (String s: alg.getHelp()) {
    				System.out.println("     "+ s);
    			}
    			System.out.println();
    		}
    	}
	}

	@Override
	public String getDescription() {
		return "shows available features extractors";
	}

	@Override
	public Boolean checkCallingSyntax(String[] args) {
		if (args.length!=0) return false;
		return true;
	}

}
