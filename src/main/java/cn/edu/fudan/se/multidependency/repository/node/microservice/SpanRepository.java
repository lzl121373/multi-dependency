package cn.edu.fudan.se.multidependency.repository.node.microservice;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.microservice.Span;

@Repository
public interface SpanRepository extends Neo4jRepository<Span, Long>  {
	
	@Query("MATCH (n:Span) RETURN n")
	public List<Span> findAllSpans();
	
	@Query("MATCH (n:Span{traceId:$traceId}) RETURN n")
	public List<Span> findSpansByTraceId(@Param("traceId") String traceId);
	
}
