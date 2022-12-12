package cn.edu.fudan.se.multidependency.service.insert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.service.insert.build.BuildInserterForNeo4jService;
import cn.edu.fudan.se.multidependency.service.insert.code.CppExtractorServiceImpl;
import cn.edu.fudan.se.multidependency.service.insert.code.DependsCodeExtractorForNeo4jServiceImpl;
import cn.edu.fudan.se.multidependency.service.insert.code.JavaExtractorServiceImpl;
import cn.edu.fudan.se.multidependency.service.insert.dynamic.CppDynamicInserter;
import cn.edu.fudan.se.multidependency.service.insert.dynamic.FeatureAndTestCaseFromJSONFileForMicroserviceInserter;
import cn.edu.fudan.se.multidependency.service.insert.dynamic.JavassistDynamicInserter;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.config.DynamicConfig;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import depends.entity.repo.EntityRepo;

public class InserterForNeo4jServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(InserterForNeo4jServiceFactory.class);
    
	private static InserterForNeo4jServiceFactory instance = new InserterForNeo4jServiceFactory();
	
	private InserterForNeo4jServiceFactory() {}
	
	public static InserterForNeo4jServiceFactory getInstance() {
		return instance;
	}
	
	public DependsCodeExtractorForNeo4jServiceImpl createCodeInserterService(EntityRepo entityRepo, ProjectConfig config) throws Exception {
		switch(config.getLanguage()) {
		case java:
			return new JavaExtractorServiceImpl(entityRepo, config);
		case cpp:
			return new CppExtractorServiceImpl(entityRepo, config);
		}
		throw new Exception("程序语言不为java或c/c++，提取失败");
	}
	
	public BuildInserterForNeo4jService createBuildInserterService(Language language) {
		return new BuildInserterForNeo4jService();
	}/**
     * 通过feature的json文件引入
    *
    * @param featuresJsonPath
    * @throws Exception
    */
   public static ExtractorForNodesAndRelations insertFeatureAndTestCaseByJSONFile(String featuresJsonPath) throws Exception {
       return new FeatureAndTestCaseFromJSONFileForMicroserviceInserter(featuresJsonPath);
   }

   public static ExtractorForNodesAndRelations insertDynamic(Language language, File[] dynamicLogFiles) throws Exception {
       switch (language) {
           case java:
               return new JavassistDynamicInserter(dynamicLogFiles);
           case cpp:
               return new CppDynamicInserter(dynamicLogFiles);
       }
       throw new LanguageErrorException(language.toString());
   }

   public static File[] analyseDynamicLogs(DynamicConfig dynamicConfig) {
       File[] result;
       String[] dynamicFileSuffixes = new String[dynamicConfig.getFileSuffixes().size()];
       dynamicConfig.getFileSuffixes().toArray(dynamicFileSuffixes); // 后缀为.log
       LOGGER.info("分析动态运行文件的后缀为：" + dynamicConfig.getFileSuffixes());
       File dynamicDirectory = new File(dynamicConfig.getLogsDirectoryPath());
       LOGGER.info("分析动态运行文件的目录为：" + dynamicConfig.getLogsDirectoryPath());
       List<File> resultList = new ArrayList<>();
       FileUtil.listFiles(dynamicDirectory, resultList, dynamicFileSuffixes);
       result = new File[resultList.size()];
       resultList.toArray(result);
       return result;
   }
}
