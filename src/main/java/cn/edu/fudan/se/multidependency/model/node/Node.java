package cn.edu.fudan.se.multidependency.model.node;

import java.io.Serializable;
import java.util.Map;

public interface Node extends Serializable {

	Long getId();
	
	void setId(Long id);
	
	Long getEntityId();
	
	void setEntityId(Long entityId);

	Map<String, Object> getProperties();
	
	/**
	 * 为简单起见，一个节点只有一个neo4j节点标签
	 * @return
	 */
	NodeLabelType getNodeType();
	
	String getName();
	
}
