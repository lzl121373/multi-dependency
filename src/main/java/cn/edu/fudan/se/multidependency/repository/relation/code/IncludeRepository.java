package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;

@Repository
public interface IncludeRepository extends Neo4jRepository<Include, Long> {

	@Query("MATCH result=(file:ProjectFile)-[:" + RelationType.str_INCLUDE + "]->(:ProjectFile) with file,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file) where id(project)=$projectId RETURN result")
	List<Include> findProjectContainFileIncludeFileRelations(@Param("projectId") Long projectId);

	@Query("MATCH result=(file:ProjectFile)-[:" + RelationType.str_INCLUDE + "]->() where id(file) = $fileId RETURN result")
	List<Include> findFileIncludes(@Param("fileId") Long fileId);
}
