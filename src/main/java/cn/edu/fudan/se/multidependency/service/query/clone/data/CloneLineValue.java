package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import lombok.Data;

/**
 * 一个项目或者一个微服务项目
 * @author fan
 *
 * @param <P>
 */
@Data
public class CloneLineValue<P> implements Serializable {
	
	private static final long serialVersionUID = -5684168202304114767L;

	public CloneLineValue(P project) {
		this.project = project;
	}
	
	private P project;

	private Set<ProjectFile> allFiles = new HashSet<>();
	
	private Map<CloneRelationType, Set<CodeNode>> cloneNodes = new HashMap<>();
	
	private long allFilesLines = 0;
	
	public void addAllFiles(Collection<ProjectFile> files) {
		this.allFiles.addAll(files);
		for(ProjectFile file : files) {
			this.allFilesLines += file.getLines();
		}
	}
	
	public void addCloneNode(CloneRelationType type, CodeNode node) {
		Set<CodeNode> nodes = cloneNodes.getOrDefault(type, new HashSet<>());
		nodes.add(node);
		this.cloneNodes.put(type, nodes);
	}
	
	public void addCloneRelation(Clone clone) {
		addCloneNode(CloneRelationType.valueOf(clone.getCloneRelationType()), clone.getCodeNode1());
		addCloneNode(CloneRelationType.valueOf(clone.getCloneRelationType()), clone.getCodeNode2());
	}
	
}
