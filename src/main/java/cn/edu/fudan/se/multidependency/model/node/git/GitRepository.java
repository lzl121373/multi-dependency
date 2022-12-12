package cn.edu.fudan.se.multidependency.model.node.git;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.HashMap;
import java.util.Map;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class GitRepository implements Node {

    private static final long serialVersionUID = -892029084753695908L;

    @Id
    @GeneratedValue
    private Long id;

    private Long entityId;

    private String name;
    
    private String gitPath;
    
    private String path;

    public GitRepository(Long entityId, String name, String gitPath, String path){
        this.entityId = entityId;
        this.name = name;
        this.gitPath = gitPath;
        this.path = path;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
        properties.put("name", getName() == null ? "" : getName());
        properties.put("gitPath", getGitPath() == null ? "" : getGitPath());
        properties.put("path", getPath() == null ? "" : getPath());
        return properties;
    }

    @Override
    public NodeLabelType getNodeType() {
        return NodeLabelType.GitRepository;
    }

}
