package cn.edu.fudan.se.multidependency.utils;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCall;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Rank.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.LinkSource;
import lombok.Data;

public class GraphvizUtil {
	@Data
	public static class GraphvizTreeNode {
		
		private String name;
		
		private String style;
		
		private Map<String, GraphvizTreeNode> children = new HashMap<>();
		
		public int size() {
			if(isChildrenEmpty()) {
				return 1;
			}
			int sum = 1;
			for(GraphvizTreeNode child : children.values()) {
				sum += child.size();
			}
			return sum;
		}
		
		public GraphvizTreeNode(String name) {
			this.name = name;
		}
		
		public int countOfChildren() {
			return children.size();
		}
		
		public boolean isChildrenEmpty() {
			return children.isEmpty();
		}
		
		public Graph toGraphviz() {
			List<LinkSource> sources = new ArrayList<>();
			drawTree(this, sources);
			Graph result = graph(name).directed()
					.graphAttr().with(Rank.dir(RankDir.TOP_TO_BOTTOM))
					.with(sources);
			return result;
		}
		public void addChild(GraphvizTreeNode child) {
			this.children.put(child.getName(), child);
		}
	}
	
	public static void drawTree(GraphvizTreeNode root, List<LinkSource> result) {
		if(root.isChildrenEmpty()) {
			result.add(node(root.getName()));
			return ;
		}
		List<LinkSource> sources = new ArrayList<>();
		for(Map.Entry<String, GraphvizTreeNode> entry : root.getChildren().entrySet()) {
			LinkSource source = node(root.getName()).link(node(entry.getValue().getName()));
			sources.add(source);
			drawTree(entry.getValue(), result);
		}
		result.addAll(sources);
	}
	
	
	private static String generate(Function function, long order, long depth) {
		StringBuilder name = new StringBuilder();
		name.append(function.getName())
		.append(function.getParametersIdentifies())
		.append("\n")
		.append("order : ")
		.append(order)
		.append("\n")
		.append("depth : ")
		.append(depth);
		return name.toString();
	}
	
	public static GraphvizTreeNode generate(List<DynamicCall> calls) {
		GraphvizTreeNode root = new GraphvizTreeNode("Entry");
		Map<String, GraphvizTreeNode> map = new HashMap<>();
		for(DynamicCall call : calls) {
			Function caller = call.getFunction();
			Function called = call.getCallFunction();
			String callerName = generate(caller, call.getFromOrder(), call.getFromDepth());
			String calledName = generate(called, call.getToOrder(), call.getToDepth());
			GraphvizTreeNode callerNode = map.getOrDefault(callerName, new GraphvizTreeNode(callerName));
			GraphvizTreeNode calledNode = map.getOrDefault(calledName, new GraphvizTreeNode(calledName));
			map.put(callerName, callerNode);
			map.put(calledName, calledNode);
			if(call.getFromDepth() == 0) {
				root.addChild(callerNode);
			}
			callerNode.addChild(calledNode);
		}
		return root;
	}
	
	public static void print(Graph g, String outputFilePath) throws Exception {
		Graphviz.fromGraph(g).render(Format.PNG).toFile(new File(outputFilePath));
	}
	
}
