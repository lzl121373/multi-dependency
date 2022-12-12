package cn.edu.fudan.se.multidependency.utils.query;

import java.util.Collection;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class CytoscapeUtil {
	
	@Data
	@NoArgsConstructor
	@EqualsAndHashCode
	@ToString
	public static class CytoscapeNode {
		private String id;
		private String name;
		private String type;
		private String value;
		private String parent;
		private double positionX;
		private double positionY;
		
		public CytoscapeNode(long id, String name, String type) {
			this(String.valueOf(id), name, type);
		}
		
		public CytoscapeNode(String id, String name, String type) {
			this(id, name, type, "");
		}
		
		public CytoscapeNode(String id, String name, String type, String value) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.value = value;
		}
		
		public void setParent(long parent) {
			this.parent = String.valueOf(parent);
		}
		
		public void setParent(String parent) {
			this.parent = parent;
		}
		
		public void setId(long id) {
			this.id = String.valueOf(id);
		}
		
		public JSONObject toJSON() {
			JSONObject result = new JSONObject();
			JSONObject data = new JSONObject();
			data.put("id", getId());
			data.put("name", getName() == null ? "" : getName());
			data.put("type", getType() == null ? "" : getType());
			data.put("value", getValue() == null ? "" : getValue());
			if(getParent() != null) {
				data.put("parent", getParent());
			}
			result.put("data", data);
			return result;
		}
	}
	
	@Data
	@NoArgsConstructor
	@EqualsAndHashCode
	@ToString
	public static class CytoscapeEdge {
		private String id;
		private String sourceId;
		private String targetId;
		private String type;
		private String value;
		
		/*public static String generateId(Node source, Node target) {
			return generateId(source.getId(), target.getId());
		}
		
		public static String generateId(Long sourceId, Long targetId) {
			return String.join("_", sourceId.toString(), targetId.toString());
		}

		public static String generateId(String sourceId, String targetId) {
			return String.join("_", sourceId, targetId);
		}*/
		
		public CytoscapeEdge(Node source, Node target, String type, String value) {
			this(source.getId().toString(), target.getId().toString(), type, value);
		}
		
		public CytoscapeEdge(Node source, Node target, String type) {
			this(source.getId().toString(), target.getId().toString(), type, "");
		}

		public CytoscapeEdge(String sourceId, String targetId, String type) {
			this(sourceId, targetId, type, "");
		}
		
		public CytoscapeEdge(String sourceId, String targetId, String type, String value) {
			this.sourceId = sourceId;
			this.targetId = targetId;
			this.type = type;
			this.value = value;
		}
		
		public JSONObject toJSONDataContent() {
			JSONObject data = new JSONObject();
			if(getId() != null) {
				data.put("id", getId());
			}
			data.put("source", getSourceId());
			data.put("target", getTargetId());
			data.put("type", getType());
			data.put("value", getValue());
			return data;
		}
		
		public JSONObject toJSON() {
			JSONObject result = new JSONObject();
			result.put("data", toJSONDataContent());
			return result;
		}
		
	}
	
	public static JSONArray toNodes(Collection<CytoscapeNode> nodes) {
		JSONArray result = new JSONArray();
		for(CytoscapeNode node : nodes) {
			result.add(node.toJSON());
		}
		return result;
	}
	
	public static JSONArray toEdges(Collection<CytoscapeEdge> edges) {
		JSONArray result = new JSONArray();
		for(CytoscapeEdge edge : edges) {
			result.add(edge.toJSON());
		}
		return result;
	}
	
//	public static CytoscapeNode toCytoscapeNode(Node node, String type) {
//		
//	}
	
	/*public static JSONObject toCytoscapeNode(Node node, String type) {
		return toCytoscapeNode(node, node.getName(), type);
	}
	
	public static JSONObject toCytoscapeNode(Node node, String name, String type) {
		return toCytoscapeNode(node.getId(), name, type);
	}
	
	public static JSONObject toCytoscapeNode(Long id, String name, String type) {
		return toCytoscapeNode(id + "", name, type);
	}
	
	public static JSONObject toCytoscapeNode(String id, String name, String type) {
		JSONObject result = new JSONObject();
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("id", id);
		data.put("name", name);
		result.put("data", data);
		return result;
	}*/
	
	/*public static JSONObject relationToEdge(String startId, String endId, String type, String value, boolean autoId) {
		JSONObject edge = new JSONObject();
		JSONObject data = new JSONObject();
		if(autoId) {
			data.put("id", String.join("_", startId, endId));
		}
		data.put("source", startId);
		data.put("target", endId);
		if(!StringUtils.isBlank(type)) {
			data.put("type", type);
		}
		data.put("value", StringUtils.isBlank(value) ? "" : value);
		edge.put("data", data);
		return edge;
	}*/
	
	/*public static JSONObject relationToEdge(Long startId, Long endId, String type, String value, boolean autoId) {
		return relationToEdge(String.valueOf(startId), String.valueOf(endId), type, value, autoId);
	}
	
	public static JSONObject relationToEdge(Node start, Node end, String type, String value, boolean autoId) {
		return relationToEdge(start.getId(), end.getId(), type, value, autoId);
	}*/
}
