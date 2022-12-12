package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import lombok.Data;

@Data
public class MultipleASPackage implements MultipleAS {

	private Project project;

	private Package pck;

	private boolean cyclicDependency;

	private boolean hubLikeDependency;

	private boolean unstableDependency;

	private boolean unstableInterface;

	private boolean implicitCrossModuleDependency;

	private boolean unutilizedAbstraction;

	private boolean unusedInclude;

	public MultipleASPackage(Package pck) {
		this.pck = pck;
	}
}
