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
public class Scenario implements Node {

	private static final long serialVersionUID = 2260001955112320935L;

	private String name;
	
    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private Integer scenarioId;
    
    private String description;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("description", getDescription() == null ? "" : getDescription());
		properties.put("scenarioId", getScenarioId() == null ? -1 : getScenarioId());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Scenario;
	}
	
}
