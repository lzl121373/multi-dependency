package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.*;

import cn.edu.fudan.se.multidependency.model.Language;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Project implements Node {
	
	private static final long serialVersionUID = 4058945695982024026L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	private String name;

	private String path;

	private String language;
	
	@Transient
	private String microserviceName;

	private Long entityId;

	private int nop;

	private int nof;

	private int noc;

	private int nom;

	private long loc;

	private long lines;

	private int commits = -1;

	private double modularity = -1.0;

	public Project(String name, String path, Language language) {
		super();
		this.name = name;
		this.path = path;
		this.language = language.toString();
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("path", getPath() == null ? "" : getPath());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("name", getName() == null ? "" : getName());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Project;
	}

}
