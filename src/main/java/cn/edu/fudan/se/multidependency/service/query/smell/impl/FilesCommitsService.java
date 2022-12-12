package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.IssueQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.data.CommitsInFileMatrix;
import cn.edu.fudan.se.multidependency.service.query.history.data.IssuesInFileMatrix;

@Service
public class FilesCommitsService {
	
	@Autowired
	private IssueQueryService issueService;
	
	@Autowired
	private GitAnalyseService gitService;

	public CommitsInFileMatrix findFilesCommits(Collection<ProjectFile> allFiles, Collection<ProjectFile> specifiedFiles, int minCount, Project project) {
		List<Commit> commits = new ArrayList<>();
		List<ProjectFile> files = new ArrayList<>(allFiles);
		for(Commit commit : gitService.findCommitsInProject(project)) {
			if(!commit.isUsingForIssue()) {
				continue;
			}
			int count = 0;
			Set<ProjectFile> updateFiles = new HashSet<>();
			for(CommitUpdateFile update : gitService.queryCommitUpdateFiles(commit)) {
				ProjectFile updateFile = update.getFile();
				if(allFiles.contains(updateFile)) {
					updateFiles.add(updateFile);
					count++;
				}
			}
			if(specifiedFiles.size() != 0 && !updateFiles.containsAll(specifiedFiles)) {
				continue;
			}
			if(count >= minCount) {
				commits.add(commit);
			}
		}
		commits.sort((c1, c2) -> {
			return c1.getAuthoredDate().compareTo(c2.getAuthoredDate());
		});
		System.out.println("CommitsInFileMatrix " + commits.size());
		boolean[][] updates = new boolean[commits.size()][files.size()];
		for(int i = 0; i < commits.size(); i++) {
			Commit commit = commits.get(i);
			for(CommitUpdateFile update : gitService.queryCommitUpdateFiles(commit)) {
				ProjectFile updateFile = update.getFile();
				int j = files.indexOf(updateFile);
				if(j >= 0) {
					updates[i][j] = true;
				}
			}
		}
		
		CommitsInFileMatrix result = new CommitsInFileMatrix(files.get(0), files, commits, updates, new HashMap<>());
		return result;
	}
	
	public IssuesInFileMatrix findFilesIssues(Collection<ProjectFile> allFiles, Collection<ProjectFile> specifiedFiles, int minCount, Project project) {
		List<Issue> issues = new ArrayList<>();
		List<ProjectFile> files = new ArrayList<>(allFiles);
		for(Issue issue : issueService.queryIssues(project)) {
			Set<ProjectFile> relatedFiles = new HashSet<>();
			int count = 0;
			for(ProjectFile file : issueService.queryRelatedFilesOnIssue(issue)) {
				if(allFiles.contains(file)) {
					relatedFiles.add(file);
					count++;
				}
			}
			if(specifiedFiles.size() != 0 && !relatedFiles.containsAll(specifiedFiles)) {
				continue;
			}
			if(count >= minCount) {
				issues.add(issue);
			}
		}
		System.out.println("IssuesInFileMatrix " + issues.size());
		boolean[][] related = new boolean[issues.size()][files.size()];
		for(int i = 0; i < issues.size(); i++) {
			Issue issue = issues.get(i);
			for(ProjectFile file : issueService.queryRelatedFilesOnIssue(issue)) {
				int j = files.indexOf(file);
				if(j >= 0) {
					related[i][j] = true;
				}
			}
		}
		
		IssuesInFileMatrix result = new IssuesInFileMatrix(files, issues, related);
		return result;
	}
	
	
}
