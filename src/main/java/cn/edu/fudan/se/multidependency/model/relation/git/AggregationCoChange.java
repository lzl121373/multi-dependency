package cn.edu.fudan.se.multidependency.model.relation.git;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_AGGREGATION_CO_CHANGE)
public class AggregationCoChange implements Relation, RelationWithTimes {

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

	public AggregationCoChange(CoChange c) {
		this.node1 = c.getNode1();
		this.node2 = c.getNode2();
		this.times = c.getTimes();
		this.coChangeType = c.getCoChangeType();
		this.node1ChangeTimes = c.getNode1ChangeTimes();
		this.node2ChangeTimes = c.getNode2ChangeTimes();
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
		return RelationType.AGGREGATION_CO_CHANGE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", this.getTimes());
		properties.put("cochangeType", getCoChangeType());
		properties.put("node1ChangeTimes", getNode1ChangeTimes());
		properties.put("node2ChangeTimes", getNode2ChangeTimes());
		return properties;
	}
}
