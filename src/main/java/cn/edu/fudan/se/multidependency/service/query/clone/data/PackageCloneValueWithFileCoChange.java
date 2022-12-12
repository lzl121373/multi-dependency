package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;

@Data
public class PackageCloneValueWithFileCoChange implements Serializable {

	private static final long serialVersionUID = 1287543734251562271L;
	
	private Package pck1;
	
	private Package pck2;
	
	// 两个克隆节点内部的克隆关系
	protected List<FileCloneWithCoChange> children = new ArrayList<>();
	
	private Set<ProjectFile> cloneFiles1 = new HashSet<>();
	
	private Set<ProjectFile> cloneFiles2 = new HashSet<>();

	private Set<ProjectFile> noneCloneFiles1 = new HashSet<>();

	private Set<ProjectFile> noneCloneFiles2 = new HashSet<>();

	private Set<ProjectFile> allFiles1 = new HashSet<>();
	
	private Set<ProjectFile> allFiles2 = new HashSet<>();
	
	public void addCloneFile1(ProjectFile file) {
		this.cloneFiles1.add(file);
	}
	
	public void addCloneFile2(ProjectFile file) {
		this.cloneFiles2.add(file);
	}
	
	public void addFile1(ProjectFile file) {
		this.allFiles1.add(file);
	}
	
	public void addFile2(ProjectFile file) {
		this.allFiles2.add(file);
	}

	public void addNoneCloneFiles1(Collection<ProjectFile> files) {
		this.noneCloneFiles1.addAll(files);
	}
	public void addNoneCloneFiles2(Collection<ProjectFile> files) {
		this.noneCloneFiles2.addAll(files);
	}

	public void addFile1(Collection<ProjectFile> files) {
		this.allFiles1.addAll(files);
	}

	public void addFile2(Collection<ProjectFile> files) {
		this.allFiles2.addAll(files);
	}
	
	public void sortChildren() {
		children.sort((clone1, clone2) -> {
			return clone2.getCochangeTimes() - clone1.getCochangeTimes();
		});
	}

	public void calculateNoneClone() {
		Set<ProjectFile> cloneFiles1 = this.getCloneFiles1();
		Set<ProjectFile> cloneFiles2 = this.getCloneFiles2();
		Set<ProjectFile> allFiles1 = this.getAllFiles1();
		Set<ProjectFile> allFiles2 = this.getAllFiles2();
		allFiles1.removeAll(cloneFiles1);
		allFiles2.removeAll(cloneFiles2);

		for(ProjectFile projectFile: allFiles1){
			noneCloneFiles1.add(projectFile);
		}

		for(ProjectFile projectFile: allFiles2){
			noneCloneFiles2.add(projectFile);
		}

	}
	
	public void addChild(FileCloneWithCoChange child) {
		this.children.add(child);
	}
	
}
