package cn.edu.fudan.se.multidependency.repository.relation.git;

import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperSubmitCommit;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface DeveloperSubmitCommitRepository extends Neo4jRepository<DeveloperSubmitCommit, Long> {

    @Query("match (d:Developer)-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]->(c:Commit) where id(c)=$cId return d")
    Developer findDeveloperByCommitId(@Param("cId") Long commitId);
}
