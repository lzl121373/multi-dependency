package cn.edu.fudan.se.multidependency.model.relation.git;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DEVELOPER_REPORT_ISSUE)
public class DeveloperReportIssue implements Relation {

    private static final long serialVersionUID = -9113367528187930324L;

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Developer developer;

    @EndNode
    private Issue issue;

    public DeveloperReportIssue(Developer developer, Issue issue){
        this.developer = developer;
        this.issue = issue;
    }

    @Override
    public Node getStartNode() {
        return developer;
    }

    @Override
    public Node getEndNode() {
        return issue;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.DEVELOPER_REPORT_ISSUE;
    }

    @Override
    public Map<String, Object> getProperties() {
        return new HashMap<>();
    }
}
