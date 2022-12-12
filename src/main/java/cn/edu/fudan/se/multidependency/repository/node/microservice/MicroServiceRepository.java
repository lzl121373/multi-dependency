package cn.edu.fudan.se.multidependency.repository.node.microservice;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;

@Repository
public interface MicroServiceRepository extends Neo4jRepository<MicroService, Long>{

}
