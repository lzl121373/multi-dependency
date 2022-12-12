package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;

public class CloneValueCalculatorForMicroService implements CloneValueCalculator<String> {
	
	/**
	 * 微服务内的方法数
	 */
	private Map<MicroService, List<Function>> msToFunctions = new HashMap<>();
	
	public void addFunctions(Iterable<Function> functions, MicroService ms) {
		List<Function> temp = msToFunctions.getOrDefault(ms, new ArrayList<>());
		for(Function function : functions) {
			temp.add(function);
		}
		msToFunctions.put(ms, temp);
	}

	@Override
	public String calculate(CloneValueForDoubleNodes<? extends Node> clone) {
		assert(clone.getNode1() instanceof MicroService && clone.getNode2() instanceof MicroService);
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		Collection<? extends Clone> childrenClones = clone.getChildren();
		builder.append(childrenClones.size());
		builder.append(", ");
		double b = clone.getValue() / (childrenClones.size() + 0.0);
		builder.append(String.format("%.2f", b));
		builder.append(")");
		return builder.toString();
	}

}
