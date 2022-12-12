package cn.edu.fudan.se.multidependency.model.relation.lib;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_CALL_LIBRARY_API)
public class FunctionCallLibraryAPI implements Relation {

	private static final long serialVersionUID = 284658643353415139L;

	@StartNode
	private Function function;
	
	@EndNode
	private LibraryAPI api;
	
	public FunctionCallLibraryAPI(Function function, LibraryAPI api) {
		this.function = function;
		this.api = api;
	}
	
	@Id
    @GeneratedValue
    private Long id;
	
	private int times;
	
	@Override
	public Node getStartNode() {
		return function;
	}

	@Override
	public Node getEndNode() {
		return api;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CALL_LIBRARY_API;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}

}
