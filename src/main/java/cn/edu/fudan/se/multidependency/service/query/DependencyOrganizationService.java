package cn.edu.fudan.se.multidependency.service.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Cast;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.model.relation.structure.Parameter;
import cn.edu.fudan.se.multidependency.model.relation.structure.Return;
import cn.edu.fudan.se.multidependency.model.relation.structure.Throw;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeEdge;
import cn.edu.fudan.se.multidependency.utils.query.CytoscapeUtil.CytoscapeNode;

@Service
public class DependencyOrganizationService {

	@Autowired
	private StaticAnalyseService staticAnalyseService;
    
    @Autowired
    private ContainRelationService containRelationService;
	
	private Map<Long, Function> functions = new HashMap<>();
	private Map<Long, ProjectFile> files = new HashMap<>();
	private Map<Long, Package> packages = new HashMap<>();
	private Map<Function, Map<Function, Integer>> countOfFunctionCall = new HashMap<>();
	private Map<ProjectFile, Map<ProjectFile, Integer>> countOfFileCall = new HashMap<>();
	private Map<Package, Map<Package, Integer>> countOfPackageCall = new HashMap<>();
	private Map<Function, ProjectFile> functionBelongToFile = new HashMap<>();
	private Map<ProjectFile, Package> fileBelongToPackage = new HashMap<>();
	
