package cn.edu.fudan.se.multidependency.service.query.history.data;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IssuesInFileMatrix {
	
	List<ProjectFile> files;
	
	List<Issue> issues;
	
	boolean[][] related;
	
}
