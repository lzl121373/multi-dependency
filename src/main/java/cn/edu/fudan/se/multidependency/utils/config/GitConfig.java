package cn.edu.fudan.se.multidependency.utils.config;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class GitConfig {
	private String path;
	private boolean specifyCommitRange;
	private boolean specifyByCommitId;
	private String commitIdFrom;
	private String commitIdTo;
	private String commitTimeSince;
	private String commitTimeUntil;
	private String currentVersionCommitId;
	private String issueFrom;
	private Set<String> issueFilePathes = new HashSet<>();
	
	private Set<String> branches = new HashSet<>();
	
	public void addBranch(String branch) {
		this.branches.add(branch);
	}
	
	public void addIssuePath(String issueFilePath) {
		this.issueFilePathes.add(issueFilePath);
	}
}
