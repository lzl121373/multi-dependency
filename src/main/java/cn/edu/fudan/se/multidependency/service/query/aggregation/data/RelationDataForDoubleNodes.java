package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;

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
public class RelationDataForDoubleNodes<N extends Node , R extends Relation> implements Serializable {

	private static final long serialVersionUID = 2471117562153525667L;

	private N node1;

	private N node2;

	private double value = 0;

	private String id;

	private String dependsOnTypes = "";

	private String dependsByTypes = "";

	private int dependsOnTimes = 0;

	private int dependsByTimes = 0;


	public RelationDataForDoubleNodes(N node1, N node2) {
		this(node1, node2, String.join("_", node1.getId().toString(), node2.getId().toString()));
	}

	public RelationDataForDoubleNodes(N node1, N node2, String dependsOnTypes, String dependsByTypes) {
		this(node1, node2, String.join("_", node1.getId().toString(), node2.getId().toString()));
		this.dependsOnTypes = dependsOnTypes;
		this.dependsByTypes = dependsByTypes;
	}

	public RelationDataForDoubleNodes(N node1, N node2, String id) {
		this.node1 = node1;
		this.node2 = node2;
		this.id = id;
	}
	
	// 两个关系节点内部的节点之间的关系对
	private List<R> children = new ArrayList<>();
	
	private Set<CodeNode> nodesInNode1 = new HashSet<>();
	
	private Set<CodeNode> nodesInNode2 = new HashSet<>();
	
	private Set<CodeNode> allNodesInNode1 = new HashSet<>();
	
	private Set<CodeNode> allNodesInNode2 = new HashSet<>();

	private int cloneNodesCount1 = 0;

	private int cloneNodesCount2 = 0;

	private int allNodesCount1 = 0;

	private int allNodesCount2 = 0;

	private int childrenPackagesCount1 = 0;

	private int childrenPackagesCount2 = 0;

	private int childrenHotspotPackageCount1 = 0;

	private int childrenHotspotPackageCount2 = 0;
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
	
	private transient RelationAggregator<?> aggregator;
	
	public Object aggregateValue(RelationAggregator<?> aggregator) {
		if(aggregator != null) {
			return aggregator.aggregate(this);
		}
		if(this.aggregator != null) {
			return this.aggregator.aggregate(this);
		}
		return "relation: " + getValue();
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

	public void setDate(int allNodesCount1, int allNodesCount2, int cloneNodesCount1, int cloneNodesCount2) {
		this.allNodesCount1 = allNodesCount1;
		this.allNodesCount2 = allNodesCount2;
		this.cloneNodesCount1 = cloneNodesCount1;
		this.cloneNodesCount2 = cloneNodesCount2;
	}
}
