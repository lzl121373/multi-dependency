package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;


/**
 * 节点node1和node2不分方向
 * 若Node为Package，则表示两个Package之间的**关系，children为pck1和pck2内的文件**关系或方法**关系等，**关系的两个节点分别在两个包里，不计同一包下的文件**
 * @author fan
 * 
 * @param <N>
 */
@Data
public class CoChangeRelationDataForDoubleNodes<N extends Node, R extends Relation> extends BasicDataForDoubleNodes<N, R> {

	private static final long serialVersionUID = 7974324881023147662L;

	private int coChangeTimes = 0;

	private int node1ChangeTimes = 0;

	private int node2ChangeTimes = 0;

	public CoChangeRelationDataForDoubleNodes(N node1, N node2){
		super(node1, node2);
	}

	public void setDate(int coChangeTimes, int node1ChangeTimes, int node2ChangeTimes) {
		this.coChangeTimes = coChangeTimes;
		this.node1ChangeTimes = node1ChangeTimes;
		this.node2ChangeTimes = node2ChangeTimes;
	}

	@Override
	public RelationType getRelationDataType() {
		return RelationType.CO_CHANGE;
	}
}
