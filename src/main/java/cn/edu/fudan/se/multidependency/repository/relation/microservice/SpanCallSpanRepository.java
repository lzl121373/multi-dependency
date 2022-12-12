package cn.edu.fudan.se.multidependency.repository.relation.microservice;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanCallSpan;

@Repository
public interface SpanCallSpanRepository extends Neo4jRepository<SpanCallSpan, Long> {

	@Query("MATCH n = (s1:Span{spanId:$spanId})-[:" + RelationType.str_SPAN_CALL_SPAN + "]->(s2:Span) return n;")
	public List<SpanCallSpan> findSpanCallSpansBySpanId(@Param("spanId") String spanId);
}
