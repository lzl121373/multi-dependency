package cn.edu.fudan.se.multidependency.repository.node.microservice;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;

@Repository
public interface TraceRepository extends Neo4jRepository<Trace, Long>  {
	
	@Query("MATCH (n:Trace) RETURN n")
	public List<Trace> findAllTrace();
	
	@Query("Match (t:Trace) where t.traceId=$traceId return t")
	public Trace findTraceByTraceId(@Param("traceId") String traceId);

}
