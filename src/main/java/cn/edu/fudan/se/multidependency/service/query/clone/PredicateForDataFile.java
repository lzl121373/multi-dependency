package cn.edu.fudan.se.multidependency.service.query.clone;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;

public class PredicateForDataFile implements FilterForCloneGroup {
	
	private StaticAnalyseService staticAnalyseService;
	
	public PredicateForDataFile(StaticAnalyseService staticAnalyseService) {
		this.staticAnalyseService = staticAnalyseService;
	}
	
	/**
	 * 如果该克隆组内的文件只有一个Type并且这个Type是DataType，则去除
	 */
	@Override
	public boolean remove(CloneGroup t) {
		for(CodeNode node : t.getNodes()) {
			if(!(node instanceof ProjectFile)) {
				return false;
			}
			if(!staticAnalyseService.isDataFile((ProjectFile) node)) {
				return false;
			}
		}
		return true;
	}

}
