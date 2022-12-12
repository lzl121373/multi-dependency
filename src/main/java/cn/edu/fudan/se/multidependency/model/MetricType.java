package cn.edu.fudan.se.multidependency.model;

public final class MetricType {
	/**
	 * -------------------------------结构性度量值-----------------------------------
	 */

	/**
	 * 包数, Number of Packages
	 */
	public static final String NOP = "NOP";
	/**
	 * 文件数, Number of Files
	 */
	public static final String NOF = "NOF";
	/**
	 * 类的个数, Number of Classes
	 */
	public static final String NOC = "NOC";
	/**
	 * 函数个数, Number of Methods
	 */
	public static final String NOM = "NOM";
	/**
	 * 代码行，去除空行和注释
	 */
	public static final String LOC = "LOC";
	/**
	 * 代码总规模， 包括空行和注释
	 */
	public static final String LINES = "Lines";
	/**
	 * 扇出依赖
	 * 也叫Efferent  Couplings，依赖出度数
	 */
	public static final String FAN_OUT = "FanOut";
	/**
	 * 扇入依赖
	 * 也叫Afferent Couplings，依赖入度数
	 */
	public static final String FAN_IN = "FanIn";
	/**
	 * 文件扇出依赖中位数
	 */
	public static final String MED_FILE_FAN_OUT = "MedFileFanOut";
	/**
	 * 文件扇入依赖中位数
	 */
	public static final String MED_FILE_FAN_IN = "MedFileFanIn";
	/**
	 * 包扇出依赖中位数
	 */
	public static final String MED_PACKAGE_FAN_OUT = "MedPackageFanOut";
	/**
	 * 包扇入依赖中位数
	 */
	public static final String MED_PACKAGE_FAN_IN = "MedPackageFanIn";
	/**
	 * 不稳定度
	 * Instability = Ce / (Ce + Ca)
	 * 即Instability = FanOut / (FanOut + FanIn)
	 */
	public static final String INSTABILITY = "Instability";
	/**
	 * 根据依赖情况，衡量节点的重要程度
	 * PageRank
	 */
	public static final String PAGE_RANK_SCORE = "PageRankScore";
	/**
	 * 衡量项目的模块性
	 * Modularity
	 */
	public static final String MODULARITY = "Modularity";

	/**
	 * 衡量项目的模块性
	 * Modularity
	 */
	public static final String SIZE = "Size";

	/**
	 * --------------------------------演化性度量值------------------------------------------
	 */

	/**
	 * 提交数， Commits（去重）
	 */
	public static final String COMMITS = "Commits";
	/**
	 * 总提交数， Commits（不去重）
	 */
	public static final String TOTAL_COMMITS = "TotalCommits";
	/**
	 * 共变文件数
	 */
	public static final String CO_CHANGE_FILES = "CoChangeFiles";
	/**
	 * 共变文件数共变次数（去重）
	 */
	public static final String CO_CHANGE_COMMITS = "CoChangeCommits";
	/**
	 * 共变文件数共变次数（不去重）
	 */
	public static final String TOTAL_CO_CHANGE_COMMITS = "TotalCoChangeCommits";
	/**
	 * 开发者数
	 */
	public static final String DEVELOPERS = "Developers";

	/**
	 * addLines
	 */
	public static final String ADD_LINES = "AddLines";

	/**
	 * subLines
	 */
	public static final String SUB_LINES = "SubLines";

	/**
	 * Issue总数数量
	 */
	public static final String ISSUES = "Issues";

	/**
	 * Issue相关提交数
	 */
	public static final String ISSUE_COMMITS = "IssueCommits";

	/**
	 * Issue相关提交文件的修改行数
	 */
	public static final String ISSUE_ADD_LINES = "IssueAddLines";

	/**
	 * Issue相关提交文件的修改行数
	 */
	public static final String ISSUE_SUB_LINES = "IssueSubLines";

	/**
	 * 提交文件的修改行数
	 */
	public static final String CHANGE_LINES = "ChangeLines";

	/**
	 * Issue相关提交文件的修改行数
	 */
	public static final String ISSUE_CHANGE_LINES = "IssueChangeLines";

	/**
	 * Bug Issue总数数量
	 */
	public static final String BUG_ISSUES = "BugIssues";

	/**
	 * New feature Issue总数数量
	 */
	public static final String NEW_FEATURE_ISSUES = "NewFeatureIssues";

	/**
	 * Improvement feature Issue总数数量
	 */
	public static final String IMPROVEMENT_ISSUES = "ImprovementIssues";

	/**
	 * git历史中文件的创建者名称
	 */
	public static final String CREATOR =  "Creator";

	/**
	 * git历史中文件的最后操作者名称
	 */
	public static final String LAST_UPDATOR = "LastUpdator";

	/**
	 * git历史中更改文件次数最多的开发者名称
	 */
	public static final String MOST_UPDATOR = "MostUpdator";

	/**
	 * 共变文件中位数
	 */
	public static final String MED_FILE_CO_CHANGE = "MedFileCoChange";

	/**
	 * 共变包中位数
	 */
	public static final String MED_PACKAGE_CO_CHANGE = "MedPackageCoChange";

	/**
	 * --------------------------------债务性度量值------------------------------------------
	 */


	public static final String DEFAULT = "Default";

}
