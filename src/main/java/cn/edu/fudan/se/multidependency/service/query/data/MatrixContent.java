package cn.edu.fudan.se.multidependency.service.query.data;

import java.util.HashSet;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatrixContent<N1 extends Node, N2 extends Node> {
	
	public MatrixContent(N1 start, N2 end, int i, int j) {
		this.start = start;
		this.end = end;
		this.i = i;
		this.j = j;
	}

	private N1 start;
	
	private N2 end;
	
	private int i;
	
	private int j;
	
	private Set<RelationType> values = new HashSet<>();
	
	private int integerValue = 0;
	
	public void addContent(RelationType value) {
		this.values.add(value);
	}
	
	
}
