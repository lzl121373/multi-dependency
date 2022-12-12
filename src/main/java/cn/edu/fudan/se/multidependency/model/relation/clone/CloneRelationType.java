package cn.edu.fudan.se.multidependency.model.relation.clone;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;

public enum CloneRelationType {
	FILE_CLONE_FILE(CloneRelationType.str_FILE_CLONE_FILE),
	TYPE_CLONE_TYPE(CloneRelationType.str_TYPE_CLONE_TYPE),
	TYPE_CLONE_FUNCTION(CloneRelationType.str_TYPE_CLONE_FUNCTION),
	TYPE_CLONE_SNIPPET(CloneRelationType.str_TYPE_CLONE_SNIPPET),
	FUNCTION_CLONE_FUNCTION(CloneRelationType.str_FUNCTION_CLONE_FUNCTION),
	FUNCTION_CLONE_SNIPPET(CloneRelationType.str_FUNCTION_CLONE_SNIPPET),
	SNIPPET_CLONE_SNIPPET(CloneRelationType.str_SNIPPET_CLONE_SNIPPET);
	
	public static final String str_FILE_CLONE_FILE = "FILE_CLONE_FILE";
	public static final String str_TYPE_CLONE_TYPE = "TYPE_CLONE_TYPE";
	public static final String str_TYPE_CLONE_FUNCTION = "TYPE_CLONE_FUNCTION";
	public static final String str_TYPE_CLONE_SNIPPET = "TYPE_CLONE_SNIPPET";
	public static final String str_FUNCTION_CLONE_FUNCTION = "FUNCTION_CLONE_FUNCTION";
	public static final String str_FUNCTION_CLONE_SNIPPET = "FUNCTION_CLONE_SNIPPET";
	public static final String str_SNIPPET_CLONE_SNIPPET = "SNIPPET_CLONE_SNIPPET";
	
	private String name;

	CloneRelationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
	
	private static final Map<NodeLabelType, Byte> nodeTypeToByteMap = new HashMap<>();
	
	private static final Map<Integer, CloneRelationType> typeToCloneTypeMap = new HashMap<>();
	
	static {
		nodeTypeToByteMap.put(NodeLabelType.ProjectFile, (byte) 1);
		nodeTypeToByteMap.put(NodeLabelType.Type, (byte) 2);
		nodeTypeToByteMap.put(NodeLabelType.Function, (byte) 4);
		nodeTypeToByteMap.put(NodeLabelType.Snippet, (byte) 8);
		typeToCloneTypeMap.put(1, FILE_CLONE_FILE);
		typeToCloneTypeMap.put(2, TYPE_CLONE_TYPE);
		typeToCloneTypeMap.put(4, FUNCTION_CLONE_FUNCTION);
		typeToCloneTypeMap.put(6, TYPE_CLONE_FUNCTION);
		typeToCloneTypeMap.put(8, SNIPPET_CLONE_SNIPPET);
		typeToCloneTypeMap.put(10, TYPE_CLONE_SNIPPET);
		typeToCloneTypeMap.put(12, FUNCTION_CLONE_SNIPPET);
	}
	
	public static CloneRelationType getCloneType(CodeNode node1, CodeNode node2) {
		Byte byte1 = nodeTypeToByteMap.get(node1.getNodeType());
		Byte byte2 = nodeTypeToByteMap.get(node2.getNodeType());
		if(byte1 == null || byte2 == null) {
			return null;
		}
		return typeToCloneTypeMap.get(byte1 | byte2);
	}
	
}
