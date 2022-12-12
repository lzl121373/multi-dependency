package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;

public class UnstableDependencyByHistory extends UnstableComponent<ProjectFile> {

	private int fanOut;

	private List<DependsOn> totalFanOutDependencies = new ArrayList<>();

	private Set<ProjectFile> coChangeFiles = new HashSet<>();

	private Set<CoChange> coChangeTimesWithFile = new HashSet<>();

	public void addAllFanOutDependencies(Collection<DependsOn> totalFanOutDependencies) {
		this.totalFanOutDependencies.addAll(totalFanOutDependencies);
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

	public int getFanOut() {
		return fanOut;
	}

	public void setFanOut(int fanOut) {
		this.fanOut = fanOut;
	}

	public List<DependsOn> getTotalFanOutDependencies() {
		return totalFanOutDependencies;
	}

	public Set<ProjectFile> getCoChangeFiles() {
		return coChangeFiles;
	}

	public Set<CoChange> getCoChangeTimesWithFile() {
		return coChangeTimesWithFile;
	}

}
