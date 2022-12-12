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
public class RestfulAPI implements Node {
	
	private static final long serialVersionUID = -404310425549237045L;

	@Id
    @GeneratedValue
    private Long id;

	private Long entityId;
	
	// 应是唯一的
	private String apiFunctionName;
	
	private String apiFunctionSimpleName;
	
	private String endPoint;
	
	private String form;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("apiFunctionName", getApiFunctionName() == null ? "" : getApiFunctionName());
		properties.put("apiFunctionSimpleName", getApiFunctionSimpleName() == null ? "" : getApiFunctionSimpleName());
		properties.put("endPoint", getEndPoint() == null ? "" : getEndPoint());
		properties.put("form", getForm() == null ? "" : getForm());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.RestfulAPI;
	}

	@Override
	public String getName() {
		return getApiFunctionSimpleName();
	}

}
