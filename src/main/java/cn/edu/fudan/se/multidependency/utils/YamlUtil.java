package cn.edu.fudan.se.multidependency.utils;

import java.io.File;
import java.util.Map;

import org.ho.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

public class YamlUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(YamlUtil.class);

	public static YamlObject getYaml(String[] args) throws Exception {
		YamlUtil.YamlObject yaml = null;
		if (args == null || args.length == 0) {
			yaml = YamlUtil.getDataBasePathDefault("src/main/resources/application.yml");
		} else {
			yaml = YamlUtil.getDataBasePath(args[0]);
		}
		return yaml;
	}

	public static YamlObject getDataBasePathDefault(String yamlPath) throws Exception {
		File file = new File(yamlPath);
		Map<?, ?> yaml = (Map<?, ?>) Yaml.load(file);
		String applicationUser = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("spring")).get("profiles")).get("active");
		StringBuilder userYamlPath = new StringBuilder();
		userYamlPath.append(yamlPath.substring(0, yamlPath.lastIndexOf(".yml")))
				.append("-").append(applicationUser).append(".yml");

		return getDataBasePath(userYamlPath.toString());
	}

	@SuppressWarnings("unchecked")
	public static YamlObject getDataBasePath(String yamlPath) throws Exception {
		LOGGER.info(yamlPath);
		File file = new File(yamlPath);
		YamlObject result = new YamlObject();
		Map<?, ?> yaml = (Map<?, ?>) Yaml.load(file);
		yaml = (Map<?, ?>) Yaml.load(file);
		String forTest = (String) ((Map<?, ?>) yaml.get("data")).get("test");
		result.setForTest(forTest);
		String neo4jDataPath = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("data_path");
		result.setNeo4jDataPath(neo4jDataPath);
		String databaseName = (String) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("database_name");
		result.setNeo4jDatabaseName(databaseName);
		boolean delete = (boolean) ((Map<?, ?>) ((Map<?, ?>) yaml.get("data")).get("neo4j")).get("delete");
		result.setDeleteDatabase(delete);
		String projectsConfig = (String) ((Map<?, ?>) yaml.get("data")).get("project_config");
		result.setProjectsConfig(projectsConfig);
		String serializePath = (String) ((Map<?, ?>) yaml.get("data")).get("serialize_path");
		result.setSerializePath(serializePath);
		boolean anonymization = (boolean) ((Map<?, ?>) yaml.get("data")).get("anonymization");
		result.setAnonymization(anonymization);

		boolean analyseDynamic = (boolean) ((Map<?, ?>) yaml.get("data")).get("dynamic_analyse");
		boolean analyseGit = (boolean) ((Map<?, ?>) yaml.get("data")).get("git_analyse");
		boolean analyseLib = (boolean) ((Map<?, ?>) yaml.get("data")).get("lib_analyse");
		boolean analyseClone = (boolean) ((Map<?, ?>) yaml.get("data")).get("clone_analyse");
		int projectThreadsType = (int) ((Map<?, ?>) yaml.get("data")).get("project_analyse_threads_count");
		result.setProjectThreadsType(projectThreadsType);
		result.setAnalyseDynamic(analyseDynamic);
		result.setAnalyseGit(analyseGit);
		result.setAnalyseLib(analyseLib);
		result.setAnalyseClone(analyseClone);
		return result;
	}

	@Data
	public static class YamlObject {
		private String projectsConfig;

		private String forTest;

		private boolean deleteDatabase;
		private String neo4jDataPath;
		private String neo4jDatabaseName;

		private String serializePath;

		private boolean anonymization;

		private boolean analyseDynamic;

		private boolean analyseGit;

		private boolean analyseLib;

		private boolean analyseClone;

		private int projectThreadsType;
	}
}
