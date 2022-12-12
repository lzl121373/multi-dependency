package cn.edu.fudan.se.multidependency.service.insert.code;

import cn.edu.fudan.se.multidependency.model.relation.structure.*;
import depends.deptypes.DependencyType;
import depends.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import depends.entity.repo.EntityRepo;
import depends.relations.Inferer;

import java.util.List;
import java.util.Map;

/**
 * 
 * 7259
 * 11460
 * @author fan
 *
 */
public class CppExtractorServiceImpl extends DependsCodeExtractorForNeo4jServiceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CppExtractorServiceImpl.class);
	
	public CppExtractorServiceImpl(EntityRepo entityRepo, ProjectConfig projectConfig) {
		super(entityRepo, projectConfig);
	}
	
	private Namespace process(PackageEntity entity) {
		// C++中的命名空间
		Node node = this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Namespace,
				entity.getId().longValue(), currentProject);
		if(node == null) {
			Namespace namespace = new Namespace();
			namespace.setLanguage(Language.cpp.name());
			namespace.setName(entity.getQualifiedName());
			namespace.setEntityId(entity.getId().longValue());
			namespace.setSimpleName(entity.getRawName().getName());
			addNode(namespace, currentProject);
			return namespace;
		}else {
			return (Namespace) node;
		}
	}
	
	private ProjectFile process(FileEntity entity) {
		final String projectPath = currentProject.getPath();
		ProjectFile file = new ProjectFile();
		file.setLanguage(Language.cpp.name());
		file.setEntityId(entity.getId().longValue());
		String filePath = entity.getQualifiedName();
		file.setName(FileUtil.extractFilePathName(filePath));
		filePath = FileUtil.extractFilePath(filePath, projectPath);
		file.setPath(filePath);
		file.setSuffix(FileUtil.extractSuffix(entity.getQualifiedName()));
		file.setEndLine(entity.getEndLine() == null ? -1 : entity.getEndLine());
		file.setLoc(entity.getLoc() == null ? -1 : entity.getLoc());
		file.setProjectBelongPath(projectPath);
		addNode(file, currentProject);
		// 文件所在目录
		String directoryPath = FileUtil.extractDirectoryFromFile(entity.getQualifiedName()) + "/";
		directoryPath = FileUtil.extractFilePath(directoryPath, projectPath);
		Package pck = this.getNodes().findPackageByDirectoryPath(directoryPath, currentProject);
		if (pck == null) {
			pck = new Package();
//			pck.setEntityId(entityRepo.generateId().longValue());
			pck.setEntityId(generateEntityId());
			pck.setName(directoryPath);
			pck.setLanguage(Language.cpp.name());
			pck.setDirectoryPath(directoryPath);
			addNode(pck, currentProject);
			Contain projectContainsPackage = new Contain(currentProject, pck);
			addRelation(projectContainsPackage);
		}
		Contain containFile = new Contain(pck, file);
		addRelation(containFile);
		return file;
	}
	
	private Function process(FunctionEntity entity) {
		Function function = new Function();
		function.setLanguage(Language.cpp.name());
		function.setName(entity.getDisplayName());
		function.setEntityId(entity.getId().longValue());
		function.setImpl(entity.getClass() == FunctionEntityImpl.class);
		function.setSimpleName(entity.getRawName().getName());
		function.setStartLine(entity.getStartLine());
		function.setEndLine(entity.getEndLine() == null ? -1 : entity.getEndLine());
		addNode(function, currentProject);
		return function;
	}
	
	private Variable process(VarEntity entity) {
		Variable variable = new Variable();
		variable.setLanguage(Language.cpp.name());
		variable.setEntityId(entity.getId().longValue());
		variable.setName(entity.getQualifiedName());
		String typeIdentify = ((VarEntity) entity).getRawType().getName();
		List<GenericName> varArguments = ((VarEntity) entity).getRawName().getArguments();
		if(varArguments != null && !varArguments.isEmpty()){
			typeIdentify += getTypeIdentifyOfVar(varArguments);
		}
		variable.setTypeIdentify(typeIdentify);
		variable.setSimpleName(entity.getRawName().getName());
		addNode(variable, currentProject);
		return variable;
	}
	
	private Type process(TypeEntity entity) {
		Node node = this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type,
				entity.getId().longValue(), currentProject);
		if(node == null) {
			Type type = new Type();
			type.setLanguage(Language.cpp.name());
			type.setEntityId(entity.getId().longValue());
			type.setName(entity.getQualifiedName());
			type.setSimpleName(entity.getRawName().getName());
			type.setStartLine(entity.getStartLine() == null ? -1 :entity.getStartLine());
			type.setEndLine(entity.getEndLine() == null ? -1 :entity.getEndLine());
			addNode(type, currentProject);
			return type;
		} else {
			return (Type) node;
		}
	}
	
	private void process(AliasEntity entity) {
		AliasEntity aliasEntity = (AliasEntity) entity;
		TypeEntity typeEntity = aliasEntity.getType();
		if(typeEntity != null){
			if(typeEntity.getClass() == PackageEntity.class){
				Namespace namespace = process((PackageEntity)typeEntity);
				namespace.setAliasName(entity.getQualifiedName());

				Namespace aliasNamespace = new Namespace();
				aliasNamespace.setLanguage(Language.cpp.name());
				aliasNamespace.setName(entity.getQualifiedName());
				aliasNamespace.setEntityId(entity.getId().longValue());
				aliasNamespace.setSimpleName(entity.getRawName().getName());
				aliasNamespace.setAlias(true);
				addNode(aliasNamespace, currentProject);
			} else if(typeEntity instanceof TypeEntity){
				if (typeEntity.getParent() != null) {
					Type type = process(typeEntity);
					type.setAliasName(entity.getQualifiedName());
				}

				Type aliasType = new Type();
				aliasType.setLanguage(Language.cpp.name());
				aliasType.setEntityId(entity.getId().longValue());
				aliasType.setName(entity.getQualifiedName());
				aliasType.setSimpleName(entity.getRawName().getName());
				aliasType.setStartLine(entity.getStartLine() == null ? -1 :entity.getStartLine());
				aliasType.setEndLine(entity.getEndLine() == null ? -1 :entity.getEndLine());
				aliasType.setAlias(true);
				addNode(aliasType, currentProject);
			}
		}

	}
	
	@Override
	protected void addNodesWithContainRelations() {
		entityRepo.entityIterator().forEachRemaining(entity -> {
			// 每个entity对应相应的node
			if (entity instanceof PackageEntity) {
				process((PackageEntity) entity);
			} else if (entity instanceof FileEntity) {
				process((FileEntity) entity);
			} else if (entity instanceof FunctionEntity) {
				process((FunctionEntity) entity);
			} else if (entity instanceof VarEntity) {
				process((VarEntity) entity);
			} else if (entity.getClass() == TypeEntity.class) {
				process((TypeEntity) entity);
			} else if (entity.getClass() == AliasEntity.class) {
				process((AliasEntity) entity);
			} else {
			}
		});

//		addEmptyPackages();
		addCommonParentEmptyPackages();
		
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Namespace, currentProject).forEach((entityId, node) -> {
			Namespace namespace = (Namespace) node;
			Entity packageEntity = entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = packageEntity.getParent();
			while (parentEntity != null && !(parentEntity instanceof FileEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain contain = new Contain(parentNode, namespace);
			addRelation(contain);
			processIdentifier(namespace);
//			this.getNodes().addCodeNode(namespace);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject).forEach((entityId, node) -> {
			Type type = (Type) node;
			Entity typeEntity =  entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = typeEntity.getParent();
			while (parentEntity != null && !(parentEntity instanceof FileEntity || parentEntity instanceof PackageEntity)) {
				/// FIXME 内部类的情况暂不考虑
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain contain = new Contain(parentNode, type);
			addRelation(contain);
			processIdentifier(type);
//			this.getNodes().addCodeNode(type);
			while(!(parentEntity instanceof FileEntity)) {
				// 找出Type所在的文件
				parentEntity = parentEntity.getParent();
			}
			ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(parentEntity.getId().longValue(), currentProject);
			this.getNodes().putNodeToFileByEndLine(file, type);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject).forEach((entityId, node) -> {
			Function function = (Function) node;
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = functionEntity.getParent();
			while(parentEntity != null && !(parentEntity.getClass() == TypeEntity.class || parentEntity instanceof FileEntity || parentEntity instanceof PackageEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain contain = new Contain(parentNode, function);
			addRelation(contain);
			// 方法的参数
			for (VarEntity varEntity : functionEntity.getParameters()) {
				String parameterName = varEntity.getRawType().getName();
				TypeEntity typeEntity = varEntity.getType();
				if(!StringUtils.isBlank(varEntity.getTypeIdentifier())) {
					function.addParameterIdentifiers(varEntity.getTypeIdentifier());
				} else {
					if(typeEntity != null 
//						&& Inferer.externalType != typeEntity
							&& Inferer.buildInType != typeEntity
							&& Inferer.genericParameterType != typeEntity
							&& this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, parentEntity.getId().longValue(), currentProject) != null) {
						function.addParameterIdentifiers(typeEntity.getQualifiedName());
					} else {
						function.addParameterIdentifiers(parameterName);
					}
				}
			}
			processIdentifier(function);
			function.setName(function.getIdentifierSimpleName());
//			this.getNodes().addCodeNode(function);
			while(!(parentEntity instanceof FileEntity)) {
				// 找出方法所在的文件
				parentEntity = parentEntity.getParent();
			}
			ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(parentEntity.getId().longValue(), currentProject);
			this.getNodes().putNodeToFileByEndLine(file, function);
		});
		LOGGER.info("{} {} variable findNodesByNodeTypeInProject", this.currentProject.getName(), this.currentProject.getLanguage());
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Variable, currentProject).forEach((entityId, node) -> {
			Variable variable = (Variable) node;
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = varEntity.getParent();
			while(parentEntity != null 
					&& !(parentEntity.getClass() == TypeEntity.class || parentEntity instanceof FileEntity 
						|| parentEntity instanceof PackageEntity || parentEntity instanceof FunctionEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			if(parentNode instanceof Type) { 
				variable.setMemberVariable(true);
			} else if(parentNode instanceof ProjectFile){
				variable.setGlobalVariable(true);
			}
			Contain contain = new Contain(parentNode, variable);
			addRelation(contain);
			processIdentifier(variable);
//			this.getNodes().addCodeNode(variable);
		});
	}

	@Override
	protected void addRelations() {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
		extractRelationsFromVariables();
		extractRelationsFromFiles();
		// extractRelationsFromDependsType();
		extractRelationsFromNamespaces();
	}

	/**
	 * c中文件的include关系
	 */
	protected void extractRelationsFromFiles() {
		LOGGER.info("{} {} file extractRelationsFromFiles", this.currentProject.getName(), this.currentProject.getLanguage());
		Map<Long, ? extends Node> functions = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject);
		Map<Long, ? extends Node> types = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject);
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.ProjectFile, currentProject).forEach((entityId, node) -> {
			ProjectFile file = (ProjectFile) node;
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			fileEntity.getImportedFiles().forEach(entity -> {
				if (entity instanceof FileEntity) {
					ProjectFile includeFile = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.ProjectFile, entity.getId().longValue(), currentProject);
					if (includeFile != null) {
						Include fileIncludeFile = new Include(file, includeFile);
						addRelation(fileIncludeFile);
					}
				} else {
//					System.out.println("File - getImportedFiles: " + entity.getClass());
				}
			});

			fileEntity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
					case DependencyType.CONTAIN:
						// 包含的type，即namespace的成员变量的类型，此处仅指代类型直接定义的成员变量，不包含通过List<？>、Set<？>等基本数据类型中参数类型（此种情况将在变量的参数类型中处理）
						Type containUseType = (Type) types.get(relation.getEntity().getId().longValue());
						if(containUseType != null) {
							Use use = new Use(file, containUseType);
							addRelation(use);
						}
						break;
					case DependencyType.CALL:
						if( relation.getEntity() instanceof FunctionEntity ) {
							Function calledFun = (Function) functions.get(relation.getEntity().getId().longValue());
							if(calledFun != null) {
								Call call = new Call(file, calledFun);
								addRelation(call);
							}
						}
						break;
					case DependencyType.CREATE:
						Type createType = (Type) types.get(relation.getEntity().getId().longValue());
						if(createType != null) {
							Create create = new Create(file, createType);
							addRelation(create);
						}
						break;
					case DependencyType.CAST:
						Type castType = (Type) types.get(relation.getEntity().getId().longValue());
						if(castType != null) {
							Cast functionCastType = new Cast(file, castType);
							addRelation(functionCastType);
						}
						break;
					case DependencyType.IMPLLINK:
						/**
						 * 主要正对c/c++项目中，由于预编译或语法解析错误导致（方法 - IMPLLINK-方法）识别错误
						 * 可能出现 File - IMPLLINK - Function情况，为保证文件级正确，建立此种关系
						 */
						Function implLinkFunction = (Function) functions.get(relation.getEntity().getId().longValue());
						if(implLinkFunction != null ) {
							ImplLink functionImplLinkFunction = new ImplLink(file, implLinkFunction);
							addRelation(functionImplLinkFunction);
						}
						break;
					case DependencyType.USE:
						Entity relationEntity = relation.getEntity();
						Node relationNode = this.getNodes().findNodeByEntityIdInProject(relationEntity.getId().longValue(), currentProject);
						if(relationNode != null){
							if(relationNode instanceof Variable) {
								Variable var = (Variable) relationNode;
								Entity relationParentEntity = relationEntity.getAncestorOfType(FileEntity.class);
								if(relationParentEntity != null && fileEntity != relationParentEntity) {
									Use use = new Use(file, var);
									addRelation(use);
								}
							}else if(relationNode instanceof Type){
								Type other = (Type) relationNode;
								Use use = new Use(file, other);
								addRelation(use);
							}
						}
						break;
					case DependencyType.IMPORT:
						break;
					default:
						String typeStr = relation.getEntity().getQualifiedName();
						if("built-in".equals(typeStr)) break;

						LOGGER.info(file.getIdentifier() + "---" + relation.getType() + "----" + relation.getEntity().getQualifiedName()
								+ "(" + relation.getEntity().getClass().toString() + "): Line " + relation.getStartLine());
						break;
				}
			});

		});
	}

	protected void extractRelationsFromNamespaces() {
		LOGGER.info("{} {} file extractRelationsFromNamespances", this.currentProject.getName(), this.currentProject.getLanguage());
		Map<Long, ? extends Node> functions = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject);
		Map<Long, ? extends Node> types = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject);
		Map<Long, ? extends Node> namespaces = this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Namespace, currentProject);
		namespaces.forEach((entityId, node) -> {
			Namespace namespace = (Namespace) node;
			Entity namespaceEntity = entityRepo.getEntity(entityId.intValue());
			namespaceEntity.getRelations().forEach(relation -> {
				switch(relation.getType()) {
					case DependencyType.CONTAIN:
						// 包含的type，即namespace的成员变量的类型，此处仅指代类型直接定义的成员变量，不包含通过List<？>、Set<？>等基本数据类型中参数类型（此种情况将在变量的参数类型中处理）
						Type namespaceVarType = (Type) types.get(relation.getEntity().getId().longValue());
						if(namespaceVarType != null) {
							Use use = new Use(namespace, namespaceVarType);
							addRelation(use);
						}
						break;
					case DependencyType.CALL:
						if( relation.getEntity() instanceof FunctionEntity ) {
							Function calledFun = (Function) functions.get(relation.getEntity().getId().longValue());
							if(calledFun != null) {
								Call call = new Call(namespace, calledFun);
								addRelation(call);
							}
						}
						break;
					case DependencyType.CREATE:
						Type createType = (Type) types.get(relation.getEntity().getId().longValue());
						if(createType != null) {
							Create create = new Create(namespace, createType);
							addRelation(create);
						}
						break;
					case DependencyType.CAST:
						Type castType = (Type) types.get(relation.getEntity().getId().longValue());
						if(castType != null) {
							Cast functionCastType = new Cast(namespace, castType);
							addRelation(functionCastType);
						}
						break;
					case DependencyType.IMPLLINK:
						/**
						 * 主要正对c/c++项目中，由于预编译或语法解析错误导致（方法 - IMPLLINK-方法）识别错误
						 * 可能出现 Namespace - IMPLLINK - Function情况，为保证文件级正确，建立此种关系
						 */
						Function implLinkFunction = (Function) functions.get(relation.getEntity().getId().longValue());
						if(implLinkFunction != null ) {
							ImplLink functionImplLinkFunction = new ImplLink(namespace, implLinkFunction);
							addRelation(functionImplLinkFunction);
						}
						break;
					case DependencyType.USE:
						Entity relationEntity = relation.getEntity();
						Node relationNode = this.getNodes().findNodeByEntityIdInProject(relationEntity.getId().longValue(), currentProject);
						if(relationNode != null){
							if(relationNode instanceof Variable) {
								Variable var = (Variable) relationNode;
								Entity relationParentEntity = relationEntity.getAncestorOfType(PackageEntity.class);
								if(relationParentEntity != null && namespaceEntity != relationParentEntity) {
									Use use = new Use(namespace, var);
									addRelation(use);
								}
							}else if(relationNode instanceof Type){
								Type other = (Type) relationNode;
								Use use = new Use(namespace, other);
								addRelation(use);
							}
						}
						break;
					default:
						String typeStr = relation.getEntity().getQualifiedName();
						if("built-in".equals(typeStr)) break;

						LOGGER.info(namespace.getIdentifier() + "---" + relation.getType() + "----" + relation.getEntity().getQualifiedName()
								+ "(" + relation.getEntity().getClass().toString() + "): Line " + relation.getStartLine());
						break;
				}
			});
		});
	}
}
