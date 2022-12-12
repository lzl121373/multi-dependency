package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import lombok.Getter;
import lombok.Setter;

/**
 * 默认的判定有克隆关系的两个包是否相似的类
 * @author fan
 *
 */
public class RelationAggregatorForPackageByCloneLoc implements RelationAggregator<Boolean> {
	
	public static final double DEFAULT_PERCENTAGE_THRESHOLD = 0.5;
	
	private RelationAggregatorForPackageByCloneLoc() {
		initThreshold();
	}
	
	private static RelationAggregatorForPackageByCloneLoc instance = new RelationAggregatorForPackageByCloneLoc();
	
	@Setter
	@Getter
	private double percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	
	public static RelationAggregatorForPackageByCloneLoc getInstance() {
		return instance;
	}
	
	public void initThreshold() {
		this.percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	}

	@Override
	public Boolean aggregate(BasicDataForDoubleNodes<? extends Node, ? extends Relation> doubleNodes){
		Node node1 = doubleNodes.getNode1();
		Node node2 = doubleNodes.getNode2();
		if(!(node1 instanceof Package) || !(node2 instanceof Package)) {
			return false;
		}
		try {
			Collection<Node> nodesInPackage1 = doubleNodes.getNodesInNode1();
			Collection<Node> nodesInPackage2 = doubleNodes.getNodesInNode2();
			
			long cloneFilesLOC = 0;
			for(Node node : nodesInPackage1) {
				if(!(node instanceof ProjectFile)) {
					throw new Exception("clone节点不为file类型");
				}
				cloneFilesLOC += ((ProjectFile) node).getLoc();
			}
			for(Node node : nodesInPackage2) {
				if(!(node instanceof ProjectFile)) {
					throw new Exception("clone节点不为file类型");
				}
				cloneFilesLOC += ((ProjectFile) node).getLoc();
			}
			
			long allPackageLoc = ((Package) node1).getLoc() + ((Package) node2).getLoc();
			
			if((cloneFilesLOC + 0.0) / (allPackageLoc) >= percentageThreshold) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}
