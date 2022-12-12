package cn.edu.fudan.se.multidependency.repository.smell;

import cn.edu.fudan.se.multidependency.model.IssueType;
import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.smell.data.SmellMetric;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SmellRepository extends Neo4jRepository<Smell, Long> {

	@Query("match p = (smell:Smell) where id(smell) = $smellId return distinct smell")
	Smell findSmell(@Param("smellId") Long smellId);

	@Query("match p = (smell:Smell) where smell.level = $level return smell")
	List<Smell> findSmells(@Param("level") String level);

	@Query("match p = (smell:Smell) where smell.level = $level and smell.type = $type return smell")
	List<Smell> findSmells(@Param("level") String level, @Param("type") String type);

	@Query("match (smell:Smell) -[:" + RelationType.str_CONTAIN + "]-(node)" +
			" where smell.level = $level and smell.type = $type return distinct count(node)")
	Long findSmellsCount(@Param("level") String level, @Param("type") String type);

	@Query("match p = (smell:Smell) where smell.type = $type return smell")
	List<Smell> findSmellsByType(@Param("type") String type);

	@Query("match p = (smell:Smell) where smell.projectId = $projectId and smell.name = $name return distinct smell")
	Smell findProjectSmellsByName(@Param("projectId") Long projectId, @Param("name") String name);

	@Query("match p = (smell:Smell) where smell.projectId = $projectId and smell.type = $smellType and smell.level = $smellLevel return distinct smell")
	List<Smell> findSmells(@Param("projectId") Long projectId, @Param("smellType") String smellType, @Param("smellLevel") String smellLevel);

	@Query("match p= (smell:Smell) where smell.type = $type return smell limit 10;")
	List<Smell> findSmellsByTypeWithLimit(@Param("type") String type);
	
	@Query("match (smell:Smell) where smell.name=$name return smell")
	Smell querySmell(@Param("name") String name);

	@Query("match (smell:Smell) -[:" + RelationType.str_CONTAIN + "]-(node) " +
			"where id(smell) = $smellId return distinct node;")
	Set<Node> findContainedNodesBySmellId(@Param("smellId") Long smellId);

	@Query("match (smell:Smell) -[:" + RelationType.str_RELATE_TO + "]-(node) " +
			"where id(smell) = $smellId return distinct node;")
	Set<Node> findRelateToNodesBySmellId(@Param("smellId") Long smellId);

	@Query("match (n:ProjectFile) where n.suffix=\".java\" set n.language = \"java\";")
	void setJavaLanguageBySuffix();

	@Query("match (n:ProjectFile) where n.suffix<>\".java\" set n.language = \"cpp\";")
	void setCppLanguageBySuffix();

	@Query("match p = (:Smell)-[r:CONTAIN]-() delete r;")
	void deleteSmellContainRelations();

	@Query("match p = (smell:Smell)-[r:CONTAIN]-() where smell.type = $smellType delete r;")
	void deleteSmellContainRelations(@Param("smellType") String smellType);

	@Query("match p = (smell:Smell)-[r:RELATE_TO]-() where smell.type = $smellType delete r;")
	void deleteSmellRelateToRelations(@Param("smellType") String smellType);

	@Query("match p = (smell:Smell)-[r:" + RelationType.str_HAS + "] -> (m:Metric) where smell.type = $smellType delete r, m;")
	void deleteSmellMetric(@Param("smellType") String smellType);

	@Query("match (n:Smell) delete n;")
	void deleteSmells();

	@Query("match (n:Smell) where n.type = $smellType delete n;")
	void deleteSmells(@Param("smellType") String smellType);

	@Query("MATCH p=(smell:Smell)-[:" + RelationType.str_HAS + "]->(m:Metric) where id(smell) = $smellId RETURN m")
	Metric findSmellMetric(@Param("smellId") long smellId);

	@Query("MATCH p=(:Smell)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m")
	List<Metric> findSmellMetric();

	@Query("MATCH p=(:Smell)-[:" + RelationType.str_HAS + "]->(m:Metric) RETURN m limit 10;")
	List<Metric> findSmellMetricWithLimit();

	@Query("MATCH p=(:Smell)-[r:" + RelationType.str_HAS + "]->(m:Metric) delete r, m;")
	void deleteAllSmellMetric();

	@Query("CALL gds.wcc.stream({" +
			"nodeProjection: 'ProjectFile', " +
			"relationshipProjection: '" + RelationType.str_CLONE + "'}) " +
			"YIELD nodeId, componentId " +
			"with componentId as setId, collect(gds.util.asNode(nodeId)) AS files " +
			"where size(files) > 1 " +
			"match (file:ProjectFile) where file in files set file.cloneGroupId = \"file_group_\" + setId;")
	void setFileGroup();

	@Query("match (file:ProjectFile) " +
			"where file.cloneGroupId is not null " +
			"with file.cloneGroupId as cloneGroupId, count(file) as count " +
			"with cloneGroupId " +
			"create (:CloneGroup{name: cloneGroupId, cloneLevel: \"file\", entityId: -1});\n")
	void createSmellRelations();

	@Query("MATCH (n:Smell) with n match (file:ProjectFile) " +
			"where file.cloneGroupId = n.name " +
			"create (n)-[:CONTAIN]->(file);\n")
	void createSmellContainRelations();

	@Query("MATCH (n:Smell) with n set n.size = size((n)-[:CONTAIN]->());\n")
	void setSmellContainSize();

	@Query("MATCH (n:Smell)-[:CONTAIN]->(file:ProjectFile) where n.language is null with n, file set n.language = file.language;\n")
	void setSmellLanguage();

	@Query("match (cloneGroup:CloneGroup) " +
			"create (:Smell{name: cloneGroup.name, size: cloneGroup.size,language: cloneGroup.language,level: cloneGroup.cloneLevel," +
			"type:'" + SmellType.CLONE + "',entityId: -1});\n")
	void createCloneSmells();

	@Query("MATCH (smell:Smell) with smell " +
			"match (cloneGroup:CloneGroup)-[:" + RelationType.str_CONTAIN + "]-(code) " +
			"where cloneGroup.name = smell.name " +
			"create (smell)-[:" + RelationType.str_CONTAIN + "]->(code);\n")
	void createCloneSmellContains();

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(code)<-[:" +
			RelationType.str_CONTAIN + "*]-(p:Project) " +
			"where smell.type = '" + SmellType.CLONE + "' " +
			"with smell, " +
			"  reduce(tmp = '', pj in collect(distinct p) | tmp + (pj.name + '_'))  as name, " +
			"  head(collect(distinct p))  as pj " +
			"set smell += {projectId : id(pj), projectName : name};\n")
	void setCloneSmellProject();

	/**
	 * 判断是否存在co-change关系
	 */
	@Query("match (n:Smell) return n limit 10")
	List<Smell> findSmellWithLimit();

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" + RelationType.str_CONTAIN  + "]-(package:Package) " +
			"WITH smell, count(distinct package) as nop, count(distinct file) as nof, sum(file.noc) as noc, sum(file.nom) as nom, sum(file.loc) as loc " +
			"RETURN  smell,nop,nof,noc,nom,loc;")
	List<SmellMetric.StructureMetric> calculateSmellStructureMetricInFileLevel();

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" + RelationType.str_CONTAIN  + "]-(package:Package) " +
			"where id(smell)= $smellId " +
			"WITH smell,count(distinct package) as nop, count(distinct file) as nof, sum(file.noc) as noc, sum(file.nom) as nom, sum(file.loc) as loc " +
			"RETURN  smell,nop,nof,noc,nom,loc;")
	SmellMetric.StructureMetric calculateSmellStructureMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[r:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)<-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]-(d:Developer) " +
			"where id(smell)= $smellId " +
			"WITH smell, count(distinct c) as commits, count(c) as totalCommits, count(distinct d) as developers, count(d) as totalDevelopers, " +
			"  sum(r.addLines) as addLines, sum(r.subLines) as subLines " +
			"RETURN  smell,commits,totalCommits,developers,totalDevelopers,addLines,subLines;")
	SmellMetric.EvolutionMetric calculateSmellEvolutionMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file2:ProjectFile)<- [:" +
			RelationType.str_CONTAIN + "]-(smell) " +
			"where id(smell)= $smellId and id(file1) < id(file2) and size((file1)-[:" + RelationType.str_CO_CHANGE +"]-(file2)) > 0 " +
			"WITH smell, count(distinct c) as coChangeCommits,count(c) as totalCoChangeCommits,collect(distinct file1) as coFiles1, collect(distinct file2) as coFiles2 " +
			"WITH smell, coChangeCommits, totalCoChangeCommits, reduce(tmp=size(coFiles1), file in coFiles2 | tmp + (case when file in coFiles1 then 0 else 1 end)) as coChangeFiles " +
			"RETURN  smell,coChangeCommits,totalCoChangeCommits,coChangeFiles;")
	SmellMetric.CoChangeMetric calculateSmellCoChangeMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file2:ProjectFile)<- [:" +
			RelationType.str_RELATE_TO + "]-(smell) " +
			"where id(smell)= $smellId and id(file1) <> id(file2) and size((file1)-[:" + RelationType.str_CO_CHANGE +"]-(file2)) > 0 " +
			"WITH smell, count(distinct c) as coChangeCommits,count(c) as totalCoChangeCommits,count(distinct file2) as coChangeFiles " +
			"RETURN  smell,coChangeCommits,totalCoChangeCommits,coChangeFiles;")
	SmellMetric.CoChangeMetric calculateSmellRelateToNodeCoChangeMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
			"where id(smell)= $smellId " +
			"WITH smell, collect(distinct issue) as issueList " +
			"with smell, size(issueList) as issues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when '" + IssueType.BUG + "' then 1 else 0 end)) as bugIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when '" + IssueType.NEW_FEATURE + "' then 1 else 0 end)) as newFeatureIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when '" + IssueType.IMPROVEMENT + "' then 1 else 0 end)) as improvementIssues " +
			"RETURN  smell,issues,bugIssues,newFeatureIssues,improvementIssues;")
	SmellMetric.DebtMetric calculateSmellDebtMetricInFileLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(package:Package) " +
			"WITH smell, count(distinct package) as nop, sum(package.nof) as nof, sum(package.noc) as noc, sum(package.nom) as nom, sum(package.loc) as loc " +
			"RETURN  smell,nop,nof,noc,nom,loc;")
	List<SmellMetric.StructureMetric> calculateSmellStructureMetricInPackageLevel();

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(package:Package) " +
			"where id(smell)= $smellId " +
			"WITH smell,count(distinct package) as nop, sum(package.nof) as nof, sum(package.noc) as noc, sum(package.nom) as nom, sum(package.loc) as loc " +
			"RETURN  smell,nop,nof,noc,nom,loc;")
	SmellMetric.StructureMetric calculateSmellStructureMetricInPackageLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[r:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)<-[:" + RelationType.str_DEVELOPER_SUBMIT_COMMIT + "]-(d:Developer) " +
			"where id(smell)= $smellId " +
			"WITH smell, count(distinct c) as commits, count(c) as totalCommits, count(distinct d) as developers, count(d) as totalDevelopers, " +
			"  sum(r.addLines) as addLines, sum(r.subLines) as subLines " +
			"RETURN  smell,commits,totalCommits,developers,totalDevelopers,addLines,subLines;")
	SmellMetric.EvolutionMetric calculateSmellEvolutionMetricInPackageLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "*2]->(file1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file2:ProjectFile)<- [:" +
			RelationType.str_CONTAIN + "*2]-(smell) " +
			"where id(smell)= $smellId and id(file1) < id(file2) and size((file1)-[:" + RelationType.str_CO_CHANGE +"]-(file2)) > 0 " +
			"WITH smell, count(distinct c) as coChangeCommits,count(c) as totalCoChangeCommits,collect(distinct file1) as coFiles1, collect(distinct file2) as coFiles2 " +
			"WITH smell, coChangeCommits, totalCoChangeCommits, reduce(tmp=size(coFiles1), file in coFiles2 | tmp + (case when file in coFiles1 then 0 else 1 end)) as coChangeFiles " +
			"RETURN  smell,coChangeCommits,totalCoChangeCommits,coChangeFiles;")
	SmellMetric.CoChangeMetric calculateSmellCoChangeMetricInPackageLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "*2]->(file1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(file2:ProjectFile)<-[:" + RelationType.str_CONTAIN + "]-(:Package)<-[:" +
			RelationType.str_RELATE_TO + "]-(smell) " +
			"where id(smell)= $smellId and id(file1) <> id(file2) and size((file1)-[:" + RelationType.str_CO_CHANGE +"]-(file2)) > 0 " +
			"WITH smell, count(distinct c) as coChangeCommits,count(c) as totalCoChangeCommits,collect(distinct file1) as coFiles1, collect(distinct file2) as coFiles2 " +
			"WITH smell, coChangeCommits, totalCoChangeCommits, reduce(tmp=size(coFiles1), file in coFiles2 | tmp + (case when file in coFiles1 then 0 else 1 end)) as coChangeFiles " +
			"RETURN  smell,coChangeCommits,totalCoChangeCommits,coChangeFiles;")
	SmellMetric.CoChangeMetric calculateSmellRelateToNodeCoChangeMetricInPackageLevel(@Param("smellId") long smellId);

	@Query("MATCH (smell:Smell)-[:" + RelationType.str_CONTAIN + "*2]->(file:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
			"]-(c:Commit)-[:" + RelationType.str_COMMIT_ADDRESS_ISSUE + "]->(issue:Issue) " +
			"where id(smell)= $smellId " +
			"WITH smell, collect(distinct issue) as issueList " +
			"with smell, size(issueList) as issues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when '" + IssueType.BUG + "' then 1 else 0 end)) as bugIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when '" + IssueType.NEW_FEATURE + "' then 1 else 0 end)) as newFeatureIssues," +
			"     reduce(tmp = 0, isu in issueList | tmp + (case isu.type when '" + IssueType.IMPROVEMENT + "' then 1 else 0 end)) as improvementIssues " +
			"RETURN  smell,issues,bugIssues,newFeatureIssues,improvementIssues;")
	SmellMetric.DebtMetric calculateSmellDebtMetricInPackageLevel(@Param("smellId") long smellId);

	@Query("match (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(coreFile:ProjectFile) " +
			"where id(smell) = $smellId " +
			"with smell, coreFile " +
			"match (smell:Smell)-[:" + RelationType.str_RELATE_TO + "]->(unusedIncludeFile:ProjectFile) " +
			"with coreFile, collect(distinct unusedIncludeFile) as unusedIncludeFiles " +
			"return coreFile, unusedIncludeFiles;")
	UnusedInclude getUnusedIncludeWithSmellId(@Param("smellId") long smellId);

	@Query("match (smell:Smell)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) where id(file) = $fileId and smell.level = '" + SmellLevel.FILE + "' and smell.type = '" + SmellType.CYCLIC_DEPENDENCY + "' return smell;")
	List<Smell> getSmellsWithFileId(@Param("fileId") Long fileId);

	@Query("match (smell:Smell) where smell.coreNodeId = $fileId and smell.level = '" + SmellLevel.FILE + "' and smell.type = '" + SmellType.UNUSED_INCLUDE + "' return smell;")
	Smell getSmellWithCoreFileId(@Param("fileId") Long fileId);
}