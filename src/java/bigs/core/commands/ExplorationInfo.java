package bigs.core.commands;

import bigs.core.explorations.Exploration;
import bigs.core.utils.Log;

public class ExplorationInfo extends Command {

	@Override
	public String getCommandName() {
		return "exploration.info";
	}

	@Override
	public String[] getHelpString() {
		return new String[]{"'bigs "+this.getCommandName()+" explorationNumber '"};
	}

	@Override
	public void run(String[] args) throws Exception {
		Long explorationNumber = new Long(args[0]);
		
		Exploration exploration = Exploration.fromExplorationNumber(explorationNumber);
		
		Log.info("exploration: "+exploration.toString());
		Log.info("");

		System.out.println(exploration.getInfo());
	}

	@Override
	public String getDescription() {
		return "retrieves information about an exploration and its evaluations";
	}

	@Override
	public Boolean checkCallingSyntax(String[] args) {
		if (args.length!=1 ) return false;
		try {
			new Long(args[0]);
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}

}
