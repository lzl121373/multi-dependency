package cn.edu.fudan.se.multidependency.model.node.clone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
public class CloneGroup implements Node {
	
	private static final long serialVersionUID = -8494229666439859350L;

	@Id
    @GeneratedValue
    private Long id;
	
	private String name;

	private Long entityId;

	private String project;

	private int size;
	
	private String language;
	
	private String cloneLevel;

	private SmellType type;
	
	public CloneGroup(String name) {
		this.name = name;
		this.size = -1;
		this.entityId = -1L;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("project", getProject() == null ? "" : getProject());
		properties.put("size", getSize());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("cloneLevel", getCloneLevel() == null ? "" : getCloneLevel());
		properties.put("type", getType() == null ? "" : getType());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.CloneGroup;
	}

	@Transient
	private Set<CodeNode> nodes = new HashSet<>();

	@Transient
	private Set<Clone> relations = new HashSet<>();
	
	public synchronized void addNode(CodeNode node) {
		this.nodes.add(node);
	}
	
	public synchronized void addRelation(Clone relation) {
		this.relations.add(relation);
	}
	
	public int sizeOfNodes() {
		return this.nodes.size();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloneGroup other = (CloneGroup) obj;
		if (cloneLevel == null) {
			if (other.cloneLevel != null)
				return false;
		} else if (!cloneLevel.equals(other.cloneLevel))
			return false;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cloneLevel == null) ? 0 : cloneLevel.hashCode());
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

}
