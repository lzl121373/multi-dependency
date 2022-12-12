package cn.edu.fudan.se.multidependency.service.query.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;

public class Graph {
	private List<Node> nodes = new ArrayList<>();
	public List<Node> getNodes(){
		return new ArrayList<>(nodes);
	}
	/**
	 * 直接相邻的点，无向
	 */
	private Map<Node, List<Node>> adjacencyList = new HashMap<>();
	/**
	 * 直接相邻的点，正向，from -> to。（谁依赖谁）
	 */
	private Map<Node, List<Node>> directedAdjacencyList = new HashMap<>();
	/**
	 * 直接相邻的点，逆向，to -> from。（谁被谁依赖）
	 */
	private Map<Node, List<Node>> reversedDirectedAdjacencyList = new HashMap<>();
	private Map<Node, Boolean> visitedNodes = new HashMap<>();
	private List<List<Node>> connectedComponents = new ArrayList<>();
	private Map<Node, List<Node>> connectedComponnentsMapping = new HashMap<>();
	private List<List<Node>> stronglyConnectedComponents = new ArrayList<>();
	private Map<Node, List<Node>> stronglyConnectedComponnentsMapping = new HashMap<>();
	private List<Node> helperNodeList = new ArrayList<>();

	public List<Node> getVertices() {
		return this.nodes;
	}

	public void computeConnectedComponents() {
		initializeVisitedVerices();
		for (Node node : nodes) {
			if (!visitedNodes.get(node)) {
				List<Node> connectedComponent = new ArrayList<>();
				depthFirstSearch(connectedComponent, node, GraphAlingment.UNDIRECTED);
				connectedComponents.add(connectedComponent);
				updateMapping(connectedComponnentsMapping, connectedComponent);
			}
		}
	}

	public void computeStronglyConnectedComponents() {
		reversePassDFS();
		straightPassDFS();
	}

