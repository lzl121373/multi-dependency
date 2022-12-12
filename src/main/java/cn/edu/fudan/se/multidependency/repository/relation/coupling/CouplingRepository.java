package cn.edu.fudan.se.multidependency.repository.relation.coupling;

import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.Coupling;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CouplingRepository extends Neo4jRepository<Coupling, Long> {
    @Query("match (f1:ProjectFile)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]-" +
            "(:Function)-[:" + RelationType.str_CALL + "]->(m2:Function)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]" +
            "-(f2:ProjectFile) where id(f1)=$file1Id and id(f2)=$file2Id return count(distinct m2);")
    int queryTwoFilesDependsByFunctionsNum(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match (f1:ProjectFile)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]-" +
            "(m1:Function)-[:" + RelationType.str_CALL + "]->(m2:Function)-[:" + RelationType.str_CONTAIN + "]-(:Type)-[:" + RelationType.str_CONTAIN + "]" +
            "-(f2:ProjectFile) where id(f1)=$file1Id and id(f2)=$file2Id return count(distinct m1);")
    int queryTwoFilesDependsOnFunctionsNum(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);

    @Query("match p=(:ProjectFile)-[:" + RelationType.str_COUPLING + "]->(:ProjectFile) return p limit 10;")
    List<Coupling> findFileDependsWithLimit();

    @Query("match p=(f1:ProjectFile)-[r:" + RelationType.str_COUPLING + "]-(f2:ProjectFile) where id(f1)=$file1Id and id(f2)=$file2Id return p;")
    Coupling queryCouplingBetweenTwoFiles(@Param("file1Id") long file1Id, @Param("file2Id") long file2Id);
}
