package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.HashSet;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import lombok.Data;

@Data
public class SimilarComponents<T extends Node> {

	private T node1;
	
	private T node2;
	
	private double value;
	
	private String cloneType;
	
	private Module module1;
	
	private Module module2;
	
	private int node1ChangeTimes;
	
	private int node2ChangeTimes;
	
	private int cochangeTimes;

	private Double sameDependsOnRatio = null;

	private Set<Node> node1DependsOn = new HashSet<>();

	private Set<Node> node2DependsOn = new HashSet<>();

	public SimilarComponents(T node1, T node2, double value, int node1ChangeTimes, int node2ChangeTimes, int cochangeTimes) {
		this.node1 = node1;
		this.node2 = node2;
		this.value = value;
		this.node1ChangeTimes = node1ChangeTimes;
		this.node2ChangeTimes = node2ChangeTimes;
		this.cochangeTimes = cochangeTimes;
	}
	
	public void addNode1DependsOn(Node node) {
		node1DependsOn.add(node);
	}
	
	public void addNode2DependsOn(Node node) {
		node2DependsOn.add(node);
	}
	
	public void setSameDependsOnRatio() {
		int sum = node1DependsOn.size() + node2DependsOn.size();
		if(sum == 0) {
			System.out.println("相似的组件依赖项目中的外部代码单元的数量为0");
			sameDependsOnRatio = 1.0;
		}
		Set<Node> sameDependsOn = new HashSet<>();
		Set<Node> allDependsOn = new HashSet<>(node1DependsOn);
		for(Node node : node2DependsOn) {
			if (allDependsOn.contains(node)) {
				sameDependsOn.add(node);
			}
			else {
				allDependsOn.add(node);
			}
		}
		sameDependsOnRatio = ((sameDependsOn.size() + 0.0) / allDependsOn.size());
	}
}
