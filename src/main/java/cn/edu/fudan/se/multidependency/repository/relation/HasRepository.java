package cn.edu.fudan.se.multidependency.repository.relation;

import cn.edu.fudan.se.multidependency.model.relation.Has;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HasRepository extends Neo4jRepository<Has, Long> {

    @Query("match p = ()-[r:" + RelationType.str_HAS + "] -> (:Metric) delete r;")
    void clearHasMetricRelation();

    @Query("match p = ()-[:" + RelationType.str_HAS + "] -> (:Metric) return p;")
    public List<Has> findHasMetric();
}
