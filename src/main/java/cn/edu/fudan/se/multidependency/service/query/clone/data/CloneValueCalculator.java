package cn.edu.fudan.se.multidependency.service.query.clone.data;

import cn.edu.fudan.se.multidependency.model.node.Node;

public interface CloneValueCalculator<T> {
	T calculate(CloneValueForDoubleNodes<? extends Node> clone);
}