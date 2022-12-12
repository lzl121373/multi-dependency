package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.IssueType;
import cn.edu.fudan.se.multidependency.service.query.ar.DependencyPair;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.metric.FileMetric;

@Repository
public interface ProjectFileRepository extends Neo4jRepository<ProjectFile, Long> {
	
	@Query("match (f:ProjectFile) where f.path=$filePath return f")
	public ProjectFile findFileByPath(@Param("filePath") String filePath);

	@Query("match (pj:Project)-[:" + RelationType.str_CONTAIN + "*2]->(f:ProjectFile) " +
			"where id(pj) = $projectId return distinct f;")
	public List<ProjectFile> findFilesByProject(@Param("projectId") Long projectId);

	@Query("match (f:ProjectFile) where id(f)=$fileId return f")
	public ProjectFile findFileById(@Param("fileId") long fileId);

	@Query("match (p:Package)-[:" + RelationType.str_CONTAIN + "]->(f:ProjectFile) where id(p) = $packageId return count(distinct f);")
	public int getFilesNumberByPackageId(@Param("packageId") long packageId);

	@Query("MATCH (file:ProjectFile) " +
			"WITH file, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..5]->(:Type)) as noc, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..5]->(:Function)) as nom  " +
			"SET file += {noc: noc, nom: nom};")
	public void setFileMetrics();
	
	/**
	 * 所有文件的指标
	 * @return
	 */
	@Query("MATCH (file:ProjectFile) " +
			"WITH file, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..5]->(:Type)) as noc, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..5]->(:Function)) as nom, " +
			"     file.loc as loc, " +
			"     size((file)-[:"+ RelationType.str_DEPENDS_ON + "]->()) as fanOut, " +
			"     size((file)<-[:"+ RelationType.str_DEPENDS_ON + "]-()) as fanIn  " +
			"RETURN  file,noc,nom,loc,fanOut,fanIn order by(file.path) desc;")
	public List<FileMetric.StructureMetric> calculateFileStructureMetrics();
	
	@Query("MATCH (file:ProjectFile)  " +
            "where id(file)= $fileId  " +
			"WITH file, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..5]->(:Type)) as noc, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..5]->(:Function)) as nom, " +
			"     file.loc as loc, " +
			"     size((file)-[:"+ RelationType.str_DEPENDS_ON + "]->()) as fanOut, " +
			"     size((file)<-[:"+ RelationType.str_DEPENDS_ON + "]-()) as fanIn  " +
			"RETURN  file,noc,nom,loc,fanOut,fanIn;")
	public FileMetric.StructureMetric calculateFileStructureMetrics(@Param("fileId") long fileId);

	/**
	 * 所有文件的指标
	 * @return
	 */
	@Query("MATCH (file:ProjectFile) <-[r:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)<-[:" +
			RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]- (d:Developer) " +
			"with file, count(distinct c) as commits,count(distinct d) as developers,collect(distinct r) as commitUpdates " +
			"with file, commits, developers," +
			"     size((file)-[:" + RelationType.str_CO_CHANGE +"]-(:ProjectFile)) as coChangeFiles," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.addLines ) as addLines," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.subLines ) as subLines " +
			"RETURN  file,commits,developers,coChangeFiles,addLines,subLines;")
	public List<FileMetric.EvolutionMetric> calculateFileEvolutionMetrics();

	@Query("MATCH (file:ProjectFile) <-[r:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)<-[:" +
			RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]- (d:Developer) " +
			"where id(file)= $fileId " +
			"with file, count(distinct c) as commits,count(distinct d) as developers,collect(distinct r) as commitUpdates " +
			"with file, commits, developers," +
			"     size((file)-[:" + RelationType.str_CO_CHANGE +"]-(:ProjectFile)) as coChangeFiles," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.addLines ) as addLines," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.subLines ) as subLines " +
			"RETURN  file,commits,developers,coChangeFiles,addLines,subLines;")
	public FileMetric.EvolutionMetric calculateFileEvolutionMetrics(@Param("fileId") long fileId);

	@Query("MATCH (file:ProjectFile) <-[r:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)-[:" +
			RelationType.str_COMMIT_ADDRESS_ISSUE + "]-> (issue:Issue) " +
			"with file, collect(distinct issue) as issueList,collect(distinct r) as commitUpdates, count(distinct c) as issueCommits " +
			"with file, size(issueList) as issues, issueCommits," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.BUG + "\' then 1 else 0 end)) as bugIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.NEW_FEATURE + "\' then 1 else 0 end)) as newFeatureIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.IMPROVEMENT + "\' then 1 else 0 end)) as improvementIssues," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.addLines ) as issueAddLines," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.subLines ) as issueSubLines " +
			"RETURN  file,issues,bugIssues,newFeatureIssues,improvementIssues,issueCommits,issueAddLines,issueSubLines;")
	public List<FileMetric.DebtMetric> calculateFileDebtMetrics();

	@Query("MATCH (file:ProjectFile) <-[r:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)-[:" +
			RelationType.str_COMMIT_ADDRESS_ISSUE + "]-> (issue:Issue) " +
			"where id(file)= $fileId " +
			"with file, collect(distinct issue) as issueList,collect(distinct r) as commitUpdates, count(distinct c) as issueCommits " +
			"with file, size(issueList) as issues, issueCommits," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.BUG + "\' then 1 else 0 end)) as bugIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.NEW_FEATURE + "\' then 1 else 0 end)) as newFeatureIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when \'" + IssueType.IMPROVEMENT + "\' then 1 else 0 end)) as improvementIssues," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.addLines ) as issueAddLines," +
			"     reduce(tmp = 0, cu in commitUpdates | tmp + cu.subLines ) as issueSubLines " +
			"RETURN  file,issues,bugIssues,newFeatureIssues,improvementIssues,issueCommits,issueAddLines,issueSubLines;")
	public FileMetric.DebtMetric calculateFileDebtMetrics(@Param("fileId") long fileId);
	
