package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 方法访问属性关系
 * @author fan
 *
 */
@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_ACCESS)
@EqualsAndHashCode
public class Access implements RelationWithTimes, StructureRelation {
	
	private static final long serialVersionUID = -2911695752320415027L;

	@StartNode
	private CodeNode startNode;
	
	@EndNode
	private Variable field;
	
	private int times = 1;

	@Id
    @GeneratedValue
    private Long id;
	
	public Access(CodeNode startNode, Variable field) {
		this.startNode = startNode;
		this.field = field;
		this.times = 1;
	}
	
	public void addTimes() {
		this.times++;
	}

	@Override
	public CodeNode getStartCodeNode() {
		return startNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return field;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.ACCESS;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
	
}
