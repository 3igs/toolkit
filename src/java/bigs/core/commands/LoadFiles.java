package bigs.core.commands;

import java.io.File;

import bigs.api.storage.DataSource;
import bigs.api.utils.TextUtils;
import bigs.core.BIGS;
import bigs.core.utils.Data;
import bigs.core.utils.Log;
import bigs.core.utils.Text;


public class LoadFiles extends Command {

	@Override
	public String getCommandName() {
		return "load.files";
	}

	@Override
	public String[] getHelpString() {
		return new String[] { "'bigs "+this.getCommandName()+" tableName fileOrDir1 fileOrDir2'" };
	}

	@Override
	public void run(String[] args) throws Exception {
		
		String tableName = args[0];
    	DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource();
    	Data.createDataTableIfDoesNotExist(dataSource, tableName);
    	
    	Long fileNumber = 1L;
    	
		for (int i=1; i<args.length; i++) {
			String fileName = args[i];
			File fileOrDir = new File(fileName);
			if (!fileOrDir.exists()) {
				Log.info("file "+fileOrDir.getAbsolutePath()+" does not exist, skipping");
				continue;
			}
			if (fileOrDir.isFile()) {
				Data.uploadFile(fileOrDir, dataSource, tableName);
			} else if (fileOrDir.isDirectory()) {
				for (File f: fileOrDir.listFiles()) {
					if (f.isFile()) {
						Data.uploadFile(f, dataSource, tableName);
						Log.info("["+Text.zeroPad(fileNumber)+"] file "+f.getName()+" loaded into table "+tableName);
						fileNumber ++;
					}
				}
			}
		}

	}
	
	@Override
	public String getDescription() {
		return "loads files into the specified table";
	}

	@Override
	public Boolean checkCallingSyntax(String[] args) {
		return args.length>=2;
	}

}
