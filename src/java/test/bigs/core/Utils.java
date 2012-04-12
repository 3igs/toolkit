package test.bigs.core;

import bigs.api.exceptions.BIGSException;
import bigs.api.storage.DataSource;
import bigs.core.BIGS;
import bigs.core.BIGSProperties;
import bigs.core.explorations.Evaluation;
import bigs.core.explorations.Exploration;
import bigs.core.utils.Data;
import bigs.core.utils.Log;

public class Utils {

	public static Boolean bigsTableInitialized = false;
	public static Boolean evaluationsTableInitialized = false;
	public static Boolean explorationsTableInitialized = false;
	public static Boolean globalPropertiesLoaded = false;
	
	public static void initializeBIGSTable() {
		if (bigsTableInitialized) return;
		DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource(BIGSProperties.DONOT_CREATE_TABLES);
		Data.createTableIfDoesNotExist(dataSource, BIGS.tableName, BIGS.columnFamilies);
		bigsTableInitialized = true;
	}
	
	public static void initializeExplorationsTable() {
		if (explorationsTableInitialized) return ;		
		DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource(BIGSProperties.DONOT_CREATE_TABLES);
		if (dataSource.existsTable(Exploration.tableName)) {
			throw new BIGSException("must delete table "+Exploration.tableName+" before starting tests");
		}
		Data.createTableIfDoesNotExist(dataSource, Exploration.tableName, Exploration.columnFamilies);
		explorationsTableInitialized = true;
	}
	
	public static void initializeEvaluationsTable() {
		if (evaluationsTableInitialized) return ;		
		DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource(BIGSProperties.DONOT_CREATE_TABLES);
		if (dataSource.existsTable(Evaluation.tableName)) {
			Log.info("table "+Evaluation.tableName+" exists. Please delete it before staring tests");
			throw new BIGSException("must delete table "+Evaluation.tableName+" before starting tests");
		}
		Data.createTableIfDoesNotExist(dataSource, Evaluation.tableName, Evaluation.columnFamilies);
		evaluationsTableInitialized = true;
	}
}
