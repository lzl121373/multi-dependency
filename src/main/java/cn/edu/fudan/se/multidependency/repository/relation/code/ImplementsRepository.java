package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Extends;

@Repository
public interface ImplementsRepository extends Neo4jRepository<Extends, Long> {
	@Query("match p = (a:Type)-[:" + RelationType.str_IMPLEMENTS + "]->(b:Type) where id(b) = $id return a")
    List<Type> querySubTypes(@Param("id") long typeId);
	
	@Query("match p = (a:Type)-[:" + RelationType.str_IMPLEMENTS + "]->(b:Type) where id(a) = $id return b")
    List<Type> querySuperTypes(@Param("id") long typeId);
}
