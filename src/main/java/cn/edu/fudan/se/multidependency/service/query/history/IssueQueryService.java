package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.service.query.history.data.IssueFile;

public interface IssueQueryService {
	
	Issue queryIssue(long id);

	Collection<Issue> queryAllIssues();
	
	Collection<Issue> queryIssues(Project project);

	Collection<Issue> queryIssuesByGitRepoId(Long repoId);

	/**
	 * 有commit提交的issue
	 * @return
	 */
	Collection<Issue> queryIssueAddressedByCommit();
	
	Set<ProjectFile> queryRelatedFilesOnAllIssues();
	
	Map<Long, List<IssueFile>> queryRelatedFilesOnAllIssuesGroupByProject();
	
	Collection<ProjectFile> queryRelatedFilesOnIssue(Issue issue);
	
	Collection<Issue> queryRelatedIssuesOnFile(ProjectFile file);
	
	Collection<Commit> queryRelatedCommitsOnIssue(Issue issue);
	
	Collection<Issue> queryIssuesAddressedByCommit(Commit commit);
}
