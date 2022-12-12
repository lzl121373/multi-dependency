package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.Node;

public class UnstableComponent<T extends Node> {
	
	private T component;

	public T getComponent() {
		return component;
	}

	public void setComponent(T component) {
		this.component = component;
	}
	
}
