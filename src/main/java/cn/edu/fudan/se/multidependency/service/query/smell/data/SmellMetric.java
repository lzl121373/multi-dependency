package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.annotation.QueryResult;

@Data
@EqualsAndHashCode(callSuper=false)
public class SmellMetric {

	private Smell smell;

	/**
	 * 结构性度量指标
	 */
	private StructureMetric structureMetric;

	/**
	 * 演化性度量指标
	 */
	private EvolutionMetric evolutionMetric;

	/**
	 * 共变性度量指标
	 */
	private CoChangeMetric coChangeMetric;

	/**
	 * 债务性度量指标
	 */
	private DebtMetric debtMetric;

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class StructureMetric {
		private Smell smell;

		/**
		 * 包数
		 */
		private int nop;

		/**
		 * 文件数
		 */
		private int nof;

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
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class EvolutionMetric{

		private Smell smell;
		/**
		 * 修改次数（去重）
		 */
		private int commits;

		/**
		 * 总的修改次数（不去重）
		 */
		private int totalCommits;

		/**
		 * 开发者数（去重）
		 */
		private int developers;

		/**
		 * 开发者数(不去重）
		 */
		private int totalDevelopers;

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
	public class CoChangeMetric{

		private Smell smell;
		/**
		 * 出现共变的提交次数（去重）
		 */
		private int coChangeCommits;

		/**
		 * 总的共变的提交次数（不去重）
		 */
		private int totalCoChangeCommits;

		/**
		 * 共变的文件数量
		 */
		private int coChangeFiles;
	}

	@Data
	@EqualsAndHashCode(callSuper=false)
	@QueryResult
	public class DebtMetric{

		private Smell smell;

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
	}
}
