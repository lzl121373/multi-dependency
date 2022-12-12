package cn.edu.fudan.se.multidependency.service.insert.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneType;
import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultWithLocFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.FilePathFromCsv;

public class CloneInserterForFileWithLoc extends CloneExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneExtractorForFile.class);
	private String namePath;
	private String resultPath;
//	private String groupPath;
	private Language language;
	
	public CloneInserterForFileWithLoc(String namePath, String resultPath, Language language) {
		super();
		this.namePath = namePath;
		this.resultPath = resultPath;
//		this.groupPath = groupPath;
		this.language = language;
	}
	
	private Map<Integer, FilePathFromCsv> filePaths = new HashMap<>();
	private Collection<? extends CloneResultFromCsv> cloneResults = new ArrayList<>();
//	private Collection<Group> groups = new ArrayList<>();
	private Map<Integer, ProjectFile> cloneFileIdToCodeNode = new HashMap<>();

	@Override
	protected void readMeasureIndex() throws Exception {
		filePaths = CloneUtil.readCloneCsvForFilePath(namePath);
		for(Map.Entry<Integer, FilePathFromCsv> entry : filePaths.entrySet()) {
			FilePathFromCsv filePath = entry.getValue();
			ProjectFile file = this.getNodes().findFileByPathRecursion(filePath.getFilePath());
			if(file == null) {
				LOGGER.warn("file is null " + filePath.getFilePath());
				continue;
			}
			file.setEndLine(filePath.getEndLine());
		}
	}

	@Override
	protected void readResult() throws Exception {
		cloneResults = CloneUtil.readCloneResultWithLocCsv(resultPath);
	}

//	@Override
//	protected void readGroup() throws Exception {
//		groups = CloneUtil.readGroupFile(groupPath);
//	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		LOGGER.info("从文件读入"+ language.toString() + "文件克隆对数：" + cloneResults.size());
		int sizeOfFileCloneFiles = 0;
		List<Clone> clones = new ArrayList<>();
		for(CloneResultFromCsv cloneResult : cloneResults) {
			int start = cloneResult.getStart();
			int end = cloneResult.getEnd();
			double value = cloneResult.getValue();
			String type = cloneResult.getType();
			FilePathFromCsv filePath1 = filePaths.get(start);
			if(filePath1 == null) {
				LOGGER.error("path1 is null");
				continue;
			}
			FilePathFromCsv filePath2 = filePaths.get(end);
			if(filePath2 == null) {
				LOGGER.error("path2 is null");
				continue;
			}
			ProjectFile file1 = this.cloneFileIdToCodeNode.get(filePath1.getLineId());
			if(file1 == null) {
				file1 = this.getNodes().findFileByPathRecursion(filePath1.getFilePath());
			}
			ProjectFile file2 = this.cloneFileIdToCodeNode.get(filePath2.getLineId());
			if(file2 == null) {
				file2 = this.getNodes().findFileByPathRecursion(filePath2.getFilePath());
			}
			if(file1 == null) {
				LOGGER.warn("file1 is null " + filePath1.getLineId() + " " + filePath1.getFilePath());
				continue;
			}
			cloneFileIdToCodeNode.put(filePath1.getLineId(), file1);
			if(file2 == null) {
				LOGGER.warn("file2 is null " + filePath2.getLineId() + " " + filePath2.getFilePath());
				continue;
			}
			cloneFileIdToCodeNode.put(filePath2.getLineId(), file2);
			if (value >= 0.7) {
				Clone clone = new Clone(file1, file2);
				clone.setNode1Index(start);
				clone.setNode2Index(end);
				clone.setNode1StartLine(filePath1.getStartLine());
				clone.setNode1EndLine(filePath1.getEndLine());
				clone.setNode2StartLine(filePath2.getStartLine());
				clone.setNode2EndLine(filePath2.getEndLine());
				clone.setValue(value);
				clone.setCloneRelationType(CloneRelationType.str_FILE_CLONE_FILE);
				clone.setCloneType(CloneType.getCloneType(type).toString());
				
				if(cloneResult instanceof CloneResultWithLocFromCsv) {
					clone.setLinesSize1(((CloneResultWithLocFromCsv) cloneResult).getLinesSize1());
					clone.setLinesSize2(((CloneResultWithLocFromCsv) cloneResult).getLinesSize2());
					clone.setLoc1(((CloneResultWithLocFromCsv) cloneResult).getLoc1());
					clone.setLoc2(((CloneResultWithLocFromCsv) cloneResult).getLoc2());
				}
				
				addRelation(clone);
				clones.add(clone);
				sizeOfFileCloneFiles++;
			}

		}
		LOGGER.info("插入"+language.toString()+"文件级克隆数，对数：" + sizeOfFileCloneFiles);
		//addGroupFromGroupFile();
	}
	
//	private void addGroupFromGroupFile() {
//		long groupCount = 0;
//		for(Group group : this.groups) {
//			CloneGroup cloneGroup = new CloneGroup();
//			cloneGroup.setLanguage(language.toString());
//			cloneGroup.setEntityId(generateEntityId());
//			cloneGroup.setName(String.join("_", language.toString(), "file", "group", String.valueOf(groupCount++)));
//			cloneGroup.setSize(group.getGroupIds().size());
//			cloneGroup.setCloneLevel(CloneLevel.file.toString());
//			addNode(cloneGroup, null);
//			for(int id : group.getGroupIds()) {
//				CodeNode node = this.cloneFileIdToCodeNode.get(id);
//				if(node == null) {
//					FilePathFromCsv filePath = filePaths.get(id);
//					if(filePath == null) {
//						LOGGER.error("文件克隆组中找不到clone id为 " + id + " 的节点");
//						continue;
//					} else {
//						ProjectFile file = this.getNodes().findFileByPathRecursion(filePath.getFilePath());
//						if(file == null) {
//							LOGGER.error("库中找不到clone id为 " + id + " 的文件节点");
//							continue;
//						} else {
//							this.cloneFileIdToCodeNode.put(id, file);
//							node = file;
//						}
//					}
//				}
//				addRelation(new Contain(cloneGroup, node));
//			}
//		}
//		cloneGroupNumber += groupCount;
//		LOGGER.info("插入"+language.toString()+"文件级克隆组，组数：" + (groupCount));
//	}

}
