package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PieFilesData {
	
	Project project;
	
	Set<ProjectFile> normalFiles;
	Set<ProjectFile> onlyIssueFiles;
	Set<ProjectFile> onlySmellFiles;
	Set<ProjectFile> issueAndSmellFiles;
	
	Set<Issue> allIssues;
	
	Set<Issue> smellIssues;
	
	public int getAllFilesSize() {
		return normalFiles.size() + onlyIssueFiles.size() + onlySmellFiles.size() + issueAndSmellFiles.size();
	}
}
