package cn.edu.fudan.se.multidependency.utils.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

public class ProjectConfigUtil {

	public static JSONConfigFile extract(JSONObject obj) throws Exception {
		JSONConfigFile result = new JSONConfigFile();

		JSONArray projectsConfigArray = obj.getJSONArray("projects");
		if(projectsConfigArray != null) {
			result.setProjectsConfig(extractProjectsConfig(projectsConfigArray));
		}

		JSONObject dependenciesObj = obj.getJSONObject("architectures");
		if(dependenciesObj != null) {
			JSONArray microServiceDependenciesArray = dependenciesObj.getJSONArray("microservices");
			if(microServiceDependenciesArray != null) {
				result.setMicroServiceDependencies(extractMicroServiceDependencies(microServiceDependenciesArray));
			}
		}

		JSONObject dynamicConfigObj = obj.getJSONObject("dynamics");
		if (dynamicConfigObj != null) {
			result.setDynamicsConfig(extractDynamicConfig(dynamicConfigObj));
		}

		JSONArray gitsConfigObj = obj.getJSONArray("gits");
		if (gitsConfigObj != null) {
			result.setGitsConfig(extractGitsConfig(gitsConfigObj));
		}

		JSONArray libsConfigObj = obj.getJSONArray("libs");
		if (libsConfigObj != null) {
			result.setLibsConfig(extractLibsConfig(libsConfigObj));
		}

		JSONArray clonesConfigObj = obj.getJSONArray("clones");
		if (clonesConfigObj != null) {
			result.setClonesConfig(extractClonesConfig(clonesConfigObj));
		}

		return result;
	}

