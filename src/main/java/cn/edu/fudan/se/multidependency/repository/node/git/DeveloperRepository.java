package cn.edu.fudan.se.multidependency.repository.node.git;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DeveloperRepository extends Neo4jRepository<Developer, Long> {

    @Query("match (developer : Developer)" +
            " return distinct developer;")
    List<Developer> queryAllDevelopers();

    @Query("match (developer : Developer)-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]" +
            "->(commit : Commit) " +
            "where id(developer)=$developerId " +
            "return commit")
    List<Commit> queryCommitByDeveloper(@Param("developerId") long developerId);

    @Query("match (developer : Developer)-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]" +
            "->(commit : Commit) " +
            "where id(developer)=$developerId " +
            "return count(commit)")
    Integer queryCommitTimesByDeveloper(@Param("developerId") long developerId);

    @Query("match (developer : Developer)-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]->(: Commit)" +
            "-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file:ProjectFile)" +
            " where id(developer)=$developerId" +
            " return file")
    List<ProjectFile> queryFileChangedByDeveloper(@Param("developerId") long developerId);

    @Query("match (g:GitRepository) -[:" + RelationType.str_CONTAIN + "]-> (b:Branch)" +
            " -[:" + RelationType.str_CONTAIN + "]-> (c:Commit) <-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "] - (d:Developer)" +
            " where id(g) = $gitRepositoryId " +
            " with d,count(c) as times" +
            " return d order by times desc")
    List<Developer> findDevelopersByRepository(long gitRepositoryId);

}
