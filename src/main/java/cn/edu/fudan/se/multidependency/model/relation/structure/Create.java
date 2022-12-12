package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CREATE)
@EqualsAndHashCode
public class Create implements RelationWithTimes, StructureRelation {

	private static final long serialVersionUID = -5250486130542068001L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private CodeNode callerNode;
	
	@EndNode
	private Type createType;
	
	private int times = 1;
	
	public Create(CodeNode callerNode, Type createType) {
		super();
		this.callerNode = callerNode;
		this.createType = createType;
		this.times = 1;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CREATE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}

	@Override
	public CodeNode getStartCodeNode() {
		return callerNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return createType;
	}

	@Override
	public void addTimes() {
		this.times++;
	}
	
	
}