	private void reversePassDFS() {
		initializeVisitedVerices();
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (!visitedNodes.get(nodes.get(i))) {
				depthFirstSearch(new ArrayList<>(), nodes.get(i), GraphAlingment.REVERSE_DIRECTED);
			}
		}
	}

	private void straightPassDFS() {
		initializeVisitedVerices();
		for (int i = nodes.size() - 1; i >= 0; i--) {
			if (!visitedNodes.get(helperNodeList.get(i))) {
				List<Node> stronglyConnectedComponent = new ArrayList<>();
				depthFirstSearch(stronglyConnectedComponent, helperNodeList.get(i), GraphAlingment.DIRECTED);
				stronglyConnectedComponents.add(stronglyConnectedComponent);
				updateMapping(stronglyConnectedComponnentsMapping, stronglyConnectedComponent);
			}
		}
	}

	private void initializeVisitedVerices() {
		for (Node node : nodes) {
			visitedNodes.put(node, false);
		}
	}

	private void updateMapping(Map<Node, List<Node>> mapping, List<Node> component) {
		for (Node node : component) {
			mapping.put(node, component);
		}
	}

	private void depthFirstSearch(List<Node> connectedComponent, Node node, GraphAlingment align) {
		visitedNodes.put(node, true);
		if (align != GraphAlingment.REVERSE_DIRECTED) {
			connectedComponent.add(node);
		}
		for (Node adjacentNode : getAdjacentVertices(node, align)) {
			if (!visitedNodes.get(adjacentNode)) {
				depthFirstSearch(connectedComponent, adjacentNode, align);
			}
		}
		if (align == GraphAlingment.REVERSE_DIRECTED) {
			helperNodeList.add(node);
		}
	}

	public void addNode(Node node) {
		if (!nodes.contains(node)) {
			initializeNode(node);
		}
	}

	public void addEdge(Relation edge) {
		initializeEdge(edge);
		addDirectedEdge(edge);
		addUndirectedEdge(edge);
	}

	private void initializeEdge(Relation edge) {
		if (!adjacencyList.containsKey(edge.getStartNode())) {
			initializeNode(edge.getStartNode());
		}
		if (!adjacencyList.containsKey(edge.getEndNode())) {
			initializeNode(edge.getEndNode());
		}
	}

	private void initializeNode(Node node) {
		adjacencyList.put(node, new ArrayList<>());
		directedAdjacencyList.put(node, new ArrayList<>());
		reversedDirectedAdjacencyList.put(node, new ArrayList<>());
		nodes.add(node);
	}

	private void addDirectedEdge(Relation edge) {
		List<Node> adjacentVertices = directedAdjacencyList.get(edge.getStartNode());
		if (!adjacentVertices.contains(edge.getEndNode())) {
			adjacentVertices.add(edge.getEndNode());
			directedAdjacencyList.put(edge.getStartNode(), adjacentVertices);
			addReverseDirectedEdge(edge);
		}
	}

	private void addReverseDirectedEdge(Relation edge) {
		List<Node> adjacentVertices = reversedDirectedAdjacencyList.get(edge.getEndNode());
		adjacentVertices.add(edge.getStartNode());
		reversedDirectedAdjacencyList.put(edge.getEndNode(), adjacentVertices);
	}

	private void addUndirectedEdge(Relation edge) {
		List<Node> adjacentVertices = adjacencyList.get(edge.getStartNode());
		if (!adjacentVertices.contains(edge.getEndNode())) {
			adjacentVertices.add(edge.getEndNode());
			adjacencyList.put(edge.getStartNode(), adjacentVertices);
		}
		adjacentVertices = adjacencyList.get(edge.getEndNode());
		if (!adjacentVertices.contains(edge.getStartNode())) {
			adjacentVertices.add(edge.getStartNode());
			adjacencyList.put(edge.getEndNode(), adjacentVertices);
		}
	}

	private List<Node> getAdjacentVertices(Node Node, GraphAlingment align) {
		if (align == GraphAlingment.UNDIRECTED) {
			return adjacencyList.get(Node);
		} else if (align == GraphAlingment.DIRECTED) {
			return directedAdjacencyList.get(Node);
		} else if (align == GraphAlingment.REVERSE_DIRECTED) {
			return reversedDirectedAdjacencyList.get(Node);
		}
		return null;
	}

	public List<List<Node>> getConnectedComponnents() {
		return connectedComponents;
	}

	public List<List<Node>> getStronglyConnectedComponents() {
		return stronglyConnectedComponents;
	}

	public List<List<Node>> findCycleInStronglyConnectedComponnets(List<Node> stronglyConnectedComponent) {
		if (stronglyConnectedComponent.size() < 1) {
			return new ArrayList<>();
		}
		paths = new ArrayList<>();
		Stack<Node> stack = new Stack<>();
		// for(int i = 0; i < stronglyConnectedComponent.size(); i++) {
		// initializeVisitedVerices();
		// dfs(stronglyConnectedComponent.get(i), stack, stronglyConnectedComponent);
		// }
		initializeVisitedVerices();
		dfs(stronglyConnectedComponent.get(0), stack, stronglyConnectedComponent);
		return paths;
	}

	List<List<Node>> paths;

	private void dfs(Node currentNode, Stack<Node> stack, List<Node> stronglyConnectedComponent) {
		visitedNodes.put(currentNode, true);
		stack.push(currentNode);
		for (Node adjacent : getAdjacentVertices(currentNode, GraphAlingment.DIRECTED)) {
			if (!stronglyConnectedComponent.contains(adjacent)) {
				continue;
			}
			if (stack.contains(adjacent)) {
				List<Node> path = new ArrayList<>();
				for (int i = stack.indexOf(adjacent); i < stack.size(); i++) {
					path.add(stack.get(i));
				}
				paths.add(path);
			} else {
				if (!visitedNodes.get(adjacent)) {
					dfs(adjacent, stack, stronglyConnectedComponent);
				}
			}
		}
		stack.pop();
		visitedNodes.put(currentNode, false);
	}

	public List<Node> getComponentOfNode(Node Node) {
		return connectedComponnentsMapping.get(Node);
	}

	public List<Node> getStrongComponentOfNode(Node Node) {
		return stronglyConnectedComponnentsMapping.get(Node);
	}

	public boolean inSameConnectedComponent(Node Node1, Node Node2) {
		return getComponentOfNode(Node1).equals(getComponentOfNode(Node2));
	}

	private enum GraphAlingment {
		UNDIRECTED, DIRECTED, REVERSE_DIRECTED
	}

	public Map<Node, List<Node>> getAdjacencyList() {
		return adjacencyList;
	}

	public Map<Node, List<Node>> getDirectedAdjacencyList() {
		return directedAdjacencyList;
	}

	public Map<Node, List<Node>> getReversedDirectedAdjacencyList() {
		return reversedDirectedAdjacencyList;
	}

	public Map<Node, Boolean> getVisitedVertices() {
		return visitedNodes;
	}

	public List<List<Node>> getConnectedComponents() {
		return connectedComponents;
	}

	public Map<Node, List<Node>> getConnectedComponnentsMapping() {
		return connectedComponnentsMapping;
	}

	public Map<Node, List<Node>> getStronglyConnectedComponnentsMapping() {
		return stronglyConnectedComponnentsMapping;
	}

	public List<Node> getHelperNodeList() {
		return helperNodeList;
	}
}
