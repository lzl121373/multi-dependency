package cn.edu.fudan.se.multidependency.service.insert.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.structure.Import;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import depends.entity.repo.EntityRepo;
import depends.relations.Inferer;

public class JavaExtractorServiceImpl extends DependsCodeExtractorForNeo4jServiceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaExtractorServiceImpl.class);

	public JavaExtractorServiceImpl(EntityRepo entityRepo, ProjectConfig projectConfig) {
		super(entityRepo, projectConfig);
	}
	
	private ProjectFile process(FileEntity entity) {
		final String projectPath = currentProject.getPath();
		ProjectFile file = new ProjectFile();
		file.setLanguage(Language.java.name());
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
			pck.setName(directoryPath);
			pck.setLanguage(Language.java.name());
			pck.setDirectoryPath(directoryPath);
			Entity parentEntity = entity.getParent();
			if(parentEntity == null) {
				// 该java文件没有显式声明packge，为default包
//				pck.setEntityId(entityRepo.generateId().longValue());
				pck.setEntityId(generateEntityId());
				pck.setName(Package.JAVA_PACKAGE_DEFAULT);
			} else {
				pck.setEntityId(parentEntity.getId().longValue());
				pck.setName(parentEntity.getQualifiedName());
			}
			addNode(pck, currentProject);
			Contain projectContainsPackage = new Contain(currentProject, pck);
			addRelation(projectContainsPackage);
		}
		Contain containFile = new Contain(pck, file);
		addRelation(containFile);
		setTypeByteCodeName((FileEntity) entity);
		return file;
	}
	
	private Function process(FunctionEntity entity) {
		Function function = new Function();
		function.setImpl(true);
		function.setLanguage(Language.java.name());
		String functionName = entity.getQualifiedName();
		if(functionName.contains(".")) {
			String[] functionNameSplit = functionName.split("\\.");
			if(functionNameSplit.length >= 2) {
				function.setConstructor(functionNameSplit[functionNameSplit.length - 1].equals(functionNameSplit[functionNameSplit.length - 2]));
			}
		}
		function.setSimpleName(entity.getRawName().getName());
		function.setName(functionName);
		function.setEntityId(entity.getId().longValue());
		function.setStartLine(entity.getStartLine() == null ? -1 : entity.getStartLine());
		function.setEndLine(entity.getEndLine() == null ? -1 : entity.getEndLine());
		addNode(function, currentProject);
		return function;
	}
	
	private Variable process(VarEntity entity) {
		Variable variable = new Variable();
		variable.setLanguage(Language.java.name());
		variable.setEntityId(entity.getId().longValue());
		String typeIdentify = ((VarEntity) entity).getRawType().getName();
		List<GenericName> varArguments = ((VarEntity) entity).getRawName().getArguments();
		if (varArguments != null && !varArguments.isEmpty()){
			typeIdentify += getTypeIdentifyOfVar(varArguments);
		}
		variable.setTypeIdentify(typeIdentify);
		variable.setName(entity.getQualifiedName());
		variable.setSimpleName(entity.getRawName().getName());
		addNode(variable, currentProject);
		return variable;
	}

	
	private Type process(TypeEntity entity) {
		Type type = new Type();
		type.setLanguage(Language.java.name());
		type.setEntityId(entity.getId().longValue());
		type.setName(entity.getQualifiedName());
		type.setSimpleName(entity.getRawName().getName());
		type.setStartLine(entity.getStartLine() == null ? -1 :entity.getStartLine());
		type.setEndLine(entity.getEndLine() == null ? -1 :entity.getEndLine());
		addNode(type, currentProject);
		return type;
	}
	
	@Override
	protected void addNodesWithContainRelations() {
		entityRepo.entityIterator().forEachRemaining(entity -> {
			// 每个entity对应相应的node
			if(entity instanceof PackageEntity) {
			} else if(entity instanceof FileEntity) {
				process((FileEntity) entity);
			} else if(entity instanceof FunctionEntity) {
				process((FunctionEntity) entity);
			} else if(entity instanceof VarEntity) {
				process((VarEntity) entity);
			} else if(entity.getClass() == TypeEntity.class) {
				process((TypeEntity) entity);
			}
		});

//		addEmptyPackages();
		addCommonParentEmptyPackages();
		
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Type, currentProject).forEach((entityId, node) -> {
			Type type = (Type) node;
			Entity typeEntity = entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = typeEntity.getParent();
			while(!(parentEntity instanceof FileEntity)) {
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain fileContainsType = new Contain(parentNode, node);
			addRelation(fileContainsType);
			type.setName(typeEntityName.get(entityId.intValue()));
			type.setAliasName(typeEntityName.get(entityId.intValue()));
			processIdentifier(type);
			while(!(parentEntity instanceof FileEntity)) {
				// 找出方法所在的文件
				parentEntity = parentEntity.getParent();
			}
			ProjectFile file = (ProjectFile) this.getNodes().findNodeByEntityIdInProject(parentEntity.getId().longValue(), currentProject);
			this.getNodes().putNodeToFileByEndLine(file, type);
		});
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.Function, currentProject).forEach((entityId, node) -> {
			Function function = (Function) node;
			FunctionEntity functionEntity = (FunctionEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = functionEntity.getParent();
			while(parentEntity.getClass() != TypeEntity.class) {
				// 方法内的匿名类的方法的parentEntity是该方法
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			Contain typeContainsFunction = new Contain(parentNode, function);
			addRelation(typeContainsFunction);
			for(VarEntity varEntity : functionEntity.getParameters()) {
				String parameterName = varEntity.getRawType().getName();
				TypeEntity typeEntity = varEntity.getType();
				// 方法的参数
				if(!StringUtils.isBlank(varEntity.getTypeIdentifier())) {
					function.addParameterIdentifiers(varEntity.getTypeIdentifier());
				} else {
					if(typeEntity != null 
//						&& Inferer.externalType != typeEntity
							&& Inferer.buildInType != typeEntity
							&& Inferer.genericParameterType != typeEntity
							&& this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, parentEntity.getId().longValue(), currentProject) != null) {
						function.addParameterIdentifiers(parameterName);
					} else {
						function.addParameterIdentifiers(parameterName);
					}
				}
			}
			Type type = (Type) findNodeByEntityIdInProject(parentEntity);
			String newFunctionName = null;
			if(function.isConstructor()) {
				newFunctionName = type.getName();
			} else {
				newFunctionName = type.getName() + "." + function.getSimpleName();
			}
			function.setName(newFunctionName);
			processIdentifier(function);
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
			VarEntity varEntity = (VarEntity) entityRepo.getEntity(entityId.intValue());
			Entity parentEntity = varEntity.getParent();
			while (parentEntity != null && !(parentEntity instanceof FunctionEntity || parentEntity.getClass() == TypeEntity.class)) {
				/// FIXME 内部类的情况暂不考虑
				parentEntity = parentEntity.getParent();
			}
			Node parentNode = findNodeByEntityIdInProject(parentEntity);
			if(parentNode instanceof Type) { 
				((Variable) node).setMemberVariable(true);
			}
			Contain contain = new Contain(parentNode, node);
			addRelation(contain);
			processIdentifier((Variable) node);
		});
	}
	
	private Map<Integer, String> typeEntityName = new HashMap<>();
	
	private void setTypeByteCodeName(FileEntity fileEntity) {
		for(TypeEntity e : fileEntity.getDeclaredTypes()) {
			String name = test(e, fileEntity);
			if(name != null) {
				typeEntityName.put(e.getId(), name);
			} else {
				typeEntityName.put(e.getId(), e.getQualifiedName());
			}
		}
	}
	
	private int countIndex(FileEntity fileEntity, TypeEntity type) {
		int index = 0;
		for(TypeEntity e : fileEntity.getDeclaredTypes()) {
			if(e.getQualifiedName().equals(type.getQualifiedName())) {
				index++;
			}
			if(e.getId().equals(type.getId())) {
				break;
			}
		}
		return index;
	}
	
	private String test(TypeEntity typeEntity, FileEntity fileEntity) {
		Entity parent = typeEntity.getParent();
		if(parent == null) {
			return null;
		}
		if(parent instanceof FileEntity) {
			return typeEntity.getQualifiedName();
		}
		if(parent.getClass() == TypeEntity.class) {
			int index = countIndex(fileEntity, typeEntity);
			return test((TypeEntity) parent, fileEntity) + "$" + index + typeEntity.getRawName().getName();
		}
		if(parent instanceof FunctionEntity) {
			do {
				parent = parent.getParent();
			} while(parent != null && parent.getClass() != TypeEntity.class);
//			parent = parent.getParent();
			if(parent == null) {
				return null;
			}
			return test((TypeEntity) parent, fileEntity) + "$" + countIndex(fileEntity, (TypeEntity) parent) + typeEntity.getRawName().getName();
		}
		return null;
	}
	
	@Override
	protected void addRelations() {
		extractRelationsFromTypes();
		extractRelationsFromFunctions();
		extractRelationsFromVariables();		
		extractRelationsFromFiles();
	}
	
	/**
	 * java中文件的import关系
	 */
	protected void extractRelationsFromFiles() {
		LOGGER.info("{} {} file extractRelationsFromFiles", this.currentProject.getName(), this.currentProject.getLanguage());
		this.getNodes().findNodesByNodeTypeInProject(NodeLabelType.ProjectFile, currentProject).forEach((entityId, node) -> {
			ProjectFile file = (ProjectFile) node;
			FileEntity fileEntity = (FileEntity) entityRepo.getEntity(entityId.intValue());
			fileEntity.getImportedTypes().forEach(entity -> {
				if(entity instanceof FunctionEntity) {
					Function function = (Function) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Function, entity.getId().longValue(), currentProject);
					if(function != null) {
						Import fileImportFunction = new Import(file, function);
						addRelation(fileImportFunction);
					}
				} else if(entity instanceof VarEntity) {
					Variable variable = (Variable) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Variable, entity.getId().longValue(), currentProject);
					if(variable != null) {
						Import fileImportVariable = new Import(file, variable);
						addRelation(fileImportVariable);
					}
				} else if(entity.getClass() == TypeEntity.class) {
					Type type = (Type) this.getNodes().findNodeByEntityIdInProject(NodeLabelType.Type, entity.getId().longValue(), currentProject);
					if(type != null) {
						Import fileImportType = new Import(file, type);
						addRelation(fileImportType);
					}
				} else {
					// MultiDeclareEntities
					//System.out.println("extractRelationsFromFiles() " + fileEntity + " " + entity.getClass() + " " + entity.toString());
				}
			});
		});
	}
}
