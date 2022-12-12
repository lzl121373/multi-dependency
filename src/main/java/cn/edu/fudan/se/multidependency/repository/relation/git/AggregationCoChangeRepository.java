package cn.edu.fudan.se.multidependency.repository.relation.git;

import cn.edu.fudan.se.multidependency.model.relation.git.AggregationCoChange;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregationCoChangeRepository extends Neo4jRepository<AggregationCoChange, Long> {

    @Query("match p = (:Package)-[:" + RelationType.str_AGGREGATION_CO_CHANGE + "]->(:Package) return p")
    List<AggregationCoChange> findAggregationCoChange();

    @Query("match p = (:Package)-[:" + RelationType.str_AGGREGATION_CO_CHANGE + "]->(:Package) return p limit 10")
    List<AggregationCoChange> findAggregationCoChangeLimit();
}
