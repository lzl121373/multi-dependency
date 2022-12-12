package cn.edu.fudan.se.multidependency.service.query.data;

import lombok.Data;

@Data
public class HistogramWithProjectsSize {

	private String x;
	
	public HistogramWithProjectsSize(int x) {
		this.x = new StringBuilder().append("跨项目数：").append(x).toString();
	}
	
	private int groupsSize = 0;
	
	private int nodesSize = 0;
	
	private double ratio;
	
	public void addGroupSize(int size) {
		this.groupsSize += size;
	}
	
	public void addNodesSize(int size) {
		this.nodesSize += size;
	}
	
	public double getRatio() {
		return ratio = ((nodesSize + 0.0) / groupsSize);
	}
	
}
