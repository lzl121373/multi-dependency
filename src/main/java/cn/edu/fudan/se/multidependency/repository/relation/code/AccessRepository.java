package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Access;

public interface AccessRepository extends Neo4jRepository<Access, Long> {
	
	@Query("MATCH result=(function:Function)-[:" + RelationType.str_ACCESS + "]->(field:Variable) with function,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..5]->(function) where id(project)=$projectId RETURN result")
	List<Access> findProjectContainFunctionAccessFieldRelations(@Param("projectId") Long projectId);

	@Query("MATCH result=(function:Function)-[:" + RelationType.str_ACCESS + "]->(field:Variable) where id(function)=$functionId RETURN result")
	List<Access> findFunctionAccessFields(@Param("functionId") long functionId);
	
}
