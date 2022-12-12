package cn.edu.fudan.se.multidependency.service.query.metric;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Project;
import lombok.Data;

@Data
@QueryResult
public class ProjectMetric {
	
	private Project project;
	
	/**
	 * 包数
	 */
	private int nop = 0;
	
	/**
	 * 文件数
	 */
	private int nof = 0;

	/**
	 * 类数
	 */
	private int noc = 0;
	
	/**
	 * 方法数
	 */
	private int nom = 0;
	
	/**
	 * 代码行
	 */
	private int loc = 0;
	
	/**
	 * 文件总行数
	 */
	private int lines = 0;
	
	/**
	 * 与该项目相关的commit次数
	 */
	private int commits = 0;

	/**
	 * 与该项目与issue相关的commit次数
	 */
	private int issueCommits = 0;

	/**
	 * issues数量
	 */
	private int issues = 0;

	/**
	 * developers数量
	 */
	private int developers = 0;

	/**
	 * 与该项目相关的commit提交文件的修改行数
	 */
	private int changeLines = 0;

	/**
	 * 与该项目与issue相关的commit提交文件的修改行数
	 */
	private int issueChangeLines = 0;
	
	/**
	 * 模块度
	 */
	private double modularity = 0;

	/**
	 * 该项目所有文件入度中位数
	 */
	private int medFileFanIn = 0;

	/**
	 * 该项目所有文件出度中位数
	 */
	private int medFileFanOut = 0;

	/**
	 * 该项目所有包入度中位数
	 */
	private int medPackageFanIn = 0;

	/**
	 * 该项目所有包出度中位数
	 */
	private int medPackageFanOut = 0;

	/**
	 * 该项目所有文件共变中位数
	 */
	private int medFileCoChange = 0;

	/**
	 * 该项目所有包共变中位数
	 */
	private int medPackageCoChange = 0;
}
