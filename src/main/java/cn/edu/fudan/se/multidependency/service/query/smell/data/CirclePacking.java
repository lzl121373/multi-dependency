package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import lombok.Data;

@Data
public class CirclePacking {
	
	private String type;
	
	public static final String TYPE_ONLY_SMELL = "onlySmell";
	public static final String TYPE_ONLY_ISSUE = "onlyIssue";
	public static final String TYPE_SMELL_ISSUE = "smellAndIssue";
	
	private Set<ProjectFile> files = new HashSet<>();
	
	public CirclePacking(String type) {
		this.type = type;
	}
	
	private Map<Long, List<Issue>> fileIdToIssues = new HashMap<>();
	
	private Map<Long, Integer> fileIdToSmellCount = new HashMap<>();
	
	private Map<Long, Integer> fileIdToCommitsCount = new HashMap<>();
	
	public void setFileCommitsCount(ProjectFile file, int count) {
		fileIdToCommitsCount.put(file.getId(), count);
	}
	
	public void setFileSmellCount(ProjectFile file, int count) {
		fileIdToSmellCount.put(file.getId(), count);
	}
	
	public int getMaxIssueSize() {
		int max = 0;
		for(ProjectFile file : files) {
			max = Math.max(max, fileIdToIssues.getOrDefault(file.getId(), new ArrayList<>()).size());
		}
		return max;
	}
	
	public void addProjectFile(ProjectFile file) {
		this.files.add(file);
		if(this.fileIdToIssues.get(file.getId()) == null) {
			this.fileIdToIssues.put(file.getId(), new ArrayList<>());
		}
	}

	public void addProjectFile(ProjectFile file, Collection<Issue> issues) {
		this.files.add(file);
		List<Issue> temp = fileIdToIssues.getOrDefault(file.getId(), new ArrayList<>());
		temp.addAll(issues);
		fileIdToIssues.put(file.getId(), temp);
	}
	
}
