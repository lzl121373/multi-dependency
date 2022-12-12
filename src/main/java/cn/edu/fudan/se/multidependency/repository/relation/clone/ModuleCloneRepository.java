package cn.edu.fudan.se.multidependency.repository.relation.clone;

import org.springframework.data.repository.query.Param;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;

import java.util.List;

@Repository
public interface ModuleCloneRepository extends Neo4jRepository<ModuleClone, Long> {
    @Query("match p=()-[:" + RelationType.str_MODULE_CLONE + "]->() return count(p);")
    int getNumberOfModuleClone();

    @Query("match (p1:Package) where id(p1) = $pck1Id \r\n" +
            "OPTIONAL MATCH (p2:Package) where id(p2) = $pck2Id \r\n" +
            "create (p1)-[:" + RelationType.str_MODULE_CLONE + "{clonePairs:$clonePairs, cloneNodesCount1:$cloneNodesCount1, cloneNodesCount2:$cloneNodesCount2, allNodesCount1:$allNodesCount1, allNodesCount2:$allNodesCount2, cloneNodesLoc1:$cloneNodesLoc1, cloneNodesLoc2:$cloneNodesLoc2, allNodesLoc1:$allNodesLoc1, allNodesLoc2:$allNodesLoc2, cloneType1Count:$cloneType1Count, cloneType2Count:$cloneType2Count, cloneType3Count:$cloneType3Count, cloneSimilarityValue:$cloneSimilarityValue}]->(p2)")
    List<ModuleClone> createModuleClone(@Param("pck1Id") long pck1Id, @Param("pck2Id") long pck2Id, @Param("clonePairs") int clonePairs, @Param("cloneNodesCount1") int cloneNodesCount1, @Param("cloneNodesCount2") int cloneNodesCount2, @Param("allNodesCount1") int allNodesCount1, @Param("allNodesCount2") int allNodesCount2, @Param("cloneNodesLoc1") int cloneNodesLoc1, @Param("cloneNodesLoc2") int cloneNodesLoc2, @Param("allNodesLoc1") int allNodesLoc1, @Param("allNodesLoc2") int allNodesLoc2, @Param("cloneType1Count") int cloneType1Count, @Param("cloneType2Count") int cloneType2Count, @Param("cloneType3Count") int cloneType3Count, @Param("cloneSimilarityValue") double cloneSimilarityValue);

    @Query("match p= (p1:Package)-[:" + RelationType.str_MODULE_CLONE + "]->(p2:Package) where id(p1)=$pck1Id and id(p2)=$pck2Id return p;")
    ModuleClone findModuleClone(@Param("pck1Id") long pck1Id, @Param("pck2Id") long pck2Id);

    @Query("match p= ()-[:" + RelationType.str_MODULE_CLONE + "]->() return p;")
    List<ModuleClone> getAllModuleClone();

    @Query("match p= ()-[:" + RelationType.str_MODULE_CLONE + "]->() return p limit 10;")
    List<ModuleClone> getAllModuleCloneWithLimit();

    @Query("match p = (p1:Package) - [:" + RelationType.str_MODULE_CLONE + "] -> (p2:Package), " +
            "(p1) -[:CONTAIN] -> (f1:ProjectFile) <-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]- (c:Commit) - [:" + RelationType.str_COMMIT_UPDATE_FILE + "]-> (f2:ProjectFile) <- [:CONTAIN] - (p2) " +
            "where size( (f1) -[:CLONE] - (f2) ) > 0 " +
            "with p, count(distinct c) as cloneNodesCoChangeTimes " +
            "where cloneNodesCoChangeTimes >= $minCoChangeTimes " +
            "foreach (r in relationships(p)  |  set r.cloneNodesCoChangeTimes = cloneNodesCoChangeTimes ) " +
            "with p return p;")
    List<ModuleClone> setCloneNodesCoChangeTimes(@Param("minCoChangeTimes") int minCoChangeTimes);
}
