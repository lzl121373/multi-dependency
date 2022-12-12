package cn.edu.fudan.se.multidependency.utils.clone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.CloneResultWithLocFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.FilePathFromCsv;
import cn.edu.fudan.se.multidependency.utils.clone.data.Group;
import cn.edu.fudan.se.multidependency.utils.clone.data.MethodIdentifierFromCsv;

public class CloneUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneUtil.class);

	public static Map<Integer, FilePathFromCsv> readCloneCsvForFilePath(String filePath) throws Exception {
		Map<Integer, FilePathFromCsv> result = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				if("".equals(line.trim())) {
					continue;
				}
				String[] values = line.split(",");
				if(values.length < 4) {
					LOGGER.warn("克隆数据格式不正确：" + line);
					continue;
				}
				FilePathFromCsv file = new FilePathFromCsv();
				file.setLine(line);
				file.setLineId(Integer.parseInt(values[0]));
				file.setFilePath(values[1]);
				file.setStartLine(Integer.parseInt(values[2]));
				file.setEndLine(Integer.parseInt(values[3]));
				result.put(file.getLineId(), file);
			}
		}
		return result;
	}
	
	public static Map<Integer, MethodIdentifierFromCsv> readMethodIdentifiersCsv(String filePath) throws Exception {
		Map<Integer, MethodIdentifierFromCsv> result = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				if("".equals(line.trim())) {
					continue;
				}
				String[] values = line.split(",");
				MethodIdentifierFromCsv method = new MethodIdentifierFromCsv();
				method.setLine(line);
				method.setLineId(Integer.parseInt(values[0]));
				method.setProjectName(values[1]);
				method.setStartLine(Integer.parseInt(values[2]));
				method.setEndLine(Integer.parseInt(values[3]));
				int functionSimpleNameIndex = 4;
				for(functionSimpleNameIndex = 4; functionSimpleNameIndex < values.length; functionSimpleNameIndex++) {
					String suffix = Constant.isEndWithCodeNodeIdentifierSuffix(values[functionSimpleNameIndex]);
					if(suffix != null) {
						if(Constant.CODE_NODE_IDENTIFIER_SUFFIX_FILE.equals(suffix)) {
							String path = values[functionSimpleNameIndex].substring(0, values[functionSimpleNameIndex].lastIndexOf("#"));
							path = path.replace("\\", "/");
							path = path.substring(path.indexOf("/" + method.getProjectName() + "/"));
							method.setFilePath(path);
							method.addIdentifier(path + Constant.CODE_NODE_IDENTIFIER_SUFFIX_FILE);
						} else if(Constant.CODE_NODE_IDENTIFIER_SUFFIX_FUNCTION.equals(suffix)) {
							method.setFunctionSimpleName(values[functionSimpleNameIndex].substring(0, values[functionSimpleNameIndex].lastIndexOf("#")));
						} else {
							method.addIdentifier(values[functionSimpleNameIndex]);
						}
					} else {
						break;
					}
				}
				int length = 0;
				for(int i = 0; i < functionSimpleNameIndex; i++) {
					length += values[i].length() + 1;
				}
				String parametersStr = line.substring(length);
				String[] parameters = parametersStr.split("#");
				for(String parameter : parameters) {
					if(StringUtils.isBlank(parameter) || "None".equals(parameter)) {
						continue;
					}
//					method.addParameterType(FunctionUtil.processParameter(parameter));
					method.addParameterType(parameter);
				}
				result.put(method.getLineId(), method);
			}
		}
		return result;
	}
	
	public static Collection<CloneResultFromCsv> readCloneResultCsv(String filePath) throws Exception {
		List<CloneResultFromCsv> result = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				if("".equals(line.trim())) {
					continue;
				}
				String[] values = line.split(",");
				if(values.length < 3) {
					LOGGER.warn("克隆数据格式不正确：" + line);
					continue;
				}
				int start = Integer.parseInt(values[0]);
				int end = Integer.parseInt(values[1]);
				double value = Double.parseDouble(values[2]);
//				if(value < 0.7) {
//					continue;
//				}
				String type = "";
				if(values.length > 3) {
					type = values[3];
				}
				result.add(new CloneResultFromCsv(start, end, value, type));
			}
		}
		return result;
	}

	public static Collection<CloneResultWithLocFromCsv> readCloneResultWithLocCsv(String filePath) throws Exception {
		Set<CloneResultWithLocFromCsv> result = new HashSet<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				if("".equals(line.trim())) {
					continue;
				}
				String[] values = line.split(",");
				if(values.length < 8) {
					LOGGER.warn("克隆数据格式不正确：" + line);
					continue;
				}
				int start = Integer.parseInt(values[0]);
				int end = Integer.parseInt(values[1]);
				double value = Double.parseDouble(values[2]);
				String type = values[3];
				int linesSize1 = Integer.parseInt(values[4]);
				int linesSize2 = Integer.parseInt(values[5]);
				int loc1 = Integer.parseInt(values[6]);
				int loc2 = Integer.parseInt(values[7]);
//				if(value < 0.7) {
//					continue;
//				}
				result.add(new CloneResultWithLocFromCsv(start, end, value, type, linesSize1, linesSize2, loc1, loc2));
			}
		}
		return result;
	}
	
	/**
	 * 计算连通图
	 * @param relations
	 * @return
	 */
	public static Collection<Collection<CodeNode>> groupCloneNodes(Collection<Clone> relations) {
		List<Collection<CodeNode>> result = new ArrayList<>();
		Map<Node, Collection<CodeNode>> nodeToCollection = new HashMap<>();
		for(Clone relation : relations) {
			CodeNode node1 = relation.getCodeNode1();
			CodeNode node2 = relation.getCodeNode2();
			Collection<CodeNode> collections1 = nodeToCollection.get(node1);
			Collection<CodeNode> collections2 = nodeToCollection.get(node2);
			if(collections1 == null && collections2 == null) {
				collections1 = new ArrayList<>();
				collections1.add(node1);
				collections1.add(node2);
				result.add(collections1);
				nodeToCollection.put(node1, collections1);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 != null && collections2 == null) {
				collections1.add(node2);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 == null && collections2 != null) {
				collections2.add(node1);
				nodeToCollection.put(node1, collections2);
			} else {
				if(collections1 != collections2) {
					collections1.addAll(collections2);
					result.remove(collections2);
					for(Node node : collections2) {
						nodeToCollection.put(node, collections1);
					}
				}
			}
		}
		result.sort((collection1, collection2) -> {
			return collection2.size() - collection1.size();
		});
		return result;
	}
	
	public static Collection<Group> readGroupFile(String filePath) throws Exception {
		List<Group> result = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				if("".equals(line.trim())) {
					continue;
				}
				Group group = new Group(line);
				String[] values = line.split(",");
				for(String value : values) {
					group.addId(Integer.parseInt(value));
				}
				result.add(group);
			}
		}
		return result;
	}
}
