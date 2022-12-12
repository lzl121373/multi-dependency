package cn.edu.fudan.se.multidependency.repository.relation.microservice;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;

@Repository
public interface MicroServiceCallMicroServiceRepository extends Neo4jRepository<MicroServiceCallMicroService, Long> {

}
