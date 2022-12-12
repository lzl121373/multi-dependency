package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Parameter;

@Repository
public interface ParameterRepository extends Neo4jRepository<Parameter, Long>{

	@Query("MATCH result=()-[:" + RelationType.str_PARAMETER + "]->(type:Type) with type,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)=$projectId RETURN result")
	List<Parameter> findProjectContainParameterRelations(@Param("projectId") Long projectId);
	
	@Query("MATCH result=(function:Function)-[:" + RelationType.str_PARAMETER + "]->(type:Type) with function,type,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)=$projectId RETURN result")
	List<Parameter> findProjectContainFunctionParameterTypeRelations(@Param("projectId") Long projectId);
	
	@Query("MATCH result=(variable:Variable)-[:" + RelationType.str_PARAMETER + "]->(type:Type) with variable,type,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)=$projectId RETURN result")
	List<Parameter> findProjectContainVariableTypeParameterTypeRelations(@Param("projectId") Long projectId);

}
