package bigs.core.commands;

/**
 *
 * @author rlx
 */
public abstract class Command {

	/**
	 * the list of available commands
	 */
    final static Command[] availableCommands = new Command[]{
    	new LoadFiles(),
    	new DownloadFiles(),
    	new ExplorationLoad(),
    	new ExplorationPrepare(),
    	new ExplorationInfo(),
    	new ExplorationStatus(),
    	new StartWorker(),
    	new StartWeb(),
    	new ShowFeatureExtractors(),
    	new DataSourceTime(),
        new Test(),
        new Help()
    };

    /**
     * finds a command by its name
     * @param cmdName
     * @return
     */
    public static Command forName(String cmdName) {
        for (Command cmd:availableCommands) {
            if (cmd.getCommandName().equals(cmdName)) return cmd;
        }
        return null;
    }

    public abstract String getCommandName();
    public abstract String[] getHelpString();
    public abstract void run(String args[]) throws Exception;
    public abstract String getDescription();
    public abstract Boolean checkCallingSyntax(String args[]);

}
