package bigs.api.core;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import bigs.api.core.BIGSParam;
import bigs.api.exceptions.BIGSException;
import bigs.core.utils.Text;

public abstract class Algorithm implements Configurable {

	public static Integer ROWKEYPREFIX_EXPLORATION_CONFIG_STAGE = 1;
	public static Integer ROWKEYPREFIX_EXPLORATION_CONFIG       = 2;
	
	/**
	 * generic placeholder method, processing and producing uninterpreted arrays of bytes
	 * 
	 * @param arg
	 * @return
	 */
	public abstract byte[] run (byte[] arg);
	
	/**
	 * must return one of the ROWKEYPREFIX values declared above. This defines what prefix
	 * will be added to the rowkey of the input data when storing the output data
	 * 
	 * @return
	 */
	public abstract Integer outputDataRowkeyPrefix();
	
	public String getDescription() {
		return "";
	}
	
	/**
	 * checks for equality of two objects by testing if the fields annotated by the
	 * BIGSParam class have the same values
	 */
	public boolean equals(Object other) {
		if (!this.getClass().getName().equals(other.getClass().getName())) return false;
		
		for (Field field: this.getClass().getFields()) {			
			if (field.isAnnotationPresent(BIGSParam.class)) {
				Object o1;
				try {
					o1 = field.get(this);
					Object o2 = field.get(other);
					if(!o1.equals(o2)) return false;
				} catch (Exception e) {
					throw new BIGSException("error retrieve Algorithm data by reflection "+e.getMessage());
				}
			}
		}
		
		return true;
	}
	
	/**
	 * returns a description of this algorithm together with the description of the
	 * fields marked as BIGSParam
	 * @return a list of Strings with the lines of the help text
	 */
	public List<String> getHelp() {
		List<String> r = new ArrayList<String>();
		
		r.add(Text.leftJustify(this.getClass().getName(),60)+"  "+this.getDescription());
		for (Field field: this.getClass().getFields()) {			
			if (field.isAnnotationPresent(BIGSParam.class)) {
				
				Annotation annotation = field.getAnnotation(BIGSParam.class);
				String description = "";
				if (annotation!=null) {
					try {
						description = (String)annotation.annotationType().getMethod("description").invoke(annotation);
					} catch (Exception e) {
						
					}
				}
				StringBuffer sb = new StringBuffer();
				sb.append(Text.rightJustify(field.getName(),30));
				sb.append(" ").append(Text.leftJustify(field.getType().getSimpleName(), 20));
				sb.append(" ").append(description);
				r.add(sb.toString());
			}
		}
		return r;
	}
}
