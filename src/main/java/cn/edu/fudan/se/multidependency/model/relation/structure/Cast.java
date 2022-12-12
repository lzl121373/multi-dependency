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
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CAST)
public class Cast implements RelationWithTimes, StructureRelation {

	private static final long serialVersionUID = 5982413005555063698L;

	@Id
    @GeneratedValue
    private Long id;

	@StartNode
	private CodeNode startNode;
	
	@EndNode
	private Type castType;
	
	private int times = 1;

	public Cast(CodeNode startNode, Type castType) {
		super();
		this.startNode = startNode;
		this.castType = castType;
	}

	@Override
	public CodeNode getStartCodeNode() {
		return startNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return castType;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CAST;
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
