package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DEPENDS_ON)
@EqualsAndHashCode
public class DependsOn implements Relation, RelationWithTimes{
	
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

	@Transient
	private boolean isAggregatePackagePair = false;

	@Properties(allowCast = true)
	private Map<String, Long> dependsOnTypes = new HashMap<>();

	private double weightedTimes;

	public DependsOn(DependsOn d) {
		this.startNode = d.getStartNode();
		this.endNode = d.getEndNode();
		this.times = d.getTimes();
		this.dependsOnType = d.getDependsOnType();
		this.dependsOnTypes.putAll(d.getDependsOnTypes());
		this.isAggregatePackagePair = d.isAggregatePackagePair();
		this.weightedTimes = d.getWeightedTimes();
	}

	public DependsOn(Node startNode, Node endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.dependsOnType = "";
	}
	public DependsOn(Node startNode, Node endNode, String dependsOnType) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.dependsOnType = dependsOnType;
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
		return RelationType.DEPENDS_ON;
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

	public void addWeightedTimes(double weightedTimes) {
		this.weightedTimes += weightedTimes;
	}

	public void addDependsTypes(Relation relation){
		int times = 0;
		if(relation instanceof RelationWithTimes){
			times += ((RelationWithTimes) relation).getTimes();
		}else {
			times += 1;
		}

		Long dependsTimes = dependsOnTypes.get(relation.getRelationType().toString());
		if (dependsTimes != null){
			dependsTimes += times;
			dependsOnTypes.put(relation.getRelationType().toString(), dependsTimes);
		} else {
			dependsOnTypes.put(relation.getRelationType().toString(), Long.valueOf(times));
		}

		Double weight = RelationType.relationWeights.get(relation.getRelationType());
		weightedTimes += ( weight != null ? times * weight : 0 ) ;
	}
}
