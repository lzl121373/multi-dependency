package cn.edu.fudan.se.multidependency.repository.node.git;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface CommitRepository extends Neo4jRepository<Commit, Long> {

    @Query("match p = (c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f1:ProjectFile)-[:"
    		+ RelationType.str_CO_CHANGE + "]->(f2:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE 
    		+ "]-(c) where id(f1)=$file1Id and id(f2)=$file2Id return c")
	List<Commit> findCommitsInTwoFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);
	
    @Query("match (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(:ProjectFile)<-[:"
            + RelationType.str_COMMIT_UPDATE_FILE + "]-(commit:Commit) " +
            "where id(project)=$projectId " +
            "return distinct commit;")
    List<Commit> queryCommitsInProject(@Param("projectId") long projectId);

    @Query("match (project:Project)<-[:" + RelationType.str_CONTAIN + "]-(gitRepo:GitRepository)-[:" + RelationType.str_CONTAIN + "*2]->(commit:Commit)" +
            "where commit.merge=false or commit.merge is null " +
            "with project, count(distinct commit) as commits " +
            "set project.commits = commits;")
    void setCommitsForAllProject();
    
    @Query("match (c:Commit) where (c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-() return c order by c.authoredDate desc;")
    List<Commit> queryAllCommits();

    @Query("match (gitRepo:GitRepository)-[:" + RelationType.str_CONTAIN + "*2]->(commit:Commit) " +
            "where id(gitRepo)=$gitRepoId " +
            "return distinct commit order by commit.commitTime desc;")
    List<Commit> queryCommitsByGitRepoId(@Param("gitRepoId") long gitRepoId);
    
    @Query("match (c:Commit)-[:" 
    		+ RelationType.str_COMMIT_UPDATE_FILE 
    		+ "]->(file:ProjectFile) where id(file)=$fileId and (c.merge=false or c.merge is null) return c order by c.authoredDate desc;")
    List<Commit> queryUpdatedByCommits(@Param("fileId") long fileId);

    @Query("match (c:Commit) -[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file:ProjectFile)" +
            " <-[:" + RelationType.str_CONTAIN + "]- (pck:Package)" +
            " where id(c)=$commitId" +
            " return pck;")
    List<Package> queryUpdatedPackageByCommitId(@Param("commitId") Long commitId);

    @Query("match (project:Project) where id(project) = $projectId " +
            "with project " +
            "match (project)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
            "return count(distinct issueCommit);")
    Integer calculateProjectIssueCommitsByProjectId(@Param("projectId") Long projectId);

    @Query("match (project:Project) where id(project) = $projectId " +
            "with project " +
            "match (project)<-[:" + RelationType.str_CONTAIN + "]-(gitRepository:GitRepository)-[:" + RelationType.str_CONTAIN + "*2]->(allCommit:Commit) " +
            "return count(distinct allCommit);")
    Integer calculateProjectCommitsByProjectId(@Param("projectId") Long projectId);

    @Query("match (project:Project) where id(project) = $projectId " +
            "with project " +
            "match (project)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[relation:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
            "with collect(distinct relation) as updates " +
            "with reduce(lines = 0, update in updates | lines + (update.addLines + update.subLines)) as issueChangeLines " +
            "return issueChangeLines;")
    Integer calculateProjectIssueChangeLinesByProjectId(@Param("projectId") Long projectId);

    @Query("match (project:Project) where id(project) = $projectId " +
            "with project " +
            "match (project)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[relation:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit) " +
            "with collect(distinct relation) as updates " +
            "with reduce(lines = 0, update in updates | lines + (update.addLines + update.subLines)) as allChangeLines " +
            "return allChangeLines;")
    Integer calculateProjectChangeLinesByProjectId(@Param("projectId") Long projectId);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
            "return count(distinct issueCommit);")
    Integer calculateFileSmellIssueCommitsByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(allCommit:Commit) " +
            "return count(distinct allCommit);")
    Integer calculateFileSmellCommitsByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[relation:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
            "with collect(distinct relation) as updates " +
            "with reduce(lines = 0, update in updates | lines + (update.addLines + update.subLines)) as issueChangeLines " +
            "return issueChangeLines;")
    Integer calculateFileSmellIssueChangeLinesByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[relation:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit) " +
            "with collect(distinct relation) as updates " +
            "with reduce(lines = 0, update in updates | lines + (update.addLines + update.subLines)) as allChangeLines " +
            "return allChangeLines;")
    Integer calculateFileSmellChangeLinesByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
            "return count(distinct issueCommit);")
    Integer calculatePackageSmellIssueCommitsByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(allCommit:Commit) " +
            "return count(distinct allCommit);")
    Integer calculatePackageSmellCommitsByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[relation:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
            "with collect(distinct relation) as updates " +
            "with reduce(lines = 0, update in updates | lines + (update.addLines + update.subLines)) as issueChangeLines " +
            "return issueChangeLines;")
    Integer calculatePackageSmellIssueChangeLinesByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

    @Query("match (smell:Smell) " +
            "where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
            "with smell " +
            "match (smell)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[relation:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(issueCommit:Commit) " +
            "with collect(distinct relation) as updates " +
            "with reduce(lines = 0, update in updates | lines + (update.addLines + update.subLines)) as allChangeLines " +
            "return allChangeLines;")
    Integer calculatePackageSmellChangeLinesByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);
}
