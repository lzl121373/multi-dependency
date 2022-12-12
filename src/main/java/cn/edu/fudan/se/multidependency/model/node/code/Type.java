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
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class Type extends CodeUnit {
	
	private static final long serialVersionUID = 6805501035295416590L;
	
    @Id
    @GeneratedValue
    private Long id;

	private String name;
	
	private String aliasName;
	
    private Long entityId;
    
    private String identifier;
    
    private String simpleName;
	
	private int startLine = -1;
	
	private int endLine = -1;
    
    private String language;

	private boolean isAlias = false;
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("aliasName", getAliasName() == null ? (getName() == null ? "" : getName()) : getAliasName());
		properties.put("identifier", getIdentifier() == null ? "" : getIdentifier());
		properties.put("simpleName", getSimpleName() == null ? "" : getSimpleName());
		properties.put("startLine", getStartLine());
		properties.put("endLine", getEndLine());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("isAlias", isAlias());
		return properties;
	}
	
	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Type;
	}

	
	@Override
	public String getIdentifierSuffix() {
		return Constant.CODE_NODE_IDENTIFIER_SUFFIX_TYPE;
	}

	@Override
	public String getIdentifierSimpleName() {
		return getSimpleName();
	}

}
