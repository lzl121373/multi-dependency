package cn.edu.fudan.se.multidependency.service.query.metric;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Metric;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.repository.node.MetricRepository;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

@Service
public class MetricShowService {
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private MetricCalculatorService metricCalculatorService;

	@Autowired
	private SmellRepository smellRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private CacheService cache;

	public List<NodeMetric> getFileMetrics(){
		List<NodeMetric> metricList = new ArrayList<>(metricRepository.findFileMetricData());
		return metricList;
	}

	public Metric getFileMetric(ProjectFile file){
		Metric metric = metricRepository.findFileMetric(file.getId());
		if(metric == null){
			return new Metric();
		}
		return metric;
	}

	public Map<Long, List<NodeMetric>> getProjectFileMetrics(){
		Map<Long,List<NodeMetric>> result = new HashMap<>();
		Collection<ProjectFile> projectFiles = new ArrayList<>(nodeService.queryAllFiles());
		projectFiles.forEach(projectFile -> {
			Project project = containRelationService.findFileBelongToProject(projectFile);
			List<NodeMetric> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			Metric metric = metricRepository.findFileMetric(projectFile.getId());
			if(metric != null){
				NodeMetric nodeMetric = new NodeMetric();
				nodeMetric.setNode(projectFile);
				nodeMetric.setMetric(metric);
				temp.add(nodeMetric);
				result.put(project.getId(), temp);
			}
		});
		return result;
	}

	public List<Metric> getPackageMetrics(){
		List<Metric> metricList = new ArrayList<>(metricRepository.findPackageMetric());
		return metricList;
	}

	public Metric getPackageMetric(Package pck){
		Metric metric = metricRepository.findPackageMetric(pck.getId());
		if(metric == null){
			return new Metric();
		}
		return metric;
	}

	public Map<Long, List<NodeMetric>> getProjectPackageMetrics(){
		Map<Long,List<NodeMetric>> result = new HashMap<>();
		List<Package> projectPackages = new ArrayList<>(nodeService.queryAllPackages());
		for (Package pck : projectPackages){
			Project project = containRelationService.findPackageBelongToProject(pck);
			List<NodeMetric> temp = result.getOrDefault(project.getId(), new ArrayList<>());
			Metric metric = metricRepository.findPackageMetric(pck.getId());
			if(metric != null){
				NodeMetric nodeMetric = new NodeMetric();
				nodeMetric.setNode(pck);
				nodeMetric.setMetric(metric);
				temp.add(nodeMetric);
				result.put(project.getId(), temp);
			}
		}
		return result;
	}

	public List<NodeMetric> getProjectMetrics(){
		List<NodeMetric> metricList = new ArrayList<>(metricRepository.findProjectMetricData());
		return metricList;
	}

	public Metric getProjectMetric(Project project){
		Metric metric = metricRepository.findProjectMetric(project.getId());
		return metric;
	}

	public List<NodeMetric> getGitRepoMetrics(){
		List<NodeMetric> metricList = new ArrayList<>(metricRepository.findGitRepoMetricData());
		return metricList;
	}

	public List<Metric> getGitRepoMetric(GitRepository gitRepo){
		List<Metric> metricList = metricRepository.findGitRepoMetric(gitRepo.getId());
		return metricList;
	}

	public List<Metric> getSmellMetrics(){
		List<Metric> metricList = new ArrayList<>(smellRepository.findSmellMetric());
		return metricList;
	}

	public Metric getSmellMetric(Smell smell){
		Metric metric = smellRepository.findSmellMetric(smell.getId());
		return metric;
	}

