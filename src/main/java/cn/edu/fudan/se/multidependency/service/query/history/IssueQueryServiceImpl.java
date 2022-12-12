package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.GitRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.repository.node.git.IssueRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitAddressIssueRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.history.data.IssueFile;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class IssueQueryServiceImpl implements IssueQueryService {
	
	@Autowired
	private IssueRepository issueRepository;

	@Autowired
	private GitRepoRepository gitRepoRepository;
	
	@Autowired
	private CommitAddressIssueRepository commitAddressIssueRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;

	@Override
	public Collection<Issue> queryIssues(Project project) {
		String key = "queryIssuesProject_" + project.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<IssueFile> issueFiles = queryRelatedFilesOnAllIssuesGroupByProject().get(project.getId());
		Set<Issue> issues = new HashSet<>();
		for(IssueFile issueFile : issueFiles) {
			issues.addAll(issueFile.getIssues());
		}
		cache.cache(getClass(), key, issues);
		return issues;
	}

	@Override
	public Collection<Issue> queryIssuesByGitRepoId(Long gitRepoId) {
		String key = "queryIssuesByGitRepoId_" + gitRepoId;
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		GitRepository gitRepository = gitRepoRepository.findById(gitRepoId).get();
		Collection<Issue> issues = new ArrayList<>();
		if(gitRepository != null){
			issues = issueRepository.queryIssuesByGitRepoName(gitRepository.getName());
			cache.cache(getClass(), key, issues);
		}
		return issues;
	}

	@Override
	public Issue queryIssue(long id) {
		if(cache.findNodeById(id) != null) {
			return (Issue) cache.findNodeById(id);
		}
		Issue result = issueRepository.findById(id).get();
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public Collection<Issue> queryAllIssues() {
		String key = "queryAllIssues";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Issue> result = issueRepository.queryAllIssues();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Issue> queryIssueAddressedByCommit() {
		String key = "queryIssueAddressedByCommit";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Issue> result = issueRepository.queryIssueAddressedByCommit();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<ProjectFile> queryRelatedFilesOnIssue(Issue issue) {
		String key = "queryRelatedFilesInIssue_" + issue.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<ProjectFile> result = commitAddressIssueRepository.queryRelatedFilesOnIssue(issue.getId());
		result.sort((f1, f2) -> {
			return f1.getPath().compareTo(f2.getPath());
		});
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Issue> queryRelatedIssuesOnFile(ProjectFile file) {
		String key = "queryRelatedIssuesOnFile_" + file.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Issue> result = commitAddressIssueRepository.queryRelatedIssuesOnFile(file.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Issue> queryIssuesAddressedByCommit(Commit commit) {
		String key = "queryIssuesAddressedByCommit_" + commit.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Issue> result = commitAddressIssueRepository.queryIssuesAddressedByCommit(commit.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Commit> queryRelatedCommitsOnIssue(Issue issue) {
		String key = "queryRelatedCommitsOnIssue_" + issue.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Commit> result = commitAddressIssueRepository.queryRelatedCommitsOnIssue(issue.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Set<ProjectFile> queryRelatedFilesOnAllIssues() {
		String key = "queryRelatedFilesOnAllIssues";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Set<ProjectFile> result = commitAddressIssueRepository.queryRelatedFilesOnAllIssues();
		cache.cache(getClass(), key, result);
		return result;
	}
	
	private IssueFile queryIssueFile(ProjectFile file) {
		IssueFile issueFile = new IssueFile(file);
		issueFile.addAll(queryRelatedIssuesOnFile(file));
		return issueFile;
	}

	@Override
	public Map<Long, List<IssueFile>> queryRelatedFilesOnAllIssuesGroupByProject() {
		String key = "queryRelatedFilesOnAllIssuesGroupByProject";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		
		Set<ProjectFile> issueFiles = queryRelatedFilesOnAllIssues();
		List<Project> projects = nodeService.allProjects();
		Map<Long, List<IssueFile>> result = new HashMap<>();
		for(Project project : projects) {
			result.put(project.getId(), new ArrayList<>());
		}
		
		for(ProjectFile file : issueFiles) {
			Project belongToProject = containRelationService.findFileBelongToProject(file);
			List<IssueFile> files = result.get(belongToProject.getId());
			files.add(queryIssueFile(file));
			result.put(belongToProject.getId(), files);
		}
		cache.cache(getClass(), key, result);
		return result;
	}


}
