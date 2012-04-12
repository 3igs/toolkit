package test.bigs.core;

import java.io.FileReader;
import java.util.List;

import junit.framework.Assert;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bigs.api.storage.DataSource;
import bigs.core.BIGS;
import bigs.core.explorations.Evaluation;
import bigs.core.explorations.Exploration;
import bigs.core.explorations.ExplorationStage;
import bigs.core.utils.Core;
import bigs.core.utils.Log;


public class TestExplorations {

	
	@BeforeClass
	public static void initalize() {
		if (BIGS.globalProperties==null) BIGS.loadGlobalProperties();
		Utils.initializeBIGSTable();
		Utils.initializeExplorationsTable();
		Utils.initializeEvaluationsTable();
	}

	@Test
	public void testExplorationLoadFromProperties() throws Exception {
		Exploration expl001 = new Exploration();
		expl001.load (new FileReader("src/testdata/explorations/exploration.properties.001"));
		List<ExplorationStage> stages = expl001.getStages();
		Assert.assertEquals("incorrect number of stages in exploration 001", 1, stages.size());
		List<Evaluation> evals = stages.get(0).generateEvaluations();
		Assert.assertEquals("incorrect number of generated evaluations in exploration 001 stage 1", 47, evals.size());
				
	}
	
	@Test
	public void testExplorationSaveLoad() throws Exception {
		// load exploration from file
		Exploration expl001 = new Exploration();
		expl001.load (new FileReader("src/testdata/explorations/exploration.properties.001"));

		// save exploration and evaluations
		expl001.save();
		expl001.getStages().get(0).saveEvaluations();
		Log.info("saved exploration with number "+expl001.getExplorationNumber());

		// force saving a new exploration and evaluations
		expl001.cleanExplorationNumber();
		expl001.save();
		ExplorationStage s = expl001.getStages().get(0);
		s.setNumberOfSplits(s.getNumberOfSplits()+1);
		s.saveEvaluations();
		List<Evaluation> evalsFromFile = s.generateEvaluations();
		Log.info("saved exploration with number "+expl001.getExplorationNumber());
				
		// force saving a new exploration and evaluations
		expl001.cleanExplorationNumber();
		expl001.save();
		expl001.getStages().get(0).saveEvaluations();
		Log.info("saved exploration with number "+expl001.getExplorationNumber());

		// retrieve all evaluations from the second saved exploration and compare
		List<Evaluation> evalsFromDB = expl001.getAllEvaluations(Core.NOCACHE);
		Assert.assertEquals("incorrect number of evaluations retrieved from DB ",evalsFromFile.size(), evalsFromDB.size());
		
		for (int i=0; i<evalsFromFile.size(); i++) {
			Evaluation e1 = evalsFromFile.get(i);
			Evaluation e2 = evalsFromDB.get(i);
			Assert.assertTrue("unequal evaluations retrieved from DB", e1.equals(e2));
		}
		
	}
	
	@AfterClass
	public static void cleanup() {
		DataSource dataSource = BIGS.globalProperties.getConfiguredDataSource();
//		dataSource.dropTable(ExplorationProperties.tableName);
//		dataSource.dropTable(Evaluation.tableName);
//		Log.info("tables for explorations and evaluations dropped");
		Log.info("test data kept in tables for user inspection");
	}
}
