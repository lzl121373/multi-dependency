package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import lombok.Data;

/**
 * 节点node1和node2不分方向
 * 若Node为Package，则表示两个Package之间的克隆关系，children为pck1和pck2内的文件克隆或方法克隆等，克隆关系的两个节点分别在两个包里，不计同一包下的文件克隆
 * @author fan
 * 
 * @param <N>
 */
@Data
public class CloneValueForDoubleNodes<N extends Node> implements Serializable {
	
	private static final long serialVersionUID = -2262794801616872866L;
	
	private N node1;
	
	private N node2;
	
	private double value = 0;
	
	private String id;
	
	public CloneValueForDoubleNodes(N node1, N node2) {
		this(node1, node2, String.join("_", node1.getId().toString(), node2.getId().toString()));
	}
	
	public CloneValueForDoubleNodes(N node1, N node2, String id) {
		this.node1 = node1;
		this.node2 = node2;
		this.id = id;
	}
	
	// 两个克隆节点内部的节点之间的克隆关系对
	private List<Clone> children = new ArrayList<>();
	
	private Set<CodeNode> nodesInNode1 = new HashSet<>();
	
	private Set<CodeNode> nodesInNode2 = new HashSet<>();
	
	private Set<CodeNode> allNodesInNode1 = new HashSet<>();
	
	private Set<CodeNode> allNodesInNode2 = new HashSet<>();
	
	/*public double ratio1() {
		return allNodesInNode1.isEmpty() ? -1 : (nodesInNode1.size() + 0.0) / allNodesInNode1.size();
	}
	
	public double ratio2() {
		return allNodesInNode2.isEmpty() ? -1 : (nodesInNode2.size() + 0.0) / allNodesInNode2.size();
	}*/
	
	public void addCodeNodeToNode1(CodeNode node) {
		this.nodesInNode1.add(node);
	}
	
	public void addCodeNodeToNode2(CodeNode node) {
		this.nodesInNode2.add(node);
	}
	
	public int sizeOfChildren() {
		return children.size();
	}
	
	public void sortChildren() {
		children.sort((clone1, clone2) -> {
			CodeNode node11 = clone1.getCodeNode1();
			CodeNode node21 = clone2.getCodeNode1();
			int sort = node11.getIdentifier().compareTo(node21.getIdentifier());
			if(sort == 0) {
				CodeNode node12 = clone1.getCodeNode2();
				CodeNode node22 = clone2.getCodeNode2();
				return node12.getIdentifier().compareTo(node22.getIdentifier());
			} else {
				return sort;
			}
		});
	}
	
	private transient CloneValueCalculator<?> calculator;
	
	public Object calculateValue(CloneValueCalculator<?> calculator) {
		if(calculator != null) {
			return calculator.calculate(this);
		}
		if(this.calculator != null) {
			return this.calculator.calculate(this);
		}
		return "clone: " + getValue();
	}
	
	public void addChild(Clone clone) {
		this.children.add(clone);
		addValue(clone.getValue());
	}
	
	public void addValue(double value) {
		this.value += value;
	}
	
	public void addChildren(Collection<Clone> clones) {
		for(Clone clone : clones) {
			addChild(clone);
		}
	}
	
}