	public JSONObject projectToCytoscape(Project project, Iterable<DynamicCall> dynamicCalls,
			Iterable<Clone> functionClones) {
		JSONObject result = new JSONObject();
		JSONObject staticAndDynamicResult = projectToCytoscape(project, dynamicCalls);
		JSONArray nodes = staticAndDynamicResult.getJSONArray("nodes");
		JSONArray edges = staticAndDynamicResult.getJSONArray("edges");
		
		Map<Function, Map<Function, Boolean>> hasFunctionCallFunction = new HashMap<>();
		for(Clone clone : functionClones) {
			Function function1 = (Function) clone.getCodeNode1();
			Function function2 = (Function) clone.getCodeNode2();
			if(!hasFunctionCallFunction.getOrDefault(function1, new HashMap<>()).getOrDefault(function2, false)) {
				edges.add(new CytoscapeEdge(function1, function2, "Function_clone_Function", "Function_clone_Function").toJSON());
				Map<Function, Boolean> temp = hasFunctionCallFunction.getOrDefault(function1, new HashMap<>());
				temp.put(function2, true);
				hasFunctionCallFunction.put(function1, temp);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject projectToCytoscape(Project project, Iterable<DynamicCall> dynamicCalls) {
		JSONObject result = new JSONObject();
		JSONObject staticResult = projectToCytoscape(project);
		JSONArray nodes = staticResult.getJSONArray("nodes");
		JSONArray edges = staticResult.getJSONArray("edges");
		
		Map<Function, Map<Function, Boolean>> hasFunctionCallFunction = new HashMap<>();
		for(DynamicCall call : dynamicCalls) {
			Function start = call.getFunction();
			Function end = call.getCallFunction();
			if(!hasFunctionCallFunction.getOrDefault(start, new HashMap<>()).getOrDefault(end, false)) {
				edges.add(new CytoscapeEdge(start, end, "FunctionDynamicCallFunction", "FunctionDynamicCallFunction").toJSON());
				Map<Function, Boolean> temp = hasFunctionCallFunction.getOrDefault(start, new HashMap<>());
				temp.put(end, true);
				hasFunctionCallFunction.put(start, temp);
			}
		}

		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject projectToCytoscape(Project project) {
		JSONObject result = new JSONObject();
		List<CytoscapeNode> nodes = new ArrayList<>();
		List<CytoscapeEdge> edges = new ArrayList<>();
		
		Iterable<Package> packages = containRelationService.findProjectContainPackages(project);
		
		for(Package pck : packages) {
			nodes.add(new CytoscapeNode(pck.getId(), "Package: " + pck.getName(), "Package"));
			Iterable<ProjectFile> files = containRelationService.findPackageContainFiles(pck);
			for(ProjectFile file : files) {
				CytoscapeNode fileNode = new CytoscapeNode(file.getId(), "File: " + file.getName(), "File");
				fileNode.setParent(pck.getId().toString());
				nodes.add(fileNode);
				
				Iterable<Type> types = containRelationService.findFileDirectlyContainTypes(file);
				for(Type type : types) {
					CytoscapeNode typeJson = new CytoscapeNode(type.getId(), "Type: " + type.getName(), "Type");
					typeJson.setParent(file.getId().toString());
					nodes.add(typeJson);
					
					Iterable<Function> functions = containRelationService.findTypeDirectlyContainFunctions(type);
					for(Function function : functions) {
						CytoscapeNode functionJson = new CytoscapeNode(function.getId(), "Function: " + function.getName(), "Function");
						functionJson.setParent(type.getId().toString());
						nodes.add(functionJson);
						Iterable<Variable> variables = containRelationService.findFunctionDirectlyContainVariables(function);
						for(Variable variable : variables) {
							CytoscapeNode variableJson = new CytoscapeNode(variable.getId(), "Variable: " + variable.getName(), "Variable");
							variableJson.setParent(function.getId());
							nodes.add(variableJson);
						}
					}
					
					Iterable<Variable> variables = containRelationService.findTypeDirectlyContainFields(type);
					for(Variable variable : variables) {
						CytoscapeNode variableJson = new CytoscapeNode(variable.getId(), "Variable: " + variable.getName(), "Variable");
						variableJson.setParent(type.getId());
						nodes.add(variableJson);
					}
				}
				
				Iterable<Function> functions = containRelationService.findFileDirectlyContainFunctions(file);
				for(Function function : functions) {
					CytoscapeNode functionJson = new CytoscapeNode(function.getId(), "Function: " + function.getName(), "Function");
					functionJson.setParent(file.getId());
					nodes.add(functionJson);
				}
			}
		}
		
		List<Call> functionCallFunctions = staticAnalyseService.findProjectContainFunctionCallFunctionRelations(project);
		for(Call call : functionCallFunctions) {
			edges.add(new CytoscapeEdge(call.getCallerNode(), call.getCallFunction(), "FunctionCallFunction", "call"));
		}
		
		List<Call> typeCallFunctions = staticAnalyseService.findProjectContainTypeCallFunctions(project);
		for(Call call : typeCallFunctions) {
			edges.add(new CytoscapeEdge(call.getCallerNode(), call.getCallFunction(), "TypeCallFunction", "call"));
		}
		
		List<Cast> functionCastTypes = staticAnalyseService.findProjectContainFunctionCastTypeRelations(project);
		for(Cast relation : functionCastTypes) {
			edges.add(new CytoscapeEdge(relation.getStartNode(), relation.getCastType(), "FunctionCastType", "cast"));
		}
		List<Parameter> functionParameterTypes = staticAnalyseService.findProjectContainFunctionParameterTypeRelations(project);
		for(Parameter relation : functionParameterTypes) {
			edges.add(new CytoscapeEdge(relation.getCodeNode(), relation.getParameterType(), "FunctionParameterType", "parameter"));
		}
		List<Return> functionReturnTypes = staticAnalyseService.findProjectContainFunctionReturnTypeRelations(project);
		for(Return relation : functionReturnTypes) {
			edges.add(new CytoscapeEdge(relation.getFunction(), relation.getReturnType(), "FunctionReturnType", "return"));
		}
		List<Throw> functionThrowTypes = staticAnalyseService.findProjectContainFunctionThrowTypeRelations(project);
		for(Throw relation : functionThrowTypes) {
			edges.add(new CytoscapeEdge(relation.getFunction(), relation.getType(), "FunctionThrowType", "throw"));
		}
		List<Import> fileImportTypes = staticAnalyseService.findProjectContainImportRelations(project);
		for(Import relation : fileImportTypes) {
			edges.add(new CytoscapeEdge(relation.getFile(), relation.getImportCodeNode(), "FileImportType", "import"));
		}
		/*List<VariableTypeParameterType> variableTypeParameterTypes = staticAnalyseService.findProjectContainVariableTypeParameterTypeRelations(project);
		for(VariableTypeParameterType relation : variableTypeParameterTypes) {
			edges.add(ProjectUtil.relationToEdge(relation.getVariable(), relation.getType(), "VariableParameterType", "use", false));
		}*/
		

		result.put("nodes", CytoscapeUtil.toNodes(nodes));
		result.put("edges", CytoscapeUtil.toEdges(edges));
		return result;
	}
	
	public JSONObject packageAndFileToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Package pck : packages.values()) {
			JSONObject packageJson = new JSONObject();
			JSONObject packageDataValue = new JSONObject();
			packageDataValue.put("id", pck.getId());
			packageDataValue.put("name", pck.getName());
			packageDataValue.put("type", "package");
			packageJson.put("data", packageDataValue);
			nodes.add(packageJson);
		}
		
		for(ProjectFile file : files.values()) {
			JSONObject fileJson = new JSONObject();
			JSONObject fileDataValue = new JSONObject();
			fileDataValue.put("id", file.getId());
			fileDataValue.put("name", file.getName());
			fileDataValue.put("type", "file");
			fileJson.put("data", fileDataValue);
			nodes.add(fileJson);
			
			Package pck = fileBelongToPackage.get(file);
			JSONObject edge = new JSONObject();
			JSONObject value = new JSONObject();
			value.put("id", pck.getId() + "_" + file.getId());
			value.put("value", "contain");
			value.put("source", pck.getId());
			value.put("target", file.getId());
			value.put("type", "contain");
			edge.put("data", value);
			edges.add(edge);	
		}
		
		for(Package callerPackage : countOfPackageCall.keySet()) {
			for(Package calledPackage : countOfPackageCall.get(callerPackage).keySet()) {
				Integer count = countOfPackageCall.get(callerPackage).get(calledPackage);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerPackage.getId() + "_" + calledPackage.getId());
				value.put("value", count);
				value.put("source", callerPackage.getId());
				value.put("target", calledPackage.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		for(ProjectFile callerFile : countOfFileCall.keySet()) {
			for(ProjectFile calledFile : countOfFileCall.get(callerFile).keySet()) {
				Integer count = countOfFileCall.get(callerFile).get(calledFile);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerFile.getId() + "_" + calledFile.getId());
				value.put("value", count);
				value.put("source", callerFile.getId());
				value.put("target", calledFile.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject fileCallToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(ProjectFile file : files.values()) {
			JSONObject fileJson = new JSONObject();
			JSONObject fileDataValue = new JSONObject();
			fileDataValue.put("id", file.getId());
			fileDataValue.put("name", file.getName());
			fileJson.put("data", fileDataValue);
			nodes.add(fileJson);
		}
		
		for(ProjectFile callerFile : countOfFileCall.keySet()) {
			for(ProjectFile calledFile : countOfFileCall.get(callerFile).keySet()) {
				Integer count = countOfFileCall.get(callerFile).get(calledFile);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerFile.getId() + "_" + calledFile.getId());
				value.put("value", count);
				value.put("source", callerFile.getId());
				value.put("target", calledFile.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject functionCallToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Function function : functions.values()) {
			JSONObject functionJson = new JSONObject();
			JSONObject functionDataValue = new JSONObject();
			functionDataValue.put("id", function.getId());
			functionDataValue.put("name", function.getName());
			functionJson.put("data", functionDataValue);
			nodes.add(functionJson);
		}
		
		for(Function callerFunction : countOfFunctionCall.keySet()) {
			for(Function calledFunction : countOfFunctionCall.get(callerFunction).keySet()) {
				Integer count = countOfFunctionCall.get(callerFunction).get(calledFunction);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerFunction.getId() + "_" + calledFunction.getId());
				value.put("value", count);
				value.put("source", callerFunction.getId());
				value.put("target", calledFunction.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public JSONObject directoryCallToCytoscape() {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		
		for(Package pck : packages.values()) {
			JSONObject packageJson = new JSONObject();
			JSONObject packageDataValue = new JSONObject();
			packageDataValue.put("id", pck.getId());
			packageDataValue.put("name", pck.getName());
			packageJson.put("data", packageDataValue);
			nodes.add(packageJson);
		}
		
		for(Package callerPackage : countOfPackageCall.keySet()) {
			for(Package calledPackage : countOfPackageCall.get(callerPackage).keySet()) {
				Integer count = countOfPackageCall.get(callerPackage).get(calledPackage);
				JSONObject edge = new JSONObject();
				JSONObject value = new JSONObject();
				value.put("id", callerPackage.getId() + "_" + calledPackage.getId());
				value.put("value", count);
				value.put("source", callerPackage.getId());
				value.put("target", calledPackage.getId());
				edge.put("data", value);
				edges.add(edge);
			}
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
	
	public void dynamicCallDependency(List<DynamicCall> dynamicCalls) {
		
		countOfFunctionCall = new HashMap<>();
		countOfFileCall = new HashMap<>();
		countOfPackageCall = new HashMap<>();
		functions = new HashMap<>();
		files = new HashMap<>();
		packages = new HashMap<>();
		functionBelongToFile = new HashMap<>();
		fileBelongToPackage = new HashMap<>();

		for(DynamicCall dynamicCall : dynamicCalls) {
			Function callerFunction = dynamicCall.getFunction();
			Function calledFunction = dynamicCall.getCallFunction();
			if(callerFunction.getId().equals(calledFunction.getId())) {
				continue;
			}
			functions.put(callerFunction.getId(), callerFunction);
			functions.put(calledFunction.getId(), calledFunction);
			Map<Function, Integer> tempFunction = countOfFunctionCall.getOrDefault(callerFunction, new HashMap<>());
			Integer size = tempFunction.getOrDefault(calledFunction, 0);
			size++;
			tempFunction.put(calledFunction, size);
			countOfFunctionCall.put(callerFunction, tempFunction);
			
			ProjectFile callerFile = containRelationService.findFunctionBelongToFile(callerFunction);
			ProjectFile calledFile = containRelationService.findFunctionBelongToFile(calledFunction);
			if(callerFile.getId().equals(calledFile.getId())) {
				continue;
			}
			functionBelongToFile.put(callerFunction, callerFile);
			functionBelongToFile.put(calledFunction, calledFile);
			files.put(callerFile.getId(), callerFile);
			files.put(calledFile.getId(), calledFile);
			Map<ProjectFile, Integer> tempFile = countOfFileCall.getOrDefault(callerFile, new HashMap<>());
			size = tempFile.getOrDefault(calledFile, 0);
			size++;
			tempFile.put(calledFile, size);
			countOfFileCall.put(callerFile, tempFile);
			
			Package callerPackage = containRelationService.findFileBelongToPackage(callerFile);
			Package calledPackage = containRelationService.findFileBelongToPackage(calledFile);
			if(callerPackage.getId().equals(calledPackage.getId())) {
				continue;
			}
			fileBelongToPackage.put(callerFile, callerPackage);
			fileBelongToPackage.put(calledFile, calledPackage);
			packages.put(callerPackage.getId(), callerPackage);
			packages.put(calledPackage.getId(), calledPackage);
			Map<Package, Integer> tempPackage = countOfPackageCall.getOrDefault(callerPackage, new HashMap<>());
			size = tempPackage.getOrDefault(calledPackage, 0);
			size++;
			tempPackage.put(calledPackage, size);
			countOfPackageCall.put(callerPackage, tempPackage);
			
		}
		
	}	
}
