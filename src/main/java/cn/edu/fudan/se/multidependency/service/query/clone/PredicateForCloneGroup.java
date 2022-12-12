package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;

public class PredicateForCloneGroup implements Predicate<CloneGroup> {

	private List<FilterForCloneGroup> filters = new ArrayList<>();
	
	public void addFilter(FilterForCloneGroup filter) {
		this.filters.add(filter);
	}
	
	public int size() {
		return filters.size();
	}
	
	@Override
	public boolean test(CloneGroup t) {
		for(FilterForCloneGroup filter : filters) {
			if(filter.remove(t)) {
				return true;
			}
		}
		return false;
	}

}
