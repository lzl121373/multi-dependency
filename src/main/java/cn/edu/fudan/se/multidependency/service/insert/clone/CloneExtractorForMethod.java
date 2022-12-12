package cn.edu.fudan.se.multidependency.service.insert.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneType;
import cn.edu.fudan.se.multidependency.utils.clone.CloneUtil;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.FilePathFromCsv;

public class CloneExtractorForMethod extends CloneExtractor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneExtractorForMethod.class);
	private String methodNameTablePath;
	private String methodResultPath;
//	private String groupPath;
	private Language language;

	//括号前后各加2行，避免由于括号原因的导致方法识别问题
	public static final int SNIPPET_ADJUSTMENT_LINES = 4;

	private Map<Integer, FilePathFromCsv> methodPaths = new HashMap<>();
	private Collection<CloneResultFromCsv> cloneResults = new ArrayList<>();
//	private Collection<Group> groups = new ArrayList<>();
	private Map<Integer, CodeNode> cloneNodeIdToCodeNode = new HashMap<>();
	
	public CloneExtractorForMethod(String methodNameTablePath, String methodResultPath, String groupPath, Language language) {
		super();
		this.methodNameTablePath = methodNameTablePath;
		this.methodResultPath = methodResultPath;
//		this.groupPath = groupPath;
		this.language = language;
	}

	@Override
	protected void readMeasureIndex() throws Exception {
		methodPaths = CloneUtil.readCloneCsvForFilePath(methodNameTablePath);
	}

	@Override
	protected void readResult() throws Exception {
		cloneResults = CloneUtil.readCloneResultCsv(methodResultPath);
	}

//	@Override
//	protected void readGroup() throws Exception {
//		groups = CloneUtil.readGroupFile(groupPath);
//	}

	@Override
	protected void extractNodesAndRelations() throws Exception {
		LOGGER.info("从文件读入"+ language.toString() + "方法级克隆对数：" + cloneResults.size());
		List<Clone> clones = new ArrayList<>();
		int sizeOfClones = 0;
		for(CloneResultFromCsv cloneResult : cloneResults) {
			int start = cloneResult.getStart();
			int end = cloneResult.getEnd();
			double value = cloneResult.getValue();
			String type = cloneResult.getType();
			FilePathFromCsv filePath1 = methodPaths.get(start);
			if(filePath1 == null) {
				LOGGER.error("path1 is null");
				continue;
			}
			FilePathFromCsv filePath2 = methodPaths.get(end);
			if(filePath2 == null) {
				LOGGER.error("path2 is null");
				continue;
			}
			ProjectFile file1 = this.getNodes().findFileByPathRecursion(filePath1.getFilePath());
			ProjectFile file2 = this.getNodes().findFileByPathRecursion(filePath2.getFilePath());
			if(file1 == null) {
				LOGGER.warn("file1 is null " + filePath1.getLineId() + " " + filePath1.getFilePath());
				continue;
			}
			if(file2 == null) {
				LOGGER.warn("file2 is null " + filePath2.getLineId() + " " + filePath2.getFilePath());
				continue;
			}
			CodeNode node1 = this.cloneNodeIdToCodeNode.get(filePath1.getLineId());
			CodeNode node2 = this.cloneNodeIdToCodeNode.get(filePath2.getLineId());
			if(node1 == null) {
				node1 = this.getNodes().findNodeByEndLineInFile(file1, filePath1.getEndLine());
			}
			if(node2 == null) {
				node2 = this.getNodes().findNodeByEndLineInFile(file2, filePath2.getEndLine());
			}

			// 既不是方法也不是Type的，改为片段
			if(node1 == null) {
				LOGGER.warn("node1 is null "  + filePath1.getLineId() + " " + filePath1.getFilePath() + " " + filePath1.getStartLine() + " " + filePath1.getEndLine());

				CodeNode fatherNode1 = this.getNodes().findFatherNodeByLineInFile(file1, filePath1.getStartLine(),filePath1.getEndLine());
				//括号前后各加2行，避免由于括号原因的导致方法识别问题
				int snippetLines1 = filePath1.getEndLine() - filePath1.getStartLine() + 1 + SNIPPET_ADJUSTMENT_LINES;
				if (fatherNode1 != null && snippetLines1 >= fatherNode1.getLines()){
					node1 = fatherNode1;
				}else {
					Snippet snippet1 = new Snippet();
					snippet1.setEntityId(generateEntityId());
					snippet1.setStartLine(filePath1.getStartLine());
					snippet1.setEndLine(filePath1.getEndLine());
					snippet1.setIdentifier(String.join(",", file1.getPath(), String.valueOf(filePath1.getStartLine()), String.valueOf(filePath1.getEndLine())));
					snippet1.setName(String.join(",", file1.getName(), String.valueOf(filePath1.getStartLine()), String.valueOf(filePath1.getEndLine())));
					addNode(snippet1, null);
					if (fatherNode1 != null) {
						addRelation(new Contain(fatherNode1, snippet1));
					} else {
						addRelation(new Contain(file1, snippet1));
					}

					node1 = snippet1;
				}
			}

			if(node2 == null) {
				LOGGER.warn("node2 is null "  + filePath2.getLineId() + " " + filePath2.getFilePath() + " " + filePath2.getStartLine() + " " + filePath2.getEndLine());

				CodeNode fatherNode2 = this.getNodes().findFatherNodeByLineInFile(file2, filePath2.getStartLine(),filePath2.getEndLine());
				//括号前后各加2行，避免由于括号原因的导致方法识别问题
				int snippetLines2 = filePath2.getEndLine() - filePath2.getStartLine() + 1 + SNIPPET_ADJUSTMENT_LINES;
				if (fatherNode2 != null && snippetLines2 >= fatherNode2.getLines()){
					node2 = fatherNode2;
				}else {
					Snippet snippet2 = new Snippet();
					snippet2.setEntityId(generateEntityId());
					snippet2.setStartLine(filePath2.getStartLine());
					snippet2.setEndLine(filePath2.getEndLine());
					snippet2.setIdentifier(String.join(",", file2.getPath(), String.valueOf(filePath2.getStartLine()), String.valueOf(filePath2.getEndLine())));
					snippet2.setName(String.join(",", file2.getName(), String.valueOf(filePath2.getStartLine()), String.valueOf(filePath2.getEndLine())));
					addNode(snippet2, null);
					if (fatherNode2 != null) {
						addRelation(new Contain(fatherNode2, snippet2));
					} else {
						addRelation(new Contain(file2, snippet2));
					}

					node2 = snippet2;
				}
			}

			if(node1.getClass() != node2.getClass()) {
				LOGGER.warn("有克隆关系的两个节点不是同一个类型的节点" + filePath1.getLineId() + " " + filePath2.getLineId());
			}
			CloneRelationType cloneType = CloneRelationType.getCloneType(node1, node2);
			if(cloneType == null) {
				LOGGER.error("克隆类型为null：");
				continue;
			}
			cloneNodeIdToCodeNode.put(filePath1.getLineId(), node1);
			cloneNodeIdToCodeNode.put(filePath2.getLineId(), node2);

			if (value >= 0.7) {
				Clone clone = new Clone(node1, node2);
				clone.setNode1Index(start);
				clone.setNode2Index(end);
				clone.setNode1StartLine(filePath1.getStartLine());
				clone.setNode1EndLine(filePath1.getEndLine());
				clone.setNode2StartLine(filePath2.getStartLine());
				clone.setNode2EndLine(filePath2.getEndLine());
				clone.setValue(value);
				clone.setCloneRelationType(cloneType.toString());
				clone.setCloneType(CloneType.getCloneType(type).toString());
				clones.add(clone);
				addRelation(clone);
				sizeOfClones++;
			}
		}
		LOGGER.info("插入"+language.toString()+"方法级克隆数，对数：" + sizeOfClones);
//		addGroupFromGroupFile();
	}
	
