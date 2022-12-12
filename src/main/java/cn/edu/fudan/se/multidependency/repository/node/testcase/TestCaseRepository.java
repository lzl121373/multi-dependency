package cn.edu.fudan.se.multidependency.repository.node.testcase;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface TestCaseRepository extends Neo4jRepository<TestCase, Long> {
	
	@Query("MATCH (a:TestCase)-[:" + RelationType.str_TESTCASE_EXECUTE_FEATURE + "]->(n:Feature{featureName:$featureName}) RETURN a")
	public List<TestCase> findTestCasesByFeatureName(@Param("featureName") String featureName);
	
}
