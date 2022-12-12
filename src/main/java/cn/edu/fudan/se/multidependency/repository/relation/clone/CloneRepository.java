package cn.edu.fudan.se.multidependency.repository.relation.clone;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;

@Repository
public interface CloneRepository extends Neo4jRepository<Clone, Long> {
	
	/**
	 * 根据克隆类型找出克隆关系
	 * @param cloneRelationType
	 * @return
	 */
	@Query("match p= ()-[:" + RelationType.str_CLONE + "]->() where r.cloneRelationType=$cloneRelationType return p")
	public List<Clone> findAllClonesByCloneType(@Param("cloneRelationType") String cloneRelationType);
	
	@Query("match p= (:ProjectFile)-[:" + RelationType.str_CLONE + "]->(:ProjectFile) return p")
	public List<Clone> findAllFileClones();
	
	@Query("match p= (:Function)-[:" + RelationType.str_CLONE + "]->(:Function) return p")
	public List<Clone> findAllFunctionClones();
	
	@Query("match p= (:Type)-[:" + RelationType.str_CLONE + "]->(:Type) return p")
	public List<Clone> findAllTypeClones();
	
	@Query("match p= (:Snippet)-[:" + RelationType.str_CLONE + "]->(:Snippet) return p")
	public List<Clone> findAllSnippetClones();

	@Query("match p= ()-[:" + RelationType.str_CLONE + "]->() return p limit 10;")
	public List<Clone> findClonesLimit();

	/**
	 * 根据克隆类型找出包含此克隆类型关系的所有克隆组
	 * @param cloneRelationType
	 * @return
	 */
	@Deprecated
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->()-[r:" + RelationType.str_CLONE + "]->()<-[:" + RelationType.str_CONTAIN + "]-(g) where r.cloneRelationType=$cloneRelationType return g")
	public List<CloneGroup> findGroupsByCloneType(@Param("cloneRelationType") String cloneRelationType);
	
	/*@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(:ProjectFile)-[r:" + RelationType.str_CLONE + "]->(:ProjectFile)<-[:" + RelationType.str_CONTAIN + "]-(g) return g")
	public List<CloneGroup> findGroupsByFileCloneFileRelation();
	
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(:Function)-[r:" + RelationType.str_CLONE + "]->(:Function)<-[:" + RelationType.str_CONTAIN + "]-(g) return g")
	public List<CloneGroup> findGroupsByFunctionCloneFunctionRelation();
	
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(:Type)-[r:" + RelationType.str_CLONE + "]->(:Type)<-[:" + RelationType.str_CONTAIN + "]-(g) return g")
	public List<CloneGroup> findGroupsByTypeCloneTypeRelation();
	
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(:Snippet)-[r:" + RelationType.str_CLONE + "]->(:Snippet)<-[:" + RelationType.str_CONTAIN + "]-(g) return g")
	public List<CloneGroup> findGroupsBySnippetCloneSnippetRelation();*/
	
	
	/**
	 * 根据克隆组的id找出克隆组内的所有克隆关系
	 * @param groupId
	 * @return
	 */
	@Query("match p= (g:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->()-[:" + RelationType.str_CLONE + "]->()<-[:" + RelationType.str_CONTAIN + "]-(g) where id(g)=$groupId return p")
	public List<Clone> findCloneGroupContainClones(@Param("groupId") long groupId);

	/**
	 * 根据项目的id找出项目内所有有克隆的文件
	 * @param projectId
	 * @return
	 */
	@Query("match (project:Project)-[:CONTAIN*2]->(file:ProjectFile)-[:CLONE]-() where id(project)=$projectId return file;")
	public List<ProjectFile> findProjectContainCloneFiles(@Param("projectId") long projectId);

	@Query("match (project:Project)-[:CONTAIN*2..4]->(node:CodeUnit) set node.projectId = id(project);")
	void setProjectClone();

	@Query("match p = (f1:ProjectFile)-[r:" + RelationType.str_CLONE + "]->(f2:ProjectFile) where id(f1) = $fileId1 and id(f2) = $fileId2 return p;")
	List<Clone> judgeCloneByFileId(@Param("fileId1") long fileId1, @Param("fileId2") long fileId2);

	@Query("match p = (p1:ProjectFile)-[:" + RelationType.str_CLONE + "]->(p2:ProjectFile) where p1.projectBelongPath = $projectBelongPath and p1.language = $language and p2.projectBelongPath = $projectBelongPath and p2.language = $language return p")
	List<Clone> findFileClonesInProjectByPathAndLanguage(@Param("projectBelongPath") String projectBelongPath, @Param("language") String language);

}
