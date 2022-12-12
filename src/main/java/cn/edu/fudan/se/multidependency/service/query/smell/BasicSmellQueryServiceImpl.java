package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BasicSmellQueryServiceImpl implements BasicSmellQueryService {

    @Autowired
	private SmellRepository smellRepository;

	@Override
	public Collection<Smell> findSmellsByLevel(String level) {
		Collection<Smell> result = smellRepository.findSmells(level);
		for(Smell smell : result){
			Set<Node> set = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
			smell.setNodes(set);
		}
		return result;
	}

	@Override
	public JSONArray smellDataToGraph(){
		JSONArray smellDataArray = new JSONArray();
		try {
			Collection<Smell> smellGroups = findSmellsByLevel(SmellLevel.FILE);

			for(Smell smell : smellGroups){

				JSONObject temp_smell = new JSONObject();
				JSONArray temp_nodes = new JSONArray();
				temp_smell.put("name", smell.getName());
				temp_smell.put("id", smell.getId());
				temp_smell.put("smell_type", smell.getType());
				temp_smell.put("smell_level", smell.getLevel());
				temp_smell.put("project_belong", smell.getProjectId());

				for(Node node : smell.getNodes()){
					ProjectFile file = (ProjectFile)node;
					JSONObject temp_node = new JSONObject();
					temp_node.put("id", "id_" + file.getId());
					temp_node.put("path", file.getPath());
					temp_node.put("name", file.getName());

					temp_nodes.add(temp_node);
				}

				temp_smell.put("nodes", temp_nodes);
				smellDataArray.add(temp_smell);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return smellDataArray;
	}

	@Override
	public JSONArray smellInfoToGraph(long projectId){
		JSONArray smellInfoArray = new JSONArray();

		try {
			Collection<Smell> smellGroups = findSmellsByLevel(SmellLevel.FILE);
			List<String> smellTypeList = new ArrayList<>();

			for(Smell smell : smellGroups) {
				if (!smellTypeList.contains(smell.getType()) && smell.getProjectId().equals(projectId)) {
					smellTypeList.add(smell.getType());
					JSONObject smellTypeInfo = new JSONObject();
					smellTypeInfo.put("smell_type", smell.getType());
					smellTypeInfo.put("smell_num", smellRepository.findSmellsCount(smell.getLevel(), smell.getType()));

					smellInfoArray.add(smellTypeInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return smellInfoArray;
	}

	@Override
	public Metric findMetricBySmellId(long smellId){
		return smellRepository.findSmellMetric(smellId);
	}
}
