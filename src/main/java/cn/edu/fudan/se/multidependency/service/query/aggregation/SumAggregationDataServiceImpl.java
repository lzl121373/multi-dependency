package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.*;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.RelationAggregatorForMicroServiceByClone;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.RelationDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SumAggregationDataServiceImpl implements SumAggregationDataService {

    @Autowired
    ContainRelationService containRelationService;

	@Override
	public Collection<RelationDataForDoubleNodes<Node, Relation>> findMicroServiceCloneFromFunctionClone(Collection<? extends Relation> functionClones) {
		Collection<RelationDataForDoubleNodes<Node, Relation>> projectClones = findProjectCloneFromFunctionClone(functionClones);
		List<RelationDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
		Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> msToMsClones = new HashMap<>();
		for(RelationDataForDoubleNodes<Node, Relation> projectClone : projectClones) {
			Project project1 = (Project)projectClone.getNode1();
			Project project2 = (Project)projectClone.getNode2();
			if(project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null || ms1.equals(ms2)) {
				continue;
			}
			RelationDataForDoubleNodes<Node, Relation> msClone =  getSuperNodeRelationWithSubNodeRelation2(msToMsClones, ms1, ms2);
			if(msClone == null) {
				msClone = new RelationDataForDoubleNodes(ms1, ms2);
				result.add(msClone);
				RelationAggregatorForMicroServiceByClone<Function> aggregator = new RelationAggregatorForMicroServiceByClone();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				aggregator.addNodes(functions1, ms1);
				aggregator.addNodes(functions2, ms2);
				msClone.setAggregator(aggregator);
			}
			msClone.addChildren(projectClone.getChildren());
			Map<Node, RelationDataForDoubleNodes<Node, Relation>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, msClone);
			msToMsClones.put(ms1, ms1ToClones);

		}
		return result;
	}

	@Override
	public Collection<RelationDataForDoubleNodes<Node, Relation>> findMicroServiceCloneFromFileClone(
			Collection<? extends Relation> fileClones) {
		Iterable<RelationDataForDoubleNodes<Node, Relation>> projectClones = findProjectCloneFromFileClone(fileClones);
		List<RelationDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
		Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> msToMsClones = new HashMap<>();
		for(RelationDataForDoubleNodes<Node, Relation> projectClone : projectClones) {
			Project project1 = (Project)projectClone.getNode1();
			Project project2 = (Project)projectClone.getNode2();
			if(project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null) {
				continue;
			}
			if(ms1.equals(ms2)) {
				continue;
			}
			RelationDataForDoubleNodes<Node, Relation> msClone = getSuperNodeRelationWithSubNodeRelation2(msToMsClones, ms1, ms2);
			if(msClone == null) {
				msClone = new RelationDataForDoubleNodes<Node, Relation>(ms1, ms2);
				result.add(msClone);
				RelationAggregatorForMicroServiceByClone<ProjectFile> aggregator = new RelationAggregatorForMicroServiceByClone();
				Iterable<ProjectFile> files1 = containRelationService.findMicroServiceContainFiles(ms1);
				Iterable<ProjectFile> files2 = containRelationService.findMicroServiceContainFiles(ms2);
				aggregator.addNodes(files1, ms1);
				aggregator.addNodes(files2, ms2);
				msClone.setAggregator(aggregator);
			}
			msClone.addChildren(projectClone.getChildren());
			Map<Node, RelationDataForDoubleNodes<Node, Relation>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, msClone);
			msToMsClones.put(ms1, ms1ToClones);

		}
		return result;
	}

	@Override
	public Collection<RelationDataForDoubleNodes<Node, Relation>> findProjectCloneFromFileClone(Collection<? extends Relation> fileClones) {
		List<RelationDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
		Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> projectToProjectClones = new HashMap<>();
		for(Relation clone : fileClones) {
			CodeNode node1 = (CodeNode)clone.getStartNode();
			CodeNode node2 = (CodeNode)clone.getEndNode();
			if(!(node1 instanceof ProjectFile) || !(node2 instanceof ProjectFile)) {
				continue;
			}
			ProjectFile file1 = (ProjectFile) node1;
			ProjectFile file2 = (ProjectFile) node2;
			if(file1.equals(file2)) {
				continue;
			}
			Project project1 = containRelationService.findFileBelongToProject(file1);
			Project project2 = containRelationService.findFileBelongToProject(file2);
			if(project1.equals(project2)) {
				continue;
			}
			RelationDataForDoubleNodes<Node, Relation> cloneValue = getSuperNodeRelationWithSubNodeRelation2(projectToProjectClones, project1, project2);
			if(cloneValue == null) {
				cloneValue = new RelationDataForDoubleNodes<Node, Relation>(project1, project2);
				result.add(cloneValue);
			}
			cloneValue.addChild(clone);
			
			Map<Node, RelationDataForDoubleNodes<Node, Relation>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, cloneValue);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}


	@Override
	public Collection<RelationDataForDoubleNodes<Node, Relation>> findProjectCloneFromFunctionClone(Collection<? extends Relation> functionClones) {
		List<RelationDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
		Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> projectToProjectClones = new HashMap<>();
		for(Relation functionCloneFunction : functionClones) {
			CodeNode node1 = (CodeNode)functionCloneFunction.getStartNode();
			CodeNode node2 = (CodeNode)functionCloneFunction.getEndNode();
			if(!(node1 instanceof Function) || !(node2 instanceof Function)) {
				continue;
			}
			Function function1 = (Function) node1;
			Function function2 = (Function) node2;
			if(function1.equals(function2)) {
				continue;
			}
			Project project1 = containRelationService.findFunctionBelongToProject(function1);
			Project project2 = containRelationService.findFunctionBelongToProject(function2);
			if(project1.equals(project2)) {
				continue;
			}
			RelationDataForDoubleNodes<Node, Relation> clone = (RelationDataForDoubleNodes<Node, Relation>) getSuperNodeRelationWithSubNodeRelation2(projectToProjectClones, project1, project2);
			if(clone == null) {
				clone = new RelationDataForDoubleNodes(project1, project2);
				result.add(clone);
			}
			// 函数间的克隆作为Children
			clone.addChild(functionCloneFunction);
			
			Map<Node, RelationDataForDoubleNodes<Node, Relation>> project1ToClones
				= projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, clone);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}

}
