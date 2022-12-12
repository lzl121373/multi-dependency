package cn.edu.fudan.se.multidependency.utils.clone.data;

import lombok.Data;

@Data
public class FilePathFromCsv {
	private String line;
	private int lineId;
	private String filePath;
	private int startLine;
	private int endLine;
}
