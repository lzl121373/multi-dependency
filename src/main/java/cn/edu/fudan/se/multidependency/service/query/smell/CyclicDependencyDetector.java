package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import com.alibaba.fastjson.JSONObject;

public interface CyclicDependencyDetector {

	/**
	 * 获取类的循环依赖
	 */
	Map<Long, List<Cycle<Type>>> queryTypeCyclicDependency();

	/**
	 * 获取文件的循环依赖
	 */
	Map<Long, List<Cycle<ProjectFile>>> queryFileCyclicDependency();

	/**
	 * 获取包的循环依赖
	 */
	Map<Long, List<Cycle<Package>>> queryPackageCyclicDependency();

	/**
	 * 检测类的循环依赖
	 */
	Map<Long, List<Cycle<Type>>> detectTypeCyclicDependency();

	/**
	 * 检测文件的循环依赖
	 */
	Map<Long, List<Cycle<ProjectFile>>> detectFileCyclicDependency();

	/**
	 * 检测包的循环依赖
	 */
	Map<Long, List<Cycle<Package>>> detectPackageCyclicDependency();

	/**
	 * 根据smellName生成类的循环依赖的json格式信息
	 */
	JSONObject getTypeCyclicDependencyJson(Long projectId, String smellName);

	/**
	 * 根据smellName生成文件的循环依赖的json格式信息
	 */
	JSONObject getFileCyclicDependencyJson(Long projectId, String smellName);

	/**
	 * 根据smellName生成包的循环依赖的json格式信息
	 */
	JSONObject getPackageCyclicDependencyJson(Long projectId, String smellName);

	/**
	 * 根据file的Id生成文件所在的循环依赖的json格式信息
	 */
	JSONObject getFileCyclicDependencyJson(Long smellId);

	void exportCycleDependency();
}
