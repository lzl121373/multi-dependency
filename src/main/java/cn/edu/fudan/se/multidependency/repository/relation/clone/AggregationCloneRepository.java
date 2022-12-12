package cn.edu.fudan.se.multidependency.repository.relation.clone;

import org.springframework.data.repository.query.Param;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.AggregationClone;

import java.util.List;

@Repository
public interface AggregationCloneRepository extends Neo4jRepository<AggregationClone, Long> {
    @Query("match p=()-[:" + RelationType.str_AGGREGATION_CLONE + "]->() return count(p)")
    int getNumberOfAggregationClone();

    @Query("match (p1:Package) where id(p1) = $pck1Id \r\n" +
            "OPTIONAL MATCH (p2:Package) where id(p2) = $pck2Id \r\n" +
            "create (p1)-[:" + RelationType.str_AGGREGATION_CLONE + "{parent1Id:$parent1Id, parent2Id:$parent2Id, clonePairs:$clonePairs, cloneNodesCount1:$cloneNodesCount1, cloneNodesCount2:$cloneNodesCount2, allNodesCount1:$allNodesCount1, allNodesCount2:$allNodesCount2, cloneNodesLoc1:$cloneNodesLoc1, cloneNodesLoc2:$cloneNodesLoc2, allNodesLoc1:$allNodesLoc1, allNodesLoc2:$allNodesLoc2, cloneType1Count:$cloneType1Count, cloneType2Count:$cloneType2Count, cloneType3Count:$cloneType3Count, cloneSimilarityValue:$cloneSimilarityValue}]->(p2)")
    void createAggregationClone(@Param("pck1Id") long pck1Id, @Param("pck2Id") long pck2Id, @Param("parent1Id") long parent1Id, @Param("parent2Id") long parent2Id, @Param("clonePairs") int clonePairs, @Param("cloneNodesCount1") int cloneNodesCount1, @Param("cloneNodesCount2") int cloneNodesCount2, @Param("allNodesCount1") int allNodesCount1, @Param("allNodesCount2") int allNodesCount2, @Param("cloneNodesLoc1") int cloneNodesLoc1, @Param("cloneNodesLoc2") int cloneNodesLoc2, @Param("allNodesLoc1") int allNodesLoc1, @Param("allNodesLoc2") int allNodesLoc2, @Param("cloneType1Count") int cloneType1Count, @Param("cloneType2Count") int cloneType2Count, @Param("cloneType3Count") int cloneType3Count, @Param("cloneSimilarityValue") double cloneSimilarityValue);

    @Query("match p= (p1:Package)-[r:" + RelationType.str_AGGREGATION_CLONE + "]->(p2:Package) where r.parent1Id=$parent1Id and r.parent2Id=$parent2Id and p1.language=$language and p2.language=$language return p")
    List<AggregationClone> findAggregationCloneByParentId(@Param("parent1Id") long parent1Id, @Param("parent2Id") long parent2Id, @Param("language") String language);

    @Query("match p= (p1:Package)-[:" + RelationType.str_AGGREGATION_CLONE + "]->(p2:Package) where id(p1) = $pck1Id and id(p2) = $pck2Id return p")
    AggregationClone findAggregationCloneByPackageId(@Param("pck1Id") long pck1Id, @Param("pck2Id") long pck2Id);

    @Query("match p= ()-[:" + RelationType.str_AGGREGATION_CLONE + "]->() return p")
    List<AggregationClone> getAllAggregationClone();
}
