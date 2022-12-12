package cn.edu.fudan.se.multidependency.service.insert;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.Relations;

public interface InserterForNeo4j {

	public void insertToNeo4jDataBase() throws Exception ;

	public void setDataPath(String databasePath);
	public void setDatabaseName(String databaseName);

	public void setDelete(boolean delete);
	
	/**
	 * 表示该节点属于哪个Project，inProject可以为null
	 * 若inProject为null，则表示该节点不属于任何一个项目
	 * 基本上仅供静态分析使用
	 * @param node
	 * @param inProject 
	 * @return
	 */
	public boolean addNode(Node node, Project inProject);
	
	public boolean addRelation(Relation relation);

	public Nodes getNodes();
	
	public Relations getRelations();
	
	public boolean existNode(Node node);
	
	public boolean existRelation(Relation relation);
	
}
