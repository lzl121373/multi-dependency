package cn.edu.fudan.se.multidependency.model.node;

import cn.edu.fudan.se.multidependency.model.MetricType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Metric implements Node {

	private static final long serialVersionUID = -5961353797212753946L;

	@Id
    @GeneratedValue
    private Long id;

	private Long entityId;

	private String language;

	private String name;

	private NodeLabelType nodeType;

	@Properties(allowCast = true)
	private Map<String, Object> metricValues = new HashMap<>();

	public Metric(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("nodeType", getNodeType() == null ? "" : getNodeType());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Metric;
	}

}
