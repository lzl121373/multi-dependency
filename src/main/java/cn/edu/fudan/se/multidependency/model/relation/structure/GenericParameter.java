package cn.edu.fudan.se.multidependency.model.relation.structure;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 变量以类型作为类型参数的关系
 * @author zdh
 *
 */
@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_GENERIC_PARAMETER)
public class GenericParameter implements RelationWithTimes, StructureRelation {

	private static final long serialVersionUID = -2364382655805821358L;

	@Id
    @GeneratedValue
    private Long id;

	@StartNode
	private CodeNode codeNode;

	@EndNode
	private Type genericParameterType;

	private int times = 1;

	public GenericParameter(CodeNode codeNode, Type genericParameterType) {
		super();
		this.codeNode = codeNode;
		this.genericParameterType = genericParameterType;
	}

	@Override
	public CodeNode getStartCodeNode() {
		return codeNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return genericParameterType;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.GENERIC_PARAMETER;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
	
	public void addTimes() {
		this.times++;
	}

}
