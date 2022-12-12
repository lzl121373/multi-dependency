package cn.edu.fudan.se.multidependency.model.node.lib;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Library implements Node {

	private static final long serialVersionUID = -3952639891369830644L;
	
	@Id
    @GeneratedValue
    private Long id;

	private Long entityId;
	
	private String version;
	
	private String groupId;
	
	private String name;
	
	private String fullName;
	
	public boolean isSameLibDifferentVersion(Library other) {
		if(other == null) {
			return false;
		}
		return this.getGroupId().equals(other.getGroupId()) 
				&& this.getName().equals(other.getName())
				&& !this.getVersion().equals(other.getVersion());
	}
	
	public String groupIdAndName() {
		return groupId + "." + name;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("fullName", getFullName() == null ? "" : getFullName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("version", getVersion() == null ? "" : getVersion());
		properties.put("groupId", getGroupId() == null ? "" : getGroupId());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Library;
	}

}
