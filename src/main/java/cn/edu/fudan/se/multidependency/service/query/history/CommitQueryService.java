package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.service.query.history.data.CommitsInFileMatrix;

public interface CommitQueryService {

	Commit queryCommit(long id);

	Collection<Commit> queryAllCommits();

	Collection<Commit> queryCommitsByGitRepoId(Long gitRepoId);
	
	Collection<Commit> queryUpdatedByCommits(ProjectFile file);
	
	CommitsInFileMatrix queryUpdatedFilesByCommitsInFile(ProjectFile file);
}
