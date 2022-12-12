package cn.edu.fudan.se.multidependency.model.node.ar;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.HashMap;
import java.util.Map;

@Data
@NodeEntity
@NoArgsConstructor
public class Module implements Node {

    private static final long serialVersionUID = -8302976657555596847L;

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    
    private Long entityId;

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
        properties.put("name", getName() == null ? "" : getName());
        return properties;
    }

    @Override
    public NodeLabelType getNodeType() {
        return NodeLabelType.Module;
    }
}
