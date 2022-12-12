package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 节点node1和node2不分方向
 * 若Node为Package，则表示两个Package之间的**关系，children为pck1和pck2内的文件**关系或方法**关系等，**关系的两个节点分别在两个包里，不计同一包下的文件**
 * @author fan
 * 
 * @param <N>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CloneRelationDataForDoubleNodes<N extends Node, R extends Relation> extends BasicDataForDoubleNodes<N, R> {

	private static final long serialVersionUID = -7780703024314601425L;

	//克隆文件对数
	private int clonePairs = 0;

	//包1下克隆文件个数
	private int cloneNodesCount1 = 0;

	//包2下克隆文件个数
	private int cloneNodesCount2 = 0;

	//包1下全部文件个数
	private int allNodesCount1 = 0;

	//包2下全部文件个数
	private int allNodesCount2 = 0;

	//克隆匹配率=（包1下克隆文件个数+包2下克隆文件个数）/（包1下全部文件个数+包2下全部文件个数）
	private double cloneMatchRate = 0.00;

	//包1下克隆文件代码行数
	private int cloneNodesLoc1 = 0;

	//包2下克隆文件代码行数
	private int cloneNodesLoc2 = 0;

	//包1下全部文件代码行数
	private int allNodesLoc1 = 0;

	//包2下全部文件代码行数
	private int allNodesLoc2 = 0;

	//克隆代码行数比率=（包1下克隆文件代码行数+包2下克隆文件代码行数）/（包1下全部文件代码行数+包2下全部文件代码行数）
	private double cloneLocRate = 0.00;

	//克隆包对下克隆文件Cochange次数
	private int cloneNodesCoChangeTimes = 0;

	//克隆包对下全部文件Cochange次数
	private int allNodesCoChangeTimes = 0;

	//克隆包对Cochange比率=克隆包对下克隆文件Cochange次数/克隆包对下全部文件Cochange次数
	private double cloneCoChangeRate = 0.00;

	//克隆包对下克隆文件Type1个数
	private int cloneType1Count = 0;

	//克隆包对下克隆文件Type2个数
	private int cloneType2Count = 0;

	//克隆包对下克隆文件Type3个数
	private int cloneType3Count = 0;

	//克隆包克隆类型
	private String cloneType = "";


	//克隆包相似值=克隆包对下克隆文件Type1的value+克隆包对下克隆文件Type2的value+克隆包对下克隆文件Type3的value
	private double cloneSimilarityValue = 0.00;

	//克隆包相似比率=克隆包相似值/（克隆包对下克隆文件Type1个数+克隆包对下克隆文件Type2个数+克隆包对下克隆文件Type3个数）
	private double cloneSimilarityRate = 0.00;

	public CloneRelationDataForDoubleNodes(N node1, N node2){
		super(node1, node2);
	}

	public void setClonePairs(int clonePairs) {
		this.clonePairs = clonePairs;
	}

	public void setCloneCountDate(int cloneNodesCount1, int cloneNodesCount2, int allNodesCount1, int allNodesCount2) {
		this.cloneNodesCount1 = cloneNodesCount1;
		this.cloneNodesCount2 = cloneNodesCount2;
		this.allNodesCount1 = allNodesCount1;
		this.allNodesCount2 = allNodesCount2;
		if(allNodesCount1 + allNodesCount2 > 0) {
			this.cloneMatchRate = (cloneNodesCount1 + cloneNodesCount2 + 0.0) / (allNodesCount1 + allNodesCount2);
		}
	}

	public void setCloneLocDate(int cloneNodesLoc1, int cloneNodesLoc2, int allNodesLoc1, int allNodesLoc2) {
		this.cloneNodesLoc1 = cloneNodesLoc1;
		this.cloneNodesLoc2 = cloneNodesLoc2;
		this.allNodesLoc1 = allNodesLoc1;
		this.allNodesLoc2 = allNodesLoc2;
		if(allNodesLoc1 + allNodesLoc2 > 0) {
			this.cloneLocRate = (cloneNodesLoc1 + cloneNodesLoc2 + 0.0) / (allNodesLoc1 + allNodesLoc2);
		}
	}

	public void setCoChangeTimesData(int cloneNodesCoChangeTimes, int allNodesCoChangeTimes) {
		this.cloneNodesCoChangeTimes = cloneNodesCoChangeTimes;
		this.allNodesCoChangeTimes = allNodesCoChangeTimes;
		if(allNodesCoChangeTimes > 0) {
			this.cloneCoChangeRate = (cloneNodesCoChangeTimes + 0.00) / allNodesCoChangeTimes;
		}
	}

	public void setCloneTypeDate(int cloneType1Count, int cloneType2Count, int cloneType3Count, double cloneSimilarityValue) {
		this.cloneType1Count = cloneType1Count;
		this.cloneType2Count = cloneType2Count;
		this.cloneType3Count = cloneType3Count;
		this.cloneSimilarityValue = cloneSimilarityValue;
		if(cloneType1Count + cloneType2Count + cloneType3Count > 0) {
			this.cloneSimilarityRate = (cloneSimilarityValue + 0.00) / (cloneType1Count + cloneType2Count + cloneType3Count);
		}
		if(cloneType3Count > 0) {
			this.cloneType = "type_3";
		}
		else if(cloneType2Count > 0) {
			this.cloneType = "type_2";
		}
		else if(cloneType1Count > 0) {
			this.cloneType = "type_1";
		}
	}

	@Override
	public RelationType getRelationDataType() {
		return RelationType.CLONE;
	}
}
