package cn.edu.fudan.se.multidependency.service.insert;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;

public interface ExtractorForNodesAndRelations {

	public void addNodesAndRelations() throws Exception ;
	
	public Nodes getNodes();
	
	public Relations getRelations();
	
	public boolean existNode(Node node);
	
	public boolean existRelation(Relation relation);
	
}
