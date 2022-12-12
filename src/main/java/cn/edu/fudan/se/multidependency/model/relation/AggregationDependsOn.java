package cn.edu.fudan.se.multidependency.model.relation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_AGGREGATION_DEPENDS_ON)
@EqualsAndHashCode
public class AggregationDependsOn implements Relation, RelationWithTimes{

	private static final long serialVersionUID = 6381791099417646137L;

	@Id
    @GeneratedValue
    private Long id;

    @StartNode
	private Node startNode;

    @EndNode
	private Node endNode;

	private int times = 0;

	private String dependsOnType = "";

	@Properties(allowCast = true)
	private Map<String, Long> dependsOnTypes = new HashMap<>();

	private double weightedTimes;

	public AggregationDependsOn(DependsOn d) {
		this.startNode = d.getStartNode();
		this.endNode = d.getEndNode();
		this.times = d.getTimes();
		this.dependsOnType = d.getDependsOnType();
		this.dependsOnTypes.putAll(d.getDependsOnTypes());
		this.weightedTimes = d.getWeightedTimes();
	}

	@Override
	public Node getStartNode() {
		return startNode;
	}

	@Override
	public Node getEndNode() {
		return endNode;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.AGGREGATION_DEPENDS_ON;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		properties.put("dependsOnType", getDependsOnType());
		return properties;
	}

	@Override
	public void addTimes() {
		this.times++;
	}
}
