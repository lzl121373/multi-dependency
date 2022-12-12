package cn.edu.fudan.se.multidependency.service.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import cn.edu.fudan.se.multidependency.model.relation.lib.CallLibrary;
import cn.edu.fudan.se.multidependency.model.relation.lib.FunctionCallLibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.structure.Access;
import cn.edu.fudan.se.multidependency.model.relation.structure.Annotation;
import cn.edu.fudan.se.multidependency.model.relation.structure.Call;
import cn.edu.fudan.se.multidependency.model.relation.structure.Cast;
import cn.edu.fudan.se.multidependency.model.relation.structure.Extends;
import cn.edu.fudan.se.multidependency.model.relation.structure.Implements;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.model.relation.structure.Include;
import cn.edu.fudan.se.multidependency.model.relation.structure.Parameter;
import cn.edu.fudan.se.multidependency.model.relation.structure.Return;
import cn.edu.fudan.se.multidependency.model.relation.structure.Throw;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableType;
import cn.edu.fudan.se.multidependency.service.query.metric.Fan_IO;

public interface StaticAnalyseService {
	
	List<Project> queryAllProjectsByPage(int page, int size, String... sortByProperties);
	
	/**
	 * 找子类
	 * @param type
	 * @return
	 */
	Collection<Type> queryExtendsSubTypes(Type type);
	
	/**
	 * 找父类
	 * @param type
	 * @return
	 */
	Collection<Type> queryExtendsSuperTypes(Type type);
	
	/**
	 * 找子类
	 * @param type
	 * @return
	 */
	Collection<Type> queryImplementsSubTypes(Type type);
	
	/**
	 * 找父类
	 * @param type
	 * @return
	 */
	Collection<Type> queryImplementsSuperTypes(Type type);
	
	List<Call> findAllFunctionCallFunctionRelations();
	
	Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller();
	Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller(Project project);
	Map<Function, List<Access>> findAllFunctionAccessRelationsGroupByCaller(Project project);

	List<StructureRelation> findProjectContainStructureRelations(Project project);
	List<Extends> findProjectContainInheritsRelations(Project project);
	List<Implements> findProjectContainImplementsRelations(Project project);
	List<Include> findProjectContainFileIncludeFileRelations(Project project);
	List<Import> findProjectContainImportRelations(Project project);
	List<Import> findProjectContainFileImportTypeRelations(Project project);
	List<Import> findProjectContainFileImportFunctionRelations(Project project);
	List<Import> findProjectContainFileImportVariableRelations(Project project);
	List<Call> findProjectContainFunctionCallFunctionRelations(Project project);
	List<Call> findProjectContainTypeCallFunctions(Project project);
	List<Cast> findProjectContainFunctionCastTypeRelations(Project project);
	List<Return> findProjectContainFunctionReturnTypeRelations(Project project);
	List<Throw> findProjectContainFunctionThrowTypeRelations(Project project);
	List<Annotation> findProjectContainNodeAnnotationTypeRelations(Project project);
	List<VariableType> findProjectContainVariableIsTypeRelations(Project project);
	List<Parameter> findProjectContainParameterRelations(Project project);
	List<Parameter> findProjectContainFunctionParameterTypeRelations(Project project);
	List<Parameter> findProjectContainVariableTypeParameterTypeRelations(Project project);
	List<Access> findProjectContainFunctionAccessVariableRelations(Project project);

	boolean isSubType(Type subType, Type superType);
	
	/**
	 * 找出所有函数调用第三方库数据
	 * @return
	 */
	Map<Function, List<FunctionCallLibraryAPI>> findAllFunctionCallLibraryAPIs();
	
	/**
	 * 找出Project调用了哪些三方
	 * @param project
	 * @return
	 */
	CallLibrary<Project> findProjectCallLibraries(Project project);
	
	Iterable<Library> findAllLibraries();
	
	List<Fan_IO<ProjectFile>> queryAllFileFanIOs(Project project);
	
	Fan_IO<ProjectFile> queryJavaFileFanIO(ProjectFile file);
	
	Collection<Call> queryFunctionCallFunctions(Function function);

	Collection<Call> queryFunctionCallByFunctions(Function function);
	
	boolean isDataClass(Type type);
	
	boolean isDataFile(ProjectFile file);
	
	boolean isDependsOn(ProjectFile file1, ProjectFile file2);

	
	Map<Package, List<DependsOn>> findPackageDependsOn(Project project);

	Map<Package, List<DependsOn>> findPackageDependsOnBy(Project project);
	
	Map<ProjectFile, List<DependsOn>> findFileDependsOn(Project project);

	Map<ProjectFile, List<DependsOn>> findFileDependsOnBy(Project project);

	Collection<DependsOn> findFileDependsOn(ProjectFile file);
	
	Collection<DependsOn> findFileDependedOnBy(ProjectFile file);

	Collection<DependsOn> findPackageDependsOn(Package pck);

	Collection<DependsOn> findPackageDependedOnBy(Package pck);

	DependsOn findDependsOnBetweenFiles(ProjectFile file1, ProjectFile file2);

	Collection<DependsOn> findAllDependsOnsBetweenFiles(ProjectFile file1, ProjectFile file2);

	Collection<ProjectFile> findFilesCommonDependsOn(ProjectFile file1, ProjectFile file2);

	Collection<ProjectFile> findFilesCommonDependedOnBy(ProjectFile file1, ProjectFile file2);
}
