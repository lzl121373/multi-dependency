package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.smell.data.*;
import com.alibaba.fastjson.JSONObject;

public interface MultipleSmellDetector {
	
	Map<Long, HistogramAS> projectHistogramOnVersion();
	
	Map<Long, List<CirclePacking>> circlePacking(MultipleAS multipleAS);
	
	Map<Long, List<MultipleASFile>> queryMultipleSmellASFile(boolean removeNoASFile);

	Map<Long, List<MultipleASPackage>> queryMultipleSmellASPackage(boolean removeNoASPackage);

	Map<Long, List<MultipleASFile>> detectMultipleSmellASFile(boolean removeNoASFile);

	Map<Long, List<MultipleASPackage>> detectMultipleSmellASPackage(boolean removeNoASPackage);

	Map<Long, PieFilesData> smellAndIssueFiles(MultipleAS multipleAS);

	Map<Long, JSONObject> getProjectTotal();

	Map<Long, JSONObject> getFileSmellOverview();

	Map<Long, JSONObject> getPackageSmellOverview();
}
