package cn.edu.fudan.se.multidependency.service.query.clone.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import lombok.Data;

@Data
public class FileCloneWithCoChange {

	private ProjectFile file1;
	
	private ProjectFile file2;
	
	private Clone fileClone;
	
	private CoChange cochange;

	private int cochangeTimes;
	
	public FileCloneWithCoChange(Clone fileClone, CoChange cochange) throws Exception {
		if(fileClone.getCodeNode1().getClass() != ProjectFile.class || fileClone.getCodeNode2().getClass() != ProjectFile.class) {
			throw new Exception();
		}
		this.fileClone = fileClone;
		ProjectFile cloneFile1 = (ProjectFile) fileClone.getCodeNode1();
		ProjectFile cloneFile2 = (ProjectFile) fileClone.getCodeNode2();
		if(cochange == null) {
			this.file1 = cloneFile1;
			this.file2 = cloneFile2;
			cochangeTimes = 0;
			this.cochange = cochange;
		} else {
			ProjectFile cochangeFile1 = (ProjectFile)cochange.getNode1();
			ProjectFile cochangeFile2 = (ProjectFile)cochange.getNode2();
			if((cloneFile1.getId().equals(cochangeFile1.getId()) && cloneFile2.getId().equals(cochangeFile2.getId()))
					|| (cloneFile2.getId().equals(cochangeFile1.getId()) && cloneFile1.getId().equals(cochangeFile2.getId()))) {
				this.file1 = cloneFile1;
				this.file2 = cloneFile2;
				cochangeTimes = cochange.getTimes();
				this.cochange = cochange;
			} else {
				throw new Exception();
			}
		}
	}

	public int getCochangeTimes() {
		return cochangeTimes;
	}
	
}
