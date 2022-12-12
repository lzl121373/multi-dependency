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
public class Feature implements Node {

	private static final long serialVersionUID = -2410710967921462154L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private Integer featureId;
    
    private String name;
    
    private String description;
    
    private Integer parentFeatureId;
    
    public Feature(Integer featureId, String name, String description) {
    	this.featureId = featureId;
    	this.name = name;
    	this.description = description;
    }
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("featureId", getFeatureId() == null ? -1 : getFeatureId());
		properties.put("description", getDescription() == null ? "" : getDescription());
		properties.put("parentFeatureId", getParentFeatureId() == null ? -1 : getParentFeatureId());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Feature;
	}
	
}
