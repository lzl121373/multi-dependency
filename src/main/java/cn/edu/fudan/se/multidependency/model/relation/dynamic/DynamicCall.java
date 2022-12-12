package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DYNAMIC_CALL)
public class DynamicCall implements DynamicCallFunctionByTestCase {

	private static final long serialVersionUID = -7640490954063715746L;
	
	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	public DynamicCall(Function function, Function callFunction, String projectName, String language) {
		super();
		this.function = function;
		this.callFunction = callFunction;
		this.projectName = projectName;
		this.language = language;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	private String projectName;
	
	private String language;
	
	private String order;
	
	private Long fromOrder;
	
	private Long toOrder;
	
	private Long fromDepth;
	
	private Long toDepth;
	
	private String traceId;
	
	private String spanId;
	
	private Integer testCaseId;
	
	@Override
	public Node getStartNode() {
		return function;
	}

	@Override
	public Node getEndNode() {
		return callFunction;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.DYNAMIC_CALL;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("projectName", getProjectName() == null ? "" : getProjectName());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("order", getOrder() == null ? "" : getOrder());
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		properties.put("spanId", getSpanId() == null ? "" : getSpanId());
		properties.put("fromOrder", getFromOrder() == null ? -1 : getFromOrder());
		properties.put("toOrder", getToOrder() == null ? -1 : getToOrder());
		properties.put("fromDepth", getFromDepth() == null ? -1 : getFromDepth());
		properties.put("toDepth", getToDepth() == null ? -1 : getToDepth());
		properties.put("testCaseId", getTestCaseId() == null ? -1 : getTestCaseId());
		return properties;
	}

}
