package pilot.modules.containers;

import pilot.core.DataItem;
import pilot.core.Task;

public interface DataPartitionTask extends Task {

	public void beforeProcessingPartition();
	
	public void afterProcessingPartition();
	
	public DataItem processDataItem(DataItem item);
	
}
