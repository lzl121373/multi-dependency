package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class SimilarPackage {

	@Getter
	private final CloneValueForDoubleNodes<Package> clonePackages;

	@Getter
	private final Collection<SimilarPackage> childrenSimilarPackages;

	@Getter
	private final Collection<Package> childrenOtherPackages1;

	@Getter
	private final Collection<Package> childrenOtherPackages2;
	
	@Getter
	private Package package1;

	@Getter
	private Package package2;

	@Getter
	private int cloneNodes1;

	@Getter
	private int cloneNodes2;

	@Getter
	private int allNodes1;

	@Getter
	private int allNodes2;

	@Getter
	private final String id;
	
	@Getter
	@Setter
	private double value;
	
	private void swapPackage() {
		Package pck = package1;
		package1 = package2;
		package2 = pck;
	}
	
	public SimilarPackage(@NonNull CloneValueForDoubleNodes<Package> clonePackages) {
		this.clonePackages = clonePackages;
		this.childrenSimilarPackages = new ArrayList<>();
		this.childrenOtherPackages1 = new ArrayList<>();
		this.childrenOtherPackages2 = new ArrayList<>();
		this.package1 = clonePackages.getNode1();
		this.package2 = clonePackages.getNode2();
		if(package1.getDirectoryPath().compareTo(package2.getDirectoryPath()) > 0) {
			swapPackage();
		}
		this.id = clonePackages.getId();
	}
	
	public void addSimilarChild(SimilarPackage child) {
		if(childrenSimilarPackages == null) {
			return ;
		}
		this.childrenSimilarPackages.add(child);
	}

	public void addOtherChild1(Package child) {
		if(childrenOtherPackages1 == null) {
			return ;
		}
		this.childrenOtherPackages1.add(child);
	}

	public void addOtherChild2(Package child) {
		if(childrenOtherPackages2 == null) {
			return ;
		}
		this.childrenOtherPackages2.add(child);
	}

	public void setData(int allNodes1, int allNodes2, int cloneNodes1, int cloneNodes2) {
		this.allNodes1 = allNodes1;
		this.allNodes2 = allNodes2;
		this.cloneNodes1 = cloneNodes1;
		this.cloneNodes2 = cloneNodes2;
	}

	public boolean isContainSimilarChild(SimilarPackage p) {
		return this.childrenSimilarPackages.contains(p);
	}

	public boolean isContainOtherChild1(Package p) {
		return this.childrenOtherPackages1.contains(p);
	}

	public boolean isContainOtherChild2(Package p) {
		return this.childrenOtherPackages2.contains(p);
	}
}
