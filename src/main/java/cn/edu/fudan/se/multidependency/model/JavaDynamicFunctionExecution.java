package cn.edu.fudan.se.multidependency.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class JavaDynamicFunctionExecution extends DynamicFunctionExecution {
	protected Long order;
	protected Long depth;
	protected List<String> parameters = new ArrayList<>();
	protected boolean constructor;
	public void addParameter(String parameter) {
		this.parameters.add(parameter);
	}
	protected Long threadId;
	protected String threadName;
	
	public Language getLanguage() {
		return Language.java;
	}
	
}
