package cn.edu.fudan.se.multidependency.repository.relation.git;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Package;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;


@Repository
public interface CoChangeRepository extends Neo4jRepository<CoChange, Long> {

	/**
	 * 找出cochange次数至少为count的cochange关系
	 * @param count
	 * @return
	 */
    @Query("match p=(:ProjectFile)-[r:" + RelationType.str_CO_CHANGE + "]->() where r.times >= $count return p")
    List<CoChange> findGreaterThanCountCoChanges(@Param("count") int count);

    @Query("MATCH (project:Project) " +
            "where id(project) = $projectId " +
            "optional match p=(file1:ProjectFile)-[coChange:" + RelationType.str_CO_CHANGE + "]->(file2:ProjectFile) " +
            "where (project)-[:" + RelationType.str_CONTAIN + "*2]->(file1) " +
            "and (project)-[:" + RelationType.str_CONTAIN + "*2]->(file2) " +
            "and coChange.times >= $count " +
            "return distinct p")
    List<CoChange> findProjectFileCoChangeGreaterThanCount(@Param("projectId") long projectId, @Param("count") int count);

    @Query("MATCH (project:Project) " +
            "where id(project) = $projectId " +
            "optional match p=(package1:Package)-[coChange:" + RelationType.str_CO_CHANGE + "]->(package2:Package) " +
            "where (project)-[:" + RelationType.str_CONTAIN + "]->(package1) " +
            "and (project)-[:" + RelationType.str_CONTAIN + "]->(package2) " +
            "and coChange.times >= $count " +
            "return distinct p")
    List<CoChange> findProjectPackageCoChangeGreaterThanCount(@Param("projectId") long projectId, @Param("count") int count);
    
