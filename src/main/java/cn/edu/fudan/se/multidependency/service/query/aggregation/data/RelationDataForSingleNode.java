package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * 若Node为Package，则表示这个Package下有多少**关系
 * @author fan
 *
 * @param <N>
 */
@Data
public class RelationDataForSingleNode<N extends Node, R extends Relation> implements Serializable  {

	private static final long serialVersionUID = 2827798496624205543L;

	private N node;

	private double value = 0;
	
	// 两个关系节点内部的关系
	private List<R> children = new ArrayList<>();
	
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
		for(R relation : relations) {
			addChild(relation);
		}
	}
}
