package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Cast;

@Repository
public interface CastRepository extends Neo4jRepository<Cast, Long> {

	@Query("MATCH result=(function:Function)-[:" + RelationType.str_CAST + "]->(type:Type) with function,type,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)=$projectId RETURN result")
	List<Cast> findProjectContainFunctionCastTypeRelations(@Param("projectId") Long projectId);
}