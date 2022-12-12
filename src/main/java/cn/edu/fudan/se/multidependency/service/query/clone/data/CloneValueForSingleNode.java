package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import lombok.Data;

@Data
/**
 * 若Node为Package，则表示这个Package下有多少克隆关系
 * @author fan
 *
 * @param <N>
 */
public class CloneValueForSingleNode<N extends Node> implements Serializable  {

	private static final long serialVersionUID = -1015918498310454795L;
	
	private N node;
	
	private double value = 0;
	
	// 两个克隆节点内部的克隆关系
	private List<Clone> children = new ArrayList<>();
	
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
