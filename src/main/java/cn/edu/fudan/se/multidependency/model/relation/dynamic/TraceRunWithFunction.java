package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单体应用中，测试用例-trace-Function
 * 相当于是个Entry
 * @author fan
 *
 */
@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_TRACE_RUN_WITH_FUNCTION)
public class TraceRunWithFunction implements DynamicCallFunctionByTestCase {

	private static final long serialVersionUID = 1477035391487647267L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Trace trace;
	
	@EndNode
	private Function function;
	
	private Integer testCaseId;
	
	private String traceId;
	
	public TraceRunWithFunction(Trace trace, Function function) {
		this.trace = trace;
		this.function = function;
		this.traceId = trace.getTraceId();
	}
	
	private String order;
	
	@Override
	public Node getStartNode() {
		return trace;
	}

	@Override
	public Node getEndNode() {
		return function;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TRACE_RUN_WITH_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("order", getOrder() == null ? "" : order);
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		properties.put("testCaseId", getTestCaseId() == null ? -1 : getTestCaseId());
		return properties;
	}

}
