package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface UnusedIncludeDetector {

    /**
     * 获取未使用引入
     */
    Map<Long, List<UnusedInclude>> queryFileUnusedInclude();

    /**
     * 检测未使用引入
     */
    Map<Long, List<UnusedInclude>> detectFileUnusedInclude();

    /**
     * 根据smellName生成文件的循环依赖的json格式信息
     */
    JSONObject getFileUnusedIncludeJson(Long projectId, String smellName);

    /**
     * 根据file的Id生成文件所在的Unused Include的json格式信息
     */
    JSONObject getFileUnusedIncludeJson(Long smellId);

    /**
     * 根据project的Id导出项目的各个层次的Unused Include
     */
    Boolean exportUnusedInclude(Project project);
}
