package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import lombok.Getter;
import lombok.Setter;

public class FileGraph {

	@Getter
	@Setter
	private List<ProjectFile> files = new ArrayList<>();

	@Getter
	@Setter
	private Collection<Clone> fileClones = new ArrayList<>();
	
	@Setter
	private ContainRelationService containRelationService;
	
	public JSONObject matrixForClone() {
		JSONObject result = new JSONObject();
		Set<ProjectFile> filesSet = new HashSet<>();
		for(Clone clone : fileClones) {
			filesSet.add((ProjectFile) clone.getCodeNode1());
			filesSet.add((ProjectFile) clone.getCodeNode2());
		}
		List<ProjectFile> files = new ArrayList<>(filesSet);
		FileCloneType[][] fileClonesMatrix = new FileCloneType[files.size()][files.size()];
		files.sort((file1, file2) -> {
			return file1.getPath().compareTo(file2.getPath());
		});
		for(int i = 0; i < files.size(); i++) {
			for(int j = 0; j < files.size(); j++) {
				fileClonesMatrix[i][j] = new FileCloneType();
			}
		}
		Map<ProjectFile, Integer> fileToIndex = new HashMap<>();
		List<Integer> differentPackages = new ArrayList<>();
		Package pck = null;
		for(int i = 0; i < files.size(); i++) {
			fileToIndex.put(files.get(i), i);
			if(pck == null) {
				pck = containRelationService.findFileBelongToPackage(files.get(i));
			} else {
				Package temp = containRelationService.findFileBelongToPackage(files.get(i));
				if(!temp.equals(pck)) {
					differentPackages.add(i);
					pck = temp;
				}
			}
		}
		for(Clone clone : fileClones) {
			ProjectFile file1 = (ProjectFile) clone.getCodeNode1();
			ProjectFile file2 = (ProjectFile) clone.getCodeNode2();
			int index1 = fileToIndex.get(file1);
			int index2 = fileToIndex.get(file2);
			fileClonesMatrix[index1][index2].setType(clone.getCloneType());
			fileClonesMatrix[index2][index1].setType(clone.getCloneType());
			boolean isDifferentPackage = containRelationService.isDifferentPackage(file1, file2);
			fileClonesMatrix[index1][index2].setDifferentPackage(isDifferentPackage);
			fileClonesMatrix[index2][index1].setDifferentPackage(isDifferentPackage);
		}
		result.put("packageIndex", differentPackages);
		result.put("data", fileClonesMatrix);
		return result;
	}
	
}
