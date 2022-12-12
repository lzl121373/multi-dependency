package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Relation;

import java.util.*;

public class RelationAggregatorForMicroServiceByClone<CloneLevel extends Node> implements RelationAggregator<String> {
	
	/**
	 * 微服务内的T
	 */
	private Map<MicroService, List<CloneLevel>> msToNodes = new HashMap<>();
	
	public void addNodes(Iterable<CloneLevel> nodes, MicroService ms) {
		List<CloneLevel> temp = msToNodes.getOrDefault(ms, new ArrayList<>());
		for(CloneLevel node: nodes) {
			temp.add(node);
		}
		msToNodes.put(ms, temp);
	}

	@Override
	public String aggregate(BasicDataForDoubleNodes<? extends Node, ? extends Relation> doubleNodes) {
		assert(doubleNodes.getNode1() instanceof MicroService && doubleNodes.getNode2() instanceof MicroService);
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		Collection<? extends Relation> childrenClones = doubleNodes.getChildren();
		builder.append(childrenClones.size());
		builder.append(", ");
		double b = doubleNodes.getValue() / (childrenClones.size() + 0.0);
		builder.append(String.format("%.2f", b));
		builder.append(")");
		return builder.toString();
	}

}
