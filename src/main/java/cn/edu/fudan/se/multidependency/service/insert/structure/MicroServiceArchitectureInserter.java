package cn.edu.fudan.se.multidependency.service.insert.structure;

import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.config.MicroServiceDependency;

public class MicroServiceArchitectureInserter extends ExtractorForNodesAndRelationsImpl {
	
	private Iterable<MicroServiceDependency> microServiceDependencies = new ArrayList<>();
	
	public MicroServiceArchitectureInserter(Iterable<MicroServiceDependency> microServiceDependencies) {
		this.microServiceDependencies = microServiceDependencies;
	}

	@Override
	public void addNodesAndRelations() throws Exception {
		for(MicroServiceDependency dependency : microServiceDependencies) {
			System.out.println(dependency);
			String microServiceName = dependency.getMicroService();
			MicroService microService = this.getNodes().findMicroServiceByName(microServiceName);
			if(microService == null) {
				throw new Exception(microServiceName + " is null");
			}
			List<String> depends = dependency.getDependencies();
			for(String depend : depends) {
				MicroService dependOnMicroService = this.getNodes().findMicroServiceByName(depend);
				if(dependOnMicroService == null) {
					throw new Exception(depend + " is null");
				}
				MicroServiceDependOnMicroService dependOn = new MicroServiceDependOnMicroService(microService, dependOnMicroService);
				addRelation(dependOn);
			}
		}
	}

}
