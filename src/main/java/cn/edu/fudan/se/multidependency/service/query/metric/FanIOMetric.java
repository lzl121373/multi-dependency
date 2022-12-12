package cn.edu.fudan.se.multidependency.service.query.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;

public abstract class FanIOMetric {
	
	public abstract Node getComponent();
	
	public abstract int getFanIn();
	
	public abstract int getFanOut();

	public int allFanIO() {
		return getFanIn() + getFanOut();
	}
	
	public int fanIODValue() {
		return Math.abs(getFanIn() - getFanOut());
	}
	
	public static double calculateFanInUpperQuartile(Collection<? extends FanIOMetric> metrics) {
		List<? extends FanIOMetric> list = new ArrayList<>(metrics);
		list.sort((m1, m2) -> {
			return m1.getFanIn() - m2.getFanIn();
		});
		System.out.println("fanIn");
		for(FanIOMetric metric : list) {
			System.out.print(metric.getFanIn() + " ");
		}
		System.out.println();
		int size = list.size();
		double temp = 3 * (size + 1) / 4.0;
		double result = 0;
		if(temp == (int) temp) {
			result = list.get((int) temp - 1).getFanIn();
		} else {
			result = list.get((int) temp - 1).getFanIn() * 0.75 + list.get((int) temp).getFanIn() * 0.25;
		}
		System.out.println(temp + " " + result);
		return result;
	}
	
	public static double calculateFanOutUpperQuartile(Collection<? extends FanIOMetric> metrics) {
		List<? extends FanIOMetric> list = new ArrayList<>(metrics);
		list.sort((m1, m2) -> {
			return m1.getFanOut() - m2.getFanOut();
		});
		int size = list.size();
		double temp = 3 * (size + 1) / 4.0;
		if(temp == (int) temp) {
			return list.get((int) temp - 1).getFanOut();
		} else {
			return list.get((int) temp - 1).getFanOut() * 0.75 + list.get((int) temp).getFanOut() * 0.25;
		}
	}
	
	/**
	 * 获取FanIn中位数
	 * @param metrics
	 * @return
	 */
	public static double calculateFanInMedian(Collection<? extends FanIOMetric> metrics) {
		List<? extends FanIOMetric> list = new ArrayList<>(metrics);
		list.sort((m1, m2) -> {
			return m1.getFanIn() - m2.getFanIn();
		});
		int size = list.size();
		if(list.size() % 2 == 0) {
			return (list.get((size - 1) / 2).getFanIn() + list.get(size / 2).getFanIn()) / 2;
		} else {
			return list.get(size / 2).getFanIn();
		}
	}
	
	/**
	 * 获取FanOut中位数
	 * @param metrics
	 * @return
	 */
	public static double calculateFanOutMedian(Collection<? extends FanIOMetric> metrics) {
		List<? extends FanIOMetric> list = new ArrayList<>(metrics);
		list.sort((m1, m2) -> {
			return m1.getFanOut() - m2.getFanOut();
		});
		int size = list.size();
		if(list.size() % 2 == 0) {
			return (list.get((size - 1) / 2).getFanOut() + list.get(size / 2).getFanOut()) / 2;
		} else {
			return list.get(size / 2).getFanOut();
		}
	}

	public double getInstability() {
		return getFanOut() + getFanIn() == 0 ? -1 : getFanOut() / (getFanOut() + getFanIn() + 0.0);
	}
}
