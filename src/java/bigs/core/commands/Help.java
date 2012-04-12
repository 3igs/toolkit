package bigs.core.commands;

import bigs.core.utils.Text;

/**
 *
 * @author rlx
 */
public class Help extends Command {

    @Override
    public String[] getHelpString() {
        return new String[] {
            "'help' displays the list of available commands",
            "'help command' displays help on specific command"
        };
    }
    
    @Override
    public Boolean checkCallingSyntax(String[] args) {
    	return (args.length<=1);
    }

    @Override
    public void run(String[] args) {
        if (args.length==0) {
            System.out.println("\navailable commands: \n");
            for (Command cmd:Command.availableCommands) {
                System.out.println(Text.leftJustifyExact(cmd.getCommandName(), 20)+" "+cmd.getDescription());
            }
            System.out.println("\ntype 'bigs help <command>' for further information on a specific command");
        } else {
            String cmdName = args[0];
            Command cmd = Command.forName(args[0]);
            if (cmd==null) {
                System.err.println("invalid command '"+cmdName+"'");
                System.exit(1);
            } else {
                System.out.println("command '"+cmdName+"', "+cmd.getDescription()+"\n");
                for (String s:cmd.getHelpString()) {
                    System.out.println("  "+s);
                }
                System.out.println();
            }
        }
    }

    @Override
    public String getCommandName() {
        return("help");
    }

    @Override
    public String getDescription() {
        return ("displays help on commands");
    }



}
