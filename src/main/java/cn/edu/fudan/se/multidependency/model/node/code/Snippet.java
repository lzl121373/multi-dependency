package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.CodeUnit;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Snippet extends CodeUnit {
	
	private static final long serialVersionUID = -2425172282148281962L;

	@Id
    @GeneratedValue
    private Long id;

	private String name;
	
	private Long entityId;
    
	private int startLine = -1;
	
	private int endLine = -1;
	
	private String identifier;
    
    private String language;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("startLine", getStartLine());
		properties.put("endLine", getEndLine());
		properties.put("identifier", getIdentifier() == null ? "" : getIdentifier());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Snippet;
	}

	@Override
	public String getIdentifierSimpleName() {
		return getName();
	}

	@Override
	public String getIdentifierSuffix() {
		return Constant.CODE_NODE_IDENTIFIER_SUFFIX_SNIPPET;
	}

}
