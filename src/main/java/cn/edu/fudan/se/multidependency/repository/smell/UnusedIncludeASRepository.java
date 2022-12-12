package cn.edu.fudan.se.multidependency.repository.smell;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UnusedIncludeASRepository extends Neo4jRepository<ProjectFile, Long> {

	@Query("MATCH (file1:ProjectFile)-[:" + RelationType.str_INCLUDE + "]->(file2:ProjectFile),(file1)-[r:DEPENDS_ON]->(file2) " +
			"where file1.suffix = $suffix and r.times - r.`dependsOnTypes.INCLUDE` <= 0 " +
			"with file1 as coreFile, collect(distinct file2) as unusedIncludeFiles " +
			"return coreFile, unusedIncludeFiles;")
	public Set<UnusedInclude> findUnusedIncludeWithSuffix(@Param("suffix") String suffix);

	@Query("MATCH (file1:ProjectFile)-[:" + RelationType.str_INCLUDE + "]->(file2:ProjectFile) where id(file2) = $fileId return collect(distinct file1);")
	public Set<ProjectFile> findFileByHeadFileId(@Param("fileId") Long fileId);

	@Query("MATCH (file1:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(file2:ProjectFile) where id(file1) = $file1Id and id(file2) = $file2Id " +
			"with r.times as allTimes, r.`dependsOnTypes.INCLUDE` as includeTimes " +
			"return (case includeTimes when null then allTimes else (allTimes-includeTimes) end) > 0;")
	public Boolean isUsedFile(@Param("file1Id") Long file1Id, @Param("file2Id") Long file2Id);
}
