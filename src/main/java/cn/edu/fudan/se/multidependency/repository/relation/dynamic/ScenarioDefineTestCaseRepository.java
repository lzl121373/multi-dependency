package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.dynamic.ScenarioDefineTestCase;

@Repository
public interface ScenarioDefineTestCaseRepository extends Neo4jRepository<ScenarioDefineTestCase, Long> {

}
