package cn.edu.fudan.se.multidependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.service.insert.BatchInserterService;
import cn.edu.fudan.se.multidependency.service.insert.RepositoryService;
import cn.edu.fudan.se.multidependency.service.insert.ThreadService;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;

public class InsertStaticData {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertStaticData.class);

    public static void main(String[] args) {
        LOGGER.info("InsertStaticData");
        insert(args);
    }

    public static void insert(String[] args) {
        try {
            YamlUtil.YamlObject yaml = YamlUtil.getYaml(args);

            ThreadService ts = new ThreadService(yaml);
            
            ts.staticAnalyse();
            
            RepositoryService service = RepositoryService.getInstance();
            LOGGER.info("静态分析节点数：" + service.getNodes().size());
            LOGGER.info("静态分析关系数：" + service.getRelations().size());
            
            FileUtil.writeObject(yaml.getSerializePath(), service);
            System.exit(0);
        } catch (Exception e) {
            // 所有步骤中有一个出错，都会终止执行
            e.printStackTrace();
            LOGGER.error("插入出错：" + e.getMessage());
        }
    }
}
