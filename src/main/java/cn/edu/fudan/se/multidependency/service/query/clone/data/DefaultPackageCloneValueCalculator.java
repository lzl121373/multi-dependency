package cn.edu.fudan.se.multidependency.service.query.clone.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Getter;
import lombok.Setter;

/**
 * 默认的判定有克隆关系的两个包是否相似的类
 * @author fan
 *
 */
public class DefaultPackageCloneValueCalculator implements CloneValueCalculator<Boolean> {
	
	public static final int DEFAULT_COUNT_THRESHOLD = 10;
	public static final double DEFAULT_PERCENTAGE_THRESHOLD = 0.5;
	
	private DefaultPackageCloneValueCalculator() {
		initThreshold();
	}
	
	private static DefaultPackageCloneValueCalculator instance = new DefaultPackageCloneValueCalculator();
	
	/**
	 * 若两个包内的有克隆关系的文件总数大于等于此值，则认为这两个包之间重复度过高
	 */
	@Setter
	@Getter
	private int countThreshold = DEFAULT_COUNT_THRESHOLD;
	
	@Setter
	@Getter
	private double percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	
	public static DefaultPackageCloneValueCalculator getInstance() {
		return instance;
	}
	
	public void initThreshold() {
		this.countThreshold = DEFAULT_COUNT_THRESHOLD;
		this.percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	}

	@Override
	public Boolean calculate(CloneValueForDoubleNodes<? extends Node> clone) {
		Node node1 = clone.getNode1();
		Node node2 = clone.getNode2();
		if(!(node1 instanceof Package) || !(node2 instanceof Package)) {
			return false;
		}
		//（包内很多文件，只有一部分文件克隆的情况）
		// 先判断两个包内有克隆关系的 文件数 （不是文件克隆对数）是否大于等于countThreshold （默认10）
		if(clone.getNodesInNode1().size() + clone.getNodesInNode2().size() >= countThreshold) {
			return true;
		}
		try {
			// 如果小于countThreshold（有可能是文件少，但是基本都有克隆关系的情况），
			// 则计算 克隆相关的文件数 / 两个包的文件总数 是否大于等于 percentageThreshold （默认0.8）
			if((clone.getNodesInNode1().size() + clone.getNodesInNode2().size() + 0.0) 
					/ (clone.getAllNodesInNode1().size() + clone.getAllNodesInNode2().size()) >= percentageThreshold) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		
		return false;
	}

}
