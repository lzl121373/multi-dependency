package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import org.springframework.data.neo4j.annotation.QueryResult;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

import java.util.*;

@Data
@QueryResult
public class FileHubLike {

	private ProjectFile file;
	
	private int fanIn;
	
	private int fanOut;

	private Set<ProjectFile> coChangeFilesIn = new HashSet<>();

	private Map<Long, CoChange> coChangeTimesWithFileIn = new HashMap<>();

	private Set<ProjectFile> coChangeFilesOut = new HashSet<>();

	private Map<Long, CoChange> coChangeTimesWithFileOut = new HashMap<>();
	
	public FileHubLike(ProjectFile file, int fanIn, int fanOut) {
		this.file = file;
		this.fanIn = fanIn;
		this.fanOut = fanOut;
	}

	public void addAllCoChangesWithFanIn(Collection<CoChange> cochanges) {
		for(CoChange cochange : cochanges) {
			addCoChangeWithFanIn(cochange);
		}
	}

	public void addAllCoChangesWithFanOut(Collection<CoChange> cochanges) {
		for(CoChange cochange : cochanges) {
			addCoChangeWithFanOut(cochange);
		}
	}

	public void addCoChangeWithFanIn(CoChange cochange) {
		ProjectFile cochangeFileIn = (ProjectFile) cochange.getNode1();
		if(cochangeFileIn.equals(file)) {
			cochangeFileIn = (ProjectFile) cochange.getNode2();
		}
		coChangeFilesIn.add(cochangeFileIn);
		coChangeTimesWithFileIn.put(cochangeFileIn.getId(), cochange);
	}

	public void addCoChangeWithFanOut(CoChange cochange) {
		ProjectFile cochangeFileOut = (ProjectFile) cochange.getNode1();
		if(cochangeFileOut.equals(file)) {
			cochangeFileOut = (ProjectFile) cochange.getNode2();
		}
		coChangeFilesOut.add(cochangeFileOut);
		coChangeTimesWithFileOut.put(cochangeFileOut.getId(), cochange);
	}
	
	public int getLoc() {
		return file.getEndLine();
	}
}
