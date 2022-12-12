package cn.edu.fudan.se.multidependency.service.query.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.history.data.CommitsInFileMatrix;

@Service
public class CommitQueryServiceImpl implements CommitQueryService {
	
	@Autowired
	private CommitRepository commitRepository;
	
	@Autowired
	private CacheService cache;
	
	@Autowired
	private GitAnalyseService gitAnalyseService;
	
	@Override
	public Commit queryCommit(long id) {
		Commit result = commitRepository.findById(id).get();
		return result;
	}

	@Override
	public Collection<Commit> queryCommitsByGitRepoId(Long gitRepoId) {
		String key = "queryCommitsByGitRepoId_" + gitRepoId;
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Commit> result = commitRepository.queryCommitsByGitRepoId(gitRepoId);
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Commit> queryAllCommits() {
		String key = "queryAllCommits";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Commit> result = commitRepository.queryAllCommits();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Commit> queryUpdatedByCommits(ProjectFile file) {
		String key = "queryUpdatedByCommits_" + file.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<Commit> result = commitRepository.queryUpdatedByCommits(file.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public CommitsInFileMatrix queryUpdatedFilesByCommitsInFile(ProjectFile file) {
		List<Commit> commits = new ArrayList<>(queryUpdatedByCommits(file));
		commits.sort((c1, c2) -> {
			return gitAnalyseService.queryCommitUpdateFiles(c2).size() - gitAnalyseService.queryCommitUpdateFiles(c1).size();
		});
		Map<ProjectFile, Integer> files = new HashMap<>();
		Map<Long, Integer> filePathToCommitTimes = new HashMap<>();
		for(Commit commit : commits) {
			Collection<CommitUpdateFile> updateFiles = gitAnalyseService.queryCommitUpdateFiles(commit);
			for(CommitUpdateFile update : updateFiles) {
				files.put(update.getFile(), files.getOrDefault(update.getFile(), 0) + 1);
				filePathToCommitTimes.put(update.getFile().getId(), filePathToCommitTimes.getOrDefault(update.getFile().getId(), 0) + 1);
			}
		}
		List<ProjectFile> sortFiles = new ArrayList<>(files.keySet());
		sortFiles.sort((f1, f2) -> {
			if(f1.equals(file)) {
				return -1;
			}
			if(f2.equals(file)) {
				return 1;
			}
			return files.get(f2).compareTo(files.get(f1));
		});
		files.clear();
		for(int i = 0; i < sortFiles.size(); i++) {
			files.put(sortFiles.get(i), i);
		}
		boolean[][] updates = new boolean[commits.size()][sortFiles.size()];
		for(int i = 0; i < commits.size(); i++) {
			Commit commit = commits.get(i);
			Collection<CommitUpdateFile> updateFiles = gitAnalyseService.queryCommitUpdateFiles(commit);
			for(CommitUpdateFile update : updateFiles) {
				updates[i][files.get(update.getFile())] = true;
			}
		}
		CommitsInFileMatrix result = new CommitsInFileMatrix(file, sortFiles, commits, updates, filePathToCommitTimes);
		return result;
	}

}
