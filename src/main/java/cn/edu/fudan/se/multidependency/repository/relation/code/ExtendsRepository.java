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
public interface ExtendsRepository extends Neo4jRepository<Extends, Long> {
	
	@Query("match (a:Type)-[:" + RelationType.str_EXTENDS + "]->(b:Type) where id(b) = $id return a")
    List<Type> querySubTypes(@Param("id") long typeId);
	
	@Query("match (a:Type)-[:" + RelationType.str_EXTENDS + "]->(b:Type) where id(a) = $id return b")
    List<Type> querySuperTypes(@Param("id") long typeId);
	
	@Query("MATCH result=(type1:Type)-[:" + RelationType.str_EXTENDS + "]->(type2:Type) with type1,type2,result match (project:Project)-[:" + RelationType.str_CONTAIN + "*3..4]->(type1) where id(project)=$projectId RETURN result")
	List<Extends> findProjectContainTypeExtendsTypeRelations(@Param("projectId") long projectId);

	@Query("match p=(a:Type)-[:" + RelationType.str_EXTENDS + "*1..]->(b:Type) where id(a)=$subTypeId and id(b)=$superTypeId return b;")
	Type findIsTypeExtendsType(@Param("subTypeId") long subTypeId, @Param("superTypeId") long superTypeId);
	
}
