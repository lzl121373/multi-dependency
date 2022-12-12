package cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCallFunctionByTestCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_SPAN_START_WITH_FUNCTION)
public class SpanStartWithFunction implements DynamicCallFunctionByTestCase {

	private static final long serialVersionUID = 7462518725070039162L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Span span;
	
	@EndNode
	private Function function;
	
	private String traceId;
	
	private Integer testCaseId;
	
	public SpanStartWithFunction(Span span, Function function) {
		this.span = span;
		this.function = function;
		this.traceId = span.getTraceId();
	}
	
	@Override
	public Node getStartNode() {
		return span;
	}

	@Override
	public Node getEndNode() {
		return function;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.SPAN_START_WITH_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		properties.put("testCaseId", getTestCaseId() == null ? -1 : getTestCaseId());
		return properties;
	}

}
