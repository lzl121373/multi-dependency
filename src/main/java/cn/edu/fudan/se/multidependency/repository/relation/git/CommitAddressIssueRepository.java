package cn.edu.fudan.se.multidependency.repository.relation.git;

import java.util.List;
import java.util.Set;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitAddressIssue;

@Repository
public interface CommitAddressIssueRepository extends Neo4jRepository<CommitAddressIssue, Long> {

	@Query("match p= (:commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(:Issue) return p")
	List<CommitAddressIssue> queryAllCommitAddressIssues();
	
	@Query("match p = (commit:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE
			+ "]->(issue:Issue) where id(commit)=$id return issue;")
	List<Issue> queryIssuesAddressedByCommit(@Param("id") long commitId);
	
	@Query("match p = (issue:Issue)<-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE
			+ "]-(commit:Commit) where id(issue)=$id return commit;")
	List<Commit> queryRelatedCommitsOnIssue(@Param("id") long issueId);
	
	@Query("match p = (issue:Issue)<-[:"
			+ RelationType.str_COMMIT_ADDRESS_ISSUE 
			+ "]-(commit:Commit)-[:" 
			+ RelationType.str_COMMIT_UPDATE_FILE
			+ "]->(file:ProjectFile) where id(issue)=$id return file;")
	List<ProjectFile> queryRelatedFilesOnIssue(@Param("id") long issueId);

	@Query("match p = (issue:Issue)<-[:"
			+ RelationType.str_COMMIT_ADDRESS_ISSUE 
			+ "]-(commit:Commit)-[:" 
			+ RelationType.str_COMMIT_UPDATE_FILE
			+ "]->(file:ProjectFile) return file order by file.path;")
	Set<ProjectFile> queryRelatedFilesOnAllIssues();

	@Query("match p = (file:ProjectFile)<-[:"
			+ RelationType.str_COMMIT_UPDATE_FILE 
			+ "]-(commit:Commit) where id(file) = $id with commit "
			+ "match (commit)-[:" 
			+ RelationType.str_COMMIT_ADDRESS_ISSUE 
			+ "]->(issue:Issue) return issue;")
	List<Issue> queryRelatedIssuesOnFile(@Param("id") long fileId);
}
