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
@RelationshipEntity(RelationType.str_IMPORT)
public class Import implements StructureRelation {
	private static final long serialVersionUID = 1908046997347759875L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private ProjectFile file;

	@EndNode
	private CodeNode importCodeNode;

	public Import(ProjectFile file, CodeNode importCodeNode) {
		super();
		this.file = file;
		this.importCodeNode = importCodeNode;
	}

	@Override
	public CodeNode getStartCodeNode() {
		return file;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return importCodeNode;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.IMPORT;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
}
