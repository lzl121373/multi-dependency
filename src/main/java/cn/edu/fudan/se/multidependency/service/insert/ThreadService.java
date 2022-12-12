package cn.edu.fudan.se.multidependency.service.insert;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.fudan.se.multidependency.service.insert.dynamic.FeatureAndTestCaseFromJSONFileForMicroserviceInserter;
import cn.edu.fudan.se.multidependency.service.insert.git.GitExtractor;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.insert.clone.CloneExtractorForMethod;
import cn.edu.fudan.se.multidependency.service.insert.clone.CloneInserterForFileWithLoc;
import cn.edu.fudan.se.multidependency.service.insert.code.Depends096Extractor;
import cn.edu.fudan.se.multidependency.service.insert.code.DependsCodeExtractorForNeo4jServiceImpl;
import cn.edu.fudan.se.multidependency.service.insert.code.DependsEntityRepoExtractor;
import cn.edu.fudan.se.multidependency.service.insert.code.RestfulAPIFileExtractor;
import cn.edu.fudan.se.multidependency.service.insert.code.RestfulAPIFileExtractorImpl;
import cn.edu.fudan.se.multidependency.service.insert.code.SwaggerJSON;
import cn.edu.fudan.se.multidependency.service.insert.dynamic.TraceStartExtractor;
import cn.edu.fudan.se.multidependency.service.insert.git.EvolutionExtractor;
import cn.edu.fudan.se.multidependency.service.insert.lib.LibraryInserter;
import cn.edu.fudan.se.multidependency.service.insert.structure.MicroServiceArchitectureInserter;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;
import cn.edu.fudan.se.multidependency.utils.config.CloneConfig;
import cn.edu.fudan.se.multidependency.utils.config.DynamicConfig;
import cn.edu.fudan.se.multidependency.utils.config.GitConfig;
import cn.edu.fudan.se.multidependency.utils.config.JSONConfigFile;
import cn.edu.fudan.se.multidependency.utils.config.LibConfig;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfigUtil;
import cn.edu.fudan.se.multidependency.utils.config.RestfulAPIConfig;