//	@Query("match (file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit) where id(file) = $fileId with c where size((c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) > 1 return c")
//	public List<Commit> cochangeCommitsWithFile(@Param("fileId") long fileId);
	
	/**
	 * 有commit更新的文件，并且该commit提交文件的个数大于1
	 * @return
	 */
	@Query("MATCH (file:ProjectFile) " + 
			"with file " +
			"match (file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)  " +
			"with file, c  " +
			"where size((c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) > 1 " +
			"with file, count(c) as cochangeCommitTimes " +
			"WITH size((file)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut,  " + 
			"     size((file)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn, " + 
			"     size((file)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-()) as commits, " +
			"     size((file)-[:" + RelationType.str_CONTAIN + "*1..3]->(:Function)) as nom, " + 
			"     size((file)-[:" + RelationType.str_CO_CHANGE + "]-(:ProjectFile)) as coChangeFile, " +
			"     file.endLine as loc, " + 
			"     file.score as score, " + 
			"     cochangeCommitTimes, " + 
			"     file " + 
			"RETURN  file,fanIn,fanOut,commits,cochangeCommitTimes,nom,loc,score,coChangeFiles order by(file.path) desc;")
	public List<FileMetric> calculateFileMetricsWithCoChangeCommitTimes();
	
	@Query("CALL gds.pageRank.stream({" +
			"nodeProjection:\'ProjectFile\', " +
			"relationshipProjection: \'" + RelationType.str_DEPENDS_ON + "\', " +
			"maxIterations: $iterations, " +
			"dampingFactor: $dampingFactor}) " +
			"YIELD nodeId, score " +
			"with gds.util.asNode(nodeId) AS file, score " +
			"set file.score=score " +
			"RETURN file " +
			"ORDER BY score DESC")
	public List<ProjectFile> pageRank(@Param("iterations") int iterations, @Param("dampingFactor") double dampingFactor);
	
	@Query("match (f1:ProjectFile)-[:DEPENDS_ON]->(f2:ProjectFile) where id(f2)=$fileId return f1")
	public List<ProjectFile> calculateFanIn(@Param("fileId") long fileId);
	
	@Query("match (f1:ProjectFile)-[:DEPENDS_ON]->(f2:ProjectFile) where id(f1)=$fileId return f2")
	public List<ProjectFile> calculateFanOut(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:DEPENDS_ON]->(f2:ProjectFile) where id(f2)=$fileId return count(distinct f1)")
	int getFanIn(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:DEPENDS_ON]->(f2:ProjectFile) where id(f1)=$fileId return count(distinct f2)")
	int getFanOut(@Param("fileId") long fileId);
	
	@Query("match (f1:ProjectFile)-[:IMPORT]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImportBtwFile(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:INCLUDE]->(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getIncludeBtwFile(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[:EXTENDS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getExtendBtwType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[:IMPLEMENTS]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImplementBtwType(@Param("fileId") long fileId);

	@Query("match (f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[:IMPLEMENTS]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImplementBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCreateBtwType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CREATE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCreateFromFuncToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:IMPLLINK]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getImpllinkBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCallBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[r:CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCallFromTypeToFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:ACCESS]->(:Variable)<-[:CONTAIN*1..4]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getAccessFromFuncToVar(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..2]->(:Type)-[:CONTAIN]->(:Variable)-[r:VARIABLE_TYPE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getMemberVarTypeFromVarToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[:CONTAIN]->(:Variable)-[r:VARIABLE_TYPE]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getLocalVarTypeFromVarToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:RETURN]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getReturnFromFuncToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:THROW]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getThrowFromFuncToType(@Param("fileId") long fileId);

	//泛型
	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Variable)-[r:PARAMETER]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getParaFromVarToType(@Param("fileId") long fileId);

	//函数中包含强转
	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:CAST]->(:Type)<-[:CONTAIN*1..2]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getCastFromFuncToType(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[:CONTAIN*1..3]->(:Function)-[r:DYNAMIC_CALL]->(:Function)<-[:CONTAIN*1..3]-(f2:ProjectFile) where id(f1)=$fileId and f1 <> f2 return f2 as projectFile, count(r) as count")
	List<DependencyPair> getDynamicCallBtwFunc(@Param("fileId") long fileId);

	@Query("match p=(f1:ProjectFile)-[r:CO_CHANGE]->(f2:ProjectFile) where id(f1)=$fileId return f2 as projectFile, r.times as count")
	List<DependencyPair> getCoChangeFiles(@Param("fileId") long fileId);

	/**
	 * 按照特定codeveloper属性查找节点
	 */
	@Query("match (f:ProjectFile) -[:" + RelationType.str_HAS + "]-> (m:Metric) where m.`metricValues.Creator` = $name return f")
	List<ProjectFile> findFilesByCreatorName(@Param("name") String name);

	@Query("match (f:ProjectFile) -[:" + RelationType.str_HAS + "]-> (m:Metric) where m.`metricValues.LastUpdator` = $name return f")
	List<ProjectFile> findFilesByLastUpdatorName(@Param("name") String name);

	@Query("match (f:ProjectFile) -[:" + RelationType.str_HAS + "]-> (m:Metric) where m.`metricValues.MostUpdator` = $name return f")
	List<ProjectFile> findFilesByMostUpdatorName(@Param("name") String name);

	@Query("match (f:ProjectFile) -[:" + RelationType.str_HAS + "]-> (m:Metric) where id(f) = $fileId with m.`metricValues.Creator` as developer\n" +
			"match (f:ProjectFile) -[:HAS]-> (m:Metric) where m.`metricValues.Creator` = developer return f")
	List<ProjectFile> findCoCreatorFileList(@Param("fileId")Long fileId);

	@Query("match (f:ProjectFile) -[:" + RelationType.str_HAS + "]-> (m:Metric) where id(f) = $fileId with m.`metricValues.LastUpdator` as developer\n" +
			"match (f:ProjectFile) -[:HAS]-> (m:Metric) where m.`metricValues.Creator` = developer return f")
	List<ProjectFile> findCoLastUpdatorFileList(@Param("fileId")Long fileId);

	@Query("match (f:ProjectFile) -[:" + RelationType.str_HAS + "]-> (m:Metric) where id(f) = $fileId with m.`metricValues.MostUpdator` as developer\n" +
			"match (f:ProjectFile) -[:HAS]-> (m:Metric) where m.`metricValues.Creator` = developer return f")
	List<ProjectFile> findCoMostUpdatorFileList(@Param("fileId")Long fileId);

	@Query("MATCH (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)-[:" + RelationType.str_HAS + "]->(metric:Metric) " +
			"where id(project) = $projectId " +
			"with distinct metric, metric.`metricValues.FanIn` as fanIn " +
			"where fanIn > 0 " +
			"RETURN fanIn order by fanIn;")
	List<Integer> findFileFanInByProjectId(@Param("projectId") Long projectId);

	@Query("MATCH (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)-[:" + RelationType.str_HAS + "]->(metric:Metric) " +
			"where id(project) = $projectId " +
			"with distinct metric, metric.`metricValues.FanOut` as fanOut " +
			"where fanOut > 0 " +
			"RETURN fanOut order by fanOut;")
	List<Integer> findFileFanOutByProjectId(@Param("projectId") Long projectId);

	@Query("MATCH (file:ProjectFile) where id(file) = $fileId return file.fanOut;")
	Integer getFileFanOutByFileId(@Param("fileId") Long fileId);

	@Query("match (project:Project) where id(project) = $projectId " +
			"with project " +
			"match (project)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)" +
			" return count(distinct file);")
	Integer calculateProjectFileCountByProjectId(@Param("projectId") Long projectId);

	@Query("match (smell:Smell) " +
			"where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
			"with smell " +
			"match (smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)" +
			" return count(distinct file);")
	Integer calculateFileSmellFileCountByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

	@Query("match (smell:Smell) " +
			"where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel " +
			"with smell " +
			"match (smell)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)" +
			" return count(distinct file);")
	Integer calculatePackageSmellFileCountByProjectId(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);
}
