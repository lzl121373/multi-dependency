package cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_MICROSERVICE_CALL_MICROSERVICE)
public class MicroServiceCallMicroService implements Relation {
	private static final long serialVersionUID = -2020305373964102611L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private MicroService ms;
	
	@EndNode
	private MicroService callMs;
	
	private int times;
	
	@Transient
	private List<SpanCallSpan> spanCallSpans = new ArrayList<>();
	
	public MicroServiceCallMicroService(MicroService ms, MicroService callMs) {
		this.ms = ms;
		this.callMs = callMs;
		this.times = 0;
	}
	
	public void addTimes(Integer times) {
		this.times += times;
	}
	
	public void addSpanCallSpan(SpanCallSpan spanCallSpan) {
		this.spanCallSpans.add(spanCallSpan);
	}
	
	public List<SpanCallSpan> getSpanCallSpans() {
		return new ArrayList<>(this.spanCallSpans);
	}

	@Override
	public Node getStartNode() {
		return ms;
	}

	@Override
	public Node getEndNode() {
		return callMs;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.MICROSERVICE_CALL_MICROSERVICE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
}
