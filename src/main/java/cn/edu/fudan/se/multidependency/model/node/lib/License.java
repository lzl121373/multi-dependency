package cn.edu.fudan.se.multidependency.model.node.lib;

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
public class License implements Node {

	private static final long serialVersionUID = -6384195929315642970L;

	@Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private String name;
    
    private String version;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("version", getVersion() == null ? "" : getVersion());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.License;
	}
	
}
