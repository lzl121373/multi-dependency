package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_INCLUDE)
public class Include implements StructureRelation {

	private static final long serialVersionUID = 364395424089272866L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private ProjectFile start;
	
	@EndNode
	private ProjectFile end;

	public Include(ProjectFile start, ProjectFile end) {
		super();
		this.start = start;
		this.end = end;
	}
	
	@Override
	public CodeNode getStartCodeNode() {
		return start;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return end;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.INCLUDE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
