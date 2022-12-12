package cn.edu.fudan.se.multidependency.service.insert.code;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;

public class RestfulAPIFileExtractorImpl implements RestfulAPIFileExtractor {
	
	public static Set<String> tests = new HashSet<>();
	
	private String filePath;
	
	private SwaggerJSON swagger;
	
	public RestfulAPIFileExtractorImpl(SwaggerJSON swagger) {
		this.swagger = swagger;
	}
	
	@Override
	public Iterable<RestfulAPI> extract() {
		String filePath = swagger.getPath();
		this.filePath = filePath;
		List<RestfulAPI> result = new ArrayList<>();
		try {
			JSONObject json = extractSwagger();
			JSONObject paths = json.getJSONObject("paths");
			for(String endpoint : paths.keySet()) {
				JSONObject forms = paths.getJSONObject(endpoint);
				for(String form : forms.keySet()) {
					JSONObject value = forms.getJSONObject(form);
					JSONArray tags = value.getJSONArray("tags");
					boolean exclude = false;
					for(int i = 0; i < tags.size(); i++) {
						if(swagger.getExcludeTags().contains(tags.getString(i))) {
							exclude = true;
							break;
						}
					}
					if(!exclude) {
						RestfulAPI api = new RestfulAPI();
						api.setEndPoint(endpoint);
						api.setForm(form);
						api.setApiFunctionSimpleName(forms.getJSONObject(form).getString("summary"));
						result.add(api);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private JSONObject extractSwagger() throws Exception {
		return JSONUtil.extractJSONObject(new File(filePath));
	}

}
