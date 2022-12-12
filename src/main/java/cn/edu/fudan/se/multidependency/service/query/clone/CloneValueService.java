package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChangeMatrix;

public interface CloneValueService {
	
	/**
	 * 根据函数间的克隆找出微服务间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<MicroService>> findMicroServiceCloneFromFunctionClone(Collection<Clone> functionClones);
	
	/**
	 * 根据文件间的克隆找出微服务间的克隆
	 * @param fileClones
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<MicroService>> findMicroServiceCloneFromFileClone(Collection<Clone> fileClones);
	
	/**
	 * 根据函数间的克隆找出项目间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<Project>> findProjectCloneFromFunctionClone(Collection<Clone> functionClones);
	
	/**
	 * 根据文件间的克隆找出项目间的克隆
	 * @param fileClones
	 * @return
	 */
	Collection<CloneValueForDoubleNodes<Project>> queryProjectCloneFromFileClone(Collection<Clone> fileClones);
	
	Map<Package, Map<Package, CloneValueForDoubleNodes<Package>>> queryPackageCloneFromFileClone(Collection<Clone> fileClones);
	
	Collection<CloneValueForDoubleNodes<Package>> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones);
	
	default Collection<CloneValueForDoubleNodes<Package>> queryPackageCloneFromFileClone(Collection<Clone> fileClones, List<Package> pcks) {
		if(pcks == null || pcks.isEmpty()) {
			return new ArrayList<>();
		}
		List<CloneValueForDoubleNodes<Package>> result = new ArrayList<>();
		for(int i = 0; i < pcks.size(); i++) {
			for(int j = i + 1; j < pcks.size(); j++) {
				CloneValueForDoubleNodes<Package> queryResult = queryPackageCloneFromFileCloneSort(fileClones, pcks.get(i), pcks.get(j));
				if(queryResult != null){
					result.add(queryResult);
				}
			}
		}
		
		return result;
	}

	CloneValueForDoubleNodes<Package> queryPackageCloneFromFileCloneSort(Collection<Clone> fileClones, Package pck1, Package pck2);
	
	PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<Clone> fileClones, Package pck1, Package pck2) throws Exception;

	PackageCloneValueWithFileCoChangeMatrix queryPackageCloneWithFileCoChangeMatrix(Collection<Clone> fileClones, Package pck1, Package pck2);
	
}
