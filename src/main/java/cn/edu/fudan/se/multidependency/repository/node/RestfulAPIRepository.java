package cn.edu.fudan.se.multidependency.repository.node;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;

@Repository
public interface RestfulAPIRepository extends Neo4jRepository<RestfulAPI, Long> {

}
