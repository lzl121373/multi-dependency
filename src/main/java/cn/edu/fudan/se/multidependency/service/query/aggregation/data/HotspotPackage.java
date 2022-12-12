package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
public class HotspotPackage {

	private RelationDataForDoubleNodes<Node, Relation> relationPackages;

	private Collection<HotspotPackage> childrenHotspotPackages;

	private Collection<Package> childrenOtherPackages1;

	private Collection<Package> childrenOtherPackages2;

	private Package package1;

	private Package package2;

	private int clonePairs;

	private int relationNodes1;

	private int relationNodes2;

	private int allNodes1;

	private int allNodes2;

	private String id;

	private double similarityValue;

	private int packageCochangeTimes = 0;

	private int packageCloneCochangeTimes = 0;

	private String dependsOnTypes = "";

	private String dependsByTypes = "";

	private int dependsOnTimes = 0;

	private int dependsByTimes = 0;
	
	public HotspotPackage(@NonNull RelationDataForDoubleNodes<Node, Relation> relationPackages) {
		this.relationPackages = relationPackages;
		this.childrenHotspotPackages = new ArrayList<>();
		this.childrenOtherPackages1 = new ArrayList<>();
		this.childrenOtherPackages2 = new ArrayList<>();
		this.package1 = (Package) relationPackages.getNode1();
		this.package2 = (Package) relationPackages.getNode2();
		this.id = relationPackages.getId();
		this.clonePairs = relationPackages.getChildren().size();
		this.allNodes1 = 0;
		this.allNodes2 = 0;
		this.relationNodes1 = 0;
		this.relationNodes2 = 0;
		this.dependsOnTypes = relationPackages.getDependsOnTypes();
		this.dependsByTypes = relationPackages.getDependsByTypes();
		this.dependsOnTimes = relationPackages.getDependsOnTimes();
		this.dependsByTimes = relationPackages.getDependsByTimes();
	}

	public boolean addHotspotChild(HotspotPackage child) {
		if(childrenHotspotPackages == null) {
			return false;
		}
		if(!this.childrenHotspotPackages.contains(child)) {
			this.childrenHotspotPackages.add(child);
		}
		return true;
	}

	public boolean addOtherChild1(Package child) {
		if(childrenOtherPackages1 == null) {
			return false;
		}
		if(!this.childrenOtherPackages1.contains(child)) {
			this.childrenOtherPackages1.add(child);
		}
		return true;
	}

	public boolean addOtherChild2(Package child) {
		if(childrenOtherPackages2 == null) {
			return false;
		}
		if(!this.childrenOtherPackages2.contains(child)) {
			this.childrenOtherPackages2.add(child);
		}
		return true;
	}

	public void setData(int allNodes1, int allNodes2, int relationNodes1, int relationNodes2) {
		this.allNodes1 = allNodes1;
		this.allNodes2 = allNodes2;
		this.relationNodes1 = relationNodes1;
		this.relationNodes2 = relationNodes2;
		this.similarityValue = (relationNodes1 + relationNodes2 + 0.0) / (allNodes1 + allNodes2);
	}

	public void setClonePairs(int clonePairs) {
		this.clonePairs = clonePairs;
	}
	public void swapPackages() {
		Package pck = this.package1;
		this.package1 = this.package2;
		this.package2 = pck;
	}
}