	public Map<Long, Map<String, List<NodeMetric>>>  getProjectSmellMetricsInFileLevel(){
		String key = "getProjectSmellMetricsInFileLevel";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.FILE));
		Map<Long, Map<String, List<NodeMetric>>> result = new HashMap<>();
		if(!smells.isEmpty()){
			smells.forEach(smell ->{
				Long projectId = smell.getProjectId();
				if (projectId != null){
					Map<String, List<NodeMetric>> smellTypeMetricMap = result.getOrDefault(projectId, new HashMap<>());
					String type = smell.getType();
					List<NodeMetric> metricList = smellTypeMetricMap.getOrDefault(type,new ArrayList<>());
					Metric metric = smellRepository.findSmellMetric(smell.getId());
					if(metric != null){
						NodeMetric nodeMetric = new NodeMetric();
						nodeMetric.setNode(smell);
						nodeMetric.setMetric(metric);
						metricList.add(nodeMetric);
						smellTypeMetricMap.put(type, metricList);
						result.put(projectId, smellTypeMetricMap);
					}
				}
			});
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	public Map<Long, Map<String, List<NodeMetric>>>  getProjectSmellMetricsInPackageLevel(){
		String key = "getProjectSmellMetricsInPackageLevel";
		if(cache.get(getClass(), key) != null) {
			return cache.get(getClass(), key);
		}

		List<Smell> smells = new ArrayList<>(smellRepository.findSmells(SmellLevel.PACKAGE));
		Map<Long, Map<String, List<NodeMetric>>> result = new HashMap<>();
		if(!smells.isEmpty()){
			smells.forEach(smell ->{
				Long projectId = smell.getProjectId();
				if (projectId != null){
					Map<String, List<NodeMetric>> smellTypeMetricMap = result.getOrDefault(projectId, new HashMap<>());
					String type = smell.getType();
					List<NodeMetric> metricList = smellTypeMetricMap.getOrDefault(type,new ArrayList<>());
					Metric metric = smellRepository.findSmellMetric(smell.getId());
					if(metric != null){
						NodeMetric nodeMetric = new NodeMetric();
						nodeMetric.setNode(smell);
						nodeMetric.setMetric(metric);
						metricList.add(nodeMetric);
						smellTypeMetricMap.put(type, metricList);
						result.put(projectId, smellTypeMetricMap);
					}
				}
			});
			cache.cache(getClass(), key, result);
		}
		return result;
	}

	public void printPackageMetricExcel(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		Map<Long, List<PackageMetric>> allPackageMetrics = metricCalculatorService.calculateProjectPackageMetrics();
		List<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			Sheet sheet = hwb.createSheet(new StringBuilder().append(project.getName()).append("(").append(project.getLanguage()).append(")").toString());
			List<PackageMetric> packageMetrics = allPackageMetrics.get(project.getId());
			Row row = sheet.createRow(0);
			CellStyle style = hwb.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
//			sheet.setColumnWidth(0, "xxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(1, "xxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(2, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(3, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(4, "xxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(5, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(6, "xxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(7, "xxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(8, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(9, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(10, "xxxxxx".length() * 256);
//			sheet.setColumnWidth(11, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(12, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(13, "xxxxxxxxxx".length() * 256);
			Cell cell = null;
			cell = row.createCell(0);
			cell.setCellValue("id");
			cell.setCellStyle(style);
			cell = row.createCell(1);
			cell.setCellValue("目录");
			cell.setCellStyle(style);
			cell = row.createCell(2);
			cell.setCellValue("NOF");
			cell.setCellStyle(style);
			cell = row.createCell(3);
			cell.setCellValue("NOM");
			cell.setCellStyle(style);
			cell = row.createCell(4);
			cell.setCellValue("LOC");
			cell.setCellStyle(style);
			cell = row.createCell(5);
			cell.setCellValue("Fan In");
			cell.setCellStyle(style);
			cell = row.createCell(6);
			cell.setCellValue("Fan Out");
			cell.setCellStyle(style);
			for (int i = 0; i < packageMetrics.size(); i++) {
				PackageMetric packageMetric = packageMetrics.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(packageMetric.getPck().getId());
				row.createCell(1).setCellValue(packageMetric.getPck().getDirectoryPath());
				row.createCell(2).setCellValue(packageMetric.getNof());
				row.createCell(3).setCellValue(packageMetric.getNom());
				row.createCell(4).setCellValue(packageMetric.getLoc());
				row.createCell(5).setCellValue(packageMetric.getFanIn());
				row.createCell(6).setCellValue(packageMetric.getFanOut());
			}			
		}
		try {
			hwb.write(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
				hwb.close();
			} catch (IOException e) {
			}
		}
	}

	public void printFileMetricExcel(OutputStream stream) {
		Workbook hwb = new XSSFWorkbook();
		Map<Long, List<FileMetric>> allFileMetrics = metricCalculatorService.calculateProjectFileMetrics();
		List<Project> projects = nodeService.allProjects();
		for(Project project : projects) {
			Sheet sheet = hwb.createSheet(new StringBuilder().append(project.getName()).append("(").append(project.getLanguage()).append(")").toString());
			List<FileMetric> fileMetrics = allFileMetrics.get(project.getId());
			Row row = sheet.createRow(0);
			CellStyle style = hwb.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
//			sheet.setColumnWidth(0, "xxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(1, "xxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(2, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(3, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(4, "xxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(5, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(6, "xxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(7, "xxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(8, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(9, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(10, "xxxxxx".length() * 256);
//			sheet.setColumnWidth(11, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(12, "xxxxxxxxxxxxxxxxxxxxxxxx".length() * 256);
//			sheet.setColumnWidth(13, "xxxxxxxxxx".length() * 256);
			Cell cell = null;
			cell = row.createCell(0);
			cell.setCellValue("id");
			cell.setCellStyle(style);
			cell = row.createCell(1);
			cell.setCellValue("文件");
			cell.setCellStyle(style);
			cell = row.createCell(2);
			cell.setCellValue("LOC（代码行）");
			cell.setCellStyle(style);
			cell = row.createCell(3);
			cell.setCellValue("NOM（方法数）");
			cell.setCellStyle(style);
			cell = row.createCell(4);
			cell.setCellValue("Fan In");
			cell.setCellStyle(style);
			cell = row.createCell(5);
			cell.setCellValue("Fan Out");
			cell.setCellStyle(style);
			cell = row.createCell(6);
			cell.setCellValue("修改次数");
			cell.setCellStyle(style);
			cell = row.createCell(7);
			cell.setCellValue("协同修改的commit次数");
			cell.setCellStyle(style);
			cell = row.createCell(8);
			cell.setCellValue("协同修改的文件数");
			cell.setCellStyle(style);
			cell = row.createCell(9);
			cell.setCellValue("Page Rank");
			cell.setCellStyle(style);
			for (int i = 0; i < fileMetrics.size(); i++) {
				FileMetric fileMetric = fileMetrics.get(i);
				row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(fileMetric.getFile().getId());
				row.createCell(1).setCellValue(fileMetric.getFile().getPath());
				row.createCell(2).setCellValue(fileMetric.getStructureMetric().getLoc());
				row.createCell(3).setCellValue(fileMetric.getStructureMetric().getNom());
				row.createCell(4).setCellValue(fileMetric.getFanIn());
				row.createCell(5).setCellValue(fileMetric.getFanOut());
				row.createCell(6).setCellValue(fileMetric.getEvolutionMetric().getCommits());
//				row.createCell(7).setCellValue(fileMetric.getEvolutionMetric().getCoChangeCommitTimes());
				row.createCell(8).setCellValue(fileMetric.getEvolutionMetric().getCoChangeFiles());
				row.createCell(9).setCellValue(fileMetric.getPageRankScore());
			}			
		}
		try {
			hwb.write(stream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
				hwb.close();
			} catch (IOException e) {
			}
		}
	}
	
}
