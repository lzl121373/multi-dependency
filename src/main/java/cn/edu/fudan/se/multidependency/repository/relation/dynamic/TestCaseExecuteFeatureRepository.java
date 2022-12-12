package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;

@Repository
public interface TestCaseExecuteFeatureRepository extends Neo4jRepository<TestCaseExecuteFeature, Long> {

}
