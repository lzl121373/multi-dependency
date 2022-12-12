package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseRunTrace;

@Repository
public interface TestCaseRunTraceRepository extends Neo4jRepository<TestCaseRunTrace, Long> {

}
