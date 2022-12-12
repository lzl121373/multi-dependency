package cn.edu.fudan.se.multidependency.service.query;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.query.ZTreeUtil.ZTreeNode;

@Service
public class MultipleService {
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	public ZTreeNode projectToZTree(Project project) {
		return structureNodeToZTreeNodeRecursion(project);
	}
	
	private void addNodesToZTreeNode(ZTreeNode parentZTreeNode, Iterable<? extends Node> nodes) {
		for(Node node : nodes) {
			parentZTreeNode.addChild(structureNodeToZTreeNodeRecursion(node));
		}
	}
	
	private ZTreeNode structureNodeToZTreeNodeRecursion(Node node) {
		ZTreeNode result = null;
		if(node instanceof Variable) {
			result = new ZTreeNode(node, false);
		} else if(node instanceof Function) {
			Function function = (Function) node;
			result = new ZTreeNode(function.getId(), function.getName() + function.getParametersIdentifies(), false, function.getNodeType().name(), true);
			Collection<Variable> variables = containRelationService.findFunctionDirectlyContainVariables((Function) node);
			if(!variables.isEmpty()) {
				ZTreeNode variablesNodes = new ZTreeNode("变量 (" + variables.size() + " )", true);
				result.addChild(variablesNodes);
				addNodesToZTreeNode(variablesNodes, variables);
			}
		} else if(node instanceof Type) {
			result = new ZTreeNode(node, true);
			Collection<Function> functions = containRelationService.findTypeDirectlyContainFunctions((Type) node);
			if(!functions.isEmpty()) {
				ZTreeNode functionsNodes = new ZTreeNode("方法 (" + functions.size() + " )", true);
				result.addChild(functionsNodes);
				addNodesToZTreeNode(functionsNodes, functions);
			}
			Collection<Variable> fields = containRelationService.findTypeDirectlyContainFields((Type) node);
			if(!fields.isEmpty()) {
				ZTreeNode fieldsNodes = new ZTreeNode("属性 (" + fields.size() + " )", true);
				result.addChild(fieldsNodes);
				addNodesToZTreeNode(fieldsNodes, fields);
			}
		} else if(node instanceof Namespace) {
			result = new ZTreeNode(node, true);
			Collection<Type> types = containRelationService.findNamespaceDirectlyContainTypes((Namespace) node);
			Collection<Function> functions = containRelationService.findNamespaceDirectlyContainFunctions((Namespace) node);
			Collection<Variable> variables = containRelationService.findNamespaceDirectlyContainVariables((Namespace) node);
			if(!types.isEmpty()) {
				ZTreeNode typeNodes = new ZTreeNode("类型 (" + types.size() + " )", true);
				result.addChild(typeNodes);
				addNodesToZTreeNode(result, types);
			}
			if(!functions.isEmpty()) {
				ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + " )", true);
				result.addChild(functionNodes);
				addNodesToZTreeNode(result, functions);
			}
			if(!variables.isEmpty()) {
				ZTreeNode variablesNodes = new ZTreeNode("属性 (" + variables.size() + " )", true);
				result.addChild(variablesNodes);
				addNodesToZTreeNode(result, variables);
			}
		} else if(node instanceof ProjectFile) {
			result = new ZTreeNode(node, true);
			Collection<Namespace> namespaces = containRelationService.findFileContainNamespaces((ProjectFile) node);
			if(!namespaces.isEmpty()) {
				ZTreeNode namespaceNodes = new ZTreeNode("命名空间 (" + namespaces.size() + " )", true);
				result.addChild(namespaceNodes);
				addNodesToZTreeNode(namespaceNodes, namespaces);
			}
			Collection<Type> types = containRelationService.findFileDirectlyContainTypes((ProjectFile) node);
			if(!types.isEmpty()) {
				ZTreeNode typeNodes = new ZTreeNode("类型 (" + types.size() + " )", true);
				result.addChild(typeNodes);
				addNodesToZTreeNode(typeNodes, types);
			}
			Collection<Function> functions = containRelationService.findFileDirectlyContainFunctions((ProjectFile) node);
			if(!functions.isEmpty()) {
				ZTreeNode functionNodes = new ZTreeNode("方法 (" + functions.size() + " )", true);
				result.addChild(functionNodes);
				addNodesToZTreeNode(functionNodes, functions);
			}
			Collection<Variable> variables = containRelationService.findFileDirectlyContainVariables((ProjectFile) node);
			if(!variables.isEmpty()) {
				ZTreeNode variableNodes = new ZTreeNode("变量 (" + variables.size() + " )", true);
				result.addChild(variableNodes);
				addNodesToZTreeNode(variableNodes, variables);
			}
		} else if(node instanceof Package) {
			result = new ZTreeNode(node, true);
			Collection<ProjectFile> files = containRelationService.findPackageContainFiles((Package) node);
			ZTreeNode fileNodes = new ZTreeNode("文件 (" + files.size() + ")", true);
			result.addChild(fileNodes);
			addNodesToZTreeNode(fileNodes, files);
		} else if(node instanceof Project) {
			Project project = (Project) node;
			result = new ZTreeNode(project.getId(), project.getName() + "(" + project.getLanguage() + ")", false, project.getNodeType().name(), true);
			Collection<Package> pcks = containRelationService.findProjectContainPackages(project);
			ZTreeNode pckNodes = new ZTreeNode("目录 / 包 (" + pcks.size() + ")", true);
			result.addChild(pckNodes);
			addNodesToZTreeNode(pckNodes, pcks);
		} else {
			return null;
		}
		return result;
	}
	
}
