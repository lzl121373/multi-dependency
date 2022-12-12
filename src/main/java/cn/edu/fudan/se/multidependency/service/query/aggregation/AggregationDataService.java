package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.BasicDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.RelationDataForDoubleNodes;

import java.util.HashMap;
import java.util.Map;

public interface AggregationDataService {

    default BasicDataForDoubleNodes<Node, Relation> getSuperNodeRelationWithSubNodeRelation(
            Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> superNodeToSuperNodeRelations,
            Node superNode1, Node superNode2) {
        Map<Node, BasicDataForDoubleNodes<Node, Relation>> superNode1ToRelations = superNodeToSuperNodeRelations.getOrDefault(superNode1, new HashMap<>());
        BasicDataForDoubleNodes<Node, Relation> relation = superNode1ToRelations.get(superNode2);
        if(relation != null) {
            return relation;
        }
        Map<Node, BasicDataForDoubleNodes<Node, Relation>> superNode2ToRelations = superNodeToSuperNodeRelations.getOrDefault(superNode2, new HashMap<>());
        relation = superNode2ToRelations.get(superNode1);
        return relation;
    }

    default RelationDataForDoubleNodes<Node, Relation> getSuperNodeRelationWithSubNodeRelation2(
            Map<Node, Map<Node, RelationDataForDoubleNodes<Node, Relation>>> superNodeToSuperNodeRelations,
            Node superNode1, Node superNode2) {
        Map<Node, RelationDataForDoubleNodes<Node, Relation>> superNode1ToRelations = superNodeToSuperNodeRelations.getOrDefault(superNode1, new HashMap<>());
        RelationDataForDoubleNodes<Node, Relation> relation = superNode1ToRelations.get(superNode2);
        if(relation != null) {
            return relation;
        }
        Map<Node, RelationDataForDoubleNodes<Node, Relation>> superNode2ToRelations = superNodeToSuperNodeRelations.getOrDefault(superNode2, new HashMap<>());
        relation = superNode2ToRelations.get(superNode1);
        return relation;
    }
}