    /**
     * 找出两个指定文件的cochange关系
     * @param file1Id
     * @param file2Id
     * @return
     */
    @Query("match p=(f1:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]->(f2:ProjectFile) where id(f1)=$file1Id and id(f2)=$file2Id return p")
    CoChange findCoChangesBetweenTwoFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match p=(f1:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]-(f2:ProjectFile) where id(f1)=$file1Id and id(f2)=$file2Id return distinct p")
    List<CoChange> findCoChangesBetweenTwoFilesWithoutDirection(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match p=(p1:Package)-[:" + RelationType.str_CO_CHANGE + "]-(p2:Package) where id(p1)=$package1Id and id(p2)=$package2Id return distinct p")
    List<CoChange> findCoChangesBetweenTwoPackagesWithoutDirection(@Param("package1Id") long package1Id, @Param("package2Id") long package2Id);

    @Query("match (f1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
    		"]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f2:ProjectFile) " + 
    		"where id(f1) < id(f2) " + 
    		"and (c.merge=false or c.merge is null) " + 
    		"with f1,f2,count(distinct c) as times " +
    		"where times >= $minCoChangeTimes " +
    		"create p=(f1)-[:" + RelationType.str_CO_CHANGE + "{times:times}]->(f2);")
    void createCoChangesRemoveMerge(@Param("minCoChangeTimes") int minCoChangeTimes);

    @Query("match (f1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
    		"]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f2:ProjectFile) " + 
    		"where id(f1) < id(f2) and c.commitFilesSize <= 50  " +
    		"with f1,f2,count(distinct c) as times " +
    		"where times >= $minCoChangeTimes " +
    		"create (f1)-[:" + RelationType.str_CO_CHANGE + "{times:times}]->(f2);")
    void createCoChanges(@Param("minCoChangeTimes") int minCoChangeTimes);

    @Query("match (f1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE +
            "]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f2:ProjectFile) " +
            "where id(f1) < id(f2) and c.commitFilesSize <= 50  " +
            "with f1,f2,count(distinct c) as times " +
            "with f1,f2,times,size((:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f1)) as times1," +
            "     size((:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f2)) as times2 " +
            "with f1,f2,times,times1,times2, (times * 1.0)/times1 as conf1, (times * 1.0)/times2 as conf2 " +
            "where times >= $minCoChangeTimes and (conf1 >= $confidence or conf2 >= $confidence) " +
            "create (f1)-[:CO_CHANGE{times:times,node1ChangeTimes:times1,node2ChangeTimes:times2,node1Confidence:conf1,node2Confidence:conf2}]->(f2)")
    void createCoChanges(@Param("minCoChangeTimes") int minCoChangeTimes, @Param("confidence") double confidence);
    
    @Query("match (c1:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f1:ProjectFile)-[co:CO_CHANGE]-> " +
            "(f2:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c2:Commit) " +
            "with f1, f2, co, count(distinct c1) as times1, count(distinct c2) as times2 " +
            "set co.node1ChangeTimes = times1, co.node2ChangeTimes = times2;")
    void updateCoChangesForFile();

    @Query("match (p1:Package)-[:CONTAIN]->(f1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f2:ProjectFile)<-[:CONTAIN]-(p2:Package) " +
            "where id(p1) < id(p2)  " +
            "with p1, p2, count(distinct c) as moduleCoChangeTimes " +
            "where moduleCoChangeTimes >= $minCoChangeTimes " +
            "create (p1) - [:CO_CHANGE{times: moduleCoChangeTimes } ] -> (p2);")
    void createCoChangesForModule(@Param("minCoChangeTimes") int minCoChangeTimes);

    @Query("match (c1:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f1:ProjectFile)<-[:CONTAIN]-(p1:Package)-[co:CO_CHANGE]-> " +
            "(p2:Package)-[:CONTAIN]->(f2:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c2:Commit) " +
            "with p1, p2, co, count(distinct c1) as times1, count(distinct c2) as times2 " +
            "set co.node1ChangeTimes = times1, co.node2ChangeTimes = times2;")
    void updateCoChangesForModule();
    
    @Query("match p= (f:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]->(:ProjectFile) where id(f)=$fileId return p")
    List<CoChange> cochangesRight(@Param("fileId") long fileId);
    
    @Query("match p= (:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]->(f:ProjectFile) where id(f)=$fileId return p")
    List<CoChange> cochangesLeft(@Param("fileId") long fileId);

    /**
     * 判断是否存在co-change关系
     * @param
     * @return
     */
    @Query("match p = (:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]->(:ProjectFile) return p")
    List<CoChange> findFileCoChange();

    @Query("match p = ()-[:" + RelationType.str_CO_CHANGE + "]->() return p limit 10")
    List<CoChange> findCoChangesLimit();

    @Query("match p = (:Package)-[:" + RelationType.str_CO_CHANGE + "]->(:Package) return p")
    List<CoChange> findModuleCoChange();

    @Query("match p = (:Package)-[:" + RelationType.str_CO_CHANGE + "]->(:Package) return p limit 10")
    List<CoChange> findModuleCoChangeLimit();

    @Query("match p = (p1:Package)-[:" + RelationType.str_CO_CHANGE + "]-(p2:Package) " +
            "where id(p1) = $pckId1 and id(p2) = $pckId2 return p")
    CoChange findPackageCoChangeByPackageId(@Param("pckId1") long pckId1, @Param("pckId2") long pckId2);

    @Query("match p = (project:Project)-[:" + RelationType.str_CONTAIN + "*2]->(:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]->(:ProjectFile)<-[:" + RelationType.str_CONTAIN + "*2]-(project) where id(project)=$id return p")
    List<CoChange> findFileCoChangeInProject(@Param("id") long projectId);

    @Query("match p = (p1:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]->(p2:ProjectFile) where p1.projectBelongPath = $projectBelongPath and p1.language = $language and p2.projectBelongPath = $projectBelongPath and p2.language = $language return p")
    List<CoChange> findFileCoChangeInProjectByPathAndLanguage(@Param("projectBelongPath") String projectBelongPath, @Param("language") String language);

    @Query("match p = (project:Project)-[:" + RelationType.str_CONTAIN + "]->(:Package)-[:" + RelationType.str_CO_CHANGE + "]->(:Package)<-[:" + RelationType.str_CONTAIN + "]-(project) where id(project)=$id return p")
    List<CoChange> findPackageCoChangeInProject(@Param("id") long projectId);

    @Query("match (p:Package)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) where id(file) = $fileId return p")
    Package findFileBelongPackageByFileId(@Param("fileId") long fileId);

    @Query("MATCH (project:Project) " +
            "where id(project) = $projectId " +
            "optional match (file1:ProjectFile)-[coChange:" + RelationType.str_CO_CHANGE + "]->(file2:ProjectFile) " +
            "where (project)-[:" + RelationType.str_CONTAIN + "*2]->(file1) " +
            "and (project)-[:" + RelationType.str_CONTAIN + "*2]->(file2) " +
            "with distinct coChange, coChange.times as times " +
            "RETURN times order by times;")
    List<Integer> findFileCoChangeByProjectId(@Param("projectId") Long projectId);

    @Query("MATCH (project:Project) " +
            "where id(project) = $projectId " +
            "optional match (package1:Package)-[coChange:" + RelationType.str_CO_CHANGE + "]->(package2:Package) " +
            "where (project)-[:" + RelationType.str_CONTAIN + "]->(package1) " +
            "and (project)-[:" + RelationType.str_CONTAIN + "]->(package2) " +
            "with distinct coChange, coChange.times as times " +
            "RETURN times order by times;")
    List<Integer> findPackageCoChangeByProjectId(@Param("projectId") Long projectId);
}
