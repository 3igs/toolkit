package pilot.core;

import java.util.ArrayList;
import java.util.List;

public class DataItem implements TextRepresentable {

	List<List<Double>> data = new ArrayList<List<Double>>();
	
	public DataItem(List<List<Double>> data) {
		this.data = data;
	}
	
	public List<List<Double>> getData() {
		return data;
	}

	@Override
	public String toTextRepresentation() {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<data.size(); i++) {
			List<Double> row = data.get(i);
			for (Double d: row) {
				sb.append(d).append(" ");
			}
			if (i!=data.size()-1) {
				sb.append("; ");
			}			
		}
		return sb.toString();
		
	}

	@Override
	public TextRepresentable fromTextRepresentation(String textRepresentation) {
		// TODO Auto-generated method stub
		return null;
	}

}
