package cn.edu.fudan.se.multidependency.model.relation.git;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DEVELOPER_SUBMIT_COMMIT)
public class DeveloperSubmitCommit implements Relation {

    private static final long serialVersionUID = 3429048638016401498L;

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Developer developer;

    @EndNode
    private Commit commit;

    public DeveloperSubmitCommit(Developer developer, Commit commit){
        this.developer = developer;
        this.commit = commit;
    }

    @Override
    public Node getStartNode() {
        return developer;
    }

    @Override
    public Node getEndNode() {
        return commit;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.DEVELOPER_SUBMIT_COMMIT;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>();
    }
}
