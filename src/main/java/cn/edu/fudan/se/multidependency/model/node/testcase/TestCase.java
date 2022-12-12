package cn.edu.fudan.se.multidependency.model.node.testcase;

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
@EqualsAndHashCode
@NoArgsConstructor
public class TestCase implements Node {

	private static final long serialVersionUID = 7817933207475762644L;
	
	private String name;
	
	private String inputContent;
	
	private boolean success;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private Integer testCaseId;
    
    private String description;
    
    /**
     * 测试用例分组
     */
    private String group;
    public static final String DEFAULT_GROUP = "default";
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("success", isSuccess());
		properties.put("inputContent", getInputContent() == null ? "" : getInputContent());
		properties.put("testCaseId", getTestCaseId() == null ? -1 : getTestCaseId());
		properties.put("description", getDescription() == null ? "" : getDescription());
		properties.put("group", getGroup() == null ? DEFAULT_GROUP : getGroup());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.TestCase;
	}
	
}
