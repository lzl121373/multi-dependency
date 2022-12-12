package cn.edu.fudan.se.multidependency.utils.clone.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloneResultFromCsv {
	private int start;
	private int end;
	private double value;
	private String type;
}
