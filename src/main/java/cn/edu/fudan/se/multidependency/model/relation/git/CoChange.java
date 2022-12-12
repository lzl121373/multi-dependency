package cn.edu.fudan.se.multidependency.model.relation.git;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import org.neo4j.ogm.annotation.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CO_CHANGE)
public class CoChange implements Relation, RelationWithTimes {

	private static final long serialVersionUID = -8677714146194368352L;

	@Id
	@GeneratedValue
	private Long id;

	@StartNode
	private Node node1;

	@EndNode
	private Node node2;

	private int times = 1;

	private String coChangeType = "";

	private int node1ChangeTimes = 0;

	private int node2ChangeTimes = 0;

	private double node1Confidence = 0.0;

	private double node2Confidence = 0.0;

	public CoChange(Node node1, Node node2) {
		this.node1 = node1;
		this.node2 = node2;
	}

	public CoChange(Node node1, Node node2, String coChangeType) {
		this.node1 = node1;
		this.node2 = node2;
		this.coChangeType = coChangeType;
	}

	@Override
	public void addTimes() {
		this.times++;
	}

	@Override
	public Node getStartNode() {
		return node1;
	}

	@Override
	public Node getEndNode() {
		return node2;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CO_CHANGE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", this.getTimes());
		properties.put("coChangeType", getCoChangeType());
		properties.put("node1ChangeTimes", getNode1ChangeTimes());
		properties.put("node2ChangeTimes", getNode2ChangeTimes());
		properties.put("node1Confidence", getNode1Confidence());
		properties.put("node2Confidence", getNode2Confidence());
		return properties;
	}
}