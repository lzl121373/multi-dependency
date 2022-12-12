package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_TESTCASE_EXECUTE_FEATURE)
public class TestCaseExecuteFeature implements Relation {

	private static final long serialVersionUID = 3879809639261277046L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private TestCase testCase;
	
	@EndNode
	private Feature feature;
	
	public TestCaseExecuteFeature(TestCase testCase, Feature feature) {
		this.testCase = testCase;
		this.feature = feature;
	}

	@Override
	public Node getStartNode() {
		return testCase;
	}

	@Override
	public Node getEndNode() {
		return feature;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TESTCASE_EXECUTE_FEATURE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
