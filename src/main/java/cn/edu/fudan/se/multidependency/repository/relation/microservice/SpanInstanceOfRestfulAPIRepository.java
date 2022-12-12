package cn.edu.fudan.se.multidependency.repository.relation.microservice;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanInstanceOfRestfulAPI;

@Repository
public interface SpanInstanceOfRestfulAPIRepository extends Neo4jRepository<SpanInstanceOfRestfulAPI, Long>{

	@Query("match p=(s:Span)-[:" + RelationType.str_SPAN_INSTANCE_OF_RESTFUL_API + "]->(api:RestfulAPI) where s.spanId = $spanId return p")
	public SpanInstanceOfRestfulAPI findSpanBelongToAPI(@Param("spanId") String spanId);
	
}
