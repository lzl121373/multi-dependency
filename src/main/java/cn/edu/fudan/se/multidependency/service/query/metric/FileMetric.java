package cn.edu.fudan.se.multidependency.service.query.metric;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@QueryResult
public class FileMetric extends FanIOMetric {

	private ProjectFile file;

	/**
	 * fanIn
	 */
	private int fanIn;

	/**
	 * fanOut
	 */
	private int fanOut;

	/**
	 * 结构性度量指标
	 */
	private StructureMetric structureMetric;

	/**
	 * 演化性度量指标
	 */
	private EvolutionMetric evolutionMetric;

	/**
	 * 债务性度量指标
	 */
	private DebtMetric debtMetric;

	/**
	 *  开发者度量指标
	 */
	private DeveloperMetric developerMetric;

	/**
	 * 不稳定度
	 * Instability = Ce / (Ce + Ca)
	 */
	private double instability;

	/**
	 * PageRank Score
	 */
	private double pageRankScore;


	@Override
	public Node getComponent() {
		return file;
	}


	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class StructureMetric {

		private ProjectFile file;

		/**
		 * 类数
		 */
		private int noc;

		/**
		 * 方法数
		 */
		private int nom;

		/**
		 * 代码行
		 */
		private int loc;

		/**
		 * fanOut
		 */
		private int fanOut;

		/**
		 * fanIn
		 */
		private int fanIn;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class EvolutionMetric{

		private ProjectFile file;

		/**
		 * 修改次数
		 */
		private int commits;

		/**
		 * 开发总数
		 */
		private int developers;

		/**
		 * 与该文件协同修改的文件数量
		 */
		private int coChangeFiles;

		/**
		 * 增加代码行
		 */
		private int addLines;

		/**
		 * 删除代码行
		 */
		private int subLines;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class DebtMetric{
		private ProjectFile file;

		/**
		 * Issue总数数量
		 */
		private int issues;
		/**
		 * Bug Issue总数数量
		 */
		private int bugIssues;
		/**
		 * New feature Issue总数数量
		 */
		private int newFeatureIssues;
		/**
		 * Improvement feature Issue总数数量
		 */
		private int improvementIssues;
		/**
		 * 该文件关联issue相关的commit次数
		 */
		private int issueCommits;
		/**
		 * 改文件关联issue相关的commit提交文件的增加行数
		 */
		private int issueAddLines;
		/**
		 * 改文件关联issue相关的commit提交文件的删除行数
		 */
		private int issueSubLines;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class DeveloperMetric{
		private ProjectFile file;

		/**
		 * git历史中文件的创建者名称
		 */
		private String creator;

		/**
		 * git历史中文件的最后操作者名称
		 */
		private String lastUpdator;

		/**
		 * git历史中更改文件次数最多的开发者名称
		 */
		private String mostUpdator;
	}


}
