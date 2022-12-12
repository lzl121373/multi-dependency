package cn.edu.fudan.se.multidependency.repository.relation.microservice;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;

@Repository
public interface MicroServiceDependOnMicroServiceRepository extends Neo4jRepository<MicroServiceDependOnMicroService, Long> {

}
