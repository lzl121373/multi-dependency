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
public class Branch implements Node {

    private static final long serialVersionUID = -5878073388640554742L;

    @Id
    @GeneratedValue
    private Long id;

    private Long entityId;

    private String branchId;

    private String name;

    public Branch(Long entityId, String branchId, String name){
        this.entityId = entityId;
        this.branchId = branchId;
        this.name = name;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
        properties.put("name", getName() == null ? "" : getName());
        return properties;
    }

    @Override
    public NodeLabelType getNodeType() {
        return NodeLabelType.Branch;
    }

}
