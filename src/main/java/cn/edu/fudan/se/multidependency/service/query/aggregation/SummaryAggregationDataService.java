package cn.edu.fudan.se.multidependency.service.query.aggregation;

import cn.edu.fudan.se.multidependency.model.node.Node;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.BasicDataForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChangeMatrix;

import java.util.*;

public interface SummaryAggregationDataService extends AggregationDataService{

    /**
     * 根据文件间的克隆找出包间的克隆
     * @param fileClones
     * @return
     */
    Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCloneFromFileClone(Collection<? extends Relation> fileClones);

    /**
     * 根据文件间的克隆找出包间的克隆，排序
     * @param fileClones
     * @return
     */
    List<BasicDataForDoubleNodes<Node, Relation>> queryPackageCloneFromFileCloneSort(Collection<? extends Relation> fileClones);

    /**
     * 列出包克隆中，包中克隆文件的co-change情况
     * @param fileClones
     * @param pck1
     * @param pck2
     * @return
     * @throws Exception
     */
    PackageCloneValueWithFileCoChange queryPackageCloneWithFileCoChange(Collection<? extends Relation> fileClones, Package pck1, Package pck2) throws Exception;

    /**
     * 根据文件间的克隆找出包间的克隆
     * @param fileCoChanges
     * @return
     */
    Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> queryPackageCoChangeFromFileCoChange(Collection<? extends Relation> fileCoChanges);

    /**
     * 根据文件间的co-change找出包间的co-change，排序
     * @param fileCoChanges
     * @return
     */
    Collection<BasicDataForDoubleNodes<Node, Relation>> queryPackageCoChangeFromFileCoChangeSort(Collection<? extends Relation> fileCoChanges);

    default Collection<BasicDataForDoubleNodes<Node, Relation>> querySuperNodeRelationFromSubNodeRelation(Collection<? extends Relation> subNodeRelations, List<Node> pcks) {
        if(pcks == null || pcks.isEmpty()) {
            return new ArrayList<>();
        }
        List<BasicDataForDoubleNodes<Node, Relation>> result = new ArrayList<>();
        for(int i = 0; i < pcks.size(); i++) {
            for(int j = i + 1; j < pcks.size(); j++) {
                BasicDataForDoubleNodes<Node, Relation> queryResult = querySuperNodeRelationFromSubNodeRelationSort(subNodeRelations, pcks.get(i), pcks.get(j));
                if(queryResult != null){
                    result.add(queryResult);
                }
            }
        }

        return result;
    }

    /**
     * 两个节点（包）之间的关系（cochange）聚合，两个节点之间不分先后顺序
     * @param subNodeRelations
     * @param pck1
     * @param pck2
     * @return
     */
    default BasicDataForDoubleNodes<Node, Relation> querySuperNodeRelationFromSubNodeRelationSort(Collection<? extends Relation> subNodeRelations, Node pck1, Node pck2) {
        Map<Node, Map<Node, BasicDataForDoubleNodes<Node, Relation>>> packageRelations = new HashMap<>();
        if(subNodeRelations == null || subNodeRelations.isEmpty())
            return null;
        if(((List<Relation>)subNodeRelations).get(0) instanceof Clone)
            packageRelations = queryPackageCloneFromFileClone(subNodeRelations);
        else if(((List<Relation>)subNodeRelations).get(0) instanceof CoChange){
            packageRelations = queryPackageCoChangeFromFileCoChange(subNodeRelations);
        }else
            return null;
        Map<Node, BasicDataForDoubleNodes<Node, Relation>> map = packageRelations.getOrDefault(pck1, new HashMap<>());
        BasicDataForDoubleNodes<Node, Relation> result = map.get(pck2);
        if(result == null) {
            map = packageRelations.getOrDefault(pck2, new HashMap<>());
            result = map.get(pck1);
        }
        if(result != null) {
            result.sortChildren();
        }
        return result;
    }

    /**
     * 两个节点（包）之间的关系克隆，矩阵形式展示
     * @param fileClones
     * @param pck1
     * @param pck2
     * @return
     */
    PackageCloneValueWithFileCoChangeMatrix queryPackageCloneWithFileCoChangeMatrix(Collection<Clone> fileClones, Package pck1, Package pck2);

}
