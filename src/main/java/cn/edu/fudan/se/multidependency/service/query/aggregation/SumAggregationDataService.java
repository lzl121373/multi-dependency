package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.RelationDataForDoubleNodes;

import java.util.*;

public interface SumAggregationDataService extends AggregationDataService{
    /**
     * 根据函数间的克隆找出微服务间的克隆
     * @param functionClones
     * @return
     */
    Collection<RelationDataForDoubleNodes<Node, Relation>> findMicroServiceCloneFromFunctionClone(Collection<? extends Relation> functionClones);

    /**
     * 根据文件间的克隆找出微服务间的克隆
     * @param fileClones
     * @return
     */
    Collection<RelationDataForDoubleNodes<Node, Relation>> findMicroServiceCloneFromFileClone(Collection<? extends Relation> fileClones);

    /**
     * 根据函数间的克隆找出项目间的克隆
     * @param functionClones
     * @return
     */
    Collection<RelationDataForDoubleNodes<Node, Relation>> findProjectCloneFromFunctionClone(Collection<? extends Relation> functionClones);

    /**
     * 根据文件间的克隆找出项目间的克隆
     * @param fileClones
     * @return
     */
    Collection<RelationDataForDoubleNodes<Node, Relation>> findProjectCloneFromFileClone(Collection<? extends Relation> fileClones);

}
