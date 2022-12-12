package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogicCouplingComponents<T extends Node> {
	
	private T node1;
	
	private T node2;
	
	private int cochangeTimes;
}