//	private void addGroupFromGroupFile() {
//		long groupCount = 0;
//		for(Group group : this.groups) {
//			CloneGroup cloneGroup = new CloneGroup();
//			cloneGroup.setLanguage(language.toString());
//			cloneGroup.setEntityId(generateEntityId());
//			cloneGroup.setName(String.join("_", language.toString(), "method", "group", String.valueOf(groupCount++)));
//			cloneGroup.setSize(group.getGroupIds().size());
//			CloneLevel level = null;
//			boolean uniqueNodeType = true;
//			for(int id : group.getGroupIds()) {
//				CodeNode node = this.cloneNodeIdToCodeNode.get(id);
//				if(node == null) {
//					LOGGER.error("方法克隆组中找不到clone id为 " + id + " 的节点");
//					continue;
//				}
//				if(uniqueNodeType) {
//					CloneLevel temp = CloneLevel.getCodeNodeCloneLevel(node);
//					if(temp == null) {
//						LOGGER.error("节点类型错误，找不到克隆级别");
//						continue;
//					}
//					if(level == null) {
//						level = temp;
//					} else {
//						if(level != temp) {
//							uniqueNodeType = false;
//						}
//					}
//				}
//				addRelation(new Contain(cloneGroup, node));
//			}
//			if(level == null) {
//				cloneGroup.setCloneLevel(CloneLevel.multiple_level.toString());
//			} else {
//				cloneGroup.setCloneLevel(uniqueNodeType ? level.toString() : CloneLevel.multiple_level.toString());
//			}
//			addNode(cloneGroup, null);
//		}
//		cloneGroupNumber += groupCount;
//		LOGGER.info("插入"+language.toString()+"方法级克隆组，组数：" + (groupCount));
//	}
}
