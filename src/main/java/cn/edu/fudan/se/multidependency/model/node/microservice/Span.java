package cn.edu.fudan.se.multidependency.model.node.microservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Span implements Node {

	private static final long serialVersionUID = 1083944968038564253L;
	
	@Id
    @GeneratedValue
    private Long id;

    private Long entityId;
    
    private String traceId;
    
    private String spanId;
    
    private String operationName;
    
	private Long time;
	
	private Integer order;
	
	private String apiFunctionName;
	
	public boolean isStartSpan() {
		return order == 0;
	}
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("spanId", getSpanId() == null ? "" : getSpanId());
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		properties.put("time", getTime() == null ? -1L : getTime());
		properties.put("operationName", getOperationName() == null ? "" : getOperationName());
		properties.put("order", getOrder() == null ? -1 : getOrder());
		properties.put("apiFunctionName", getApiFunctionName() == null ? "" : getApiFunctionName());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Span;
	}
	
	@Override
	public String getName() {
		return getSpanId();
	}

}
