package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@EqualsAndHashCode
@NoArgsConstructor
public class Variable implements CodeNode {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private static final long serialVersionUID = 7656480620809763012L;

	private String name;
	
	private String simpleName;

	private Long entityId;
	
	private String typeIdentify;
    
    private String identifier;
	
	private int startLine = -1;
	
	private int endLine = -1;
	
	/**
	 * 是否为类的成员变量
	 */
	private boolean isMemberVariable;

	/**
	 * 是否为文件下的变量，定义为全局变量
	 */
	private boolean isGlobalVariable;
    
    private String language;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("simpleName", getSimpleName() == null ? "" : getSimpleName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("typeIdentify", getTypeIdentify() == null ? "" : getTypeIdentify());
		properties.put("isMemberVariable", isMemberVariable());
		properties.put("isGlobalVariable", isGlobalVariable());
		properties.put("identifier", getIdentifier() == null ? "" : getIdentifier());
		properties.put("startLine", getStartLine());
		properties.put("endLine", getEndLine());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Variable;
	}
	
	@Override
	public String getIdentifierSuffix() {
		return Constant.CODE_NODE_IDENTIFIER_SUFFIX_VARIABLE;
	}

	@Override
	public String getIdentifierSimpleName() {
		return getSimpleName();
	}
	
}
