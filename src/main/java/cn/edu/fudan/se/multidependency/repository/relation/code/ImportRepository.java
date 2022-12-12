package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;

@Repository
public interface ImportRepository extends Neo4jRepository<Import, Long> {

	@Query("MATCH result=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->(:Function) with file,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file) where id(project)=$projectId RETURN result")
	List<Import> findProjectContainFileImportFunctionRelations(@Param("projectId") Long projectId);

	@Query("MATCH result=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->(:Type) with file,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file) where id(project)=$projectId RETURN result")
	List<Import> findProjectContainFileImportTypeRelations(@Param("projectId") Long projectId);
	
	@Query("MATCH result=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->(:Variable) with file,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file) where id(project)=$projectId RETURN result")
	List<Import> findProjectContainFileImportVariableRelations(@Param("projectId") Long projectId);
	
	@Query("MATCH result=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->() with file,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file) where id(project)=$projectId RETURN result")
	List<Import> findProjectContainImportRelations(@Param("projectId") Long projectId);

	@Query("MATCH result=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->() where id(file) = $fileId RETURN result")
	List<Import> findFileImports(@Param("fileId") Long fileId);

	@Query("MATCH p=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->(t:Type) where id(file) = $fileId RETURN t")
	List<Type> findFileImportTypes(@Param("fileId") Long fileId);

	@Query("MATCH p=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->(t:Function) where id(file) = $fileId RETURN t")
	List<Function> findFileImportFunctions(@Param("fileId") Long fileId);

	@Query("MATCH p=(file:ProjectFile)-[:" + RelationType.str_IMPORT + "]->(t:Variable) where id(file) = $fileId RETURN t")
	List<Variable> findFileImportVariables(@Param("fileId") Long fileId);
}
