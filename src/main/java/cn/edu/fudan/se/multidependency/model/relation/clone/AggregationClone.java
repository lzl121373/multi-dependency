package cn.edu.fudan.se.multidependency.model.relation.clone;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.*;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_AGGREGATION_CLONE)
public class AggregationClone implements Relation {
    private static final long serialVersionUID = 8708817258770543568L;

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Node node1;

    @EndNode
    private Node node2;

    public AggregationClone(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    private double value = 0;

    //克隆文件对数
    private int clonePairs = 0;

    //包1下克隆文件个数
    private int cloneNodesCount1 = 0;

    //包2下克隆文件个数
    private int cloneNodesCount2 = 0;

    //包1下全部文件个数
    private int allNodesCount1 = 0;

    //包2下全部文件个数
    private int allNodesCount2 = 0;

    //包1下克隆文件代码行数
    private int cloneNodesLoc1 = 0;

    //包2下克隆文件代码行数
    private int cloneNodesLoc2 = 0;

    //包1下全部文件代码行数
    private int allNodesLoc1 = 0;

    //包2下全部文件代码行数
    private int allNodesLoc2 = 0;

    //克隆包对下克隆文件Type1个数
    private int cloneType1Count = 0;

    //克隆包对下克隆文件Type2个数
    private int cloneType2Count = 0;

    //克隆包对下克隆文件Type3个数
    private int cloneType3Count = 0;

    //克隆包相似值=克隆包对下克隆文件Type1的value+克隆包对下克隆文件Type2的value+克隆包对下克隆文件Type3的value
    private double cloneSimilarityValue = 0.00;

    /**
     * 克隆关系类型：文件间克隆，方法间克隆等
     */
    private String cloneRelationType;

    /**
     * 克隆类型，type1，type2等
     */
    private String cloneType;

    @Override
    public Node getStartNode() {
        return node1;
    }

    @Override
    public Node getEndNode() {
        return node2;
    }

    @Override
    public RelationType getRelationType() {
        return RelationType.AGGREGATION_CLONE;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }
}
