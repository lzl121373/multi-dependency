package cn.edu.fudan.se.multidependency.service.query.clone;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;

public class PredicateForLanguage implements FilterForCloneGroup {
	
	private Language language;
	
	public PredicateForLanguage(Language language) {
		this.language = language;
	}

	/**
	 * 如果language == null，不去除该克隆组
	 * 如果language == java，则去除非java的克隆组
	 */
	@Override
	public boolean remove(CloneGroup t) {
		if(language == null) {
			return false;
		}
		return !language.toString().equals(t.getLanguage());
	}

}
