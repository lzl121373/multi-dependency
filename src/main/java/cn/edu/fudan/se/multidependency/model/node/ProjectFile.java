package cn.edu.fudan.se.multidependency.model.node;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.config.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ProjectFile extends CodeUnit {
	
	private static final long serialVersionUID = -8736926263545574636L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;

	private String projectBelongPath;

	private String name;
	
	private String path;
	
	private String suffix;
	
	private int endLine = -1;

	private int noc;

	private int nom;

	private int loc = -1;
	
	private double score = -1;
    
    private String language;
    
    private double instability = -1;
	
	public int getStartLine() {
		return 1;
	}
	
	public static final String SUFFIX_JAVA = ".java";
	
	public ProjectFile(Long entityId, String name, String path, String suffix) {
		this.entityId = entityId;
		this.name = name;
		this.path = path;
		this.suffix = suffix;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("projectBelongPath", getProjectBelongPath() == null ? "" : getProjectBelongPath());
		properties.put("path", getPath() == null ? "" : getPath());
		properties.put("suffix", getSuffix() == null ? "" : getSuffix());
		properties.put("endLine", getEndLine());
		properties.put("loc", getLoc());
		properties.put("score", getScore());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("instability", getInstability());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.ProjectFile;
	}

	@Override
	public String getIdentifier() {
		return path;
	}

	@Override
	public String getIdentifierSimpleName() {
		return path;
	}

	@Override
	public String getIdentifierSuffix() {
		return Constant.CODE_NODE_IDENTIFIER_SUFFIX_FILE;
	}

	@Override
	public void setIdentifier(String identifier) {
		// do nothing
	}

}
