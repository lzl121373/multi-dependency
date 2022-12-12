package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Bug implements Node {

	private static final long serialVersionUID = 4123992909482501778L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private String name;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1L : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Bug;
	}
	
	public static final String LABEL_INDEX = "name";

}
