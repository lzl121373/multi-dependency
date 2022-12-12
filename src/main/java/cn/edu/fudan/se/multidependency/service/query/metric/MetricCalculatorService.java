package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.edu.fudan.se.multidependency.model.MetricType;
import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.relation.Has;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.GitRepoRepository;
import cn.edu.fudan.se.multidependency.repository.relation.HasRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.query.history.data.GitRepoMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

@Service
public class MetricCalculatorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetricCalculatorService.class);

	@Autowired
	private ProjectFileRepository fileRepository;
	
	@Autowired
	private ProjectRepository projectRepository;
	
	@Autowired
	private PackageRepository packageRepository;

	@Autowired
	private CommitRepository commitRepository;

	@Autowired
	private DeveloperSubmitCommitRepository developerSubmitCommitRepository;

	@Autowired
	private GitAnalyseService gitAnalyseService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private CacheService cache;

	@Autowired
	private HasRepository hasRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private CoChangeRepository coChangeRepository;
	
	@Resource(name="modularityCalculatorImplForFieldMethodLevel")
	private ModularityCalculator modularityCalculator;

	public void setBasicMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findFileMetricsWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在File Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllFileMetric();
		Map<Long, Metric> fileMetricNodesMap = generateFileMetricNodes();
		if(fileMetricNodesMap != null && !fileMetricNodesMap.isEmpty()){
			Collection<Metric> fileMetricNodes = fileMetricNodesMap.values();
			metricRepository.saveAll(fileMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Long, Metric> entry : fileMetricNodesMap.entrySet()){
				ProjectFile file = fileRepository.findFileById(entry.getKey());
				Has has = new Has(file, entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public void createFileMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findFileMetricsWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在File Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllFileMetric();

		LOGGER.info("计算Project/Package/ProjectFile基本度量值...");
		fileRepository.setFileMetrics();
		packageRepository.setEmptyPackageMetrics();
		packageRepository.setPackageMetrics();
		projectRepository.setProjectMetrics();

		Map<Long, Metric> fileMetricNodesMap = generateFileMetricNodes();
		if(fileMetricNodesMap != null && !fileMetricNodesMap.isEmpty()){
			Collection<Metric> fileMetricNodes = fileMetricNodesMap.values();
			metricRepository.saveAll(fileMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Long, Metric> entry : fileMetricNodesMap.entrySet()){
				ProjectFile file = fileRepository.findFileById(entry.getKey());
				Has has = new Has(file, entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public void createPackageMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findPackageMetricsWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在Package Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllPackageMetric();
		Map<Long, Metric> packageMetricNodesMap = generatePackageMetricNodes();
		if(packageMetricNodesMap != null && !packageMetricNodesMap.isEmpty()){
			Collection<Metric> pckMetricNodes = packageMetricNodesMap.values();
			metricRepository.saveAll(pckMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			int size = 0;
			for(Map.Entry<Long, Metric> entry : packageMetricNodesMap.entrySet()){
				Package pck = packageRepository.findPackageById(entry.getKey());
				Has has = new Has(pck, entry.getValue());
				hasMetrics.add(has);
				if(++size > 500){
					hasRepository.saveAll(hasMetrics);
					hasMetrics.clear();
					size = 0;
				}
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public void createProjectMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findProjectMetricWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在Project Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllProjectMetric();
		Map<Long, Metric> projectMetricNodesMap = generateProjectMetricNodes();
		if(projectMetricNodesMap != null && !projectMetricNodesMap.isEmpty()){
			Collection<Metric> projectMetricNodes = projectMetricNodesMap.values();
			metricRepository.saveAll(projectMetricNodes);

			Collection<Has> hasMetrics = new ArrayList<>();
			for(Map.Entry<Long, Metric> entry : projectMetricNodesMap.entrySet()){
				Project project = projectRepository.findProjectById(entry.getKey());
				Has has = new Has(project, entry.getValue());
				hasMetrics.add(has);
			}
			hasRepository.saveAll(hasMetrics);
		}
	}

	public void createGitRepoMetric(boolean isRecreate) {
		List<Metric> metricsTmp = metricRepository.findGitRepoMetricWithLimit();
		if(metricsTmp != null && !metricsTmp.isEmpty()){
			LOGGER.info("已存在GitRepo Metric度量值节点和关系");
			if(!isRecreate){
				LOGGER.info("不重新创建");
				return;
			}
			LOGGER.info("重新创建...");
		}

		metricRepository.deleteAllGitRepoMetric();
		Map<Long, List<Metric>> gitRepoMetricNodesMap = generateGitRepoMetricNodes();
		if(gitRepoMetricNodesMap != null && !gitRepoMetricNodesMap.isEmpty()){
			Collection<Metric> gitRepoMetricNodes = new ArrayList<>();
			Collection<Has> hasMetrics = new ArrayList<>();
			for(Map.Entry<Long, List<Metric>> entry : gitRepoMetricNodesMap.entrySet()){
				List<Metric> metrics = entry.getValue();
				GitRepository gitRepository = gitAnalyseService.findGitRepositoryById(entry.getKey());
				if(metrics != null && !metrics.isEmpty()){
					gitRepoMetricNodes.addAll(metrics);
					metrics.forEach(metric -> {
						Has has = new Has(gitRepository, metric);
						hasMetrics.add(has);
					});
				}
			}
			metricRepository.saveAll(gitRepoMetricNodes);
			hasRepository.saveAll(hasMetrics);
		}
	}

	public Map<Long, Metric> generateFileMetricNodes(){
		Map<Long, Metric> result = new HashMap<>();
		Map<Long, FileMetric> fileFileMetricsMap = calculateFileMetrics();
		if(fileFileMetricsMap != null && !fileFileMetricsMap.isEmpty()){
			fileFileMetricsMap.forEach((fileId, fileMetrics) ->{
				ProjectFile file = fileRepository.findFileById(fileId);
				Metric metric = new Metric();
				metric.setEntityId((long) -1);
				metric.setLanguage(file.getLanguage());
				metric.setName(file.getName());
				metric.setNodeType(file.getNodeType());

				Map<String, Object> metricValues =  new HashMap<>();
				metricValues.put(MetricType.NOC, fileMetrics.getStructureMetric().getNoc());
				metricValues.put(MetricType.NOM, fileMetrics.getStructureMetric().getNom());
				metricValues.put(MetricType.LOC, fileMetrics.getStructureMetric().getLoc());
				metricValues.put(MetricType.FAN_OUT, fileMetrics.getStructureMetric().getFanOut());
				metricValues.put(MetricType.FAN_IN, fileMetrics.getStructureMetric().getFanIn());
				metricValues.put(MetricType.INSTABILITY, fileMetrics.getInstability());
				metricValues.put(MetricType.PAGE_RANK_SCORE, fileMetrics.getPageRankScore());
				FileMetric.EvolutionMetric evolutionMetric = fileMetrics.getEvolutionMetric();
				if (evolutionMetric != null){
					metricValues.put(MetricType.COMMITS, evolutionMetric.getCommits());
					metricValues.put(MetricType.DEVELOPERS, evolutionMetric.getDevelopers());
					metricValues.put(MetricType.CO_CHANGE_FILES, evolutionMetric.getCoChangeFiles());
					metricValues.put(MetricType.ADD_LINES, evolutionMetric.getAddLines());
					metricValues.put(MetricType.SUB_LINES, evolutionMetric.getSubLines());
				}

				FileMetric.DebtMetric detMetric = fileMetrics.getDebtMetric();
				if(detMetric != null){
					metricValues.put(MetricType.ISSUES, detMetric.getIssues());
					metricValues.put(MetricType.BUG_ISSUES, detMetric.getBugIssues());
					metricValues.put(MetricType.NEW_FEATURE_ISSUES, detMetric.getNewFeatureIssues());
					metricValues.put(MetricType.IMPROVEMENT_ISSUES, detMetric.getImprovementIssues());
					metricValues.put(MetricType.ISSUE_COMMITS, detMetric.getIssueCommits());
					metricValues.put(MetricType.ISSUE_ADD_LINES, detMetric.getIssueAddLines());
					metricValues.put(MetricType.ISSUE_SUB_LINES, detMetric.getIssueSubLines());
				}

				FileMetric.DeveloperMetric developerMetric = fileMetrics.getDeveloperMetric();
				if(developerMetric != null){
					metricValues.put(MetricType.CREATOR, developerMetric.getCreator());
					metricValues.put(MetricType.LAST_UPDATOR, developerMetric.getLastUpdator());
					metricValues.put(MetricType.MOST_UPDATOR, developerMetric.getMostUpdator());
				}
				metric.setMetricValues(metricValues);
				result.put(file.getId(), metric);
			});
		}

		return result;
	}

	public Map<Long, Metric> generatePackageMetricNodes(){
		Map<Long, Metric> result = new HashMap<>();
		Map<Long, PackageMetric> packageMetricsMap = calculatePackageMetrics();
		if(packageMetricsMap != null && !packageMetricsMap.isEmpty()){
			packageMetricsMap.forEach((pckId, packageMetrics) ->{
				Package pck = packageRepository.findPackageById(pckId);
				Metric metric = new Metric();
				metric.setEntityId((long) -1);
				metric.setLanguage(pck.getLanguage());
				metric.setName(pck.getName());
				metric.setNodeType(pck.getNodeType());

				Map<String, Object> metricValues =  new HashMap<>();
				metricValues.put(MetricType.NOF, packageMetrics.getNof());
				metricValues.put(MetricType.NOC, packageMetrics.getNoc());
				metricValues.put(MetricType.NOM, packageMetrics.getNom());
				metricValues.put(MetricType.LOC, packageMetrics.getLoc());
				metricValues.put(MetricType.FAN_OUT, packageMetrics.getFanOut());
				metricValues.put(MetricType.FAN_IN, packageMetrics.getFanIn());
				metricValues.put(MetricType.LINES, packageMetrics.getLines());
				int fanInOut = packageMetrics.getFanOut() + packageMetrics.getFanIn();
				double instability  = (fanInOut > 0 ? (double)(packageMetrics.getFanOut())/fanInOut : 0.0);
				metricValues.put(MetricType.INSTABILITY, instability);
				metric.setMetricValues(metricValues);

				result.put(pck.getId(), metric);
			});
		}

		return result;
	}

	public Map<Long, Metric> generateProjectMetricNodes(){
		Map<Long, Metric> result = new HashMap<>();
		Map<Long, ProjectMetric> projectMetricsMap = calculateProjectMetrics();
		if(projectMetricsMap != null && !projectMetricsMap.isEmpty()){
			projectMetricsMap.forEach((pck, projectMetrics) ->{
				Metric metric = new Metric();
				metric.setEntityId((long) -1);
				metric.setLanguage(projectMetrics.getProject().getLanguage());
				metric.setName(projectMetrics.getProject().getName());
				metric.setNodeType(projectMetrics.getProject().getNodeType());
				Map<String, Object> metricValues =  new HashMap<>();
				metricValues.put(MetricType.NOP, projectMetrics.getNop());
				metricValues.put(MetricType.NOF, projectMetrics.getNof());
				metricValues.put(MetricType.NOC, projectMetrics.getNoc());
				metricValues.put(MetricType.NOM, projectMetrics.getNom());
				metricValues.put(MetricType.LOC, projectMetrics.getLoc());
				metricValues.put(MetricType.LINES, projectMetrics.getLines());
				metricValues.put(MetricType.COMMITS, projectMetrics.getCommits());
				metricValues.put(MetricType.DEVELOPERS, projectMetrics.getDevelopers());
				metricValues.put(MetricType.ISSUES, projectMetrics.getIssues());
				metricValues.put(MetricType.ISSUE_COMMITS, projectMetrics.getIssueCommits());
				metricValues.put(MetricType.CHANGE_LINES, projectMetrics.getChangeLines());
				metricValues.put(MetricType.ISSUE_CHANGE_LINES, projectMetrics.getIssueChangeLines());
				metricValues.put(MetricType.MODULARITY, projectMetrics.getModularity());
				metricValues.put(MetricType.MED_FILE_FAN_IN, projectMetrics.getMedFileFanIn());
				metricValues.put(MetricType.MED_FILE_FAN_OUT, projectMetrics.getMedFileFanOut());
				metricValues.put(MetricType.MED_PACKAGE_FAN_IN, projectMetrics.getMedPackageFanIn());
				metricValues.put(MetricType.MED_PACKAGE_FAN_OUT, projectMetrics.getMedPackageFanOut());
				metricValues.put(MetricType.MED_FILE_CO_CHANGE, projectMetrics.getMedFileCoChange());
				metricValues.put(MetricType.MED_PACKAGE_CO_CHANGE, projectMetrics.getMedPackageCoChange());
				metric.setMetricValues(metricValues);
				result.put(projectMetrics.getProject().getId(), metric);
			});
		}

		return result;
	}

	public Map<Long, List<Metric>> generateGitRepoMetricNodes(){
		Map<Long, List<Metric>> result = new HashMap<>();
		Map<Long, GitRepoMetric> gitRepoMetricMap = gitAnalyseService.calculateGitRepoMetrics();
		if(gitRepoMetricMap != null && !gitRepoMetricMap.isEmpty()){
			for(Map.Entry<Long, GitRepoMetric> entry : gitRepoMetricMap.entrySet()){
				GitRepoMetric gitRepoMetric = entry.getValue();
				Collection<ProjectMetric> projectMetricsList = gitRepoMetric.getProjectMetricsList();
				List<Metric> gitRepoPjMetric = new ArrayList<>();
				if(projectMetricsList != null && !projectMetricsList.isEmpty()){
					projectMetricsList.forEach(pjMetric ->{
						Metric metric = new Metric();
						metric.setEntityId((long) -1);
						metric.setLanguage(pjMetric.getProject().getLanguage());
						metric.setName(pjMetric.getProject().getName());
						metric.setNodeType(gitRepoMetric.getGitRepository().getNodeType());
						Map<String, Object> metricValues =  new HashMap<>();
						metricValues.put(MetricType.NOP, pjMetric.getNop());
						metricValues.put(MetricType.NOF, pjMetric.getNof());
						metricValues.put(MetricType.NOC, pjMetric.getNoc());
						metricValues.put(MetricType.NOM, pjMetric.getNom());
						metricValues.put(MetricType.LOC, pjMetric.getLoc());
						metricValues.put(MetricType.LINES, pjMetric.getLines());
						metricValues.put(MetricType.COMMITS, gitRepoMetric.getCommits());
						metricValues.put(MetricType.ISSUES, gitRepoMetric.getIssues());
						metricValues.put(MetricType.DEVELOPERS, gitRepoMetric.getDevelopers());
						metric.setMetricValues(metricValues);
						gitRepoPjMetric.add(metric);
					});
					result.put(gitRepoMetric.getGitRepository().getId(),gitRepoPjMetric);
				}
			}
		}
		return result;
	}

	public Map<Long, FileMetric> calculateFileMetrics() {
		String key = "calculateFileMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, FileMetric> result = new HashMap<>();
		List<FileMetric.StructureMetric> fileStructureMetricsList = fileRepository.calculateFileStructureMetrics();
		if(fileStructureMetricsList != null && !fileStructureMetricsList.isEmpty()){
			fileStructureMetricsList.forEach(structureMetric -> {
				FileMetric fileMetrics = new FileMetric();

				ProjectFile file = structureMetric.getFile();
				fileMetrics.setFile(file);
				FileMetric.EvolutionMetric fileEvolutionMetrics = fileRepository.calculateFileEvolutionMetrics(file.getId());
				FileMetric.DebtMetric fileDebtMetrics = fileRepository.calculateFileDebtMetrics(file.getId());
				FileMetric.DeveloperMetric developerMetric = calculateFileDeveloperMetrics(file);

				fileMetrics.setStructureMetric(structureMetric);
				fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
				fileMetrics.setDebtMetric(fileDebtMetrics);
				fileMetrics.setDeveloperMetric(developerMetric);

				fileMetrics.setFanIn(structureMetric.getFanIn());
				fileMetrics.setFanOut(structureMetric.getFanOut());
				//计算不稳定度
				double instability = (structureMetric.getFanIn() + structureMetric.getFanOut()) == 0 ? -1 : (structureMetric.getFanOut() + 0.0) / (structureMetric.getFanIn() + structureMetric.getFanOut());
				fileMetrics.setInstability(instability);
				fileMetrics.setPageRankScore(file.getScore());
				result.put(file.getId(), fileMetrics);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}
	
	public Map<Long, List<FileMetric>> calculateProjectFileMetrics() {
		String key = "calculateProjectFileMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		Map<Long, List<FileMetric>> result = new HashMap<>();
		Map<Long, FileMetric> fileMetricsCache = new HashMap<>(calculateFileMetrics());
		if(!fileMetricsCache.isEmpty()){
			fileMetricsCache.forEach((fileId, fileMetrics)->{
				ProjectFile file = fileRepository.findFileById(fileId);
				Project project = containRelationService.findFileBelongToProject(file);
				List<FileMetric> temp = result.getOrDefault(project.getId(), new ArrayList<>());
				temp.add(fileMetrics);
				result.put(project.getId(), temp);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}
	
	public Collection<FileMetric> calculateFileMetrics(Project project) {
		return calculateProjectFileMetrics().get(project.getId());
	}

	public Map<Long, PackageMetric> calculatePackageMetrics() {
		String key = "calculatePackageMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, PackageMetric> result = new HashMap<>();
		for(PackageMetric pckMetrics : packageRepository.calculatePackageMetrics()) {
			Package pck = pckMetrics.getPck();
			result.put(pck.getId(), pckMetrics);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	public Map<Long, List<PackageMetric>> calculateProjectPackageMetrics() {
		String key = "calculateProjectPackageMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, List<PackageMetric>> result = new HashMap<>();
		Map<Long, PackageMetric> packageMetricsCache = new HashMap<>(calculatePackageMetrics());
		packageMetricsCache.forEach((pckId, packageMetrics)->{
			Package pck = packageRepository.findPackageById(pckId);
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<PackageMetric> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			temp.add(packageMetrics);
			result.put(project.getId(), temp);
		});
		cache.cache(getClass(), key, result);
		return result;
	}
	
	public Collection<PackageMetric> calculateProjectPackageMetrics(Project project) {
		return calculateProjectPackageMetrics().get(project.getId());
	}

	public Collection<ProjectFile> calculateFanIn(ProjectFile file) {
		return fileRepository.calculateFanIn(file.getId());
	}
	
	public Collection<ProjectFile> calculateFanOut(ProjectFile file) {
		return fileRepository.calculateFanOut(file.getId());
	}

	public FileMetric calculateFileMetric(ProjectFile file) {
		FileMetric fileMetrics = new FileMetric();
		FileMetric.StructureMetric fileStructureMetrics = fileRepository.calculateFileStructureMetrics(file.getId());
		FileMetric.EvolutionMetric fileEvolutionMetrics = fileRepository.calculateFileEvolutionMetrics(file.getId());
		FileMetric.DebtMetric fileDebtMetrics = fileRepository.calculateFileDebtMetrics(file.getId());
		FileMetric.DeveloperMetric fileDeveloperMetrics = calculateFileDeveloperMetrics(file);
		fileMetrics.setStructureMetric(fileStructureMetrics);
		fileMetrics.setEvolutionMetric(fileEvolutionMetrics);
		fileMetrics.setDebtMetric(fileDebtMetrics);
		fileMetrics.setDeveloperMetric(fileDeveloperMetrics);
		return fileMetrics;
	}

	public PackageMetric calculatePackageMetric(Package pck){
		return packageRepository.calculatePackageMetrics(pck.getId());
	}

	public Map<Long, ProjectMetric> calculateProjectMetrics() {
		String key = "calculateProjectMetrics";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<Long, ProjectMetric> result = new HashMap<>();
		List<ProjectMetric> projectMetricList = projectRepository.calculateProjectMetrics();
		if(projectMetricList != null && !projectMetricList.isEmpty()) {
			projectMetricList.forEach(projectMetric -> {
				Project project = projectMetric.getProject();
				projectMetric.setMedFileFanIn(calculateMedFileFanIn(project.getId()));
				projectMetric.setMedFileFanOut(calculateMedFileFanOut(project.getId()));
				projectMetric.setMedPackageFanIn(calculateMedPackageFanIn(project.getId()));
				projectMetric.setMedPackageFanOut(calculateMedPackageFanOut(project.getId()));
				projectMetric.setMedFileCoChange(calculateMedFileCoChange(project.getId()));
				projectMetric.setMedPackageCoChange(calculateMedPackageCoChange(project.getId()));
				Integer issueCommits = commitRepository.calculateProjectIssueCommitsByProjectId(project.getId());
				Integer commits = commitRepository.calculateProjectCommitsByProjectId(project.getId());
				Integer issueChangeLines = commitRepository.calculateProjectIssueChangeLinesByProjectId(project.getId());
				Integer changeLines = commitRepository.calculateProjectChangeLinesByProjectId(project.getId());
				if (issueCommits == null) {
					issueCommits = 0;
				}
				if (commits == null) {
					commits = 0;
				}
				if (issueChangeLines == null) {
					issueChangeLines = 0;
				}
				if (changeLines == null) {
					changeLines = 0;
				}
				projectMetric.setIssues(calculateProjectIssues(project));
				projectMetric.setDevelopers(calculateProjectDevelopers(project));
				projectMetric.setIssueCommits(issueCommits);
				projectMetric.setCommits(commits);
				projectMetric.setIssueChangeLines(issueChangeLines);
				projectMetric.setChangeLines(changeLines);
				result.put(project.getId(), projectMetric);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}

	public Map<String, List<ProjectMetric>> calculateProjectMetricsByGitRepository() {
		String key = "calculateProjectMetricsByGitRepository";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		Map<String, List<ProjectMetric>> result = new HashMap<>();
		List<ProjectMetric> projectMetricsList = projectRepository.calculateProjectMetrics();
		if(projectMetricsList != null && !projectMetricsList.isEmpty()) {
			projectMetricsList.forEach(projectMetrics -> {
				List<ProjectMetric> projectMetricsTmp = result.getOrDefault(projectMetrics.getProject().getName(), new ArrayList<>());
				projectMetricsTmp.add(projectMetrics);
				result.put(projectMetrics.getProject().getName() , projectMetricsTmp);
			});
			cache.cache(getClass(), key, result);
		}

		return result;
	}
	
	public double calculateProjectModularity(Project project) {
		return modularityCalculator.calculate(project).getValue();
	}
	
	public int calculateProjectCommits(Project project) {
		return gitAnalyseService.findCommitsInProject(project).size();
	}

	public int calculateProjectDevelopers(Project project) {
		return gitAnalyseService.findDeveloperInProject(project).size();
	}

	public int calculateProjectIssues(Project project) {
		GitRepository gitRepository = gitAnalyseService.findGitRepositoryByProject(project);
		return gitAnalyseService.findIssuesInGitRepository(gitRepository).size();
	}

	private FileMetric.DeveloperMetric calculateFileDeveloperMetrics(ProjectFile file){
		long fileId = file.getId();
		FileMetric.DeveloperMetric developerMetric = new FileMetric().new DeveloperMetric();
		List<Commit> commits = commitRepository.queryUpdatedByCommits(fileId);
		if(commits.size() > 0){
			Developer creator = developerSubmitCommitRepository.findDeveloperByCommitId(commits.get(commits.size() - 1).getId());
			Developer lastUpdator = developerSubmitCommitRepository.findDeveloperByCommitId(commits.get(0).getId());
			Developer mostUpdator = new Developer();
			Map<Developer, Integer> updateTimes= new HashMap<>();
			int mostUpdateTime = 0;
			for (Commit commit:
					commits) {
				Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
				updateTimes.put(developer, updateTimes.getOrDefault(developer, 0) + 1);
				if(updateTimes.get(developer) >= mostUpdateTime){
					mostUpdator = developer;
					mostUpdateTime = updateTimes.get(developer);
				}
			}
			developerMetric.setFile(file);
			developerMetric.setCreator(creator.getName());
			developerMetric.setMostUpdator(mostUpdator.getName());
			developerMetric.setLastUpdator(lastUpdator.getName());
		}
		return developerMetric;
	}

	private int calculateMedian(List<Integer> list) {
		int median = 0;
		int size = list.size();
		if (size > 0 && list.get(0) != null) {
			if (size % 2 == 0) {
				median = (list.get((size / 2) - 1) + list.get(size / 2)) / 2;
			}
			else {
				median = list.get(size / 2);
			}
		}
		return median;
	}

	private int calculateMedFileFanIn(long projectId) {
		List<Integer> fileFanInList = new ArrayList<>(fileRepository.findFileFanInByProjectId(projectId));
		return calculateMedian(fileFanInList);
	}

	private int calculateMedFileFanOut(long projectId) {
		List<Integer> fileFanOutList = new ArrayList<>(fileRepository.findFileFanOutByProjectId(projectId));
		return calculateMedian(fileFanOutList);
	}

	private int calculateMedPackageFanIn(long projectId) {
		List<Integer> packageFanInList = new ArrayList<>(packageRepository.findPackageFanInByProjectId(projectId));
		return calculateMedian(packageFanInList);
	}

	private int calculateMedPackageFanOut(long projectId) {
		List<Integer> packageFanOutList = new ArrayList<>(packageRepository.findPackageFanOutByProjectId(projectId));
		return calculateMedian(packageFanOutList);
	}

	private int calculateMedFileCoChange(long projectId) {
		List<Integer> fileCoChangeList = new ArrayList<>(coChangeRepository.findFileCoChangeByProjectId(projectId));
		return calculateMedian(fileCoChangeList);
	}

	private int calculateMedPackageCoChange(long projectId) {
		List<Integer> packageCoChangeList = new ArrayList<>(coChangeRepository.findPackageCoChangeByProjectId(projectId));
		return calculateMedian(packageCoChangeList);
	}
}
