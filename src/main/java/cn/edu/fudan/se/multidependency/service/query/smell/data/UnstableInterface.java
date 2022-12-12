package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;

import java.util.*;

public class UnstableInterface extends UnstableComponent<ProjectFile> {

	private int fanIn;

	private List<DependsOn> totalFanInDependencies = new ArrayList<>();
	
	private Set<ProjectFile> coChangeFiles = new HashSet<>();
	
	private Set<CoChange> coChangeTimesWithFile = new HashSet<>();

	public void addAllFanInDependencies(Collection<DependsOn> totalFanInDependencies) {
		this.totalFanInDependencies.addAll(totalFanInDependencies);
	}
	
	public void addAllCoChanges(Collection<CoChange> cochanges) {
		for(CoChange cochange : cochanges) {
			addCoChange(cochange);
		}
	}
	
	public void addCoChange(CoChange cochange) {
		ProjectFile cochangeFile = (ProjectFile) cochange.getNode1();
		if(cochangeFile.equals(super.getComponent())) {
			cochangeFile = (ProjectFile) cochange.getNode2();
		}
		coChangeFiles.add(cochangeFile);
		coChangeTimesWithFile.add(cochange);
	}

	public int getFanIn() {
		return fanIn;
	}

	public void setFanIn(int fanIn) {
		this.fanIn = fanIn;
	}

	public List<DependsOn> getTotalFanInDependencies() {
		return totalFanInDependencies;
	}

	public Set<ProjectFile> getCoChangeFiles() {
		return coChangeFiles;
	}

	public Set<CoChange> getCoChangeTimesWithFile() {
		return coChangeTimesWithFile;
	}

}
