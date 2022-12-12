package cn.edu.fudan.se.multidependency.service.query;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
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
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.VariableRepository;
import cn.edu.fudan.se.multidependency.repository.node.lib.LibraryRepository;
import cn.edu.fudan.se.multidependency.repository.relation.DependsOnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.AccessRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.AnnotationRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.CallRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.CastRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ExtendsRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ImplementsRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ImportRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.IncludeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ParameterRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ReturnRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.ThrowRepository;
import cn.edu.fudan.se.multidependency.repository.relation.code.VariableTypeRepository;
import cn.edu.fudan.se.multidependency.repository.relation.dynamic.FunctionDynamicCallFunctionRepository;
import cn.edu.fudan.se.multidependency.repository.relation.lib.FunctionCallLibraryAPIRepository;
import cn.edu.fudan.se.multidependency.service.query.metric.Fan_IO;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.utils.query.PageUtil;

/**
 * 
 * @author fan
 *
 */
@Service
public class StaticAnalyseServiceImpl implements StaticAnalyseService {
	
	@Autowired
	ProjectFileRepository fileRepository;
	
	@Autowired
	IncludeRepository fileIncludeFileRepository;
	
	@Autowired
	ImportRepository importRepository;
	
	@Autowired
	CallRepository callRepository;
	
	@Autowired
	FunctionRepository functionRepository;
	
	@Autowired
	AccessRepository functionAccessFieldRepository;
	
	@Autowired
	FunctionDynamicCallFunctionRepository functionDynamicCallFunctionRepository;
	
	@Autowired
	ReturnRepository functionReturnTypeRepository;
	
	@Autowired
	ParameterRepository parameterRepository;
	
	@Autowired
	NamespaceRepository namespaceRepository;
	
	@Autowired
    PackageRepository packageRepository;

    @Autowired
    ProjectRepository projectRepository;
    
    @Autowired
    TypeRepository typeRepository;
    
    @Autowired
    ExtendsRepository extendsRepository;
    
    @Autowired
    ImplementsRepository implementsRepository;

    @Autowired
    VariableTypeRepository variableIsTypeRepository;
    
    @Autowired
    VariableRepository variableRepository;
    
    @Autowired
    CastRepository functionCastTypeRepository;
    
    @Autowired
    ThrowRepository functionThrowTypeRepository;
    
    @Autowired
    AnnotationRepository nodeAnnotationTypeRepository;
    
    @Autowired
    FunctionCallLibraryAPIRepository functionCallLibraryAPIRepository;
    
    @Autowired
    LibraryRepository libraryRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    CacheService cache;
    
    @Autowired
    DependsOnRepository dependsOnRepository;

	@Override
	public CallLibrary<Project> findProjectCallLibraries(Project project) {
		CallLibrary<Project> result = new CallLibrary<Project>();
		result.setCaller(project);
		Map<Function, List<FunctionCallLibraryAPI>> functionCallLibAPIs = findAllFunctionCallLibraryAPIs();
		Iterable<Function> functions = containRelationService.findProjectContainAllFunctions(project);
		for(Function function : functions) {
			List<FunctionCallLibraryAPI> calls = functionCallLibAPIs.getOrDefault(function, new ArrayList<>());
			for(FunctionCallLibraryAPI call : calls) {
				LibraryAPI api = call.getApi();
				Library lib = containRelationService.findAPIBelongToLibrary(api);
				result.addLibraryAPI(api, lib, call.getTimes());
			}
		}
		return result;
	}
	
