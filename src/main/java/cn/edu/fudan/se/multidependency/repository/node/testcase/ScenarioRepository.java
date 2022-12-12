package cn.edu.fudan.se.multidependency.repository.node.testcase;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;

@Repository
public interface ScenarioRepository extends Neo4jRepository<Scenario, Long> {

}
