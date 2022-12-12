package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;

public interface RelationAggregator<T> {
	T aggregate(BasicDataForDoubleNodes<? extends Node, ? extends Relation> doubleNodes);

	default boolean aggregate(RelationDataForDoubleNodes<? extends Node, ? extends Relation> doubleNodes){
		return true;
	}
}