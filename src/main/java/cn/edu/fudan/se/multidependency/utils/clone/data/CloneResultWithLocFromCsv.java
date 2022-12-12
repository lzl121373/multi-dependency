package cn.edu.fudan.se.multidependency.utils.clone.data;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CloneResultWithLocFromCsv extends CloneResultFromCsv {
	public CloneResultWithLocFromCsv(int start, int end, double value, String type, int linesSize1, int linesSize2,
		int loc1, int loc2) {
	super(start, end, value, type);
	this.linesSize1 = linesSize1;
	this.linesSize2 = linesSize2;
	this.loc1 = loc1;
	this.loc2 = loc2;
}
	private int linesSize1;
	private int linesSize2;
	private int loc1;
	private int loc2;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloneResultFromCsv other = (CloneResultFromCsv) obj;
		if (getEnd() != other.getEnd())
			return false;
		if (getStart() != other.getStart())
			return false;
		return true;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getEnd();
		result = prime * result + getStart();
		return result;
	}
	public int getLinesSize1() {
		return linesSize1;
	}
	public void setLinesSize1(int linesSize1) {
		this.linesSize1 = linesSize1;
	}
	public int getLinesSize2() {
		return linesSize2;
	}
	public void setLinesSize2(int linesSize2) {
		this.linesSize2 = linesSize2;
	}
	public int getLoc1() {
		return loc1;
	}
	public void setLoc1(int loc1) {
		this.loc1 = loc1;
	}
	public int getLoc2() {
		return loc2;
	}
	public void setLoc2(int loc2) {
		this.loc2 = loc2;
	}
	
}
