package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.HashSet;
import java.util.Set;

@Data
@QueryResult
public class UnusedInclude {

	private ProjectFile coreFile;

	private Set<ProjectFile> unusedIncludeFiles;

	public UnusedInclude(ProjectFile coreFile, Set<ProjectFile> unusedIncludeFiles) {
		this.coreFile = coreFile;
		this.unusedIncludeFiles = new HashSet<>(unusedIncludeFiles);
	}
}
