package cn.edu.fudan.se.multidependency.utils.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ZTreeUtil {

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ZTreeNode implements Serializable {
		
		public static final long DEFAULT_ID = -1;
		
		private static final long serialVersionUID = -8811436922800805233L;
		
		public static final String DEFAULT_TYPE = "default";
		
		public ZTreeNode(String name, boolean isParent) {
			this(DEFAULT_ID, name, false, DEFAULT_TYPE, isParent);
		}
		
		public ZTreeNode(Node node, boolean isParent) {
			this(node.getId(), node.getName(), false, node.getNodeType().toString(), isParent);
		}
		
		public ZTreeNode(long id, String name, boolean open, String type, boolean isParent) {
			this.id = id;
			this.name = name;
			this.open = open;
			this.type = type;
			this.parent = isParent;
		}
		
		private long id;
		private String name;
		private String type;
		private boolean open;
		private boolean parent;
		private boolean checked = false;
		private boolean nocheck = false;
		
		private List<ZTreeNode> children = new ArrayList<>();
		
		public void addChild(ZTreeNode ztree) {
			this.children.add(ztree);
			children.sort(new Comparator<ZTreeNode>() {
				@Override
				public int compare(ZTreeNode o1, ZTreeNode o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
		public JSONObject toJSON() {
			JSONObject result = new JSONObject();
			result.put("name", name);
			result.put("id", id);
			result.put("type", type);
			result.put("open", open);
			result.put("isParent", parent);
			result.put("checked", checked);
			if(parent) {
				JSONArray childrenJSON = new JSONArray();
				for(ZTreeNode child : children) {
					childrenJSON.add(child.toJSON());
				}
				result.put("children", childrenJSON);
			}
			return result;
		}
	}
	
}
