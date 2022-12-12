package cn.edu.fudan.se.multidependency.utils.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

public class PageUtil {

	public static Pageable generatePageable(int page, int size, String... sortByProperties) {
		List<Order> orders = new ArrayList<>();
		for(String property : sortByProperties) {
			Order order = Order.by(property);
			orders.add(order);
		}
		Pageable result = null;
		if(orders.isEmpty()) {
			result = PageRequest.of(page, size);
		} else {
			result = PageRequest.of(page, size, Sort.by(orders));
		}
		return result;
	}
}
