package cn.edu.fudan.se.multidependency.service.query.history.data;

import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.service.query.metric.ProjectMetric;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.Collection;


@Data
@QueryResult
public class GitRepoMetric {
	
	private GitRepository gitRepository;

	private Collection<ProjectMetric> projectMetricsList;

	private int commits = 0;

	private int issues = 0;

	private int developers = 0;

	private int issueCommits = 0;

	private int bugIssues = 0;

	private int newFeatureIssues = 0;

	private int improvementIssues = 0;

	private int addLines = 0;

	private int subLines = 0;

	private int issueAddLines = 0;

	private int issueSubLines = 0;
}
