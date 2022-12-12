package cn.edu.fudan.se.multidependency.repository.relation.git;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CommitUpdateFileRepository extends Neo4jRepository<CommitUpdateFile, Long> {

    @Query("match p=(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f:ProjectFile) where id(c)=$cId return f")
    List<ProjectFile> findUpdatedFilesByCommitId(@Param("cId") long commitId);
    
    @Query("match p=(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile) where id(c)=$id return p")
    List<CommitUpdateFile> findCommitUpdatedFiles(@Param("id") long commitId);
    
    @Query("match (c:Commit) with c, size((c)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(:ProjectFile)) as size set c.commitFilesSize = size return count(c);")
    int setCommitFilesSize();

    @Query("match (commit:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f:ProjectFile)" +
            "<-[:" + RelationType.str_CONTAIN + "]-(p:Package)" +
            " where id(p)=$packageId  and (commit.merge=false or commit.merge is null)" +
            " return distinct commit;")
    Set<Commit> findCommitInPackageByPackageId(@Param("packageId") long packageId);

    @Query("match (p1:Package)-[:" + RelationType.str_CONTAIN + "]->(f1:ProjectFile)<-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]-(c:Commit)-[:" + RelationType.str_COMMIT_UPDATE_FILE + "]->(f2:ProjectFile)<-[:" + RelationType.str_CONTAIN + "]-(p2:Package) " +
            "where id(p1)=$package1Id and id(p2)=$package2Id " +
            "return distinct c")
    List<Commit> findCommitBetweenPackagesByPackageId(@Param("package1Id") long package1Id, @Param("package2Id") long package2Id);
}
