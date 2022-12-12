package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_TESTCASE_RUN_TRACE)
public class TestCaseRunTrace implements Relation {

	private static final long serialVersionUID = 1534483975155883947L;
	
	public TestCaseRunTrace(TestCase testCase, Trace trace) {
		this.testCase = testCase;
		this.trace = trace;
	}
	
	@Id
    @GeneratedValue
    private Long id;

	@StartNode
	private TestCase testCase;
	
	@EndNode
	private Trace trace;

	@Override
	public Node getStartNode() {
		return testCase;
	}

	@Override
	public Node getEndNode() {
		return trace;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TESTCASE_RUN_TRACE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
