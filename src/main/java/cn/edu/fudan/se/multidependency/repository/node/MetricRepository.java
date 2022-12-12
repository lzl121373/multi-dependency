package cn.edu.fudan.se.multidependency.repository.node;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.metric.NodeMetric;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricRepository extends Neo4jRepository<Metric, Long> {

    @Query("MATCH p=(t:Type)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(t) = $typeId RETURN m;")
    Metric findTypeMetric(@Param("typeId") Long typeId);

    @Query("MATCH p=(:Type)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findTypeMetric();

    @Query("MATCH p=(file:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(file) = $fileId RETURN m;")
    Metric findFileMetric(@Param("fileId") Long fileId);

    @Query("MATCH p=(:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findFileMetric();

    @Query("MATCH p=(node:ProjectFile)-[:" + RelationType.str_HAS + "]->(metric:Metric) RETURN node,metric;")
    List<NodeMetric> findFileMetricData();

    @Query("MATCH p=(pj:Project) -[:" + RelationType.str_CONTAIN + "]-> (:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) " +
            "where id(pj) = $projectId " +
            "RETURN m;")
    List<Metric> findFileMetricByProject(@Param("projectId") Long projectId);

    @Query("MATCH p=(:ProjectFile)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
    List<Metric> findFileMetricsWithLimit();

    @Query("MATCH p=(:ProjectFile)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
    void deleteAllFileMetric();

    @Query("MATCH p=(pck:Package)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(pck) = $pckId RETURN m;")
    Metric findPackageMetric(@Param("pckId") Long pckId);

    @Query("MATCH p=(:Package)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findPackageMetric();

    @Query("MATCH p=(node:Package)-[:" + RelationType.str_HAS + "]->(metric:Metric) RETURN node,metric;")
    List<NodeMetric> findPackageMetricData();

    @Query("MATCH p=(:Package)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
    List<Metric> findPackageMetricsWithLimit();

    @Query("MATCH p=(:Package)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
    void deleteAllPackageMetric();

    @Query("MATCH p=(project:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(project) = $projectId RETURN m")
    Metric findProjectMetric(@Param("projectId") Long projectId);

    @Query("MATCH p=(node:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) RETURN node,metric;")
    List<NodeMetric> findProjectMetricData();

    @Query("MATCH p=(:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findProjectMetric();

    @Query("MATCH p=(:Project)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
    List<Metric> findProjectMetricWithLimit();

    @Query("MATCH p=(:Project)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
    void deleteAllProjectMetric();

    @Query("MATCH p=(girRepo:GitRepository)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(girRepo) = gitRepoId  RETURN m;")
    List<Metric> findGitRepoMetric(@Param("gitRepoId") Long gitRepoId);

    @Query("MATCH p=(:GitRepository)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findGitRepoMetric();

    @Query("MATCH p=(node:GitRepository)-[:" + RelationType.str_HAS + "]->(metric:Metric) RETURN node,metric order by node.name asc;")
    List<NodeMetric> findGitRepoMetricData();

    @Query("MATCH p=(:GitRepository)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
    List<Metric> findGitRepoMetricWithLimit();

    @Query("MATCH p=(:GitRepository)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
    void deleteAllGitRepoMetric();

    @Query("MATCH p=(commit:Commit)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(commit) = $commitId RETURN m;")
    Metric findCommitMetric(@Param("commitId") Long commitId);

    @Query("MATCH p=(:Commit)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m;")
    List<Metric> findCommitMetric();

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.MedFileFanIn`;")
    Integer getMedFileFanInByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.MedFileFanOut`;")
    Integer getMedFileFanOutByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.MedPackageFanIn`;")
    Integer getMedPackageFanInByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.MedPackageFanOut`;")
    Integer getMedPackageFanOutByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.MedFileCoChange`;")
    Integer getMedFileCoChangeByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.MedPackageCoChange`;")
    Integer getMedPackageCoChangeByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.NOF`;")
    Integer getProjectFileCountByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.IssueCommits`;")
    Integer getProjectIssueCommitsByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.Commits`;")
    Integer getProjectCommitsByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.IssueChangeLines`;")
    Integer getProjectIssueChangeLinesByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project)-[:" + RelationType.str_HAS + "]->(metric:Metric) where id(project) = $projectId RETURN metric.`metricValues.ChangeLines`;")
    Integer getProjectChangeLinesByProjectId(@Param("projectId") Long projectId);
}
