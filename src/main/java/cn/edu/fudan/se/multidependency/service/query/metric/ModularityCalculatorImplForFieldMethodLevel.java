package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.structure.Access;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class ModularityCalculatorImplForFieldMethodLevel implements ModularityCalculator {
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private StaticAnalyseService staticAnalyseService;
	
	private Project project = null;
	
	//private Collection<Function> allFunctions;
	
	private boolean isSameFile(Node node1, Node node2) {
		Type file1 = findNodeBelongToType(node1);
		Type file2 = findNodeBelongToType(node2);
		if(file1 == null || file2 == null) {
			return false;
		}
		return file1.equals(file2);
	}

	private boolean isSameType(Node node1, Node node2) {
		Type type1 = findNodeBelongToType(node1);
		Type type2 = findNodeBelongToType(node2);
		if(type1 == null || type2 == null) {
			return false;
		}
		return type1.equals(type2);
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

		Map<Function, List<Call>> staticCalls = staticAnalyseService.findAllFunctionCallRelationsGroupByCaller(project);
		Map<Function, List<Access>> staticAccesses = staticAnalyseService.findAllFunctionAccessRelationsGroupByCaller(project);

		Map<Function,List<RelationWithTimes>> staticFunctionCallsAndAccesses = new HashMap<>();
		//Map<Function,List<RelationWithTimes>> staticFunctionCallers = new HashMap<>();
		Map<Variable,List<RelationWithTimes>> staticFieldAccessers = new HashMap<>();

		Map<Function,Integer> functionFanInAndOut = new HashMap<>();
		Map<Variable,Integer> fieldFanInAndOut = new HashMap<>();

		double weightSum = 0;
		for (Function caller:staticCalls.keySet()){
			Iterable<Call> calls = staticCalls.get(caller);
			List<RelationWithTimes> callsTmp = new ArrayList<>();
			int times = 0;
			for (Call call:calls){
				callsTmp.add(call);
				times +=call.getTimes();

				Function callFunction = call.getCallFunction();
				List<RelationWithTimes> functionCallerList = staticFunctionCallsAndAccesses.getOrDefault(callFunction, new ArrayList<>());
				functionCallerList.add(call);
				staticFunctionCallsAndAccesses.put(callFunction,functionCallerList);
			}
			List<RelationWithTimes> functionCallList = staticFunctionCallsAndAccesses.getOrDefault(caller, new ArrayList<>());
			functionCallList.addAll(callsTmp);
			staticFunctionCallsAndAccesses.put(caller,functionCallList);
			weightSum += times;
		}
		for (Function caller:staticAccesses.keySet()){
			Iterable<Access> accesses = staticAccesses.get(caller);
			List<RelationWithTimes> accessesTmp = new ArrayList<>();
			int times = 0;
			for (Access access:accesses){
				accessesTmp.add(access);
				times += access.getTimes();

				Variable var = access.getField();
				List<RelationWithTimes> functionAccesserList = staticFieldAccessers.getOrDefault(var, new ArrayList<>());
				functionAccesserList.add(access);
				staticFieldAccessers.put(var,functionAccesserList);
			}
			List<RelationWithTimes> functionCallList = staticFunctionCallsAndAccesses.getOrDefault(caller, new ArrayList<>());
			functionCallList.addAll(accessesTmp);
			staticFunctionCallsAndAccesses.put(caller,functionCallList);
			weightSum += times;
		}

		for (Function caller:staticFunctionCallsAndAccesses.keySet()){
			int fan_in_out = 0;
			Iterable<RelationWithTimes> calls = staticFunctionCallsAndAccesses.get(caller);
			for (RelationWithTimes call:calls){
				fan_in_out += call.getTimes();
			}
			functionFanInAndOut.put(caller,Integer.valueOf(fan_in_out));
		}

		for (Variable var:staticFieldAccessers.keySet()){
			int fan_in_out = 0;
			Iterable<RelationWithTimes> accesses = staticFieldAccessers.get(var);
			for (RelationWithTimes access:accesses){
				fan_in_out += access.getTimes();
			}
			fieldFanInAndOut.put(var,Integer.valueOf(fan_in_out));
		}

		double q_sum = 0.0;

		for(Function caller:staticCalls.keySet()){
			Iterable<Call> calls = staticCalls.get(caller);
			for (Call call:calls){
				Function function = caller;
				Function callFunction = call.getCallFunction();
				double a_i_j =0.0;
				try {
					a_i_j = call.getTimes() - ( functionFanInAndOut.get(function).intValue() * functionFanInAndOut.get(callFunction).intValue()) / (weightSum *2) ;
				}catch (Exception e){
					e.printStackTrace();
				}
				q_sum += a_i_j * ( isSameType(function,callFunction)? 1:0);
			}
		}

		for(Function caller:staticAccesses.keySet()){
			Iterable<Access> accesses = staticAccesses.get(caller);
			for (Access access:accesses){
				Function function = caller;
				Variable var = access.getField();
				double a_i_j =0.0;
				try {
					a_i_j = access.getTimes() - ( functionFanInAndOut.get(function).intValue() * fieldFanInAndOut.get(var).intValue()) / (weightSum *2) ;
				}catch (Exception e){
					e.printStackTrace();
				}
				q_sum += a_i_j * ( isSameType(function,var)? 1:0);
			}
		}

//		for(Variable variable:staticFieldAccessers.keySet()){
//			Iterable<RelationWithTimes> accesses = staticFieldAccessers.get(variable);
//			for (RelationWithTimes access:accesses){
//				FunctionAccessField accesstmp = (FunctionAccessField)access;
//				Function function = accesstmp.getFunction();
//				double a_i_j = 0.0;
//				try{
//					a_i_j = accesstmp.getTimes() - ( fieldFanAndOut.get(variable) * functionFanAndOut.get(function) ) / (weightSum * 2) ;
//				}catch (Exception e){
//					e.printStackTrace();
//				}
//				q_sum += a_i_j * ( isSameType(function,variable)? 1:0);
//			}
//		}


		double Q = 0.0;

		try{
			Q = q_sum / (2*weightSum);
		}catch (Exception e){
			e.printStackTrace();
		}

		Modularity result = new Modularity();
		result.setValue(Q);
		result.setModularityType(NodeLabelType.Project);
		return result;
	}

}
