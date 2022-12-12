package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * 节点node1和node2不分方向
 * 若Node为Package，则表示两个Package之间的**关系，children为pck1和pck2内的文件**关系或方法**关系等，**关系的两个节点分别在两个包里，不计同一包下的文件**
 * @author fan
 * 
 * @param <N>
 */
@Data
@NoArgsConstructor
public abstract class BasicDataForDoubleNodes<N extends Node , R extends Relation> implements Serializable {

	private static final long serialVersionUID = 7803384497653185337L;

	protected N node1;

	protected N node2;

	protected double value = 0;

	protected String id;

	abstract public RelationType getRelationDataType();

	public BasicDataForDoubleNodes(N node1, N node2) {
		this(node1, node2, String.join("_", node1.getId().toString(), node2.getId().toString()));
	}

	public BasicDataForDoubleNodes(N node1, N node2, String id) {
		this.node1 = node1;
		this.node2 = node2;
		this.id = id;
	}
	
	// 两个关系节点内部的节点之间的关系对
	protected List<R> children = new ArrayList<>();

	protected Set<Node> nodesInNode1 = new HashSet<>();

	protected Set<Node> nodesInNode2 = new HashSet<>();

	protected Set<Node> allNodesInNode1 = new HashSet<>();

	protected Set<Node> allNodesInNode2 = new HashSet<>();
	
	public void addCodeNodeToNode1(Node node) {
		this.nodesInNode1.add(node);
	}
	
	public void addCodeNodeToNode2(Node node) {
		this.nodesInNode2.add(node);
	}
	
	public int sizeOfChildren() {
		return children.size();
	}
	
	public void sortChildren() {
		children.sort((relation1, relation2) -> {
			CodeNode node11 = (CodeNode)relation1.getStartNode();
			CodeNode node21 = (CodeNode)relation2.getStartNode();
			int sort = node11.getIdentifier().compareTo(node21.getIdentifier());
			if(sort == 0) {
				CodeNode node12 = (CodeNode)relation1.getEndNode();
				CodeNode node22 = (CodeNode)relation2.getEndNode();
				return node12.getIdentifier().compareTo(node22.getIdentifier());
			} else {
				return sort;
			}
		});
	}
	
	public void addChild(R relation) {
		this.children.add(relation);
		if(relation instanceof Clone){
			addValue(((Clone) relation).getValue());
		}else if(relation instanceof CoChange){
			addValue(((CoChange) relation).getTimes());
		}
	}
	
	public void addValue(double value) {
		this.value += value;
	}
	
	public void addChildren(Collection<R> relations) {
		for(R  relation : relations) {
			addChild(relation);
		}
	}
}
