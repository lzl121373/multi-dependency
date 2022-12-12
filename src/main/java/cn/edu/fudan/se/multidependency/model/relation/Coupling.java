package cn.edu.fudan.se.multidependency.model.relation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_COUPLING)
@EqualsAndHashCode
public class Coupling implements Relation{

	private static final long serialVersionUID = 318878262563469533L;

	@Id
    @GeneratedValue
    private Long id;

    @StartNode
	private Node startNode;

    @EndNode
	private Node endNode;

	private DependsOn forwardDependsOn;

	private DependsOn reverseDependsOn;

	private int mAAtoB;

	private int mBAtoB;

	private int mABtoA;

	private int mBBtoA;

	private int dAtoB;

	private int dBtoA;

	private double CStartToEnd;

	private double CEndToStart;

	private double C;

	private double UStartToEnd;

	private double UEndToStart;

	private double I;

	private double disp;

	private double dist;

	private String dependsOnTypeStartToEnd = "";

	private String dependsOnTypeEndToStart = "";

	public Coupling(Node startNode, Node endNode, DependsOn forwardDependsOn, DependsOn reverseDependsOn,
					int mAAtoB, int mBAtoB, int mABtoA, int mBBtoA, int dAtoB, int dBtoA,
					double CStartToEnd, double CEndToStart, double C, double UStartToEnd, double UEndToStart,
					double I, double disp, double dist) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.forwardDependsOn = forwardDependsOn;
		this.reverseDependsOn = reverseDependsOn;
		this.mAAtoB = mAAtoB;
		this.mBAtoB = mBAtoB;
		this.mABtoA = mABtoA;
		this.mBBtoA = mBBtoA;
		this.dAtoB = dAtoB;
		this.dBtoA = dBtoA;
		this.CStartToEnd = CStartToEnd;
		this.CEndToStart = CEndToStart;
		this.C = C;
		this.UStartToEnd = UStartToEnd;
		this.UEndToStart = UEndToStart;
		this.I = I;
		this.disp = disp;
		this.dist = dist;
		this.dependsOnTypeStartToEnd = getDependsOnType(forwardDependsOn.getDependsOnTypes());
		this.dependsOnTypeEndToStart = getDependsOnType(reverseDependsOn.getDependsOnTypes());
	}

	private String getDependsOnType(Map<String, Long> dependsOnTypes) {
		StringBuilder typesAndTimes = new StringBuilder();
		Iterator<String> iterator = dependsOnTypes.keySet().iterator();
		while(iterator.hasNext()) {
			String type = iterator.next();
			if(iterator.hasNext()){
				typesAndTimes.append(type).append("(").append(dependsOnTypes.get(type)).append(")_");
			}else{
				typesAndTimes.append(type).append("(").append(dependsOnTypes.get(type)).append(")");
			}
		}
		return typesAndTimes.toString();
	}

	public Coupling(Node startNode, Node endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}

	@Override
	public Node getStartNode() {
		return startNode;
	}

	@Override
	public Node getEndNode() {
		return endNode;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.DEPENDS_ON;
	}

	@Override
	public Map<String, Object> getProperties() {
		return null;
	}

}
