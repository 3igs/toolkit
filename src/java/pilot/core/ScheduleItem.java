package pilot.core;

import bigs.core.utils.Text;

public class ScheduleItem {

	String className;
	String objectTextRepresentation;
	String methodName;
	Integer priority;
	ScheduleItem parent;
	
	public ScheduleItem (String className, String objectTextRepresentation, String methodName, Integer priority, ScheduleItem parent) {
		this.className = className;
		this.objectTextRepresentation = objectTextRepresentation;
		this.methodName = methodName;
		this.priority = priority;
		this.parent = parent;
	}
	
	public String toString() {
		String r = Text.zeroPad(new Long(priority), 3)  + " " + className + " " + methodName + " " + objectTextRepresentation;
		if (parent!=null) {
			r = r + "\n   "+Text.zeroPad(new Long(parent.priority), 3)  + " " + parent.className + " " + parent.methodName + " " + parent.objectTextRepresentation;
		}
		return r;
	}
	
}
