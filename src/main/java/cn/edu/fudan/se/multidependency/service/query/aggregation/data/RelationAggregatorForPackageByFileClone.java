package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueCalculator;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.util.Pack;

/**
 * 默认的判定有克隆关系的两个包是否相似的类
 * @author fan
 *
 */
public class RelationAggregatorForPackageByFileClone implements RelationAggregator<Boolean> {
	
	public static final int DEFAULT_COUNT_THRESHOLD = 10;
	public static final double DEFAULT_PERCENTAGE_THRESHOLD = 0.5;
	
	private RelationAggregatorForPackageByFileClone() {
		initThreshold();
	}
	
	private static RelationAggregatorForPackageByFileClone instance = new RelationAggregatorForPackageByFileClone();
	
	/**
	 * 若两个包内的有克隆关系的文件总数大于等于此值，则认为这两个包之间重复读过高
	 */
	@Setter
	@Getter
	private int countThreshold = DEFAULT_COUNT_THRESHOLD;
	
	@Setter
	@Getter
	private double percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	
	public static RelationAggregatorForPackageByFileClone getInstance() {
		return instance;
	}
	
	public void initThreshold() {
		this.countThreshold = DEFAULT_COUNT_THRESHOLD;
		this.percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	}

	@Override
	public Boolean aggregate(BasicDataForDoubleNodes<? extends Node, ? extends Relation> doubleNodes) {
		CloneRelationDataForDoubleNodes<? extends Node, ? extends Relation> cloneRelationDataForDoubleNodes = (CloneRelationDataForDoubleNodes<? extends Node, ? extends Relation>) doubleNodes;
		int allNodes1 = cloneRelationDataForDoubleNodes.getAllNodesCount1();
		int allNodes2 = cloneRelationDataForDoubleNodes.getAllNodesCount2();
		int cloneNodes1 = cloneRelationDataForDoubleNodes.getCloneNodesCount1();
		int cloneNodes2 = cloneRelationDataForDoubleNodes.getCloneNodesCount2();
		return ((cloneNodes1 + cloneNodes2 + 0.0) / (allNodes1 + allNodes2) > 0.5);
	}
}
