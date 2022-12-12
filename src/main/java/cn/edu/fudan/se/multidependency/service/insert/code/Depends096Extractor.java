package cn.edu.fudan.se.multidependency.service.insert.code;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.exception.LanguageErrorException;
import cn.edu.fudan.se.multidependency.model.Language;
import depends.entity.Entity;
import depends.entity.repo.EntityRepo;
import depends.extractor.AbstractLangProcessor;
import multilang.depends.util.file.FileUtil;
import multilang.depends.util.file.FolderCollector;
import multilang.depends.util.file.TemporaryFile;
import edu.emory.mathcs.backport.java.util.Arrays;
import lombok.Setter;

public class Depends096Extractor implements DependsEntityRepoExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(Depends096Extractor.class);
	
	public Depends096Extractor() {}
	
	private EntityRepo executeCommand(Language language, String inputDir, 
			String[] includeDir, boolean autoInclude) throws Exception {
		LOGGER.info(projectPath + " " + inputDir);
		inputDir = FileUtil.uniqFilePath(inputDir);
		boolean supportImplLink = false;
		AbstractLangProcessor langProcessor = null;
		switch(language) {
		case cpp:
			supportImplLink = true;
			langProcessor = new depends.extractor.cpp.CppProcessor();
			break;
		case java:
			langProcessor = new depends.extractor.java.JavaProcessor();
			break;
		default:
			throw new LanguageErrorException(language.toString());
		}
		
		if (autoInclude) {
			FolderCollector includePathCollector = new FolderCollector();
			List<String> additionalIncludePaths = includePathCollector.getFolders(inputDir);
			additionalIncludePaths.addAll(Arrays.asList(includeDir));
			includeDir = additionalIncludePaths.toArray(new String[] {});
		}
		langProcessor.buildDependencies(inputDir, includeDir, new ArrayList<>(), supportImplLink, false, true, excludes);
		return langProcessor.getEntityRepo();
	}
	
	@Setter
	private Language language;
	@Setter
	private String projectPath;
	@Setter
	private List<String> excludes;
	@Setter
	private String[] includeDirs;
	@Setter
	private boolean autoInclude;

	private EntityRepo entityRepo ;
	
	private int countOfEntities = 0;

	@Override
	public int getEntityCount() {
		return countOfEntities;
	}
	
	private int calculateEntityCount(EntityRepo entityRepo) {
		if(entityRepo == null) {
			return 0;
		}
		List<Entity> entities = new ArrayList<>();
		Iterator<Entity> iterator = entityRepo.entityIterator();
		iterator.forEachRemaining(entity -> {
			entities.add(entity);
		});
		return entities.size();
	}

	@Override
	public EntityRepo extractEntityRepo() throws Exception {
		this.entityRepo = executeCommand(language, projectPath, new String[] {}, autoInclude);
		TemporaryFile.resetCurrentThread();
		this.countOfEntities = calculateEntityCount(this.entityRepo);
		LOGGER.info(projectPath + "(" + language.toString() + ") : " + countOfEntities);
		return entityRepo;
	}

}
