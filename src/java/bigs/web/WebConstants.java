package bigs.web;

public class WebConstants {

	// servlets producing html content
	public final static String CONFIG_IN_REPEAT_SERVLET_NAME = "/configinrepeat";
	public final static String EXPLORATION_DETAIL_SERVLET_NAME = "/exploration";
	public final static String EXPLORATIONS_SUMMARY_SERVLET_NAME = "/explorations";
	public final static String RUN_ROC_CURVE_SERVLET_NAME = "/rocforun";
	
	// servlets producing charts (img/png)
	public final static String RUNS_IN_CONFIG_CHART_SERVLET_NAME = "/runsinconfigchart";

	// servlets producing matlab code (application/matlab)
	public final static String DATASET_IN_RUN_SERVLET_NAME = "/datasetinrun";
	public final static String EXPLORATION_RESULTS_SERVLET_NAME = "/explorationresults";
	public final static String EXPLORATION_RESULTS_FOR_ENGINE_SERVLET_NAME = "/explorationresultsforengine";
	
	// servlets producing JNLP files
	public final static String JNLP_SERVLET_NAME = "/jnlp";
	
}
