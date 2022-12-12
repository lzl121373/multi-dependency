package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;

@QueryResult
public class UnstableDependencyByInstability<T extends Node> extends UnstableComponent<T> {

	private double instability;
	
	private int badDependencies;
	
	private int allDependencies;
	
	private List<DependsOn> totalDependsOns = new ArrayList<>();
	
	private List<DependsOn> badDependsOns = new ArrayList<>();
	
	public void addAllBadDependencies(Collection<DependsOn> badDependsOns) {
		this.badDependsOns.addAll(badDependsOns);
	}
	
	public void addBadDependency(DependsOn badDependsOns) {
		this.badDependsOns.add(badDependsOns);
	}
	
	public void addAllTotalDependencies(Collection<DependsOn> totalDependsOns) {
		this.totalDependsOns.addAll(totalDependsOns);
	}

	public double getInstability() {
		return instability;
	}

	public void setInstability(double instability) {
		this.instability = instability;
	}

	public List<DependsOn> getTotalDependsOns() {
		return totalDependsOns;
	}

	public List<DependsOn> getBadDependsOns() {
		return badDependsOns;
	}

	public int getBadDependencies() {
		return badDependencies;
	}

	public void setBadDependencies() {
		this.badDependencies = this.badDependsOns.size();
	}

	public int getAllDependencies() {
		return allDependencies;
	}

	public void setAllDependencies() {
		this.allDependencies = this.totalDependsOns.size();
	}
	
}
