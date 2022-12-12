package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import lombok.Data;

@Data
public class CyclicHierarchy {

	private Type superType;
	
	public CyclicHierarchy(Type superType) {
		this.superType = superType;
	}
	
	/**
	 * 父类依赖的子类
	 */
	private List<DependsOn> dependsOnSubTypes = new ArrayList<>();
	
	private Set<Type> subTypes = new HashSet<>();
	
	public void addDependsOn(DependsOn dependsOn) {
		if(!(dependsOn.getEndNode() instanceof Type) || !(dependsOn.getStartNode() instanceof Type)) {
			return;
		}
		this.dependsOnSubTypes.add(dependsOn);
		this.subTypes.add((Type) dependsOn.getEndNode());
	}
	
}
