package cn.edu.fudan.se.multidependency.service.insert;

import java.io.Closeable;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.batchinsert.BatchInserter;
import org.neo4j.batchinsert.BatchInserters;
import org.neo4j.configuration.Config;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.graphdb.ConstraintViolationException;
import org.neo4j.graphdb.Label;
import org.neo4j.io.layout.DatabaseLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

public class BatchInserterService implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchInserterService.class);
	
	private BatchInserterService() {}
	private static BatchInserterService instance = new BatchInserterService();
	public static BatchInserterService getInstance() {
		return instance;
	}
	
	private BatchInserter inserter = null;

    private Map<NodeLabelType, List<Label>> mapLabels = new HashMap<>();

	public void init(String dataPath, String databaseName, boolean initDatabase) throws Exception {
		String databasesPath = dataPath + "/" + GraphDatabaseSettings.DEFAULT_DATABASES_ROOT_DIR_NAME + "/" + databaseName;
		String transactionsPath = dataPath + "/" + GraphDatabaseSettings.DEFAULT_TX_LOGS_ROOT_DIR_NAME + "/" + databaseName;

		File databasesDir = new File(databasesPath);
		File transactionsDir = new File(transactionsPath);

		if(initDatabase) {
			FileUtil.delFile(databasesDir);
			FileUtil.delFile(transactionsDir);
		}

		Config config = Config.newBuilder()
				.set( GraphDatabaseSettings.data_directory, Paths.get(dataPath) )
				.set( GraphDatabaseSettings.default_database, databaseName )
				.set(BoltConnector.enabled, true)
				.build();

		DatabaseLayout databaseLayout = DatabaseLayout.of(config);
		inserter = BatchInserters.inserter(databaseLayout);

		for(NodeLabelType nodeType : NodeLabelType.values()) {
			List<Label> labels = new ArrayList<>();
			for(String labelStr : nodeType.labels()) {
				Label label = Label.label(labelStr);
				labels.add(label);
			}
			mapLabels.put(nodeType, labels);
		}
	}
	
	public void createIndexes() {
		if(inserter == null) {
			return ;
		}
		for(NodeLabelType nodeType : NodeLabelType.values()) {
			Label label = Label.label(nodeType.toString());
			for(String index : nodeType.indexes()) {
				try {
					inserter.createDeferredSchemaIndex(label).on(index).create();
				} catch (ConstraintViolationException e) {
					LOGGER.warn("索引已创建：" + index);
				} catch (Exception e) {
					LOGGER.warn("创建索引失败：" + index);
				}
			}
		}
	}
	
	public Long insertNode(Node node) {
		List<Label> labels = mapLabels.get(node.getNodeType());
		Label[] labelsArray = new Label[labels.size()];
		for(int i = 0; i < labels.size(); i++) {
			labelsArray[i] = labels.get(i);
		}
		node.setId(inserter.createNode(node.getProperties(), labelsArray));
		return node.getId();
	}
	
	public Long insertRelation(Relation relation) {
		try {
			relation.setId(inserter.createRelationship(relation.getStartNodeGraphId(), relation.getEndNodeGraphId(), relation.getRelationType(), relation.getProperties()));
		} catch (NullPointerException e) {
			LOGGER.error(relation.getStartNodeGraphId() + " " + relation.getRelationType() + " " + relation.getEndNodeGraphId());
			throw e;
		}
		return relation.getId();
	}

	/**
	 * 获取节点属性
	 * @param id
	 * @return
	 */
	public Map<String, Object> getNodeProperties(Long id) {
		return inserter.getNodeProperties(id);
	}
	
	/**
	 * 获取关系属性
	 * @param id
	 * @return
	 */
	public Map<String, Object> getRelationshipProperties(Long id) {
		return inserter.getRelationshipProperties(id);
	}
	
	/**
	 * 获取某个节点的所有关系的id
	 * @param nodeId
	 * @return
	 */
	public List<Long> getRelationshipIds(Long nodeId) {
		List<Long> result = new ArrayList<>();
		for(Long id : inserter.getRelationshipIds(nodeId)) {
			result.add(id);
		}
		return result;
		
	}
	
	@Override
	public void close() {
		if(inserter != null) {
			inserter.shutdown();
		}
	}
	
	public boolean nodeExists(Long id) {
		return id == null ? false : inserter.nodeExists(id);
	}
	
	public boolean relationExists(Long id) {
		return id == null ? false : (inserter.getRelationshipById(id) != null);
	}
	
	public void insertNodes(Nodes allNodes) {
		allNodes.getAllNodes().forEach((nodeType, nodes) -> {
			nodes.forEach(node -> {
				if(!nodeExists(node.getId())) {
					insertNode(node);
				}
			});
		});
	}
	
	public void insertRelations(Relations allRelations) {
		allRelations.getAllRelations().forEach((relationType, relations) -> {
			relations.forEach(relation -> {
				insertRelation(relation);
			});
		});
	}
	
}
