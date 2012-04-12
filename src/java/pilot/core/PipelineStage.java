package pilot.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import bigs.api.exceptions.BIGSException;
import bigs.core.exceptions.BIGSPropertyNotFoundException;
import bigs.core.utils.Core;
import bigs.core.utils.Log;
import bigs.core.utils.Text;


public class PipelineStage {

	Integer stageNumber = 1;

	TaskContainer topLevelContainer;
	
	Task configuredTask;

	public Properties sourceProperties;


	public Task getConfiguredTask() {
		return configuredTask;
	}
	
	public Integer getStageNumber() {
		return this.stageNumber;
	}

	public Properties getSourceProperties() {
		return this.sourceProperties;
	}

	public static PipelineStage fromProperties(Properties properties, Integer stageNumber) {
		PipelineStage r = new PipelineStage();
		r.stageNumber = stageNumber;
		r.sourceProperties = properties;
		
		String stagePrefix = "stage."+Text.zeroPad(new Long(stageNumber), 2);

		try {
			r.configuredTask = Core.getConfiguredObject("task", Task.class, properties, stagePrefix);
			r.topLevelContainer = new TopLevelTaskContainer();
			List<TaskContainer> containers = TaskContainer.fromProperties(properties, stagePrefix, 1);
			
			if (containers!=null) {
				for (TaskContainer c: containers) {
					r.topLevelContainer.addTaskContainer(c);
				}
			}
			return r;
		} catch (BIGSPropertyNotFoundException e) {
			throw new BIGSException(e.getMessage());
		}
	}
	




	
	@Override
	public String toString() {
		return "PipelineStage [stageNumber=" + stageNumber
				+ ", topLevelContainer=" + topLevelContainer
				+ ", configuredTask=" + configuredTask + ", sourceProperties="
				+ sourceProperties + "]";
	}

	public List<ScheduleItem> list() {
		Log.info("Stage "+this.stageNumber);
		Log.info("configured task: "+this.configuredTask.toString());
		List<ScheduleItem> schedule = new ArrayList<ScheduleItem>();
		this.topLevelContainer.list("  ",0, schedule, null);
		return schedule;
	}
	
	public void printOut() {
		Log.info("Stage "+this.stageNumber);
		Log.info("configured task: "+this.configuredTask.toString());
		this.topLevelContainer.printOut("   ");
	}
	
}
