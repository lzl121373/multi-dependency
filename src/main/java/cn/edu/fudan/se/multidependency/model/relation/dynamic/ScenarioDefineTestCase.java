package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_SCENARIO_DEFINE_TESTCASE)
public class ScenarioDefineTestCase implements Relation {

	private static final long serialVersionUID = -3711251531514529174L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	public ScenarioDefineTestCase(Scenario scenario, TestCase testCase) {
		this.scenario = scenario;
		this.testCase = testCase;
	}
	
	@StartNode
	private Scenario scenario;
	
	@EndNode
	private TestCase testCase;

	@Override
	public Node getStartNode() {
		return scenario;
	}

	@Override
	public Node getEndNode() {
		return testCase;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.SCENARIO_DEFINE_TESTCASE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}

}
