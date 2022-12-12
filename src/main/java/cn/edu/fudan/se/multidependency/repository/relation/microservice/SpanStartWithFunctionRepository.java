package cn.edu.fudan.se.multidependency.repository.relation.microservice;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.SpanStartWithFunction;

@Repository
public interface SpanStartWithFunctionRepository extends Neo4jRepository<SpanStartWithFunction, Long> {

	
	@Query("match p = (s:Span)-[:" + RelationType.str_SPAN_START_WITH_FUNCTION + "]->(f:Function) where s.traceId = $traceId and s.spanId = $spanId return p;")
	SpanStartWithFunction findSpanStartWIthFunctionByTraceIdAndSpanId(@Param("traceId") String traceId, @Param("spanId") String spanId);
	
}
