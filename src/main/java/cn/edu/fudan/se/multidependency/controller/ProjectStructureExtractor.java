package cn.edu.fudan.se.multidependency.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.MultipleService;
import cn.edu.fudan.se.multidependency.utils.query.ZTreeUtil.ZTreeNode;

public class ProjectStructureExtractor implements Callable<ZTreeNode> {
	Project project;
	MultipleService multipleService;
	CountDownLatch latch;
	public ProjectStructureExtractor(Project project, MultipleService multipleService, CountDownLatch latch) {
		super();
		this.project = project;
		this.multipleService = multipleService;
		this.latch = latch;
	}
	@Override
	public ZTreeNode call() throws Exception {
		ZTreeNode node = multipleService.projectToZTree(project);
		latch.countDown();
		return node;
	}
	
}