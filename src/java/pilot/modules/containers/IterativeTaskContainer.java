package pilot.modules.containers;

import java.util.ArrayList;
import java.util.List;

import bigs.api.core.BIGSParam;
import pilot.core.DataItem;
import pilot.core.Task;
import pilot.core.TaskContainer;

public class IterativeTaskContainer extends TaskContainer {

	@BIGSParam
	public Integer numberOfIterations;
	
	Integer iterationNumber = null;
	
	public IterativeTaskContainer() {
	}
	
	public IterativeTaskContainer(Integer numberOfIterations, Integer iterationNumber) {
		this.numberOfIterations = numberOfIterations;
		this.iterationNumber = iterationNumber;
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
	public List<TaskContainer> generateTaskContainers() {
		List<TaskContainer> r = new ArrayList<TaskContainer>();
		for (int i=1; i<= this.numberOfIterations; i++) {
			TaskContainer tb = new IterativeTaskContainer(this.numberOfIterations, i);
			r.add(tb);
		}		
		return r;
		
	}

	@Override
	public List<Class<? extends TaskContainer>> allowedTaskContainers() {
		List<Class<? extends TaskContainer>> r = new ArrayList<Class<? extends TaskContainer>>();
		r.add(TaskContainer.class);
		return r;
	}

	@Override
	public List<Class<? extends Task>> allowedTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean supportsParallelization() {
		return false;
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
		return "IterativeTaskContainer [numberOfIterations=" + numberOfIterations
				+ ", iterationNumber=" + iterationNumber + "]";
	}

	@Override
	public void processPreContainer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPostContainer() {
		// TODO Auto-generated method stub
		
	}

	
	
	
}