public class ThreadService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class);
	
	private static final int EXECUTOR_FOR_GIR_REPOSITORY_COUNT = 5;
	
	public static final int OTHERS_ANALYSE_COUNT = 5;
	
	private YamlUtil.YamlObject yaml;
	private JSONConfigFile config;
	private CountDownLatch latchOfOthers;

	public ThreadService(YamlUtil.YamlObject yaml) throws Exception {
		this.yaml = yaml;
		this.config = ProjectConfigUtil.extract(JSONUtil.extractJSONObject(new File(yaml.getProjectsConfig())));
	}
	public ThreadService() {
	}

	public void staticAnalyse() throws Exception {
		Collection<ProjectConfig> projectsConfig = config.getProjectsConfig();
		LOGGER.info("项目结构存储线程开始，项目数量：" + projectsConfig.size());
		ExecutorService executorForStructure = yaml.getProjectThreadsType() <= 0 ? Executors.newCachedThreadPool() 
				: Executors.newFixedThreadPool(yaml.getProjectThreadsType());
		try {
			CountDownLatch latchOfProjects = new CountDownLatch(projectsConfig.size());
			for (ProjectConfig projectConfig : projectsConfig) {
				executorForStructure.execute(() -> {
					try {
						staticAnalyseCore(projectConfig);
					} catch (Exception e) {
						LOGGER.error(projectConfig.getPath() + " " + e.getMessage());
						e.printStackTrace();
					} finally {
						synchronized (latchOfProjects) {
							LOGGER.info(new StringBuilder().append("解析项目 ")
									.append(FileUtil.extractFilePathName(projectConfig.getPath()))
									.append("(").append(projectConfig.getLanguage()).append(")")
									.append(" 结束，线程- 1：").append((latchOfProjects.getCount() - 1)).toString());
						}
						latchOfProjects.countDown();
					}
				});
			}
			// 待所有项目解析完成
			latchOfProjects.await();
		} finally {
			executorForStructure.shutdown();
		}
	}

	private void staticAnalyseCore(ProjectConfig projectConfig) throws Exception {
		LOGGER.info(FileUtil.extractFilePathName(projectConfig.getPath()) + " " + projectConfig.getLanguage());
		DependsEntityRepoExtractor extractor = new Depends096Extractor();
		extractor.setIncludeDirs(projectConfig.includeDirsArray());
		extractor.setExcludes(projectConfig.getExcludes());
		extractor.setLanguage(projectConfig.getLanguage());
		extractor.setProjectPath(projectConfig.getPath());
		extractor.setAutoInclude(projectConfig.isAutoInclude());
		DependsCodeExtractorForNeo4jServiceImpl inserter = InserterForNeo4jServiceFactory.getInstance()
				.createCodeInserterService(extractor.extractEntityRepo(), projectConfig);
		RestfulAPIConfig apiConfig = projectConfig.getApiConfig();
		if (apiConfig != null && RestfulAPIConfig.FRAMEWORK_SWAGGER.equals(projectConfig.getApiConfig().getFramework())) {
			SwaggerJSON swagger = new SwaggerJSON();
			swagger.setPath(apiConfig.getPath());
			swagger.setExcludeTags(apiConfig.getExcludeTags());
			RestfulAPIFileExtractor restfulAPIFileExtractorImpl = new RestfulAPIFileExtractorImpl(swagger);
			inserter.setRestfulAPIFileExtractor(restfulAPIFileExtractorImpl);
		}
		inserter.addNodesAndRelations();
	}
	
	public void othersAnalyse() throws Exception {
		latchOfOthers = new CountDownLatch(OTHERS_ANALYSE_COUNT);
		ExecutorService executorForOthers = Executors.newCachedThreadPool();
		try {
			executorForOthers.execute(this::msDependAnalyse);
			executorForOthers.execute(this::dynamicAnalyse);
			executorForOthers.execute(this::gitAnalyse);
			executorForOthers.execute(this::cloneAnalyse);
			executorForOthers.execute(this::libAnalyse);
			latchOfOthers.await();
		} finally {
			executorForOthers.shutdown();
		}
	}

	private void msDependAnalyse() {
		try {
			if (config.getMicroServiceDependencies() == null) {
				return ;
			}
			LOGGER.info("微服务依赖存储");
			new MicroServiceArchitectureInserter(config.getMicroServiceDependencies()).addNodesAndRelations();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("微服务结构依赖提取出错：" + e.getMessage());
		} finally {
			if(config.getMicroServiceDependencies() == null) {
				logEndOfOtherAnalyse("未执行微服务依赖存储");
			} else {
				logEndOfOtherAnalyse("微服务依赖存储线程结束");
			}
			latchOfOthers.countDown();
		}
	}

	private void dynamicAnalyse() {
		try {
			if(!yaml.isAnalyseDynamic()) {
				return ;
			}
			LOGGER.info("动态运行分析");
			DynamicConfig dynamicConfig = config.getDynamicsConfig();
			File[] dynamicLogs = InserterForNeo4jServiceFactory.analyseDynamicLogs(dynamicConfig);
			
			LOGGER.info("输出trace，只输出日志中记录的trace，不做数据库操作");
			new TraceStartExtractor(dynamicLogs).addNodesAndRelations();
			
			for (Language language : Language.values()) {
				InserterForNeo4jServiceFactory.insertDynamic(language, dynamicLogs).addNodesAndRelations();
			}
			
			LOGGER.info("引入特性与测试用例，对应到trace");
			new FeatureAndTestCaseFromJSONFileForMicroserviceInserter(dynamicConfig.getFeaturesPath()).addNodesAndRelations();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error: dynamicAnalyse " + e.getMessage());
		} finally {
			if(yaml.isAnalyseDynamic()) {
				logEndOfOtherAnalyse("动态分析线程结束");
			} else {
				logEndOfOtherAnalyse("未执行动态分析");
			}
			latchOfOthers.countDown();
		}
	}

	private void gitAnalyse() {
		try {
			if(!yaml.isAnalyseGit()) {
				return ;
			}
			LOGGER.info("Git库分析");
			ExecutorService executorForGit = Executors.newFixedThreadPool(EXECUTOR_FOR_GIR_REPOSITORY_COUNT);
			try {
				Collection<GitConfig> gitsConfig = config.getGitsConfig();
				CountDownLatch latchOfGits = new CountDownLatch((gitsConfig).size());
				for (GitConfig gitConfig : gitsConfig) {
					LOGGER.info(gitConfig.getPath());
					executorForGit.execute(() -> {
						try {
							LOGGER.info(gitConfig.getPath());
							new EvolutionExtractor(gitConfig).addNodesAndRelations();
						} catch (Exception e) {
							LOGGER.error(gitConfig.getPath() + " " + e.getMessage());
						} finally {
							latchOfGits.countDown();
						}
					});
				}
				latchOfGits.await();
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("Error: gitAnalyse " + e.getMessage());
			} finally {
				executorForGit.shutdown();
			}
		} finally {
			if(yaml.isAnalyseGit()) {
				logEndOfOtherAnalyse("git分析线程结束");
			} else {
				logEndOfOtherAnalyse("未执行git分析");
			}
			latchOfOthers.countDown();
		}
	}

	private void cloneAnalyse() {
		try {
			if(!yaml.isAnalyseClone()) {
				return ;
			}
			LOGGER.info("克隆依赖分析");
			for (CloneConfig cloneConfig : config.getClonesConfig()) {
				switch (cloneConfig.getGranularity()) {
				case function:
					new CloneExtractorForMethod(cloneConfig.getNamePath(), cloneConfig.getResultPath(), cloneConfig.getGroupPath(), cloneConfig.getLanguage()).addNodesAndRelations();
					break;
				case file:
//					new CloneInserterForFile(cloneConfig.getNamePath(), cloneConfig.getResultPath(), cloneConfig.getGroupPath(), cloneConfig.getLanguage()).addNodesAndRelations();
					new CloneInserterForFileWithLoc(cloneConfig.getNamePath(), cloneConfig.getResultPath(), cloneConfig.getLanguage()).addNodesAndRelations();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error: cloneAnalyse " + e.getMessage());
		} finally {
			if(yaml.isAnalyseClone()) {
				logEndOfOtherAnalyse("克隆分析线程结束");
			} else {
				logEndOfOtherAnalyse("未执行克隆分析");
			}
			latchOfOthers.countDown();
		}

	}

	private void libAnalyse() {
		try {
			if(!yaml.isAnalyseLib()) {
				return ;
			}
			LOGGER.info("三方依赖分析");
			for (LibConfig libConfig : config.getLibsConfig()) {
				switch(libConfig.getLanguage()) {
				case java:
					new LibraryInserter(libConfig.getPath()).addNodesAndRelations();
					break;
				case cpp:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error: libAnalyse " + e.getMessage());
		} finally {
			if(yaml.isAnalyseLib()) {
				logEndOfOtherAnalyse("lib分析线程结束");
			} else {
				logEndOfOtherAnalyse("未执行lib分析");
			}
			latchOfOthers.countDown();
		}
	}

	public Map<String, List<String>> fileChangeCommitsAnalyse(Set<String> paths, GitExtractor gitExtractor, String commitTimeSince) throws Exception {
		LOGGER.info("文件大小：" + paths.size());
		int threadPoolSize = paths.size() <= 50 ? paths.size() : 50;
		ExecutorService executorForStructure = Executors.newFixedThreadPool(threadPoolSize);
		Map<String, List<String>> file2CommitIds = new ConcurrentHashMap<>();
		try {
			CountDownLatch latchOPaths = new CountDownLatch(paths.size());
			for (String path : paths) {
				executorForStructure.execute(() -> {
					try {
						file2CommitIds.put(path, gitExtractor.getProjectFileChangeCommitIds(path, commitTimeSince));
					} catch (Exception e) {
						LOGGER.error(path + " " + e.getMessage());
						e.printStackTrace();
					} finally {
						latchOPaths.countDown();
					}
				});
			}
			// 待所有文件解析完成
			latchOPaths.await();
		} finally {
			executorForStructure.shutdown();
		}
		return  file2CommitIds;
	}
	
	private void logEndOfOtherAnalyse(String start) {
		synchronized (latchOfOthers) {
			LOGGER.info(String.join("，线程 - 1：", start, String.valueOf(latchOfOthers.getCount() - 1)));
		}
	}
}
