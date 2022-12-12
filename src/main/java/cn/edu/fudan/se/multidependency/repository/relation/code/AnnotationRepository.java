package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Annotation;

@Repository
public interface AnnotationRepository extends Neo4jRepository<Annotation, Long> {

	@Query("MATCH result=()-[:" + RelationType.str_ANNOTATION + "]->(type:Type) with type,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)=$projectId RETURN result")
	List<Annotation> findProjectContainNodeAnnotationTypeRelations(@Param("projectId") Long projectId);
}