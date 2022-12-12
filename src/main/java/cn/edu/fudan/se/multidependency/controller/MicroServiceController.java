package cn.edu.fudan.se.multidependency.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.microservice.MicroServiceCallMicroService;
import cn.edu.fudan.se.multidependency.model.relation.structure.microservice.MicroServiceDependOnMicroService;
import cn.edu.fudan.se.multidependency.service.query.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.query.dynamic.FeatureOrganizationService;
import cn.edu.fudan.se.multidependency.service.query.metric.Fan_IO;
import cn.edu.fudan.se.multidependency.utils.query.ZTreeUtil.ZTreeNode;

@Controller
@RequestMapping("/microservice")
public class MicroServiceController {
	
	@Autowired
	private MicroserviceService msService;
	
	@Autowired
	private FeatureOrganizationService featureOrganizationService;
	
	@GetMapping(value = "/fanIO/{msId}")
	@ResponseBody
	public Fan_IO<MicroService> calculateFanIO(@PathVariable("msId") long id) {
		MicroService ms = msService.findMicroServiceById(id);
		return msService.microServiceDependencyFanIOInDynamicCall(ms);
	}
	
	@GetMapping(value = "/fanIO")
	@ResponseBody
	public Collection<Fan_IO<MicroService>> calculateFanIOs() {
		List<Fan_IO<MicroService>> result = new ArrayList<>();
		Collection<MicroService> allMss = msService.findAllMicroService();
		for(MicroService ms : allMss) {
			Fan_IO<MicroService> fanIO = msService.microServiceDependencyFanIOInDynamicCall(ms);
			result.add(fanIO);
		}
		result.sort(new Comparator<Fan_IO<MicroService>>() {
			@Override
			public int compare(Fan_IO<MicroService> o1, Fan_IO<MicroService> o2) {
				if(o1.size() == o2.size()) {
					return o1.getNode().getName().compareTo(o2.getNode().getName());
				}
				return o2.size() - o1.size();
			}
		});
		System.out.println(1);
		return result;
	}
	
	@GetMapping(value = "/all")
	@ResponseBody
	public Collection<MicroService> allMicroServices() {
		return msService.findAllMicroService();
	}
	
	@GetMapping(value = "/all/{page}")
	@ResponseBody
	public List<MicroService> allMicroServicesByPage(@PathVariable("page") int page) {
		return msService.queryAllMicroServicesByPage(page, Constant.SIZE_OF_PAGE, "name");
	}
	
	@GetMapping(value = "/pages/count")
	@ResponseBody
	public long queryMicroServicePagesCount() {
		long count = msService.countOfAllMicroServices();
		long pageCount = count % Constant.SIZE_OF_PAGE == 0 ? 
				count / Constant.SIZE_OF_PAGE : count / Constant.SIZE_OF_PAGE + 1;
		return pageCount;
	}
	
	@GetMapping(value = "/all/ztree/projects/{page}")
	@ResponseBody
	public JSONObject allMicroServicesContainProjectsByPage(@PathVariable("page") int page) {
		JSONObject result = new JSONObject();
		Iterable<MicroService> mss = msService.queryAllMicroServicesByPage(page, Constant.SIZE_OF_PAGE, "name");
		List<ZTreeNode> nodes = msService.queryMicroServiceContainProjectsZTree(mss);
		JSONArray values = new JSONArray();
		for(ZTreeNode node : nodes) {
			values.add(node.toJSON());
		}
		result.put("result", "success");
		result.put("values", values);
		return result;
	}
	
	@GetMapping(value = {"/", "/index"})
	public String index(HttpServletRequest request) {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls = msService.msCalls();
		
		Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns = msService.msDependOns();
		
		request.setAttribute("calls", msCalls);
		request.setAttribute("depends", msDependOns);
		return "microservice/index";
	}
	
	@GetMapping("/cytoscape")
	@ResponseBody
	public JSONObject getJson() {
		JSONObject result = new JSONObject();
		
		result.put("value", json());
		return result;
	}
	
	public JSONObject json() {
		Map<MicroService, Map<MicroService, MicroServiceCallMicroService>> msCalls = msService.msCalls();
		
		Map<MicroService, Map<MicroService, MicroServiceDependOnMicroService>> msDependOns = msService.msDependOns();
		
		Collection<MicroService> mses = featureOrganizationService.allMicroServices();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		JSONObject data = new JSONObject();
		data.put("nodes", nodes);
		data.put("edges", edges);
		for(MicroService ms : mses) {
			JSONObject node = new JSONObject();
			node.put("id", ms.getId());
			node.put("name", ms.getName());
			node.put("type", "microservice");
			node.put("length", ms.getName().length() * 10);
			JSONObject temp = new JSONObject();
			temp.put("data", node);
			nodes.add(temp);
		}
		for(MicroService start : msDependOns.keySet()) {
			for(MicroService end : msDependOns.get(start).keySet()) {
				MicroServiceDependOnMicroService depend = msDependOns.get(start).get(end);
				if(msService.isMicroServiceCall(start, end)) {
					JSONObject edge = new JSONObject();
					edge.put("id", depend.getId());
					edge.put("source", start.getId());
					edge.put("target", end.getId());
					edge.put("type", "dependon-call");
					JSONObject temp = new JSONObject();
					temp.put("data", edge);
					edges.add(temp);
				} else {
					JSONObject edge = new JSONObject();
					edge.put("id", depend.getId());
					edge.put("source", start.getId());
					edge.put("target", end.getId());
					edge.put("type", "dependon");
					JSONObject temp = new JSONObject();
					temp.put("data", edge);
					edges.add(temp);
				}
			}
		}
		for(MicroService start : msCalls.keySet()) {
			for(MicroService end : msCalls.get(start).keySet()) {
				if(!msService.isMicroServiceDependOn(start, end)) {
					MicroServiceCallMicroService call = msCalls.get(start).get(end);
					JSONObject edge = new JSONObject();
					edge.put("id", call.getId());
					edge.put("source", start.getId());
					edge.put("target", end.getId());
					edge.put("type", "call");
					JSONObject temp = new JSONObject();
					temp.put("data", edge);
					edges.add(temp);
				}
			}
		}
		return data;
	}
	
}
