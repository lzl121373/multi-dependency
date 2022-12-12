package cn.edu.fudan.se.multidependency.repository.node.git;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface IssueRepository extends Neo4jRepository<Issue, Long> {

	@Query("match (issue:Issue) return issue order by issue.issueId desc;")
	List<Issue> queryAllIssues();

	@Query("match (issue:Issue) " +
			"where issue.repoBelongName = $repoName "+
			"return issue order by issue.issueId desc;")
	List<Issue> queryIssuesByGitRepoName(@Param("repoName") String repoName);
	
	@Query("match (issue:Issue)<-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]-(:Commit) return issue order by issue.issueId desc;")
	List<Issue> queryIssueAddressedByCommit();
	
}
