package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UnutilizedAbstraction<N extends Node> implements Serializable {

	private static final long serialVersionUID = 12632676208103334L;
	
	@Getter
	@Setter
	private N component;
	
	@Getter
	private List<Node> caller = new ArrayList<>();

	public UnutilizedAbstraction(N component) {
		this.component = component;
	}
}
