package cn.edu.fudan.se.multidependency.service.query.smell.data;

import cn.edu.fudan.se.multidependency.model.node.Project;
import lombok.Data;

@Data
public class HistogramAS {
	
	public HistogramAS(Project project) {
		this.project = project;
	}

	Project project;
	
	int allFilesCount;
	
	int smellFilesCount;
	
	int issueFilesCount;
	
}
