package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.service.query.history.data.CoChangeFile;
import cn.edu.fudan.se.multidependency.service.query.history.data.GitRepoMetric;

public interface GitAnalyseService {
	
	Collection<CommitUpdateFile> queryCommitUpdateFiles(Commit commit);
	
	Map<String, List<ProjectFile>> queryCommitUpdateFilesGroupByUpdateType(Commit commit);

	Iterable<Commit> findAllCommits();
	
	Map<Developer, Map<Project, Integer>> calCntOfDevUpdPro();
	
	Iterable<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsList();
	
	Map<ProjectFile, Integer> calCntOfFileBeUpd();
	
	Map<ProjectFile, Integer> getTopKFileBeUpd(int k);
	
	Collection<CoChange> calCntOfFileCoChange();
	
	Collection<CoChange> getTopKFileCoChange(int k);
	
	Collection<CoChangeFile> cochangesWithFile(ProjectFile file);
	
	/**
	 * 找出两个文件的Cochange关系
	 * @param file1
	 * @param file2
	 * @return
	 */
	CoChange findCoChangeBetweenTwoFiles(ProjectFile file1, ProjectFile file2);

	/**
	 * 找出两个文件的Cochange关系
	 * @param file1
	 * @param file2
	 * @return
	 */
	List<CoChange> findCoChangeBetweenTwoFilesWithoutDirection(ProjectFile file1, ProjectFile file2);
	
	/**
	 * 给一个cochange，找到贡献cochange次数的commit
	 * commit按时间倒叙排序
	 * @param cochange
	 * @return
	 */
	Collection<Commit> findCommitsByCoChange(CoChange cochange);
	
	CoChange findCoChangeById(long cochangeId);
	
	Collection<Commit> findCommitsInProject(Project project);

	Collection<Developer> findDeveloperInProject(Project project);

	Collection<Issue> findIssuesInGitRepository(GitRepository gitRepository);

	GitRepository findGitRepositoryById(long gitRepoId);

	GitRepository findGitRepositoryByProject(Project project);

	Map<Long, GitRepoMetric> calculateGitRepoMetrics();
}
