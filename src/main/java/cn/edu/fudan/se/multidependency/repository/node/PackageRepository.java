package cn.edu.fudan.se.multidependency.repository.node;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.service.query.metric.PackageMetric;

@Repository
public interface PackageRepository extends Neo4jRepository<Package, Long> {

	@Query("match (p:Package) where p.directoryPath=$directoryPath and p.language = $language return p;")
	public Package queryPackage(@Param("directoryPath") String directoryPath, @Param("language") String language);

	@Query("match (p:Package) return p;")
	public List<Package> queryAllPackage();

	@Query("match (p:Package) where id(p) = $pckId return p;")
	public Package findPackageById(@Param("pckId") long pckId);

	@Query("MATCH (n:Package)-[:" + RelationType.str_CONTAIN + "*]->(p:Package) where id(n)=$pckId RETURN p;")
	public List<Package> findAllChildPackagesById(@Param("pckId") long pckId);

	@Query("MATCH (n:Package)-[:" + RelationType.str_CONTAIN + "]->(p:Package) where id(n)=$pckId RETURN p;")
	public List<Package> findOneStepPackagesById(@Param("pckId") long pckId);

	@Query("match (project:Project)-[:" + RelationType.str_CONTAIN + "]->(package:Package) where id(project) = $projectId return package;")
	public List<Package> getPackageByProjectId(@Param("projectId") long projectId);

	@Query("match (p1:Package)-[:CONTAIN]->(f1:ProjectFile)-[:" + RelationType.str_CO_CHANGE + "]-(f2:ProjectFile)<-[:CONTAIN]-(p2:Package) where id(p1) = $package1Id and id(p2) = $package2Id return count(distinct f1) + count(distinct f2);")
	public int getCoChangeFileNumberByPackagesId(@Param("package1Id") long package1Id, @Param("package2Id") long package2Id);

	@Query("match (p1:Package)-[:CONTAIN]->(f1:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]-(f2:ProjectFile)<-[:CONTAIN]-(p2:Package) where id(p1) = $package1Id and id(p2) = $package2Id return count(distinct f1) + count(distinct f2);")
	public int getDependOnFileNumberByPackagesId(@Param("package1Id") long package1Id, @Param("package2Id") long package2Id);

	@Query("MATCH (pck:Package) where pck.lines > 0\r\n" + 
			"WITH pck,pck.nof as nof, \r\n" +
			"     pck.noc as noc, \r\n" +
			"     pck.nom as nom, \r\n" +
			"     size((pck)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" + 
			"     size((pck)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" + 
			"     pck.loc as loc,\r\n" + 
			"     pck.lines as lines\r\n " +
			"RETURN pck, nof,noc,nom,fanIn, fanOut, loc,lines order by(pck.directoryPath) desc;")
	public List<PackageMetric> calculatePackageMetrics();

	@Query("MATCH (pck:Package) where id(pck) = $packageId\r\n" +
			"WITH pck, pck.nof as nof, \r\n" +
			"     pck.noc as noc, \r\n" +
			"     pck.nom as nom, \r\n" +
			"     size((pck)-[:" + RelationType.str_DEPENDS_ON + "]->()) as fanOut, \r\n" +
			"     size((pck)<-[:" + RelationType.str_DEPENDS_ON + "]-()) as fanIn,\r\n" +
			"     pck.loc as loc,\r\n" +
			"     pck.lines as lines\r\n " +
			"RETURN pck, nof,noc,nom,fanIn, fanOut, loc,lines order by(pck.directoryPath) desc;")
	public PackageMetric calculatePackageMetrics(@Param("packageId") long packageId);
	
	/*@Query("match (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) with pck, sum(file.loc) as loc set pck.loc = loc;")
	public void setPackageLoc();
	
	@Query("match (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) with pck, sum(file.endLine) as lines set pck.lines = lines;")
	public void setPackageLines();*/

	@Query("MATCH (package:Package)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile)\r\n" +
			"WITH package, count(distinct file) as nof, " +
			"     sum(file.noc) as noc, " +
			"     sum(file.nom) as nom, " +
			"     sum(file.loc) as loc, " +
			"     sum(file.endLine) as lines\r\n" +
			"SET package += {nof: nof, noc: noc, nom: nom, loc: loc, lines: lines};")
	public void setPackageMetrics();

	@Query("match (pck:Package) where not (pck)-[:" + RelationType.str_CONTAIN + "]->(:ProjectFile) " +
			"set pck += {nof: 0, noc: 0, nom: 0, loc: 0, lines: 0};")
	public void setEmptyPackageMetrics();

	@Query("match (p1:Package)-[:DEPENDS_ON]->(p2:Package) where id(p2)=$packageId return count(distinct p1)")
	int getFanIn(@Param("packageId") long packageId);

	@Query("match (p1:Package)-[:DEPENDS_ON]->(p2:Package) where id(p1)=$packageId return count(distinct p2)")
	int getFanOut(@Param("packageId") long packageId);

	@Query("MATCH (project:Project)-[:" + RelationType.str_CONTAIN + "]->(package:Package)-[:" + RelationType.str_HAS + "]->(metric:Metric) " +
			"where id(project) = $projectId " +
			"with distinct metric, metric.`metricValues.FanIn` as fanIn " +
			"where fanIn > 0 " +
			"RETURN fanIn order by fanIn;")
	List<Integer> findPackageFanInByProjectId(@Param("projectId") Long projectId);

	@Query("MATCH (project:Project)-[:" + RelationType.str_CONTAIN + "]->(package:Package)-[:" + RelationType.str_HAS + "]->(metric:Metric) " +
			"where id(project) = $projectId " +
			"with distinct metric, metric.`metricValues.FanOut` as fanOut " +
			"where fanOut > 0 " +
			"RETURN fanOut order by fanOut;")
	List<Integer> findPackageFanOutByProjectId(@Param("projectId") Long projectId);

	@Query("MATCH (package:Package) where id(package) = $packageId return package.fanOut;")
	Integer getPackageFanOutByFileId(@Param("packageId") Long packageId);

	@Query("MATCH (package:Package) where package.depth = 1 return package;")
	List<Package> findPackagesAtDepth1();
}
