package cn.edu.fudan.se.multidependency.repository.node.code;

import cn.edu.fudan.se.multidependency.model.relation.Relation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

import java.util.List;

@Repository
public interface TypeRepository extends Neo4jRepository<Type, Long> {

    @Query("MATCH p=(t:Type)-->(:Type) where id(t) = $typeId RETURN p")
    List<Relation> findTypeStructureDependencyRelations(@Param("typeId") Long typeId);
}
