package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.metric.ProjectMetric;

@Repository
public interface ProjectRepository extends Neo4jRepository<Project, Long> {

	@Query("match (p:Project) where id(p)=$projectId return p")
	public Project findProjectById(@Param("projectId") Long projectId);

	@Query("match (n)-[r]-() delete r")
    void clearRelation();

    @Query("match (n) delete n")
    void clearNode();
    
	@Query("match (project:Project) where project.name=$name and project.language=$language return project")
	Project queryProjectByNameAndLanguage(@Param("name") String name, @Param("language") String language);
	
	@Query("match (project:Project) where project.language=$language return project")
	List<Project> queryProjectsByLanguage(@Param("language") String language);

	@Query("match (project:Project) return project")
	List<Project> queryAllProjects();

	@Query("match (project:Project) return project limit 1;")
	Project queryProjectWithLimitOne();
	
	@Query("match (n) where id(n) = $id return n")
	Node queryNodeById(@Param("id") long id);
	
	@Query("MATCH (project:Project)" +
			"RETURN project, project.nop as nop, project.nof as nof, project.nom as nom, " +
			"project.loc as loc, project.lines as lines," +
			"project.commits as commits, project.modularity as modularity " +
			"order by(project.name) desc;")
	public List<ProjectMetric> getProjectMetrics();

	@Query("MATCH (project:Project)-[:" + RelationType.str_CONTAIN + "]->(package:Package) \r\n" +
			"where package.nof > 0 \r\n" +
			"WITH project, " +
			"     count(distinct package) as nop, " +
			"     sum(package.nof) as nof, " +
			"     sum(package.noc) as noc, " +
			"     sum(package.nom) as nom, " +
			"     sum(package.loc) as loc, " +
			"     sum(package.lines) as lines\r\n" +
			"SET project += {nop: nop, nof: nof, noc: noc, nom: nom, loc: loc, lines: lines};")
	public void setProjectMetrics();

	@Query("MATCH (project:Project)-[:" + RelationType.str_CONTAIN + "]->(package:Package) \r\n" +
			"where package.nof > 0 \r\n" +
			"WITH project, " +
			"     count(distinct package) as nop, " +
			"     sum(package.nof) as nof, " +
			"     sum(package.noc) as noc, " +
			"     sum(package.nom) as nom, " +
			"     sum(package.loc) as loc, " +
			"     sum(package.lines) as lines, " +
			"     project.commits as commits " +
			"return project, nop, nof, noc, nom, loc, lines, commits order by(project.path);")
	public List<ProjectMetric> calculateProjectMetrics();

	@Query("MATCH (project:Project) " +
			"where id(project) = $id " +
			"SET project.modularity = $modularity;")
	public void setModularityMetricsForProject(@Param("id") long id, @Param("modularity") double modularity);
}
