package cn.edu.fudan.se.multidependency.model.relation.clone;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CLONE)
public class Clone implements Relation {
	private static final long serialVersionUID = 8708817258770543568L;

	@Id
    @GeneratedValue
    private Long id;
	 	
	@StartNode
	private CodeNode codeNode1;
	
	@EndNode
	private CodeNode codeNode2;
	
	public Clone(CodeNode codeNode1, CodeNode codeNode2) {
		this.codeNode1 = codeNode1;
		this.codeNode2 = codeNode2;
	}
	
	private double value;
	
	private int node1Index;
	
	private int node2Index;
	
	private int node1StartLine;
	
	private int node1EndLine;
	
	private int node2StartLine;
	
	private int node2EndLine;
	
	private int linesSize1 = -1;
	
	private int linesSize2 = -1;
	
	private int loc1 = -1;
	
	private int loc2 = -1;
	
	/**
	 * 克隆关系类型：文件间克隆，方法间克隆等
	 */
	private String cloneRelationType;
	
	/**
	 * 克隆类型，type1，type2等
	 */
	private String cloneType;

	@Override
	public Node getStartNode() {
		return codeNode1;
	}

	@Override
	public Node getEndNode() {
		return codeNode2;
	}
	
	public boolean containsNode(CodeNode node) {
		return node.equals(getCodeNode1()) || node.equals(getCodeNode2());
	}
	
	public boolean containsNode(CodeNode node1, CodeNode node2) {
		return containsNode(node1) && containsNode(node2);
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CLONE;
	}
	
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("value", getValue());
		properties.put("node1Index", getNode1Index());
		properties.put("node2Index", getNode2Index());
		properties.put("node1StartLine", getNode1StartLine());
		properties.put("node1EndLine", getNode1EndLine());
		properties.put("node2StartLine", getNode2StartLine());
		properties.put("node2EndLine", getNode2EndLine());
		properties.put("cloneRelationType", getCloneRelationType() == null ? "" : getCloneRelationType());
		properties.put("cloneType", getCloneType() == null ? "" : getCloneType());
		properties.put("linesSize1", getLinesSize1());
		properties.put("linesSize2", getLinesSize2());
		properties.put("loc1", getLoc1());
		properties.put("loc2", getLoc2());
		return properties;
	}
}
