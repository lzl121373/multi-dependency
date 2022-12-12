package cn.edu.fudan.se.multidependency.service.insert;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;
import lombok.Getter;
import lombok.Setter;

public final class RepositoryService implements InserterForNeo4j, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);

    private static final long serialVersionUID = 4968121323297165683L;

    protected transient BatchInserterService batchInserterService = BatchInserterService.getInstance();

    private static RepositoryService repository = new RepositoryService();

    @Setter
    private String dataPath;

    @Setter
    private String databaseName;

    @Setter
    private boolean delete;

    private RepositoryService() {
    }

    public static RepositoryService getInstance() {
        return repository;
    }

    @Getter
    @Setter
    private Nodes nodes = new Nodes();

    @Getter
    @Setter
    private Relations relations = new Relations();

    @Override
    public void insertToNeo4jDataBase() throws Exception {
        LOGGER.info("start to store datas to database");
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        LOGGER.info("总计节点数：" + nodes.size());
        LOGGER.info("总计关系数：" + relations.size());
        LOGGER.info("初始化数据库：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        batchInserterService.init(dataPath, databaseName, delete);
        LOGGER.info("初始化数据库结束：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        LOGGER.info("插入节点：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        batchInserterService.insertNodes(nodes);
        LOGGER.info("插入节点结束：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        LOGGER.info("插入关系：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        batchInserterService.insertRelations(relations);
        LOGGER.info("插入关系结束：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        LOGGER.info("创建索引：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        batchInserterService.createIndexes();
        LOGGER.info("创建索引结束：" + sdf.format(new Timestamp(System.currentTimeMillis())));
        LOGGER.info("正在关闭数据库... " + sdf.format(new Timestamp(System.currentTimeMillis())));
        closeBatchInserter();
        LOGGER.info("数据库插入操作结束，关闭数据库：" + sdf.format(new Timestamp(System.currentTimeMillis())));
    }

    @Override
    public boolean addRelation(Relation relation) {
        this.relations.addRelation(relation);
        return true;
    }

    private void closeBatchInserter() {
        if (this.batchInserterService != null) {
            this.batchInserterService.close();
        }
    }

    @Override
    public boolean addNode(Node node, Project inProject) {
        this.nodes.addNode(node, inProject);
        return true;
    }

    @Override
    public boolean existNode(Node node) {
        return this.nodes.existNode(node);
    }

    @Override
    public boolean existRelation(Relation relation) {
        return this.relations.existRelation(relation);
    }

}
