package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

public class PredicateForFileClone implements FilterForCloneGroup {
	
	private CloneAnalyseService cloneAnalyse;
	
	private ContainRelationService containRelationService;
	
	public PredicateForFileClone(CloneAnalyseService cloneAnalyse, ContainRelationService containRelationService) {
		this.cloneAnalyse = cloneAnalyse;
		this.containRelationService = containRelationService;
	}
	
	@Override
	public boolean remove(CloneGroup t) {
		Collection<Clone> clones = t.getRelations();
		for(Clone clone : clones) {
			CodeNode node1 = clone.getCodeNode1();
			CodeNode node2 = clone.getCodeNode2();
			if(CloneRelationType.str_FILE_CLONE_FILE.equals(clone.getCloneRelationType())) {
				return false;
			}
			if(!cloneAnalyse.isCloneBetweenFiles(containRelationService.findCodeNodeBelongToFile(node1), 
					containRelationService.findCodeNodeBelongToFile(node2))) {
				return false;
			}
		}
		return true;
	}

}
