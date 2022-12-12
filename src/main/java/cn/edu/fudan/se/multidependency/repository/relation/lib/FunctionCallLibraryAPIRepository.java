package cn.edu.fudan.se.multidependency.repository.relation.lib;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;

@Repository
public interface FunctionCallLibraryAPIRepository extends Neo4jRepository<FunctionCallLibraryAPI, Long> {

}
