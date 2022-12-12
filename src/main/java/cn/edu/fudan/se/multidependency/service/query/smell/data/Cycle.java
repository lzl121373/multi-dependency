package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import org.springframework.data.neo4j.annotation.QueryResult;

@Data
@QueryResult
public class Cycle<T extends Node> {

	private int partition;

	private List<T> components;

	public Cycle(int partition, List<T> components) {
		this.partition = partition;
		this.components = new ArrayList<>(components);
	}
}
