package cn.edu.fudan.se.multidependency.service.query.structure;

import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;
import cn.edu.fudan.se.multidependency.service.query.data.PackageStructure;
import cn.edu.fudan.se.multidependency.service.query.data.ProjectStructure;
import com.alibaba.fastjson.JSONObject;

public interface ContainRelationService {

	ProjectStructure projectStructureInitialize(Project project);

	PackageStructure packageStructureInitialize(Package pck, String type);

	PackageStructure packageStructureInitializeWithNoSubPackages(Package pck, String type);

	Collection<Package> findProjectRootPackages(Project project);

	List<Package> findPackageContainPackages(Package pck);

	Collection<Package> findPackageContainPackagesWithLanguage(Package pck);

	/**
	 * 包的上一级包
	 * @param pck
	 * @return
	 */
	Package findPackageInPackage(Package pck);
	
	Collection<Package> findProjectContainPackages(Project project);

	Collection<Function> findProjectContainAllFunctions(Project project);

	Collection<ProjectFile> findProjectContainAllFiles(Project project);

	Collection<ProjectFile> findPackageContainFiles(Package pck);

	Collection<ProjectFile> findPackageContainAllFiles(Package pck);
	
	Collection<Namespace> findFileContainNamespaces(ProjectFile file);

	Collection<Type> findFileDirectlyContainTypes(ProjectFile file);

	Collection<Function> findFileDirectlyContainFunctions(ProjectFile file);

	Collection<Variable> findFileDirectlyContainVariables(ProjectFile file);

	Collection<Type> findFileDirectlyImportTypes(ProjectFile file);

	Collection<Function> findFileDirectlyImportFunctions(ProjectFile file);

	Collection<Variable> findFileDirectlyImportVariables(ProjectFile file);

	Collection<Import> findFileDirectlyImports(ProjectFile file);

	Collection<Include> findFileDirectlyIncludes(ProjectFile file);
	
	Collection<Type> findNamespaceDirectlyContainTypes(Namespace namespace);

	Collection<Function> findNamespaceDirectlyContainFunctions(Namespace namespace);

	Collection<Variable> findNamespaceDirectlyContainVariables(Namespace namespace);
	
	Collection<Function> findTypeDirectlyContainFunctions(Type type);

	Collection<Variable> findTypeDirectlyContainFields(Type type);
	
	Collection<Variable> findFunctionDirectlyContainVariables(Function function);
	
	Project findCodeNodeBelongToProject(CodeNode node);
	
	ProjectFile findCodeNodeBelongToFile(CodeNode node);
	
	Package findFileBelongToPackage(ProjectFile file);
	
	Project findFileBelongToProject(ProjectFile file);

	Project findTypeBelongToProject(Type type);
	
	Type findFunctionBelongToType(Function function);

	Type findVariableBelongToType(Variable node);
	
	ProjectFile findFunctionBelongToFile(Function function);
	
	ProjectFile findTypeBelongToFile(Type type);
	
	ProjectFile findVariableBelongToFile(Variable variable);
	
	ProjectFile findSnippetBelongToFile(Snippet snippet);
	
	Type findSnippetDirectlyBelongToType(Snippet snippet);
	
	Function findSnippetDirectlyBelongToFunction(Snippet snippet);
	
	Project findFunctionBelongToProject(Function function);
	
	Project findPackageBelongToProject(Package pck);
	
	Library findAPIBelongToLibrary(LibraryAPI api);
	
	Iterable<LibraryAPI> findLibraryContainAPIs(Library lib);

	Collection<Project> findMicroServiceContainProjects(MicroService ms);
	
	Iterable<Function> findMicroServiceContainFunctions(MicroService ms);

	Iterable<ProjectFile> findMicroServiceContainFiles(MicroService ms);

	MicroService findProjectBelongToMicroService(Project project);
	
	List<RestfulAPI> findMicroServiceContainRestfulAPI(MicroService microService);
	
	List<Span> findTraceContainSpans(Trace trace);
	
	List<Contain> findAllFeatureContainFeatures();
	
	Collection<ProjectFile> findCloneGroupContainFiles(CloneGroup group);
	
	Collection<Function> findCloneGroupContainFunctions(CloneGroup group);
	
	CloneGroup findFileBelongToCloneGroup(ProjectFile file);
	
	CloneGroup findFunctionBelongToCloneGroup(Function function);
	
	boolean isDifferentPackage(ProjectFile file1, ProjectFile file2);
	
	GitRepository findCommitBelongToGitRepository(Commit commit);

	Collection<Project> findGitRepositoryContainProject(GitRepository gitRepository);

	Collection<Call> findFunctionContainCalls (Function function);

	JSONObject doubleFileStructure(List<ProjectFile> fileList);

	Package findPackageContainFileInPackagesList(List<Package> pckList, Long fileId);
	
	/**
	 * child是否在parent下
	 * @param child
	 * @param parent
	 * @return
	 */
	default boolean isDirectlyParentOfPackage(Package child, Package parent) {
		if(child == null || parent == null) {
			return false;
		}
		return parent.equals(findPackageInPackage(child));
	}
}
