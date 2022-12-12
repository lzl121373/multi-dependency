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
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_COMMIT_INHERIT_COMMIT)
public class CommitInheritCommit implements Relation {

    private static final long serialVersionUID = 1232885772212330907L;

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Commit start;

    @EndNode
    private Commit end;

    public CommitInheritCommit(Commit start, Commit end){
        this.start = start;
        this.end = end;
    }

    @Override
    public Node getStartNode() {
        return start;
    }

    @Override
    public Node getEndNode() {
        return end;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.COMMIT_INHERIT_COMMIT;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>();
    }
}
