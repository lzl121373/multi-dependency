package cn.edu.fudan.se.multidependency.repository.relation;

import cn.edu.fudan.se.multidependency.model.relation.AggregationDependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregationDependsOnRepository extends Neo4jRepository<AggregationDependsOn, Long> {
	
	@Query("match p=(:Package)-[:" + RelationType.str_AGGREGATION_DEPENDS_ON + "]->(:Package) return p;")
	List<AggregationDependsOn> findAggregationDependsOn();

	@Query("match p=(:Package)-[:" + RelationType.str_AGGREGATION_DEPENDS_ON + "]->(:Package) return p limit 10;")
	List<AggregationDependsOn> findAggregationDependsOnWithLimit();
}
