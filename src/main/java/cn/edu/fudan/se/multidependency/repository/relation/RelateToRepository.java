package cn.edu.fudan.se.multidependency.repository.relation;

import cn.edu.fudan.se.multidependency.model.relation.RelateTo;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RelateToRepository extends Neo4jRepository<RelateTo, Long> {

    @Query("match p = ()-[r:" + RelationType.str_RELATE_TO + "] -> () delete r;")
    void clearRelateToRelation();

}
