package cn.edu.fudan.se.multidependency.repository.relation.microservice;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCreateSpan;

@Repository
public interface MicroServiceCreateSpanRepository extends Neo4jRepository<MicroServiceCreateSpan, Long>{
	
	/**
	 * 哪个microservice创建了指定的span
	 * @param spanId
	 * @return
	 */
	@Query("MATCH n = (m:MicroService)-[:" + RelationType.str_MICRO_SERVICE_CREATE_SPAN + "]->(s:Span{spanId:$spanId}) return n")
	public MicroServiceCreateSpan findMicroServiceCreateSpan(@Param("spanId") String spanId);
	

	@Query("match p = (m:MicroService)-[:" + RelationType.str_MICRO_SERVICE_CREATE_SPAN + "]->(s:Span) where id(m) = $microserviceId and s.traceId = $traceId return p")
	public List<MicroServiceCreateSpan> findMicroServiceCreateSpansInTrace(@Param("microserviceId") Long microserviceId, @Param("traceId") String traceId);

}