	@Override
	public Collection<Type> queryExtendsSubTypes(Type type) {
		String key = "queryExtendsSubTypes_" + type.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Type> result = extendsRepository.querySubTypes(type.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Type> queryExtendsSuperTypes(Type type) {
		String key = "queryExtendsSuperTypes_" + type.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Type> result = extendsRepository.querySuperTypes(type.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Type> queryImplementsSubTypes(Type type) {
		String key = "queryImplementsSubTypes_" + type.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Type> result = implementsRepository.querySubTypes(type.getId());
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<Type> queryImplementsSuperTypes(Type type) {
		String key = "queryImplementsSuperTypes_" + type.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Type> result = implementsRepository.querySuperTypes(type.getId());
		return result;
	}

	@Override
	public List<StructureRelation> findProjectContainStructureRelations(Project project) {
		List<StructureRelation> result = new ArrayList<>();
		result.addAll(findProjectContainFunctionCallFunctionRelations(project));
		result.addAll(findProjectContainInheritsRelations(project));
		result.addAll(findProjectContainTypeCallFunctions(project));
		return result;
	}

	private Map<Project, List<Import>> projectContainImportRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Import> findProjectContainImportRelations(Project project) {
		if(projectContainImportRelationsCache.get(project) == null) {
			projectContainImportRelationsCache.put(project, importRepository.findProjectContainImportRelations(project.getId()));
		}
		return projectContainImportRelationsCache.get(project);
	}
	
	@Override
	public List<Import> findProjectContainFileImportTypeRelations(Project project) {
		return importRepository.findProjectContainFileImportTypeRelations(project.getId());
	}

	@Override
	public List<Import> findProjectContainFileImportFunctionRelations(Project project) {
		return importRepository.findProjectContainFileImportFunctionRelations(project.getId());
	}

	@Override
	public List<Import> findProjectContainFileImportVariableRelations(Project project) {
		return importRepository.findProjectContainFileImportVariableRelations(project.getId());
	}

	private Map<Project, List<Call>> projectContainFunctionCallFunctionRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Call> findProjectContainFunctionCallFunctionRelations(Project project) {
		if(projectContainFunctionCallFunctionRelationsCache.get(project) == null) {
			projectContainFunctionCallFunctionRelationsCache.put(project, callRepository.findProjectContainFunctionCallFunctionRelations(project.getId()));
		}
		return projectContainFunctionCallFunctionRelationsCache.get(project);
	}
	
	private Map<Project, List<Call>> projectContainTypeCallFunctionRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Call> findProjectContainTypeCallFunctions(Project project) {
		if(projectContainTypeCallFunctionRelationsCache.get(project) == null) {
			projectContainTypeCallFunctionRelationsCache.put(project, callRepository.findProjectContainTypeCallFunctionRelations(project.getId()));
		}
		return projectContainTypeCallFunctionRelationsCache.get(project);
	}

	private Map<Project, List<Extends>> projectContainInheritsRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Extends> findProjectContainInheritsRelations(Project project) {
//		if(projectContainInheritsRelationsCache.get(project) == null) {
//			projectContainInheritsRelationsCache.put(project, typeInheritsTypeRepository.findProjectContainTypeInheritsTypeRelations(project.getId()));
//		}
//		return projectContainInheritsRelationsCache.get(project);
		/// FIXME
		return new ArrayList<>();
	}

	private Map<Project, List<Cast>> projectContainFunctionCastTypeRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Cast> findProjectContainFunctionCastTypeRelations(Project project) {
		if(projectContainFunctionCastTypeRelationsCache.get(project) == null) {
			projectContainFunctionCastTypeRelationsCache.put(project, functionCastTypeRepository.findProjectContainFunctionCastTypeRelations(project.getId()));
		}
		return projectContainFunctionCastTypeRelationsCache.get(project);
	}
	
	private Map<Project, List<Parameter>> projectContainParameterRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Parameter> findProjectContainParameterRelations(Project project) {
		if(projectContainParameterRelationsCache.get(project) == null) {
			projectContainParameterRelationsCache.put(project, parameterRepository.findProjectContainParameterRelations(project.getId()));
		}
		return projectContainParameterRelationsCache.get(project);
	}

	@Override
	public List<Parameter> findProjectContainFunctionParameterTypeRelations(Project project) {
		return parameterRepository.findProjectContainFunctionParameterTypeRelations(project.getId());
	}

	@Override
	public List<Parameter> findProjectContainVariableTypeParameterTypeRelations(Project project) {
		return parameterRepository.findProjectContainVariableTypeParameterTypeRelations(project.getId());
	}

	private Map<Project, List<Return>> projectContainFunctionReturnTypeRelationsCache = new ConcurrentHashMap<>();
	@Override
	public List<Return> findProjectContainFunctionReturnTypeRelations(Project project) {
		if(projectContainFunctionReturnTypeRelationsCache.get(project) == null) {
			projectContainFunctionReturnTypeRelationsCache.put(project, functionReturnTypeRepository.findProjectContainFunctionReturnTypeRelations(project.getId()));
		}
		return projectContainFunctionReturnTypeRelationsCache.get(project);
	}

	@Override
	public List<Throw> findProjectContainFunctionThrowTypeRelations(Project project) {
		return functionThrowTypeRepository.findProjectContainFunctionThrowTypeRelations(project.getId());
	}

	@Override
	public List<Annotation> findProjectContainNodeAnnotationTypeRelations(Project project) {
		return nodeAnnotationTypeRepository.findProjectContainNodeAnnotationTypeRelations(project.getId());
	}

	@Override
	public List<VariableType> findProjectContainVariableIsTypeRelations(Project project) {
		return variableIsTypeRepository.findProjectContainVariableIsTypeRelations(project.getId());
	}

	@Override
	public List<Include> findProjectContainFileIncludeFileRelations(Project project) {
		return fileIncludeFileRepository.findProjectContainFileIncludeFileRelations(project.getId());
	}
	
	@Override
	public List<Access> findProjectContainFunctionAccessVariableRelations(Project project) {
		return functionAccessFieldRepository.findProjectContainFunctionAccessFieldRelations(project.getId());
	}

	@Override
	public Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller() {
		List<Call> allCalls = findAllFunctionCallFunctionRelations();
		Map<Function, List<Call>> result = new HashMap<>();
		for(Call call : allCalls) {
			CodeNode callerNode = call.getCallerNode();
			if(!(callerNode instanceof Function)) {
				continue;
			}
			Function caller = (Function) callerNode;
			List<Call> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(call);
			result.put(caller, group);
		}
		return result;
	}

	@Override
	public Map<Function, List<Call>> findAllFunctionCallRelationsGroupByCaller(Project project) {
		Iterable<Call> allCalls = findProjectContainFunctionCallFunctionRelations(project);
		Map<Function, List<Call>> result = new HashMap<>();
		for(Call call : allCalls) {
			CodeNode callerNode = call.getCallerNode();
			if(!(callerNode instanceof Function)) {
				continue;
			}
			Function caller = (Function) callerNode;
			List<Call> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(call);
			result.put(caller, group);
		}
		return result;
	}

	@Override
	public Map<Function, List<Access>> findAllFunctionAccessRelationsGroupByCaller(Project project) {
		Iterable<Access> allAccesses = findProjectContainFunctionAccessVariableRelations(project);
		Map<Function, List<Access>> result = new HashMap<>();
		for(Access access : allAccesses) {
			Function caller = (Function) access.getStartNode();
			List<Access> group = result.getOrDefault(caller, new ArrayList<>());
			group.add(access);
			result.put(caller, group);
		}
		return result;
	}

	private Map<Type, Map<Type, Boolean>> subTypeCache = new HashMap<>();
	@Override
	public boolean isSubType(Type subType, Type superType) {
		/*Map<Type, Boolean> superTypeMap = subTypeCache.getOrDefault(subType, new HashMap<>());
		Boolean is = superTypeMap.get(superType);
		if(is == null) {
			Type queryType = typeInheritsTypeRepository.findIsTypeInheritsType(subType.getId(), superType.getId());
			is = queryType != null;
			superTypeMap.put(superType, is);
		}
		subTypeCache.put(subType, superTypeMap);*/
		/// FIXME
		return false;
	}


	Map<Function, List<FunctionCallLibraryAPI>> allFunctionCallLibraryAPIsCache = new ConcurrentHashMap<>();
	@Override
	public Map<Function, List<FunctionCallLibraryAPI>> findAllFunctionCallLibraryAPIs() {
		if(allFunctionCallLibraryAPIsCache.isEmpty()) {
			Iterable<FunctionCallLibraryAPI> calls = functionCallLibraryAPIRepository.findAll();
			for(FunctionCallLibraryAPI call : calls) {
				Function function = call.getFunction();
				List<FunctionCallLibraryAPI> temp = allFunctionCallLibraryAPIsCache.getOrDefault(function, new ArrayList<>());
				temp.add(call);
				allFunctionCallLibraryAPIsCache.put(function, temp);
			}
		}
		return allFunctionCallLibraryAPIsCache;
	}
	
	private Iterable<Library> cacheForAllLibraries = null;
	@Override
	public Iterable<Library> findAllLibraries() {
		if(cacheForAllLibraries == null) {
			cacheForAllLibraries = libraryRepository.findAll();
		}
		return cacheForAllLibraries;
	}

	private Map<Integer, List<Project>> pageProjectsCache = new ConcurrentHashMap<>();
	@Override
	public List<Project> queryAllProjectsByPage(int page, int size, String... sortByProperties) {
		List<Project> result = pageProjectsCache.get(page);
		if(result == null || result.size() == 0) {
			result = new ArrayList<>();
			Pageable pageable = PageUtil.generatePageable(page, size, sortByProperties);
			Page<Project> pageProjects = projectRepository.findAll(pageable);
			for(Project project : pageProjects) {
				result.add(project);
			}
			pageProjectsCache.put(page, result);
		}
		return result;
	}

	@Override
	public Fan_IO<ProjectFile> queryJavaFileFanIO(ProjectFile file) {
		Fan_IO<ProjectFile> result = new Fan_IO<ProjectFile>(file);
		Collection<Type> typesInFile = containRelationService.findFileDirectlyContainTypes(file);
		Collection<Function> functions = new ArrayList<>();
		Collection<Variable> variables = new ArrayList<>();
		for(Type type : typesInFile) {
			Collection<Function> functionsInType = containRelationService.findTypeDirectlyContainFunctions(type);
			functions.addAll(functionsInType);
			for(Function function : functions) {
				Collection<Variable> variablesInFunction = containRelationService.findFunctionDirectlyContainVariables(function);
				variables.addAll(variablesInFunction);
			}
			Collection<Variable> variablesInType = containRelationService.findTypeDirectlyContainFields(type);
			variables.addAll(variablesInType);
		}
		
		for(Type type : typesInFile) {
			Collection<Type> inherits = queryExtendsSuperTypes(type);
			for(Type inheritsType : inherits) {
				ProjectFile belongToFile = containRelationService.findTypeBelongToFile(inheritsType);
				if(!file.equals(belongToFile)) {
					result.addFanOut(belongToFile);
				}
			}
			inherits = queryExtendsSubTypes(type);
			for(Type inheritsType : inherits) {
				ProjectFile belongToFile = containRelationService.findTypeBelongToFile(inheritsType);
				if(!file.equals(belongToFile)) {
					result.addFanIn(belongToFile);
				}
			}
		}
		
		for(Function function : functions) {
			Collection<Call> calls = queryFunctionCallFunctions(function);
			for(Call call : calls) {
				ProjectFile belongToFile = containRelationService.findFunctionBelongToFile(call.getCallFunction());
				if(!file.equals(belongToFile)) {
					result.addFanOut(belongToFile);
					result.addFanOutRelations(call);
				}
			}
			
			calls = queryFunctionCallByFunctions(function);
			for(Call call : calls) {
				ProjectFile belongToFile = containRelationService.findCodeNodeBelongToFile(call.getCallerNode());
				if(!file.equals(belongToFile)) {
					result.addFanIn(belongToFile);
					result.addFanInRelations(call);
				}
			}
		}
		
		for(Variable variable : variables) {
			
		}
		
		
		return result;
	}

	@Override
	public List<Fan_IO<ProjectFile>> queryAllFileFanIOs(Project project) {
		List<Fan_IO<ProjectFile>> result = new ArrayList<>();
		Collection<ProjectFile> files = containRelationService.findProjectContainAllFiles(project);
		for(ProjectFile file : files) {
			Fan_IO<ProjectFile> fanIO = queryJavaFileFanIO(file);
			result.add(fanIO);
		}
		result.sort(new Comparator<Fan_IO<ProjectFile>>() {
			@Override
			public int compare(Fan_IO<ProjectFile> o1, Fan_IO<ProjectFile> o2) {
				if(o1.size() == o2.size()) {
					return o1.getNode().getName().compareTo(o2.getNode().getName());
				}
				return o2.size() - o1.size();
			}
		});
		return result;
	}

	@Override
	public Collection<Call> queryFunctionCallFunctions(Function function) {
		return callRepository.queryFunctionCallFunctions(function.getId());
	}

	@Override
	public Collection<Call> queryFunctionCallByFunctions(Function function) {
		return callRepository.queryFunctionCallByFunctions(function.getId());
	}
	
	@Override
	public boolean isDataClass(Type type) {
		Collection<Variable> fields = containRelationService.findTypeDirectlyContainFields(type);
		Collection<Function> functions = containRelationService.findTypeDirectlyContainFunctions(type);
		if(fields.isEmpty() && functions.isEmpty()) {
			return true;
		} else if(fields.isEmpty()) {
			return false;
		} else if(functions.isEmpty()) {
			return true;
		}
		for(Function f : functions) {
			if(f.isConstructor()) {
				continue;
			}
			if(!f.getSimpleName().startsWith("get") 
					&& !f.getSimpleName().startsWith("set") 
					&& !f.getSimpleName().startsWith("is")
					&& !"equals".equals(f.getSimpleName())
					&& !"toString".equals(f.getSimpleName())
					&& !"hashCode".equals(f.getSimpleName())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isDataFile(ProjectFile file) {
		if(!ProjectFile.SUFFIX_JAVA.equals(file.getSuffix())) {
			// 如果不是java文件，直接返回false
			return false;
		}
		Collection<Type> types = containRelationService.findFileDirectlyContainTypes(file);
		if(types.isEmpty()) {
			return false;
		}
		for(Type type : types) {
			if(!isDataClass(type)) {
				return false;
			}
		}
		return true;
	}


	@Override
	public List<Call> findAllFunctionCallFunctionRelations() {
		String key = "findAllFunctionCallFunctionRelations";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<Call> result = callRepository.findAllFunctionCallFunctionRelations();
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public List<Implements> findProjectContainImplementsRelations(Project project) {
		/// FIXME
		return new ArrayList<>();
	}

	@Override
	public boolean isDependsOn(ProjectFile file1, ProjectFile file2) {
		return dependsOnRepository.isFileDependsOnFile(file1.getId(), file2.getId());
	}

	@Override
	public Map<Package, List<DependsOn>> findPackageDependsOn(Project project) {
		String key = "findPackageDependsOn_" + project.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> dependsOns = dependsOnRepository.findPackageDependsInProject(project.getId());
		Map<Package, List<DependsOn>> result = new HashMap<>();
		for(DependsOn dependsOn : dependsOns) {
			Package start = (Package) dependsOn.getStartNode();
			List<DependsOn> temp = result.getOrDefault(start, new ArrayList<>());
			temp.add(dependsOn);
			result.put(start, temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<Package, List<DependsOn>> findPackageDependsOnBy(Project project) {
		String key = "findPackageDependsOnBy_" + project.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> dependsOns = dependsOnRepository.findPackageDependsInProject(project.getId());
		Map<Package, List<DependsOn>> result = new HashMap<>();
		for(DependsOn dependsOn : dependsOns) {
			Package end = (Package) dependsOn.getEndNode();
			List<DependsOn> temp = result.getOrDefault(end, new ArrayList<>());
			temp.add(dependsOn);
			result.put(end, temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<ProjectFile, List<DependsOn>> findFileDependsOn(Project project) {
		String key = "findFileDependsOn_" + project.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> dependsOns = dependsOnRepository.findFileDependsInProject(project.getId());
		Map<ProjectFile, List<DependsOn>> result = new HashMap<>();
		for(DependsOn dependsOn : dependsOns) {
			ProjectFile start = (ProjectFile) dependsOn.getStartNode();
			List<DependsOn> temp = result.getOrDefault(start, new ArrayList<>());
			temp.add(dependsOn);
			result.put(start, temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Map<ProjectFile, List<DependsOn>> findFileDependsOnBy(Project project) {
		String key = "findFileDependsOnBy_" + project.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> dependsOns = dependsOnRepository.findFileDependsInProject(project.getId());
		Map<ProjectFile, List<DependsOn>> result = new HashMap<>();
		for(DependsOn dependsOn : dependsOns) {
			ProjectFile end = (ProjectFile) dependsOn.getEndNode();
			List<DependsOn> temp = result.getOrDefault(end, new ArrayList<>());
			temp.add(dependsOn);
			result.put(end, temp);
		}
		cache.cache(getClass(), key, result);
		return result;
	}
	
	@Override
	public Collection<DependsOn> findFileDependsOn(ProjectFile file) {
		String key = "findFileDependsOn_" + file.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> result = dependsOnRepository.findFileDependsOn(file.getId());
		result.sort((d1, d2) -> {
			ProjectFile file1 = (ProjectFile) d1.getEndNode();
			ProjectFile file2 = (ProjectFile) d2.getEndNode();
			return file1.getPath().compareTo(file2.getPath());
		});
		cache.cache(getClass(), key, result);
		return result;
	}
	
	@Override
	public Collection<DependsOn> findFileDependedOnBy(ProjectFile file) {
		String key = "findFileDependedOnBy_" + file.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> result = dependsOnRepository.findFileDependedOnBy(file.getId());
		result.sort((d1, d2) -> {
			ProjectFile file1 = (ProjectFile) d1.getStartNode();
			ProjectFile file2 = (ProjectFile) d2.getStartNode();
			return file1.getPath().compareTo(file2.getPath());
		});
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<DependsOn> findPackageDependsOn(Package pck) {
		String key = "findPackageDependsOn_" + pck.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> result = dependsOnRepository.findModuleDependsOn(pck.getId());
		result.sort((d1, d2)->{
			Package pck1 = (Package) d1.getEndNode();
			Package pck2 = (Package) d2.getEndNode();
			return pck1.getDirectoryPath().compareTo(pck2.getDirectoryPath());
		});
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<DependsOn> findPackageDependedOnBy(Package pck) {
		String key = "findPackageDependedOnBy_" + pck.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> result = dependsOnRepository.findPackageDependedOnBy(pck.getId());
		result.sort((d1, d2)->{
			Package pck1 = (Package) d1.getStartNode();
			Package pck2 = (Package) d2.getStartNode();
			return pck1.getDirectoryPath().compareTo(pck2.getDirectoryPath());
		});
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public DependsOn findDependsOnBetweenFiles(ProjectFile file1, ProjectFile file2){
		String key = "findDependsOnBetweenFiles" + file1.getId() + file2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		DependsOn result = dependsOnRepository.findDependsOnBetweenFiles(file1.getId(),file2.getId());
		if(result != null){
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	@Override
	public Collection<DependsOn> findAllDependsOnsBetweenFiles(ProjectFile file1, ProjectFile file2){
		String key = "findAllDependsOnsBetweenFiles" + file1.getId() + file2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<DependsOn> result = dependsOnRepository.findAllDependsOnsBetweenFiles(file1.getId(),file2.getId());
		if(result != null){
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	@Override
	public Collection<ProjectFile> findFilesCommonDependsOn(ProjectFile file1, ProjectFile file2){
		String key = "findFilesCommonDependsOn_" + file1.getId() + file2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<ProjectFile> result = dependsOnRepository.findFilesCommonDependsOn(file1.getId(),file2.getId());
		result.sort(Comparator.comparing(ProjectFile::getPath));
		cache.cache(getClass(), key, result);
		return result;
	}

	@Override
	public Collection<ProjectFile> findFilesCommonDependedOnBy(ProjectFile file1, ProjectFile file2){
		String key = "findFilesCommonDependedOnBy_" + file1.getId() + file2.getId();
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}
		List<ProjectFile> result = dependsOnRepository.findFilesCommonDependedOnBy(file1.getId(),file2.getId());
		result.sort(Comparator.comparing(ProjectFile::getPath));
		cache.cache(getClass(), key, result);
		return result;
	}
}
