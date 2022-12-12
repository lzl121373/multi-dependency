package cn.edu.fudan.se.multidependency;

import cn.edu.fudan.se.multidependency.service.insert.AnonymizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.service.insert.InserterForNeo4j;
import cn.edu.fudan.se.multidependency.service.insert.RepositoryService;
import cn.edu.fudan.se.multidependency.service.insert.ThreadService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;

public class InsertOtherData {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertOtherData.class);

    public static void main(String[] args) {
        LOGGER.info("InsertOtherData");
        insert(args);
        System.exit(0);
    }

    public static void insert(String[] args) {
        try {
            YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);

            ThreadService ts = new ThreadService(yaml);

            RepositoryService service = RepositoryService.getInstance();
            RepositoryService serializedService = (RepositoryService) FileUtil.readObject(yaml.getSerializePath());
            service.setNodes(serializedService.getNodes());
            service.setRelations(serializedService.getRelations());

            LOGGER.info("静态分析节点数：" + service.getNodes().size());
            LOGGER.info("静态分析关系数：" + service.getRelations().size());

            ts.othersAnalyse();

            if(yaml.isAnonymization()){
                AnonymizationService anonymizationService = AnonymizationService.getInstance();
                anonymizationService.anonymizeNodes();
            }

            InserterForNeo4j repository = RepositoryService.getInstance();
            repository.setDataPath(yaml.getNeo4jDataPath());
            repository.setDatabaseName(yaml.getNeo4jDatabaseName());
            repository.setDelete(yaml.isDeleteDatabase());
            repository.insertToNeo4jDataBase();
        } catch (Exception e) {
            // 所有步骤中有一个出错，都会终止执行
            e.printStackTrace();
            LOGGER.error("插入出错：" + e.getMessage());
        }
    }
}
