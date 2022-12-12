package cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_SPAN_CALL_SPAN)
public class SpanCallSpan implements Relation {
	
	private static final long serialVersionUID = -24758961478391792L;
	
	public SpanCallSpan(Span span, Span callSpan) {
		this.span = span;
		this.callSpan = callSpan;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Span span;
	
	@EndNode
	private Span callSpan;
	
	private String httpRequestMethod;
	
	private String requestSpanId;
	
	private String requestTraceId;
	
	@Override
	public Node getStartNode() {
		return span;
	}

	@Override
	public Node getEndNode() {
		return callSpan;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.SPAN_CALL_SPAN;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
//		properties.put("startServiceName", span.getServiceName());
//		properties.put("endServiceName", callSpan.getServiceName());
		properties.put("httpRequestMethod", getHttpRequestMethod() == null ? "" : getHttpRequestMethod());
		properties.put("requestSpanId", getRequestSpanId() == null ? "" : getRequestSpanId());
		properties.put("requestTraceId", getRequestTraceId() == null ? "" : getRequestTraceId());
		return properties;
	}

}
