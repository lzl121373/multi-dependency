package cn.edu.fudan.se.multidependency.repository.smell;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface CycleASRepository extends Neo4jRepository<ProjectFile, Long> {

	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (p:Package) return id(p) as id\', " +
			"relationshipQuery: \'MATCH p=(p1:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(p2:Package) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(p1) AS source, id(p2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS components " +
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<Cycle<Package>> packageCycles();
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (p:Module) return id(p) as id\', " +
			"relationshipQuery: \'MATCH p=(p1:Module)-[r:" + RelationType.str_DEPENDS_ON + "]->(p2:Module) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(p1) AS source, id(p2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS components " +
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<Cycle<Module>> moduleCycles();
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (f:ProjectFile) return id(f) as id\', " +
			"relationshipQuery: \'MATCH p=(f1:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(f2:ProjectFile) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(f1) AS source, id(f2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS components " +
			"where size(components) >= 2 " + 
			"return partition, components " + 
			"ORDER BY size(components) DESC")
	public List<Cycle<ProjectFile>> fileCycles();

	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (t:Type) return id(t) as id\', " +
			"relationshipQuery: \'MATCH p=(t1:Type)-[r:" + RelationType.str_DEPENDS_ON + "]->(t2:Type) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(t1) AS source, id(t2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS components " +
			"where size(components) >= 2 " +
			"return partition, components " +
			"ORDER BY size(components) DESC")
	public List<Cycle<Type>> typeCycles();
	
	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (p:Package) return id(p) as id\', " +
			"relationshipQuery: \'MATCH p=(p1:Package)-[r:" + RelationType.str_DEPENDS_ON + "]->(p2:Package) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(p1) AS source, id(p2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS packages " +
			"match result=(a:Package)-[:" + RelationType.str_DEPENDS_ON + "]->(b:Package) "
					+ "where partition = $partition and a in packages and b in packages return result")
	public List<DependsOn> cyclePackagesBySCC(@Param("partition") int partition);

	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (p:Module) return id(p) as id\', " +
			"relationshipQuery: \'MATCH p=(p1:Module)-[r:" + RelationType.str_DEPENDS_ON + "]->(p2:Module) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(p1) AS source, id(p2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS modules " +
			"match result=(a:Module)-[:" + RelationType.str_DEPENDS_ON + "]->(b:Module) "
			+ "where partition = $partition and a in modules and b in modules return result")
	public List<DependsOn> cycleModulesBySCC(@Param("partition") int partition);

	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (f:ProjectFile) return id(f) as id\', " +
			"relationshipQuery: \'MATCH p=(f1:ProjectFile)-[r:" + RelationType.str_DEPENDS_ON + "]->(f2:ProjectFile) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(f1) AS source, id(f2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS files " +
			"match result=(a:ProjectFile)-[:" + RelationType.str_DEPENDS_ON + "]->(b:ProjectFile) "
					+ "where partition = $partition and a in files and b in files return result")
	public List<DependsOn> cycleFilesBySCC(@Param("partition") int partition);

	@Query("CALL gds.alpha.scc.stream({" +
			"nodeQuery:\'match (t:Type) return id(t) as id\', " +
			"relationshipQuery: \'MATCH p=(t1:Type)-[r:" + RelationType.str_DEPENDS_ON + "]->(t2:Type) " +
			"where (r.dependsOnType contains \\\'" + RelationType.str_EXTENDS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_INCLUDE + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_IMPLEMENTS_C + "\\\' or " +
			"       r.dependsOnType contains \\\'" + RelationType.str_CALL + "\\\') " +
			"RETURN id(t1) AS source, id(t2) AS target\' }) " +
			"YIELD nodeId, componentId " +
			"with componentId as partition, collect(gds.util.asNode(nodeId)) AS types " +
			"match result=(a:Type)-[:" + RelationType.str_DEPENDS_ON + "]->(b:Type) "
			+ "where partition = $partition and a in types and b in types return result")
	public List<DependsOn> cycleTypesBySCC(@Param("partition") int partition);
}
