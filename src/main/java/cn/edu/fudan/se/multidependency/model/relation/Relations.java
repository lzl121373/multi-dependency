package cn.edu.fudan.se.multidependency.model.relation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCallFunctionByTestCase;

public class Relations implements Serializable {

    private static final long serialVersionUID = 7873746846531623659L;

    private Map<RelationType, List<Relation>> allRelations = new ConcurrentHashMap<>();

    private Map<String, List<DynamicCallFunctionByTestCase>> traceIdToDynamicCallFunctions = new ConcurrentHashMap<>();

    private Map<Node, Map<Node, Map<RelationType, RelationWithTimes>>> startNodesToNodeRelations = new ConcurrentHashMap<>();

    private RelationWithTimes hasRelationWithTimes(Node startNode, Node endNode, RelationType relationType) {
        Map<Node, Map<RelationType, RelationWithTimes>> endNodesTemp = startNodesToNodeRelations.get(startNode);
        if (endNodesTemp == null) {
            return null;
        }
        Map<RelationType, RelationWithTimes> relationsTemp = endNodesTemp.get(endNode);
        if (relationsTemp == null) {
            return null;
        }
        return relationsTemp.get(relationType);
    }

    private void addRelationWithTimes(Relation relation) {
    	if(!(relation instanceof RelationWithTimes)) {
    		return ;
    	}
        Map<Node, Map<RelationType, RelationWithTimes>> endNodesTemp = startNodesToNodeRelations.getOrDefault(relation.getStartNode(), new ConcurrentHashMap<>());
        Map<RelationType, RelationWithTimes> relationsTemp = endNodesTemp.getOrDefault(relation.getEndNode(), new ConcurrentHashMap<>());
        relationsTemp.put(relation.getRelationType(), (RelationWithTimes) relation);
        endNodesTemp.put(relation.getEndNode(), relationsTemp);
        startNodesToNodeRelations.put(relation.getStartNode(), endNodesTemp);
    }

    public void clear() {
        allRelations.clear();
        traceIdToDynamicCallFunctions.clear();
        startNodesToNodeRelations.clear();
    }

    public Map<RelationType, List<Relation>> getAllRelations() {
        return new ConcurrentHashMap<>(allRelations);
    }

    public int size() {
        int size = 0;
        for (List<Relation> nodes : allRelations.values()) {
            size += nodes.size();
        }
        return size;
    }

    public synchronized void addRelation(Relation relation) {

        if (relation instanceof DynamicCallFunctionByTestCase) {
            DynamicCallFunctionByTestCase call = (DynamicCallFunctionByTestCase) relation;
            if (call.getTraceId() == null) {
                return;
            }
            List<DynamicCallFunctionByTestCase> calls = traceIdToDynamicCallFunctions.getOrDefault(call.getTraceId(), new CopyOnWriteArrayList<>());
            calls.add(call);
            traceIdToDynamicCallFunctions.put(call.getTraceId(), calls);
        }

        if (relation instanceof RelationWithTimes) {
            RelationWithTimes relationWithTimes = hasRelationWithTimes(relation.getStartNode(), relation.getEndNode(), relation.getRelationType());
            if (relationWithTimes == null) {
                addRelationWithTimes(relation);
                addRelationDirectly(relation);
            } else {
                relationWithTimes.addTimes();
            }
        } else {
            addRelationDirectly(relation);
        }

    }

    /**
     * 尚需测试
     * @TODO
     * @param relation
     */
    public synchronized void deleteRelation(Relation relation) {

        if (relation instanceof DynamicCallFunctionByTestCase) {
            DynamicCallFunctionByTestCase call = (DynamicCallFunctionByTestCase) relation;
            if (call.getTraceId() == null) {
                return;
            }
            List<DynamicCallFunctionByTestCase> calls = traceIdToDynamicCallFunctions.getOrDefault(call.getTraceId(), new CopyOnWriteArrayList<>());
            if(calls.contains(relation)){
                calls.remove(relation);
                traceIdToDynamicCallFunctions.put(call.getTraceId(), calls);
            }

        }

        if (relation instanceof RelationWithTimes) {
            return;
        } else {
            deleteRelationDirectly(relation);
        }

    }

    private void addRelationDirectly(Relation relation) {
        List<Relation> relations = allRelations.getOrDefault(relation.getRelationType(), new CopyOnWriteArrayList<>());
        relations.add(relation);
        allRelations.put(relation.getRelationType(), relations);
    }

    private void deleteRelationDirectly(Relation relation) {
        List<Relation> relations = allRelations.getOrDefault(relation.getRelationType(), new CopyOnWriteArrayList<>());
        if(relations.contains(relation)){
            relations.remove(relation);
            allRelations.put(relation.getRelationType(), relations);
        }
    }

    public List<DynamicCallFunctionByTestCase> findDynamicCallFunctionsByTraceId(String traceId) {
        return traceIdToDynamicCallFunctions.getOrDefault(traceId, new CopyOnWriteArrayList<>());
    }

    public List<? extends Relation> findRelationsMap(RelationType relationType) {
        return allRelations.getOrDefault(relationType, new CopyOnWriteArrayList<>());
    }

    public boolean existRelation(Relation relation) {
        List<Relation> relations = this.allRelations.get(relation.getRelationType());
        return relations == null ? false : relations.contains(relation);
    }

}
