package cn.edu.fudan.se.multidependency.utils.clone.data;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Group {
	private String line;
	private Collection<Integer> groupIds = new ArrayList<>();
	public Group(String line) {
		this.line = line;
	}
	public void addId(int id) {
		this.groupIds.add(id);
	}
}
