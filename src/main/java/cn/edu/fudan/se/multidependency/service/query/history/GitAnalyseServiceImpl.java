package cn.edu.fudan.se.multidependency.service.query.history;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.repository.node.git.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.GitRepoRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.history.data.CoChangeFile;
import cn.edu.fudan.se.multidependency.service.query.history.data.GitRepoMetric;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.metric.ProjectMetric;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class GitAnalyseServiceImpl implements GitAnalyseService {

	@Autowired
	private GitRepoRepository gitRepoRepository;

    @Autowired
    private CommitRepository commitRepository;

	@Autowired
    private IssueQueryService issueQueryService;

    @Autowired
    private CommitUpdateFileRepository commitUpdateFileRepository;

    @Autowired
    private DeveloperSubmitCommitRepository developerSubmitCommitRepository;

	@Autowired
	private DeveloperRepository developerRepository;

    @Autowired
    private ContainRelationService containRelationService;
    
    @Autowired
    private CoChangeRepository cochangeRepository;

	@Autowired
	private MetricCalculatorService metricCalculatorService;
    
    @Autowired
    private CacheService cache;

    private Map<Developer, Map<Project, Integer>> cntOfDevUpdProCache = null;

    private Map<ProjectFile, Integer> cntOfFileBeUpdtdCache = null;

	@Override
	public Map<Long, GitRepoMetric> calculateGitRepoMetrics() {
		String key = "calculateGitRepoMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, GitRepoMetric> result = new HashMap<>();
		Map<String, List<ProjectMetric>> projectMetricsMap = new HashMap<>(metricCalculatorService.calculateProjectMetricsByGitRepository());
		for(GitRepository gitRepository : gitRepoRepository.findAll()) {
			GitRepoMetric gitRepoMetric = new GitRepoMetric();
			gitRepoMetric.setGitRepository(gitRepository);
			gitRepoMetric.setProjectMetricsList(projectMetricsMap.get(gitRepository.getName()));
			gitRepoMetric.setCommits(calculateGitRepoCommits(gitRepository));
			gitRepoMetric.setIssues(issueQueryService.queryIssuesByGitRepoId(gitRepository.getId()).size());
			gitRepoMetric.setDevelopers(developerRepository.findDevelopersByRepository(gitRepository.getId()).size());
			result.put(gitRepository.getId(), gitRepoMetric);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	public int calculateGitRepoCommits(GitRepository gitRepository) {
		return gitRepoRepository.queryCommitsByGitRepoId(gitRepository.getId()).size();
	}
    
    @Override
    public Iterable<Commit> findAllCommits() {
    	String key = "findAllCommits";
    	if(cache.get(getClass(), key) != null) {
    		return cache.get(getClass(), key);
    	}
    	Iterable<Commit> result = commitRepository.findAll();
    	cache.cache(getClass(), key, result);
        return result;
    }

    @Override
    public Map<Developer, Map<Project, Integer>> calCntOfDevUpdPro() {
        if (cntOfDevUpdProCache == null) {
            cntOfDevUpdProCache = new HashMap<>();
        } else {
            return cntOfDevUpdProCache;
        }
        for (Commit commit : findAllCommits()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
            if (!cntOfDevUpdProCache.containsKey(developer)) {
                cntOfDevUpdProCache.put(developer, new HashMap<>());
            }
            Set<Project> updProjects = new HashSet<>();
            for (ProjectFile file : files) {
                Project project = containRelationService.findFileBelongToProject(file);
                if (project != null) {
                    updProjects.add(project);
                }
            }
            for (Project project : updProjects) {
                Map<Project, Integer> map = cntOfDevUpdProCache.get(developer);
                map.put(project, map.getOrDefault(project, 0) + 1);
            }
        }
        return cntOfDevUpdProCache;
    }

    @Override
    public Collection<DeveloperUpdateNode<MicroService>> cntOfDevUpdMsList() {
    	String key = "cntOfDevUpdMsList";
    	if(cache.get(getClass(), key) != null) {
    		return cache.get(getClass(), key);
    	}
        Map<MicroService, DeveloperUpdateNode<MicroService>> map = new HashMap<>();
        for (Map.Entry<Developer, Map<Project, Integer>> developer : calCntOfDevUpdPro().entrySet()) {
            List<Map.Entry<Project, Integer>> list = new ArrayList<>(developer.getValue().entrySet());
            list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            int cnt = 0;
            int k = 5;
            for (Map.Entry<Project, Integer> project : list) {
                if (cnt++ == k) break;
                Developer d = developer.getKey();
                int times = project.getValue();
                MicroService microService = containRelationService.findProjectBelongToMicroService(project.getKey());
                if (!map.containsKey(microService)) {
                    DeveloperUpdateNode<MicroService> temp = new DeveloperUpdateNode<>();
                    temp.setDeveloper(d);
                    temp.setNode(microService);
                    temp.setTimes(times);
                    map.put(microService, temp);
                } else {
                    DeveloperUpdateNode<MicroService> temp = map.get(microService);
                    temp.setTimes(temp.getTimes() + times);
                }
            }
        }
        List<DeveloperUpdateNode<MicroService>> result = new ArrayList<>(map.values());
        cache.cache(getClass(), key, result);
        return result;
    }

    @Override
    public Map<ProjectFile, Integer> calCntOfFileBeUpd() {
        if (cntOfFileBeUpdtdCache != null) {
            return cntOfFileBeUpdtdCache;
        }
        Map<ProjectFile, Integer> result = new HashMap<>();
        for (Commit commit : findAllCommits()) {
            List<ProjectFile> files = commitUpdateFileRepository.findUpdatedFilesByCommitId(commit.getId());
            for (ProjectFile file : files) {
                result.put(file, result.getOrDefault(file, 0) + 1);
            }
        }
        cntOfFileBeUpdtdCache = result;
        return result;
    }

    @Override
    public Map<ProjectFile, Integer> getTopKFileBeUpd(int k) {
        if (cntOfFileBeUpdtdCache == null) {
            cntOfFileBeUpdtdCache = calCntOfFileBeUpd();
        }
        Queue<ProjectFile> files = new PriorityQueue<>(Comparator.comparingInt(o -> cntOfFileBeUpdtdCache.get(o)));
        for (ProjectFile file : cntOfFileBeUpdtdCache.keySet()) {
            files.offer(file);
            if (files.size() > k) files.poll();
        }
        Map<ProjectFile, Integer> result = new HashMap<>();
        for (ProjectFile file : files) {
            result.put(file, cntOfFileBeUpdtdCache.get(file));
        }
        return result;
    }

	@Override
	public Collection<CoChange> calCntOfFileCoChange() {
		String key = "allFileCoChanges";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<CoChange> result = cochangeRepository.findGreaterThanCountCoChanges(1);
		result.sort((c1, c2) -> {
			return c2.getTimes() - c1.getTimes();
		});
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<CoChange> getTopKFileCoChange(int k) {
		return new ArrayList<>(calCntOfFileCoChange()).subList(0, k);
	}
	
	private CoChange findCoChange(ProjectFile file1, ProjectFile file2) {
		String key = "findCoChange_" + file1.getId() + "_" + file2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		CoChange result = cochangeRepository.findCoChangesBetweenTwoFiles(file1.getId(), file2.getId());
		if(result != null) {
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	private List<CoChange> findCoChangeWithoutDirection(ProjectFile file1, ProjectFile file2) {
		String key = "findCoChangeWithoutDirection_" + file1.getId() + "_" + file2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<CoChange> result = cochangeRepository.findCoChangesBetweenTwoFilesWithoutDirection(file1.getId(), file2.getId());
		if(result != null) {
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	@Override
	public CoChange findCoChangeBetweenTwoFiles(ProjectFile file1, ProjectFile file2) {
		CoChange result = findCoChange(file1, file2);
		if(result == null) {
			return findCoChange(file2, file1);
		}
		return result;
	}

	@Override
	public List<CoChange> findCoChangeBetweenTwoFilesWithoutDirection(ProjectFile file1, ProjectFile file2) {
		List<CoChange> result = findCoChangeWithoutDirection(file1, file2);
		if(result == null) {
			return findCoChangeWithoutDirection(file2, file1);
		}
		return result;
	}

	@Override
	public Collection<Commit> findCommitsByCoChange(CoChange cochange) {
		ProjectFile file1 = (ProjectFile)cochange.getNode1();
		ProjectFile file2 = (ProjectFile)cochange.getNode2();
		List<Commit> result = commitRepository.findCommitsInTwoFiles(file1.getId(), file2.getId());
		if(result.isEmpty()) {
			result = commitRepository.findCommitsInTwoFiles(file2.getId(), file1.getId());
		}
		result.sort((c1, c2) -> {
	        Timestamp time1 = Timestamp.valueOf(c1.getAuthoredDate());
	        Timestamp time2 = Timestamp.valueOf(c2.getAuthoredDate());
			return time2.compareTo(time1);
		});
		return result;
	}

	@Override
	public CoChange findCoChangeById(long cochangeId) {
		return cochangeRepository.findById(cochangeId).get();
	}

	@Override
	public Collection<Commit> findCommitsInProject(Project project) {
		return commitRepository.queryCommitsInProject(project.getId());
	}

	@Override
	public Collection<Developer> findDeveloperInProject(Project project) {
		GitRepository gitRepository = findGitRepositoryByProject(project);
		return developerRepository.findDevelopersByRepository(gitRepository.getId());
	}

	@Override
	public GitRepository findGitRepositoryByProject(Project project) {
		return gitRepoRepository.findGitRepositoryByProject(project.getId());
	}

	@Override
	public Collection<Issue> findIssuesInGitRepository(GitRepository gitRepository) {
		return issueQueryService.queryIssuesByGitRepoId(gitRepository.getId());
	}

	@Override
	public Collection<CoChangeFile> cochangesWithFile(ProjectFile file) {
		List<CoChangeFile> result = new ArrayList<>();
		for(CoChange cochange : cochangeRepository.cochangesLeft(file.getId())) {
			result.add(new CoChangeFile(file, cochange));
		}
		for(CoChange cochange : cochangeRepository.cochangesRight(file.getId())) {
			result.add(new CoChangeFile(file, cochange));
		}
		result.sort((f1, f2) -> {
			return f2.getTimes() - f1.getTimes();
		});
		return result;
	}

	@Override
	public Collection<CommitUpdateFile> queryCommitUpdateFiles(Commit commit) {
		String key = "queryCommitUpdateFiles_" + commit.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<CommitUpdateFile> result = commitUpdateFileRepository.findCommitUpdatedFiles(commit.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<String, List<ProjectFile>> queryCommitUpdateFilesGroupByUpdateType(Commit commit) {
		String key = "queryCommitUpdateFilesGroupByUpdateType_" + commit.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Collection<CommitUpdateFile> allUpdates = queryCommitUpdateFiles(commit);
		Map<String, List<ProjectFile>> result = new HashMap<>();
		for(CommitUpdateFile update : allUpdates) {
			String type = update.getUpdateType();
			List<ProjectFile> files = result.getOrDefault(type, new ArrayList<>());
			files.add(update.getFile());
			result.put(type, files);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public GitRepository findGitRepositoryById(long gitRepoId){
		return gitRepoRepository.findById(gitRepoId).get();
	}
    
}
