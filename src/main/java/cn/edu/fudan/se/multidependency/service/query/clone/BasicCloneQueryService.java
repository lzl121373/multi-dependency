package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import com.alibaba.fastjson.JSONArray;

public interface BasicCloneQueryService {

	/**
	 * 根据克隆类型找出所有该类型的克隆关系
	 * @param cloneType
	 * @return
	 */
	Collection<Clone> findClonesByCloneType(CloneRelationType cloneType);
	
	/**
	 * 找出包含某克隆类型关系的克隆组
	 * @param cloneType
	 * @return
	 */
	Collection<CloneGroup> findGroupsContainCloneTypeRelation(CloneRelationType cloneType);
	
	/**
	 * 找出包含某克隆类型级别的克隆组
	 * @param cloneType
	 * @return
	 */
	Collection<CloneGroup> findGroupsContainCloneTypeRelation(CloneLevel cloneType);
	
	Collection<Clone> findGroupContainCloneRelations(CloneGroup group);
	
	CloneGroup queryCloneGroup(long id);
	
	CloneGroup queryCloneGroup(String name);

	Collection<ProjectFile> findProjectContainCloneFiles(Project project);

	JSONArray ClonesInProject(Collection<Clone> clones);

}
