package cn.edu.fudan.se.multidependency.service.query.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TreeMapGraph {
	
	private int value;
	
	private String name;
	
	private String path;
	
	private List<TreeMapGraph> children = new ArrayList<>();
}
