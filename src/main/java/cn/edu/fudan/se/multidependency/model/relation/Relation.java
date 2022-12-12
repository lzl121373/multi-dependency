package cn.edu.fudan.se.multidependency.model.relation;

import java.io.Serializable;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;

public interface Relation extends Serializable {
	
	Long getId();
	
	void setId(Long id);

	default Long getStartNodeGraphId() {
		return getStartNode().getId();
	}
	
	default Long getEndNodeGraphId() {
		return getEndNode().getId();
	}
	
	Node getStartNode();
	
	Node getEndNode();
	
	RelationType getRelationType();
	
	Map<String, Object> getProperties();
	
}
