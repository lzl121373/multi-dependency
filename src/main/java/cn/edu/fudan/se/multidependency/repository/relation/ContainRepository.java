package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface ContainRepository extends Neo4jRepository<Contain, Long> {
	
	// belongto
	
	@Query("match (m:MicroService)-[:" + RelationType.str_CONTAIN + "]->(p:Project) where id(p)=$projectId return m")
	public MicroService findProjectBelongToMicroService(@Param("projectId") Long projectId);
	
	@Query("match (p:Project)-[:" + RelationType.str_CONTAIN + "]->(pck:Package) where id(pck)=$packageId return p")
	public Project findPackageBelongToProject(@Param("packageId") Long pckId);
	
	@Query("match (p:Package)-[:" + RelationType.str_CONTAIN + "]->(f:ProjectFile) where id(f)=$fileId return p")
	public Package findFileBelongToPackage(@Param("fileId") Long fileId);
	
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "*1..2]->(b:Type) where id(b)=$typeId return a")
	public ProjectFile findTypeBelongToFile(@Param("typeId") Long typeId);
	
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "*1..3]->(b:Function) where id(b)=$functionId return a")
	public ProjectFile findFunctionBelongToFile(@Param("functionId") Long functionId);
	
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "*1..4]->(b:Variable) where id(b)=$variableId return a")
	public ProjectFile findVariableBelongToFile(@Param("variableId") Long variableId);
	
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "]->(b:Snippet) where id(b)=$snippetId return a")
	public ProjectFile findSnippetDirectlyBelongToFile(@Param("snippetId") Long snippetId);
	
	@Query("match (a:Type)-[:" + RelationType.str_CONTAIN + "]->(b:Snippet) where id(b)=$snippetId return a")
	public Type findSnippetDirectlyBelongToType(@Param("snippetId") Long snippetId);
	
	@Query("match (a:Function)-[:" + RelationType.str_CONTAIN + "]->(b:Snippet) where id(b)=$snippetId return a")
	public Function findSnippetDirectlyBelongToFunction(@Param("snippetId") Long snippetId);
	
	@Query("match (a:Type)-[:" + RelationType.str_CONTAIN + "]->(b:Function) where id(b)=$functionId return a")
	public Type findFunctionBelongToType(@Param("functionId") Long functionId);
	
	@Query("match (a:Type)-[:" + RelationType.str_CONTAIN + "*1..2]->(b:Variable) where id(b)=$variableId return a")
	public Type findVariableBelongToType(@Param("variableId") Long variableId);
	
	@Query("match (lib:Library)-[:" + RelationType.str_CONTAIN + "]->(api:LibraryAPI) where id(api)=$libraryAPIId return lib")
	public Library findLibraryAPIBelongToLibrary(@Param("libraryAPIId") Long libraryAPIId);

	// contain
	
	@Query("MATCH (t:Trace{traceId:$traceId})-[:" + RelationType.str_CONTAIN + "]->(s:Span) RETURN s")
	public List<Span> findTraceContainSpansByTraceId(@Param("traceId") String traceId);
	
	@Query("MATCH (ms:MicroService)-[:" + RelationType.str_CONTAIN + "]->(api:RestfulAPI) where id(ms)=$id RETURN api")
	public List<RestfulAPI> findMicroServiceContainRestfulAPI(@Param("id") Long id);
	
	@Query("match (ms:MicroService)-[:" + RelationType.str_CONTAIN + "]->(p:Project) where id(ms)=$msId return p")
	public List<Project> findMicroServiceContainProjects(@Param("msId") Long msId);
	
	@Query("match p = (f1:Feature)-[:" + RelationType.str_CONTAIN + "]->(f2:Feature) return p")
	public List<Contain> findAllFeatureContainFeatures();
	
	@Query("match (a:Project)-[:" + RelationType.str_CONTAIN + "]->(b:Package) where id(a)=$projectId return b")
	public List<Package> findProjectContainPackages(@Param("projectId") Long projectId);
	@Query("match (a:Project)-[:" + RelationType.str_CONTAIN + "*2]->(b:ProjectFile) where id(a)=$projectId return b")
	public List<ProjectFile> findProjectContainFiles(@Param("projectId") Long projectId);
	@Query("match (a:Project)-[:" + RelationType.str_CONTAIN + "*3..5]->(b:Function) where id(a)=$projectId return b")
	public List<Function> findProjectContainFunctions(@Param("projectId") Long projectId);
	
	@Query("match (a:Package)-[:" + RelationType.str_CONTAIN + "]->(b:ProjectFile) where id(a)=$packageId return b")
	public List<ProjectFile> findPackageContainFiles(@Param("packageId") Long packageId);

	@Query("match (a:Package)-[:" + RelationType.str_CONTAIN + "*]->(b:ProjectFile) where id(a)=$packageId return b")
	public List<ProjectFile> findPackageContainAllFiles(@Param("packageId") Long packageId);

	@Query("match (a:Package)-[:" + RelationType.str_CONTAIN + "*]->(b:ProjectFile) where id(a)=$packageId return count(b);")
	public int findPackageContainAllFilesNum(@Param("packageId") Long packageId);

	@Query("match (a:Package)-[:" + RelationType.str_CONTAIN + "*]->(b:ProjectFile) where id(a)=$packageId return sum(b.loc);")
	public int findPackageContainAllFilesLOC(@Param("packageId") Long packageId);
	
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "]->(b:Namespace) where id(a)=$fileId return b")
	public List<Namespace> findFileDirectlyContainNamespaces(@Param("fileId") Long fileId);
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "]->(b:Type) where id(a)=$fileId return b")
	public List<Type> findFileDirectlyContainTypes(@Param("fileId") Long fileId);
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "*]->(b:Type) where id(a)=$fileId return b")
	public List<Type> findFileContainAllTypes(@Param("fileId") Long fileId);
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)=$fileId return b")
	public List<Function> findFileDirectlyContainFunctions(@Param("fileId") Long fileId);
	@Query("match (a:ProjectFile)-[:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)=$fileId return b")
	public List<Variable> findFileDirectlyContainVariables(@Param("fileId") Long fileId);

	@Query("match (a:Namespace)-[:" + RelationType.str_CONTAIN + "]->(b:Type) where id(a)=$namespaceId return b")
	public List<Type> findNamespaceDirectlyContainTypes(@Param("namespaceId") Long namespaceId);
	@Query("match (a:Namespace)-[:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)=$namespaceId return b")
	public List<Function> findNamespaceDirectlyContainFunctions(@Param("namespaceId") Long namespaceId);
	@Query("match (a:Namespace)-[:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)=$namespaceId return b")
	public List<Variable> findNamespaceDirectlyContainVariables(@Param("namespaceId") Long namespaceId);
	
	@Query("match (a:Type)-[:" + RelationType.str_CONTAIN + "]->(b:Function) where id(a)=$typeId return b")
	public List<Function> findTypeDirectlyContainFunctions(@Param("typeId") Long typeId);
	@Query("match (a:Type)-[:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)=$typeId return b")
	public List<Variable> findTypeDirectlyContainFields(@Param("typeId") Long typeId);
	
	@Query("match (a:Function)-[:" + RelationType.str_CONTAIN + "]->(b:Variable) where id(a)=$functionId return b")
	public List<Variable> findFunctionDirectlyContainVariables(@Param("functionId") Long functionId);

	@Query("match (lib:Library)-[:" + RelationType.str_CONTAIN + "]->(api:LibraryAPI) where id(lib)=$libraryId return api")
	public List<LibraryAPI> findLibraryContainLibraryAPIs(@Param("libraryId") Long libraryId);

	@Query("match (group:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) where id(group)=$groupId return file")
	public List<ProjectFile> findCloneGroupContainFiles(@Param("groupId") long groupId);
	
	@Query("match (group:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(function:Function) where id(group)=$groupId return function")
	public List<Function> findCloneGroupContainFunctions(@Param("groupId") long groupId);
	
	@Query("match (group:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(file:ProjectFile) where id(file)=$fileId return group")
	public CloneGroup findFileBelongToCloneGroup(@Param("fileId") long fileId);
	
	@Query("match (group:CloneGroup)-[:" + RelationType.str_CONTAIN + "]->(function:Function) where id(function)=$functionId return group")
	public CloneGroup findFunctionBelongToCloneGroup(@Param("functionId") long functionId);

	@Query("match (gitRepo:GitRepository)-[:" + RelationType.str_CONTAIN + "]->(project:Project) where id(gitRepo)=$gitRepoId return project")
	public List<Project> findGitRepositoryContainProjects(@Param("gitRepoId") long gitRepoId);

	/**
	 * 包之间的关系
	 */
	@Query("match p = (project:Project)-[:" + RelationType.str_CONTAIN + "]->(pck:Package) where not (pck)<-[:" + RelationType.str_CONTAIN + "]-(:Package) and id(project)=$projectId return pck")
	public List<Package> findProjectRootPackages(@Param("projectId") Long projectId);

	@Query("Match p = (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(children:Package) where id(pck)=$packageId return children order by children.directoryPath")
	public List<Package> findPackageContainPackages(@Param("packageId") Long packageId);

	@Query("Match p = (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(children:Package) where id(pck)=$packageId " +
			"and children.language = $language return children")
	public List<Package> findPackageContainPackagesWithLanguage(@Param("packageId") Long packageId, String language);

	@Query("Match p = (parent:Package)-[:" + RelationType.str_CONTAIN + "]->(pck:Package) where id(pck)=$packageId return parent")
	public Package findPackageInPackage(@Param("packageId") Long packageId);

	@Query("Match p = (pck:Package)-[:" + RelationType.str_CONTAIN + "]->(children:Package)-[:" + RelationType.str_CONTAIN + "]->(:Package) " +
			"where id(pck)=$parentPackageId " +
			"return children;")
	public List<Package> findPackagesWithChildPackagesForParentPackage(@Param("parentPackageId") Long parentPackageId);

	@Query("Match p = (parent:Package)-[:" + RelationType.str_CONTAIN + "]->(pck:Package) " +
			"where id(parent)=$parentPackageId " +
			"with parent, pck " +
			"set pck.depth = parent.depth + 1; ")
	public Boolean setChildPackageDepth(@Param("parentPackageId") Long parentPackageId);

	@Query("match h=(p:Package)-[:" + RelationType.str_CONTAIN + "*]->(f:ProjectFile) where id(p)=$pckId and id(f)=$fileId return count(h) > 0;")
	public boolean isPackageContainsFile(@Param("pckId") Long pckId, @Param("fileId") Long fileId);
}
