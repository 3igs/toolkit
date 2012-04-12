package pilot.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import bigs.api.core.BIGSParam;
import bigs.api.core.Configurable;
import bigs.api.exceptions.BIGSException;
import bigs.core.exceptions.BIGSPropertyNotFoundException;
import bigs.core.utils.Core;
import bigs.core.utils.Log;
import bigs.core.utils.Text;

public abstract class TaskContainer implements Configurable {

	List<TaskContainer> taskContainers = new ArrayList<TaskContainer>();

	TaskContainer parentTaskContainer = null;
	
	PipelineStage pipelineStage = null;
		
	public abstract List<Class<? extends TaskContainer>> allowedTaskContainers();		
	
	public abstract List<Class<? extends Task>> allowedTasks();
		
	public abstract List<TaskContainer> generateMyTaskContainers();	
	
	public abstract Boolean supportsParallelization();

	
	public abstract void processPreSubContainers();
	
	public abstract void processPostSubContainers();
		
	
	public abstract void processPreMyContainers();
	
	public abstract void processPostMyContainers();	

	
	public abstract void processPreDataBlock();
	
	public abstract DataItem processDataItem(DataItem dataItem);

	public abstract void processPostDataBlock();
			
	
	public List<TaskContainer> getTaskLevels() {
		return taskContainers;
	}
		
	public void addTaskContainer(TaskContainer taskContainer) {
		taskContainers.add(taskContainer);
		taskContainer.setParentTaskContainer(this);
	}

	public TaskContainer getParentTaskContainer() {
		return parentTaskContainer;
	}

	public void setParentTaskContainer(TaskContainer parentTaskContainer) {
		this.parentTaskContainer = parentTaskContainer;
	}

	public PipelineStage getPipelineStage() {
		return pipelineStage;
	}

	public void setPipelineStage(PipelineStage pipelineStage) {
		this.pipelineStage = pipelineStage;
	}
		
	public void printOut(String prefix) {

		System.out.println(prefix+this.toString());
		if (!this.taskContainers.isEmpty()) {
			for (TaskContainer tb: this.taskContainers) {
				tb.printOut(prefix+"     ");
			}
		} 
	}

	
	public Integer list(String prefix, Integer priority, List<ScheduleItem> schedule, ScheduleItem parentScheduleItem) {
		if (!this.taskContainers.isEmpty()) {
			
			ScheduleItem p1 = new ScheduleItem(this.getClass().getName(), this.toString(), "preSubContainers", priority, parentScheduleItem);
			schedule.add(p1);
			System.out.println(Text.zeroPad(new Long(priority), 3)+" "+prefix+this.toString()+".preSubContainers");			

			TaskContainer th = this.taskContainers.get(0).clone();
			priority++;
			ScheduleItem p2 = new ScheduleItem(th.getClass().getName(), th.toString(), "preMyContainers", priority, p1);
			schedule.add(p2);
			System.out.println(Text.zeroPad(new Long(priority), 3)+" "+prefix+"   "+th.toString()+".preMyContainers");

			priority++;			
			Integer returningPriority = priority;
			for (TaskContainer tb: this.taskContainers) {
				returningPriority = tb.list(prefix+"      ", priority, schedule, p2);
				if (tb!=this.taskContainers.get(this.taskContainers.size()-1) && !tb.supportsParallelization()) {
					priority = returningPriority + 1;
				}
			}
			priority = returningPriority + 1;
			schedule.add(new ScheduleItem(th.getClass().getName(), th.toString(), "postMyContainers", priority, p1));
			System.out.println(Text.zeroPad(new Long(priority), 3)+" "+prefix+"   "+th.toString()+".postMyContainers");

			priority++;
			schedule.add(new ScheduleItem(this.getClass().getName(), this.toString(), "postSubContainers", priority, parentScheduleItem));
			System.out.println(Text.zeroPad(new Long(priority), 3)+" "+prefix+this.toString()+".postSubContainers");
			return priority;
		} else {
			schedule.add(new ScheduleItem(this.getClass().getName(), this.toString(), "LOOP processDataItem", priority, parentScheduleItem));
			System.out.println(Text.zeroPad(new Long(priority), 3)+" "+prefix+this.toString()+" LOOP processDataItem");			
			return priority;
		}
	}
	
	public Boolean allowsTaskContainer(Class<? extends TaskContainer> subContainerClass) {
		if (this.allowedTaskContainers()==null) {
			return false;
		} else {
			Boolean classAllowed = false;
			for (Class<? extends TaskContainer> c: this.allowedTaskContainers()) {
				if (c.isAssignableFrom(subContainerClass)) {
					classAllowed = true;
					break;
				}
			}
			return classAllowed;
		}		
	}

	public static List<TaskContainer> fromProperties(Properties properties, String propertiesPrefix, Integer containerNumber) {
		List<TaskContainer> r = new ArrayList<TaskContainer>();
		String containerPropertyName = "container."+Text.zeroPad(new Long(containerNumber), 2);
		try {
			TaskContainer container = Core.getConfiguredObject(containerPropertyName, TaskContainer.class, properties, propertiesPrefix);
			for (TaskContainer tc: container.generateMyTaskContainers()) {
				r.add(tc);
				List<TaskContainer> subContainers = TaskContainer.fromProperties(properties, propertiesPrefix, containerNumber+1);				
				if (subContainers!=null) {
					for (TaskContainer stc: subContainers) {
						if (tc.allowsTaskContainer(stc.getClass())) {
							tc.addTaskContainer(stc);
						} else {
							throw new BIGSException(tc.getClass().getSimpleName()+" does not allow task containers of type "+stc.getClass().getSimpleName());
						}
					}
				}
			}
			return r;
		} catch (BIGSPropertyNotFoundException e) {
			return null;
		}			
	}

	public TaskContainer clone() {
		Class<? extends TaskContainer> thisClass = this.getClass();
		TaskContainer r;
		try {
			r = (TaskContainer)thisClass.newInstance();
			for (Field field: thisClass.getFields()) {
				if (field.isAnnotationPresent(BIGSParam.class)) {
					field.set(r, field.get(this));
				}
			}
			return r;
		} catch (InstantiationException e) {
			throw new BIGSException("InstantiationExpception cloning TaskContainer. "+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new BIGSException("IllegaAccesExpception cloning TaskContainer. "+e.getMessage());
		}
	}

}
