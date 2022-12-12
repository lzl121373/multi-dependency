package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;

import java.io.Serializable;
import java.util.*;


/**
 * 若Node为Package，则表示这个Package下有多少**关系
 * @author fan
 *
 * @param <N>
 */
@Data
public abstract class BasicDataForSingleNode<N extends Node, R extends Relation> implements Serializable  {

	private static final long serialVersionUID = 6183495748135612789L;

	protected N node;

	protected double value = 0;

	protected Map<RelationType, List<R>> children = new HashMap<>();

	protected Set<Node> nodesInNode1 = new HashSet<>();

	protected Set<Node> allNodesInNode1 = new HashSet<>();
	
	public int sizeOfChildren() {
		return children.size();
	}
	
	public void addChild(R relation) {
		RelationType relationType = relation.getRelationType();
		List<R> relations = children.getOrDefault(relationType, new ArrayList<>());
		relations.add(relation);
		children.put(relationType, relations);
		if(relation instanceof Clone){
			addValue(((Clone) relation).getValue());
		}else if(relation instanceof CoChange){
			addValue(((CoChange) relation).getTimes());
		}
	}
	
	public void addValue(double value) {
		this.value += value;
	}

	public void addChildren(List<R> relations, RelationType relationType) {
		List<R> relationList = children.getOrDefault(relationType, new ArrayList<>());
		relationList.addAll(relations);
		children.put(relationType, relations);
	}
}
