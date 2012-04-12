package pilot.core;

import java.util.List;

public class TopLevelTaskContainer extends TaskContainer {

	@Override
	public List<Class<? extends TaskContainer>> allowedTaskContainers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Class<? extends Task>> allowedTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TaskContainer> generateTaskContainers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean supportsParallelization() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processPreSubContainers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPostSubContainers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPreContainer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPostContainer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPreDataBlock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataItem processDataItem(DataItem dataItem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processPostDataBlock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return "TopLevelTaskContainer []";
	}

}
