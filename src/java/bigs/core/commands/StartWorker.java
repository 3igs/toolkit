package bigs.core.commands;

import bigs.core.worker.Worker;

public class StartWorker extends Command {

	@Override
	public String getCommandName() {
		return "worker";
	}

	@Override
	public String[] getHelpString() {
		return new String[]{ "'bigs "+getCommandName() };
	}

	@Override
	public void run(String[] args) throws Exception {		
		Worker worker = new Worker();
		worker.start();
	}

	@Override
	public String getDescription() {
		return "starts one worker";
	}

	@Override
	public Boolean checkCallingSyntax(String[] args) {
		if (args.length!=0) return false;
		return true;
	}

}
