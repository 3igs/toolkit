package pilot.modules.ml;

import bigs.api.core.BIGSParam;
import pilot.core.DataItem;
import pilot.core.TextRepresentable;
import pilot.modules.containers.DataPartitionTask;

public class KMeans implements DataPartitionTask {

	@BIGSParam
	public Integer numberOfCentroids;
	
	@Override
	public DataItem processDataItem(DataItem item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeProcessingPartition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterProcessingPartition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toTextRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextRepresentable fromTextRepresentation(String textRepresentation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "KMeans [numberOfCentroids=" + numberOfCentroids + "]";
	}	
	
}
