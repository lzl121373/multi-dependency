package cn.edu.fudan.se.multidependency.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FileUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

	public static final String SLASH_WINDOWS = "\\";
	public static final String SLASH_LINUX = "/";
	/**
	 * 判断文件或目录是否在某个目录下
	 * 路径最后不加slash
	 * @param fileOrSubDirectoryPath
	 * @param parentDirectoryPath
	 * @param slash
	 * @return
	 */
	public static boolean isFileOrSubDirectoryOfDirectory(String fileOrSubDirectoryPath, String parentDirectoryPath, String slash) {
		if(!SLASH_LINUX.equals(slash) && !SLASH_WINDOWS.equals(slash)) {
			return false;
		}
		if(fileOrSubDirectoryPath.equals(parentDirectoryPath)) {
			return false;
		}
		if(fileOrSubDirectoryPath.indexOf(parentDirectoryPath + slash) != 0) {
			return false;
		}
		return !(fileOrSubDirectoryPath.substring(parentDirectoryPath.length() + 1)).contains(slash);
	}

	/**
	 * 判断文件或目录是否包含特殊目錄，比如 /src/test
	 * @param filePath
	 * @param key
	 * @return
	 */
	public static boolean isFilePathContainExcludeKey(String filePath, String key) {
		if(filePath == null)
			return false;

		try {
			String newFilePath = filePath.replace(SLASH_WINDOWS, SLASH_LINUX);
			String newKey = key.replace(SLASH_WINDOWS, SLASH_LINUX);
			if(newFilePath.contains(newKey))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}

	/**
	 * D:\a\a.java -> D:/a/a.java -> /a.java
	 * /a/a/a.java -> /a/a.java
	 * @param filePath
	 * @return
	 */
	public static String extractNextPath(String filePath) {
		if(filePath == null) {
			return "";
		}
		try {
			String newFilePath = filePath.replace(SLASH_WINDOWS, SLASH_LINUX);
			if(!newFilePath.contains(SLASH_LINUX) || SLASH_LINUX.equals(newFilePath)) {
				return "";
			}
			newFilePath = newFilePath.substring(newFilePath.indexOf(SLASH_LINUX) + 1);
			if(!newFilePath.contains(SLASH_LINUX)) {
				return "";
			}
			newFilePath = newFilePath.substring(newFilePath.indexOf(SLASH_LINUX));
			return newFilePath;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * "D:\\multiple-dependency-project\\depends-update\\depends\\src\\main\\java\\depends\\format\\AbstractFormatDependencyDumper.java"
	 * /depends
	 * /depends/src/main/java/depends/format/AbstractFormatDependencyDumper.java
	 * @param fullPath
	 * @param projectPath
	 * @return
	 */
	public static String extractFilePath(String fullPath, String projectPath) {
		String filePath = fullPath;
		filePath = filePath.replace(SLASH_WINDOWS, SLASH_LINUX);
		filePath = filePath.substring(filePath.indexOf(projectPath + SLASH_LINUX));
		return filePath;
	}

	/**
	 * 输入：D:\\multiple-dependency-project\\depends-update\\depends\\src\\main\\java\\depends\\format\\AbstractFormatDependencyDumper.java
	 * 输出：src/main/java/depends/format/AbstractFormatDependencyDumper.java
	 * @param fileFullPath
	 * @param projectFullPath
	 * @return
	 */
	public static String extractRelativePath(String fileFullPath, String projectFullPath) {
		String filePath = fileFullPath;
		filePath = filePath.replace(SLASH_WINDOWS, SLASH_LINUX);
		String projectPath = projectFullPath;
		projectPath = projectPath.replace(SLASH_WINDOWS, SLASH_LINUX);
		if(projectPath.endsWith(SLASH_LINUX)){
			projectPath.substring(0,projectPath.length()-1);
		}
		filePath = filePath.substring(projectPath.length() + 1);
		return filePath;
	}

	public static void main(String[] args) {
//		String filePath = "D:\\multiple-dependency-project\\depends-update\\depends\\src\\main\\java\\depends\\format\\AbstractFormatDependencyDumper.java";
//		System.out.println(extractFilePath(filePath, "/depends"));
		String directoryPath = "/google__fdse__auto/";
		System.out.println(extractDirectoryFromFile(extractDirectoryFromFile(directoryPath)) + "/");
		/*String directoryPath = "D:\\multiple-dependency-project\\train-ticket";
		System.out.println("result: " + extractNextPath(directoryPath));
		directoryPath = "D:\\multiple-dependency-project\\doublelanguage";
		System.out.println("result: " + extractNextPath(directoryPath));
		directoryPath = "D:\\source\\source";
		System.out.println("result: " + extractNextPath(directoryPath));
		directoryPath = "D:\\source";
		System.out.println("result: " + extractNextPath(directoryPath));*/
		/*JSONObject array = readDirectoryToGenerateProjectJSONFileForDoubleLanguageProject(
				new File(directoryPath), 0, "java", true, "train-ticket");*/
		/*JSONObject array = readDirectoryToGenerateProjectJSONFile(new File(directoryPath), 1, "java", true, "source");
		try {
			writeToFileForProjectJSONFile("D:\\source.log", array);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		/*directoryPath = "D:\\";
		File rootDirectory = new File(directoryPath);
		List<File> result = new ArrayList<>();
		listDirectories(rootDirectory, 2, result);
		System.out.println(result);*/
//		System.out.println(isFileOrSubDirectoryOfDirectory("/test/test2", "/test", "/"));
	}
//	java -jar multi-dependency-0.0.1.jar D:\FudanSE\project\project.log 项目目录 1 java true 项目名称 true
	/**
	 * D:\testtesttest.log
	 * D:\multiple-dependency-project\train-ticket
	 * 1
	 * java
	 * true
	 * train-ticket
	 * true/false optional
	 * @param args
	 * @throws Exception
	 */
	public static void writeToFileForProjectJSONFile(String[] args) throws Exception {
		String outputPath = args[0];
		String projectDirectoryPath = args[1];
		LOGGER.info("projectDirectoryPath is " + projectDirectoryPath);
		int depth = Integer.parseInt(args[2]);
		String language = args[3];
		boolean isAllMicroService = Boolean.parseBoolean(args[4]);
		String microserviceGroupName = null;
		if(args.length >= 6) {
			microserviceGroupName = args[5];
		}
		boolean isSearchDoubleLanguageProject = false;
		if(args.length >= 7) {
			Boolean temp = Boolean.valueOf(args[6]);
			isSearchDoubleLanguageProject = temp == null ? false : temp;
		}
		File projectDirectory = new File(projectDirectoryPath);
		if(!projectDirectory.exists()) {
			LOGGER.error("projectDirectoryPath is null: " + projectDirectoryPath);
		} else {
			LOGGER.info("is ProjectDirectoryPath Directory true? " + projectDirectory.isDirectory());
		}
		JSONObject array = null;
		if(isSearchDoubleLanguageProject) {
			array = readDirectoryToGenerateProjectJSONFileForDoubleLanguageProject(
					projectDirectory, depth, language, isAllMicroService, microserviceGroupName);
		} else {
			array = readDirectoryToGenerateProjectJSONFile(
					projectDirectory, depth, language, isAllMicroService, microserviceGroupName);
		}
		try {
			writeToFileForProjectJSONFile(outputPath, array);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeToFileForProjectJSONFile(String outputPath, JSONObject content) throws Exception {
		try(PrintWriter writer = new PrintWriter(new File(outputPath))) {
			writer.println(content.toJSONString());
		}
	}

	public static JSONObject readDirectoryToGenerateProjectJSONFileForDoubleLanguageProject(
			File rootDirectory, int depth, String defaultLanguage,
			boolean isAllMicroservice, String serviceGroupName) {
		if(rootDirectory == null) {
			LOGGER.error("rootDirectory is null");
			return new JSONObject();
		}
		JSONObject result = new JSONObject();
		JSONArray projects = new JSONArray();
		List<File> projectDirectories = new ArrayList<>();
		FileUtil.listDirectories(rootDirectory, depth, projectDirectories);

		for(File projectDirectory : projectDirectories) {
			boolean isDoubleLanguage = false;
			File[] children = projectDirectory.listFiles();
			if(children == null) {
				continue;
			}
			for(File child : children) {
				if(child.isDirectory() && ("code".equals(child.getName()) || "codej".equals(child.getName()))) {
					isDoubleLanguage = true;
					break;
				}
				if(child.isDirectory() && ("supportc".equals(child.getName()) || "supportj".equals(child.getName()))) {
					isDoubleLanguage = true;
					break;
				}
			}
			if(isDoubleLanguage) {
				JSONObject projectJson = new JSONObject();
				projectJson.put("project", projectDirectory.getName());
				projectJson.put("path", projectDirectory.getAbsolutePath());
				projectJson.put("language", "java");
				projectJson.put("isMicroservice", isAllMicroservice);
				if(isAllMicroservice && serviceGroupName != null) {
					projectJson.put("serviceGroupName", serviceGroupName);
					projectJson.put("microserviceName", projectDirectory.getName());
				}
				projects.add(projectJson);

				projectJson = (JSONObject) projectJson.clone();
				projectJson.put("language", "cpp");
				projects.add(projectJson);
			} else {
				JSONObject projectJson = new JSONObject();
				projectJson.put("project", projectDirectory.getName());
				projectJson.put("path", projectDirectory.getAbsolutePath());
				projectJson.put("language", defaultLanguage == null ? "" : defaultLanguage);
				projectJson.put("isMicroservice", isAllMicroservice);
				if(isAllMicroservice && serviceGroupName != null) {
					projectJson.put("serviceGroupName", serviceGroupName);
					projectJson.put("microserviceName", projectDirectory.getName());
				}
				projectJson.put("includeDirs", new JSONArray());
				projectJson.put("autoInclude", true);
				projects.add(projectJson);
			}
		}
		result.put("projects", projects);
		result.put("architectures", new JSONObject());
		return result;
	}

	public static JSONObject readDirectoryToGenerateProjectJSONFile(
			File rootDirectory, int depth, String defaultLanguage,
			boolean isAllMicroservice, String serviceGroupName) {
		JSONObject result = new JSONObject();
		JSONArray projects = new JSONArray();
		List<File> projectDirectories = new ArrayList<>();
		FileUtil.listDirectories(rootDirectory, depth, projectDirectories);

		for(File projectDirectory : projectDirectories) {
			JSONObject projectJson = new JSONObject();

			projectJson.put("project", projectDirectory.getName());
			projectJson.put("path", projectDirectory.getAbsolutePath());
			projectJson.put("language", defaultLanguage == null ? "" : defaultLanguage);
			projectJson.put("isMicroservice", isAllMicroservice);
			if(isAllMicroservice && serviceGroupName != null) {
				projectJson.put("serviceGroupName", serviceGroupName);
				projectJson.put("microserviceName", projectDirectory.getName());
			}
			projectJson.put("includeDirs", new JSONArray());
			projectJson.put("autoInclude", true);
			projects.add(projectJson);
		}
		result.put("projects", projects);
		result.put("architectures", new JSONObject());
		return result;
	}

	/**
	 * 提取文件所在目录
	 * @param filePath
	 * @return
	 */
	public static String extractDirectoryFromFile(String filePath) {
//		LOGGER.info("extractDirectoryFromFile " + filePath);
		if(filePath.contains(SLASH_WINDOWS)) {
			return filePath.substring(0, filePath.lastIndexOf(SLASH_WINDOWS));
		} else if(filePath.contains(SLASH_LINUX)) {
			return filePath.substring(0, filePath.lastIndexOf(SLASH_LINUX));
		} else {
			return SLASH_LINUX;
		}
	}

	/**
	 * 提取路径最后一个名字
	 * @param filePath
	 * @return
	 */
	public static String extractFilePathName(String filePath) {
//		LOGGER.info("extractFileName " + filePath);
		String newPath = filePath;
		if(newPath.contains(SLASH_WINDOWS)) {
			if(newPath.endsWith(SLASH_WINDOWS)){
				newPath = newPath.substring(0, newPath.length()-1);
			}
			return newPath.substring(newPath.lastIndexOf(SLASH_WINDOWS) + 1);
		} else if(newPath.contains(SLASH_LINUX)) {
			if(newPath.endsWith(SLASH_LINUX)){
				newPath = newPath.substring(0, newPath.length()-1);
			}
			return newPath.substring(newPath.lastIndexOf(SLASH_LINUX) + 1);
		} else {
			return newPath;
		}
	}

	/**
	 * 提取文件名后缀
	 * @param filePath
	 * @return
	 */
	public static String extractSuffix(String filePath) {
		int lastIndex = filePath.lastIndexOf(".");
		return lastIndex >= 0 ? filePath.substring(lastIndex) : "";
	}

	/**
	 * 删除文件夹及其子文件夹的所有文件
	 * @param file
	 * @return
	 */
	public static boolean delFile(File file) {
		if (!file.exists()) {
			return false;
		}

		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if(files == null) {
				return true;
			}
			for (File f : files) {
				delFile(f);
			}
		}
		return file.delete();
	}

	/**
	 * 列出目录下所有文件，将结果保存到result中
	 * @param directory
	 * @param result
	 */
	public static void listFiles(File directory, List<File> result) {
		if(directory.isFile()) {
			result.add(directory);
			return;
		}
		File[] listFiles = directory.listFiles();
		if(listFiles == null) {
			return ;
		}
		for(File file : listFiles) {
			listFiles(file, result);
		}
	}

	public static void listDirectories(File rootDirectory, int depth, List<File> result) {
		if(rootDirectory == null || rootDirectory.isFile()) {
			return;
		}
		if(depth == 0 && rootDirectory.isDirectory()) {
			result.add(rootDirectory);
			return;
		}
		File[] listFiles = rootDirectory.listFiles();
		if(listFiles == null) {
			return ;
		}
		for(File file : listFiles) {
			listDirectories(file, depth - 1, result);
		}
	}

	/**
	 * 列出目录下所有指定后缀的文件，并将结果保存在result中
	 * @param directory
	 * @param result
	 * @param suffixes
	 */
	public static void listFiles(File directory, List<File> result, String... suffixes) {
		if(directory.isFile()) {
			for(String suffix : suffixes) {
				if(suffix.equals(extractSuffix(directory.getPath()))) {
					result.add(directory);
				}
			}
			return;
		}
		File[] listFiles = directory.listFiles();
		if(listFiles == null) {
			return ;
		}
		for(File file : listFiles) {
			listFiles(file, result, suffixes);
		}
	}

	/**
	 * 判断文件后缀是否属于指定后缀，若不是，则应该过滤掉
	 *
	 * @param filePath
	 * @param suffixes
	 * @return
	 */
	public static boolean isFiltered(String filePath, String[] suffixes) {
		for (String suffix : suffixes) {
			if (filePath.endsWith(suffix)) {
				return false;
			}
		}
		return true;
	}

	public static void writeObject(String filePath, Object obj) throws IOException {
		long startTimeOfSerialize = System.currentTimeMillis();
		File file = new File(filePath);
		if (!file.exists()) {
			file.createNewFile();
		}
		try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			oos.writeObject(obj);
			long endTimeOfSerialize = System.currentTimeMillis();
			LOGGER.info("序列化所花时间：" + (float) ((endTimeOfSerialize - startTimeOfSerialize) / 1000.00) + " s,  or "
					+ (float) ((endTimeOfSerialize - startTimeOfSerialize) / 60000.00) + " min.");
		}
	}

	public static Object readObject(String filePath) throws IOException, ClassNotFoundException {
		long startTimeOfUnSerialize = System.currentTimeMillis();
		try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(filePath))))) {
			Object obj = ois.readObject();
			long endTimeOfUnSerialize = System.currentTimeMillis();
			LOGGER.info("反序列化所花时间：" + (float) ((endTimeOfUnSerialize - startTimeOfUnSerialize) / 1000.00) + " s,  or "
					+ (float) ((endTimeOfUnSerialize - startTimeOfUnSerialize) / 60000.00) + " min.");
			return obj;
		}
	}
	
	public static void exportToFile(String filePath, String str) {
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
				bw.write(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String extractPackagePath(String path, boolean isTopLevel){
		String[] strList = path.split("/");
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("/");

		if(!isTopLevel){
			for(int i = strList.length - 2; i <= strList.length - 1; i++){
				stringBuilder.append(strList[i]).append("/");
			}
		}else{
			for(int i = 1; i <= 2; i++){
				stringBuilder.append(strList[i]).append("/");
			}
		}

		return stringBuilder.toString();
	}
}
