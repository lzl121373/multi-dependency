package cn.edu.fudan.se.multidependency.model.node.testcase;

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
public class Trace implements Node {
	
	private static final long serialVersionUID = 3264084230399475587L;

	@Id
    @GeneratedValue
    private Long id;

    private Long entityId;

    // traceId在数据库中是唯一的
    private String traceId;
    
    private boolean microServiceTrace;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		properties.put("microServiceTrace", isMicroServiceTrace());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Trace;
	}
	
	@Override
	public String getName() {
		return getTraceId();
	}


}
