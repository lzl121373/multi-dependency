package cn.edu.fudan.se.multidependency.service.insert.code;

import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.config.ProjectConfig;
import depends.entity.Entity;
import lombok.Setter;

/**
 * @author fan
 *
 */
public abstract class BasicCodeExtractorForNeo4jServiceImpl extends ExtractorForNodesAndRelationsImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicCodeExtractorForNeo4jServiceImpl.class);

	public BasicCodeExtractorForNeo4jServiceImpl(ProjectConfig projectConfig) {
		super();
		this.isMicroservice = projectConfig.isMicroService();
		this.serviceGroupName = projectConfig.getServiceGroupName();
		this.currentProject = new Project(FileUtil.extractFilePathName(projectConfig.getPath()), "/" + FileUtil.extractFilePathName(projectConfig.getPath()), projectConfig.getLanguage());
		this.microserviceName = projectConfig.getMicroserviceName();
		if(StringUtils.isBlank(microserviceName)) {
			this.microserviceName = FileUtil.extractFilePathName(projectConfig.getPath());
		}
		this.currentProject.setMicroserviceName(microserviceName);
	}

	protected Project currentProject;
	
	protected String microserviceName;
	
	protected boolean isMicroservice;
	
	protected String serviceGroupName;
	
	@Setter
	protected RestfulAPIFileExtractor restfulAPIFileExtractor;
	
	@Override
	public void addNodesAndRelations() throws Exception {
		currentProject.setEntityId(generateEntityId());
		addNode(currentProject, currentProject);
		if(isMicroservice) {
			// 被分析的项目是微服务项目，生成一个MicroService节点
			MicroService microService = this.getNodes().findMicroServiceByName(microserviceName);
			if(microService == null) {
				microService = new MicroService();
				microService.setEntityId(generateEntityId());
				microService.setName(microserviceName);
				microService.setServiceGroupName(serviceGroupName);
				addNode(microService, null);
				LOGGER.info("添加一个微服务：" + microService.getName());
			}
			Contain contain = new Contain(microService, currentProject);
			addRelation(contain);
		}
		
		if(restfulAPIFileExtractor != null) {
			MicroService currentProjectBelongToMicroService = null;
			if(isMicroservice) {
				currentProjectBelongToMicroService = this.getNodes().findMicroServiceByName(microserviceName);
			}
			Iterable<RestfulAPI> apis = restfulAPIFileExtractor.extract();
			for(RestfulAPI api : apis) {
				api.setEntityId(generateEntityId());
				addNode(api, currentProject);
				Contain projectContainAPI = new Contain(currentProject, api);
				addRelation(projectContainAPI);
				if(currentProjectBelongToMicroService != null) {
					Contain microServiceContainAPI = new Contain(currentProjectBelongToMicroService, api);
					addRelation(microServiceContainAPI);
				}
			}
			
		}
	}
	protected Node findNodeByEntityIdInProject(Entity entity) {
		if(entity == null) {
			return null;
		}
		return this.getNodes().findNodeByEntityIdInProject(entity.getId().longValue(), currentProject);
	}
}