	private static Collection<ProjectConfig> extractProjectsConfig(JSONArray array) throws Exception {
		List<ProjectConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			ProjectConfig config = extractProjectConfig(json);
			result.add(config);
		}
		return result;
	}

	private static ProjectConfig extractProjectConfig(JSONObject projectJson) throws Exception {
		ProjectConfig result = new ProjectConfig();
		Language language = Language.valueOf(projectJson.getString("language"));
		String projectPath = projectJson.getString("path");
//		String projectName = projectJson.getString("project");
		String projectName = FileUtil.extractFilePathName(projectPath);
		Boolean isMicroservice = projectJson.getBoolean("isMicroservice");
		isMicroservice = isMicroservice == null ? true : isMicroservice;
		String microserviceName = projectJson.getString("microserviceName");
		microserviceName = microserviceName == null ? projectName : microserviceName;
		String serviceGroupName = projectJson.getString("serviceGroupName");
		serviceGroupName = serviceGroupName == null ? projectName : serviceGroupName;
		Boolean autoInclude = projectJson.getBoolean("autoInclude");
		autoInclude = autoInclude == null ? true : autoInclude;
		JSONArray excludesArray = projectJson.getJSONArray("excludes");
		if(excludesArray != null) {
			for(int i = 0; i < excludesArray.size(); i++) {
				result.addExclude(excludesArray.getString(i));
			}
		}
		JSONArray includeDirsArray = projectJson.getJSONArray("includeDirs");
		if(includeDirsArray != null) {
			for(int i = 0; i < includeDirsArray.size(); i++) {
				result.addIncludeDir(includeDirsArray.getString(i));
			}
		}
		result.setMicroserviceName(microserviceName);
		result.setLanguage(language);
		result.setPath(projectPath);
		result.setProject(projectName);
		result.setServiceGroupName(serviceGroupName);
		result.setMicroService(isMicroservice);
		result.setAutoInclude(autoInclude);
		RestfulAPIConfig apiConfig = extractAPIConfig(projectJson.getJSONObject("restfulAPIs"));
		result.setApiConfig(apiConfig);
		return result;
	}

	private static RestfulAPIConfig extractAPIConfig(JSONObject json) {
		if(json == null) {
			return null;
		}
		RestfulAPIConfig config = new RestfulAPIConfig();
		String path = json.getString("path");
		String framework = json.getString("framework");
		config.setPath(path);
		config.setFramework(framework);
		JSONArray excludeTagsArray = json.getJSONArray("excludeTags");
		if(excludeTagsArray != null) {
			for(int i = 0; i < excludeTagsArray.size(); i++) {
				config.addExcludeTag(excludeTagsArray.getString(i));
			}
		}
		return config;
	}

	private static Collection<MicroServiceDependency> extractMicroServiceDependencies(JSONArray array) {
		List<MicroServiceDependency> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			MicroServiceDependency dependency = new MicroServiceDependency();
			JSONObject json = array.getJSONObject(i);
			String name = json.getString("microservice");
			dependency.setMicroService(name);
			JSONArray dependenciesArray = json.getJSONArray("dependencies");
			for(int j = 0; j < dependenciesArray.size(); j++) {
				String dependencyMicroService = dependenciesArray.getString(j);
				dependency.addDependency(dependencyMicroService);
			}
			result.add(dependency);
		}
		return result;
	}

	private static DynamicConfig extractDynamicConfig(JSONObject json) {
		DynamicConfig result = new DynamicConfig();
		JSONArray fileSuffixes = json.getJSONArray("file_suffixes");
		if(fileSuffixes != null) {
			for(int i = 0; i < fileSuffixes.size(); i++) {
				result.addFileSuffix(fileSuffixes.getString(i));
			}
		}
		result.setLogsDirectoryPath(json.getString("logs_path"));
		result.setFeaturesPath(json.getString("features_path"));
		return result;
	}

	private static Collection<GitConfig> extractGitsConfig(JSONArray array) {
		List<GitConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			GitConfig config = new GitConfig();
			config.setPath(json.getString("path"));
			config.setSpecifyCommitRange(json.getBooleanValue("specify_commit_range"));
			config.setSpecifyByCommitId(json.getBooleanValue("specify_by_commit_id"));
			config.setCommitIdFrom(json.getString("commit_id_from"));
			config.setCommitIdTo(json.getString("commit_id_to"));
			config.setCommitTimeSince(json.getString("commit_time_since"));
			config.setCommitTimeUntil(json.getString("commit_time_until"));
			config.setCurrentVersionCommitId(json.getString("commit_id_current"));
			config.setIssueFrom(json.getString("issueFrom"));
			JSONArray issueFilePathes = json.getJSONArray("issues_pathes");
			String issueDirectoryPath = json.getString("issues_directory");
			if(issueFilePathes != null) {
				for(int j = 0; i < issueFilePathes.size(); i++) {
					if(issueFilePathes.getString(j) != null) {
						config.addIssuePath(issueFilePathes.getString(j));
					}
				}
			}
			if (!StringUtils.isBlank(issueDirectoryPath)) {
				try {
					List<File> files = new ArrayList<>();
					FileUtil.listFiles(new File(issueDirectoryPath), files);
					for(File file : files) {
						config.addIssuePath(file.getAbsolutePath());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			JSONArray branches = json.getJSONArray("branches");
			if(branches != null) {
				for(int j = 0; j < branches.size(); j++) {
					config.addBranch(branches.getString(j));
				}
			}
			result.add(config);
		}
		return result;
	}

	private static Collection<LibConfig> extractLibsConfig(JSONArray array) {
		List<LibConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			LibConfig config = new LibConfig();
			config.setLanguage(Language.valueOf(json.getString("language")));
			config.setPath(json.getString("path"));
			result.add(config);
		}
		return result;
	}

	private static Collection<CloneConfig> extractClonesConfig(JSONArray array) {
		List<CloneConfig> result = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			CloneConfig config = new CloneConfig();
			config.setLanguage(Language.valueOf(json.getString("language")));
			config.setGranularity(Granularity.valueOf(json.getString("granularity")));
			config.setNamePath(json.getString("name_path"));
			config.setResultPath(json.getString("result_path"));
			config.setGroupPath(json.getString("group_path"));
			result.add(config);
		}
		return result;
	}

}
