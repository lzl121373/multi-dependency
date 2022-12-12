package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class ModularityCalculatorImplForTypeLevel implements ModularityCalculator {
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	private Project project;
	
//	private Collection<Function> allFunctions;
	
	private boolean isSameFile(Node node1, Node node2) {
		Type file1 = findNodeBelongToType(node1);
		Type file2 = findNodeBelongToType(node2);
		if(file1 == null || file2 == null) {
			return false;
		}
		return file1.equals(file2);
	}
	
	private Type findNodeBelongToType(Node node) {
		if(node instanceof Variable) {
			return containRelationService.findVariableBelongToType((Variable) node);
		} else if(node instanceof Function) {
			return containRelationService.findFunctionBelongToType((Function) node);
		}
		return null;
	}

	@Override
	public Modularity calculate(Project project) {
		this.project = project;
		Modularity result = new Modularity();
		result.setValue(0);
		result.setModularityType(NodeLabelType.Type);
		return result;
	}
	
	

}
