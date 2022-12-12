package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CALL)
@EqualsAndHashCode
public class Call implements RelationWithTimes, StructureRelation {
	
	private static final long serialVersionUID = 5982413005555063698L;

	@StartNode
	private CodeNode callerNode;
	
	@EndNode
	private Function callFunction;
	
	private int times = 1;
	
	public Call(CodeNode callerNode, Function callFunction) {
		super();
		this.callerNode = callerNode;
		this.callFunction = callFunction;
		this.times = 1;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public CodeNode getStartCodeNode() {
		return callerNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return callFunction;
	}
	
	public void addTimes() {
		this.times++;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CALL;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
}
