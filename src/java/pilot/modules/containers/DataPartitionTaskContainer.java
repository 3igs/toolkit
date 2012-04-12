package pilot.modules.containers;

import java.util.ArrayList;
import java.util.List;

import pilot.core.DataItem;
import pilot.core.Task;
import pilot.core.TaskContainer;

import bigs.api.core.BIGSParam;



public class DataPartitionTaskContainer extends TaskContainer {

	@BIGSParam
	public Integer numberOfPartitions = 1;
	
	Integer partitionNumber = null;
	
	public DataPartitionTaskContainer() {}
	
	public DataPartitionTaskContainer(Integer numberOfPartitions, Integer partitionNumber) {
		this.numberOfPartitions = numberOfPartitions;
		this.partitionNumber = partitionNumber;
	}
	
	public Integer getNumberOfPartitions() {
		return this.numberOfPartitions;
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
		for (int i=1; i<= this.numberOfPartitions; i++) {
			TaskContainer tb = new DataPartitionTaskContainer(this.numberOfPartitions, i);
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
		return true;
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
		return "DataPartitionTaskContainer [numberOfPartitions="
				+ numberOfPartitions + ", partitionNumber=" + partitionNumber
				+ "]";
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
