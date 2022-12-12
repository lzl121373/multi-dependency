package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import cn.edu.fudan.se.multidependency.model.relation.Relation;

public interface DynamicCallFunctionByTestCase extends Relation {
	
	String getTraceId();
	
	Integer getTestCaseId();
	
	void setTestCaseId(Integer testcaseId);
	
}
