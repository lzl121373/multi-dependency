package cn.edu.fudan.se.multidependency.repository.node.code;

import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariableRepository extends Neo4jRepository<Variable, Long> {

}
