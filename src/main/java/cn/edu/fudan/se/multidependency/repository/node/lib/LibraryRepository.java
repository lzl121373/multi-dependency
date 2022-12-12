package cn.edu.fudan.se.multidependency.repository.node.lib;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import cn.edu.fudan.se.multidependency.model.node.lib.Library;

public interface LibraryRepository extends Neo4jRepository<Library, Long> {

}
