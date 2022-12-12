package cn.edu.fudan.se.multidependency.repository.node;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface ModuleRepository extends Neo4jRepository<Module, Long> {

	@Query("match (m:Module)-[:CONTAIN]->(file:ProjectFile) where id(file) = $fileId return m;")
	Module findFileBelongToModule(@Param("fileId") long fileId);
	
	@Query("MATCH (m1:Module),(m2:Module) where id(m1) = $m1Id and id(m2) = $m2Id "
			+ "match p = shortestpath((m1)-[:" + RelationType.str_DEPENDS_ON + "*..]->(m2)) return count(p) > 0")
	boolean isDependsOnBetweenModules(@Param("m1Id") long module1Id, @Param("m2Id") long module2Id);

	@Query("match (project:Project)-[:CONTAIN]->(m:Module) where id(m) = $mId return project;")
	Project findModuleBelongToProject(@Param("mId") long moduleId);
}
