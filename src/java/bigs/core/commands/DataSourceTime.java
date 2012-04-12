package bigs.core.commands;

import java.util.Date;

import bigs.api.storage.DataSource;
import bigs.api.utils.TextUtils;
import bigs.core.BIGS;
import bigs.core.BIGSProperties;
import bigs.core.utils.Core;
import bigs.core.utils.Log;


public class DataSourceTime extends Command {

	@Override
	public String getCommandName() {
		return "ds.time";
	}

	@Override
	public String[] getHelpString() {
		return new String[]{"'bigs "+this.getCommandName()};
	}

	@Override
	public void run(String[] args) throws Exception {
		
		Log.info("time before calibration is "+TextUtils.FULLDATE.format(new Date(Core.getTime())));
		// retrieving the datasource already forces time calibration
		BIGS.globalProperties.getConfiguredDataSource(BIGSProperties.DONOT_CREATE_TABLES);

		Log.info("time after calibration is  "+TextUtils.FULLDATE.format(new Date(Core.getTime())));
		Log.info("time again is              "+TextUtils.FULLDATE.format(new Date(Core.getTime())));
	}

	@Override
	public String getDescription() {
		return "retrieves the time of the referenced data source";
	}

	@Override
	public Boolean checkCallingSyntax(String[] args) {
		if (args.length!=0 ) return false;
		return true;
	}

}