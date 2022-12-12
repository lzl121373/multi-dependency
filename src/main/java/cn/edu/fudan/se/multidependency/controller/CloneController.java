package cn.edu.fudan.se.multidependency.controller;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.*;

import cn.edu.fudan.se.multidependency.model.relation.clone.ModuleClone;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.repository.relation.git.CoChangeRepository;
import cn.edu.fudan.se.multidependency.service.insert.RepositoryService;
import cn.edu.fudan.se.multidependency.service.query.aggregation.SummaryAggregationDataService;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChangeMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.query.ProjectService;
import cn.edu.fudan.se.multidependency.service.query.clone.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneShowService;
import cn.edu.fudan.se.multidependency.service.query.clone.CloneValueService;
import cn.edu.fudan.se.multidependency.service.query.clone.SimilarPackageDetector;
import cn.edu.fudan.se.multidependency.service.query.clone.data.CloneValueForDoubleNodes;
import cn.edu.fudan.se.multidependency.service.query.clone.data.PackageCloneValueWithFileCoChange;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import cn.edu.fudan.se.multidependency.repository.relation.clone.ModuleCloneRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Controller
@RequestMapping("/clone")
public class CloneController {
	
	@Autowired
	private ProjectService projectService;

	@Autowired
	private CloneValueService cloneValueService;
	
	@Autowired
	private BasicCloneQueryService basicCloneQueryService;
	
	@Autowired
	private CloneShowService cloneShowService;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	@Autowired
	private SimilarPackageDetector similarPackageDetector;

	@Autowired
	private ModuleCloneRepository moduleCloneRepository;

	@Autowired
	private CoChangeRepository coChangeRepository;

	@Autowired
	private SummaryAggregationDataService summaryAggregationDataService;

	@GetMapping("/packages")
	public String graph() {
		return "clonepackage";
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryService.class);

	@GetMapping("/packages/export")
	@ResponseBody
	public void exportSimilarPackages(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.addHeader("Content-Disposition", "attachment;filename=similar_packages.xlsx");
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			OutputStream stream = response.getOutputStream();

			similarPackageDetector.exportSimilarPackages(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@GetMapping("/package/cytoscape")
	@ResponseBody
	public JSONObject packageClonesToCytoscape() {
		JSONObject result = new JSONObject();
		result.put("result", "success");
		result.put("value", cloneShowService.crossPackageCloneToCytoscape(
				cloneValueService.queryPackageCloneFromFileCloneSort(
				basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE))));
		
		return result;
	}

	@GetMapping("/package")
	@ResponseBody
	public List<ModuleClone> cloneInPackages() {
		List<ModuleClone> result = moduleCloneRepository.getAllModuleClone();
		result.forEach( moduleClone -> {
			Package pck1 = (Package)moduleClone.getNode1();
			Package pck2 = (Package)moduleClone.getNode2();
			CoChange moduleCoChange = coChangeRepository.findPackageCoChangeByPackageId(pck1.getId(),pck2.getId());
			if(moduleCoChange != null){
				moduleClone.setAllNodesCoChangeTimes(moduleCoChange.getTimes());
			}
		});
		result.sort((v1, v2) -> {
			return v2.getClonePairs() - v1.getClonePairs();
		});
		return result;
	}

	/**
	 * 两个包之间的文件依赖
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double")
	@ResponseBody
	public CloneValueForDoubleNodes<Package> cloneInPackage(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		JSONObject result = new JSONObject();
		CloneValueForDoubleNodes<Package> cloneValue = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
		JSONObject pck1JSON = new JSONObject();
		pck1JSON.put("package", pck1);
		Collection<ProjectFile> files1 = containRelationService.findPackageContainFiles(pck1);
		pck1JSON.put("files", files1);
		JSONObject pck2JSON = new JSONObject();
		pck2JSON.put("package", pck2);
		Collection<ProjectFile> files2 = containRelationService.findPackageContainFiles(pck2);
		pck2JSON.put("files", files2);
		
		result.put("result", "success");
		result.put("cloneValue", cloneValue);
		result.put("package1", pck1JSON);
		result.put("package2", pck2JSON);
		return cloneValue;
	}
	
	/**
	 * 两个包之间的文件依赖
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double/json")
	@ResponseBody
	public JSONObject cloneInPackageJson(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		CloneValueForDoubleNodes<Package> value = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
		JSONObject result = new JSONObject();
		List<Clone> children = value.getChildren();
		result.put("result", cloneShowService.graphFileClones(children));
		return result;
	}
	
	/**
	 * 两个包之间的文件依赖加上cochange次数
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double/filetree")
	@ResponseBody
	public JSONObject clonesInPackageWithCoChange(@RequestParam("package1") long package1Id, @RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		JSONObject result = new JSONObject();
		if(pck1 == null || pck2 == null) {
			return null;
		}
		try {
			PackageCloneValueWithFileCoChange pckClone = summaryAggregationDataService.queryPackageCloneWithFileCoChange(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);

			JSONArray cloneFiles1 = setCloneAndNoneCloneFiles(pckClone.getCloneFiles1());
			JSONArray cloneFiles2 = setCloneAndNoneCloneFiles(pckClone.getCloneFiles2());
			JSONArray noneCloneFiles1 = setCloneAndNoneCloneFiles(pckClone.getNoneCloneFiles1());
			JSONArray noneCloneFiles2 = setCloneAndNoneCloneFiles(pckClone.getNoneCloneFiles2());

			JSONObject clone1 = new JSONObject();
			JSONObject noneClone1 = new JSONObject();
			JSONObject clone2 = new JSONObject();
			JSONObject noneClone2 = new JSONObject();
			JSONObject package1 = new JSONObject();
			JSONObject package2 = new JSONObject();

			JSONArray packageContain1 = new JSONArray();
			JSONArray packageContain2 = new JSONArray();

			clone1.put("name", "cloneFiles(" +  pckClone.getCloneFiles1().size() + ")");
			clone1.put("open", false);
			clone1.put("children", cloneFiles1);
			clone2.put("name", "cloneFiles(" +  pckClone.getCloneFiles2().size() + ")");
			clone2.put("open", false);
			clone2.put("children", cloneFiles2);

			noneClone1.put("name", "noneCloneFiles(" +  pckClone.getNoneCloneFiles1().size() + ")");
			noneClone1.put("open", false);
			noneClone1.put("children", noneCloneFiles1);
			noneClone2.put("name", "noneCloneFiles(" +  pckClone.getNoneCloneFiles2().size() + ")");
			noneClone2.put("open", false);
			noneClone2.put("children", noneCloneFiles2);

			packageContain1.add(clone1);
			packageContain1.add(noneClone1);
			packageContain2.add(clone2);
			packageContain2.add(noneClone2);

			int size1 = pckClone.getCloneFiles1().size() + pckClone.getNoneCloneFiles1().size();
			int size2 = pckClone.getCloneFiles2().size() + pckClone.getNoneCloneFiles2().size();
			package1.put("name",pck1.getDirectoryPath() + "(" + size1 + ")");
			package1.put("open", true);
			package1.put("children",packageContain1);
			package2.put("name",pck2.getDirectoryPath() + "(" + size2 + ")");
			package2.put("open", true);
			package2.put("children",packageContain2);

			JSONArray result1 = new JSONArray();
			JSONArray result2 = new JSONArray();
			result1.add(package1);
			result2.add(package2);

			result.put(pck1.getId().toString(),result1);
			result.put(pck2.getId().toString(),result2);

			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 两个包之间的文件依赖加上cochange次数
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double/cochange")
	@ResponseBody
	public PackageCloneValueWithFileCoChange clonesInPackage(@RequestParam("package1") long package1Id, @RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		try {
			return summaryAggregationDataService.queryPackageCloneWithFileCoChange(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 两个包之间的文件依赖加上cochange次数
	 * @param package1Id
	 * @param package2Id
	 * @return
	 */
	@GetMapping("/package/double/matrix")
	@ResponseBody
	public PackageCloneValueWithFileCoChangeMatrix clonesInPackageWithMatrix(@RequestParam("package1") long package1Id, @RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		try {
			return summaryAggregationDataService.queryPackageCloneWithFileCoChangeMatrix(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONArray setCloneAndNoneCloneFiles(Set<ProjectFile> files){
		JSONArray temp = new JSONArray();
		for(ProjectFile projectFile: files){
			JSONObject cloneFile = new JSONObject();
			cloneFile.put("name", projectFile.getPath());
			temp.add(cloneFile);
		}
		return temp;
	}
	
	@GetMapping("/package/double/graph")
	@ResponseBody
	public JSONArray clonesInPackageToGraph(@RequestParam("package1") long package1Id,
			@RequestParam("package2") long package2Id) {
		Package pck1 = nodeService.queryPackage(package1Id);
		Package pck2 = nodeService.queryPackage(package2Id);
		if(pck1 == null || pck2 == null) {
			return null;
		}
		try {
			CloneValueForDoubleNodes<Package> value = cloneValueService.queryPackageCloneFromFileCloneSort(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pck1, pck2);
			Collection<Clone> clones = value == null ? new ArrayList<>() : value.getChildren();
			return cloneShowService.graphFileClones(clones);
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONArray();
		}
	}
	
	@PostMapping("/package/multiple")
	@ResponseBody
	public Collection<CloneValueForDoubleNodes<Package>> cloneInMultiplePackages(@RequestBody Map<String, Object> params) {
		List<Long> pckIds = (List<Long>) params.getOrDefault("ids", new ArrayList<>());
		List<Package> pcks = new ArrayList<>();
		for(Long pckId : pckIds) {
			Package pck = nodeService.queryPackage(pckId);
			if(pck != null) {
				pcks.add(pck);
			}
		}
		return cloneValueService.queryPackageCloneFromFileClone(basicCloneQueryService.findClonesByCloneType(CloneRelationType.FILE_CLONE_FILE), pcks);
	}

	@GetMapping("/compare")
	public String compare(@RequestParam("id1") long file1Id, @RequestParam("id2") long file2Id) {
		ProjectFile file1 = nodeService.queryFile(file1Id);
		ProjectFile file2 = nodeService.queryFile(file2Id);
		if(file1 == null || file2 == null) {
			return "error";
		}
		Project project1 = containRelationService.findFileBelongToProject(file1);
		Project project2 = containRelationService.findFileBelongToProject(file2);
		String file1AbsolutePath = projectService.getAbsolutePath(project1) + file1.getPath();
		String file2AbsolutePath = projectService.getAbsolutePath(project2) + file2.getPath();
		return "redirect:/compare?leftFilePath=" + file1AbsolutePath + "&rightFilePath=" + file2AbsolutePath;
	}

	@GetMapping("/compare/files")
	@ResponseBody
	public JSONObject compareFiles(@RequestParam("file1AbsolutePath") String file1AbsolutePath, @RequestParam("file2AbsolutePath") String file2AbsolutePath, @RequestParam("decoder1") String decoder1, @RequestParam("decoder2") String decoder2) {
		JSONObject context = new JSONObject();
		File file1 = new File(file1AbsolutePath);
		try {
			FileChannel inChannel1 = new FileInputStream(file1).getChannel();
			MappedByteBuffer buffer1 = inChannel1.map(FileChannel.MapMode.READ_ONLY, 0, file1.length());
			Charset charset1;
			switch (decoder1) {
				case "GBK":
					charset1 = Charset.forName("GBK");
					break;
				case "GB2312":
					charset1 = Charset.forName("GB2312");
					break;
				case "GB18030":
					charset1 = Charset.forName("GB18030");
					break;
				case "ISO-8859-2":
					charset1 = Charset.forName("ISO-8859-2");
					break;
				default:
					charset1 = StandardCharsets.UTF_8;
			}
			CharsetDecoder charsetDecoder1 = charset1.newDecoder();
			CharBuffer charBuffer1 = charsetDecoder1.decode(buffer1);
			context.put("file1", charBuffer1.toString());
		}
		catch (Exception e) {
			System.out.println(e);
		}
		File file2 = new File(file2AbsolutePath);
		try {
			FileChannel inChannel2 = new FileInputStream(file2).getChannel();
			MappedByteBuffer buffer2 = inChannel2.map(FileChannel.MapMode.READ_ONLY, 0, file2.length());
			Charset charset2;
			switch (decoder2) {
				case "GBK":
					charset2 = Charset.forName("GBK");
					break;
				case "GB2312":
					charset2 = Charset.forName("GB2312");
					break;
				case "GB18030":
					charset2 = Charset.forName("GB18030");
					break;
				case "ISO-8859-2":
					charset2 = Charset.forName("ISO-8859-2");
					break;
				default:
					charset2 = StandardCharsets.UTF_8;
			}
			CharsetDecoder charsetDecoder2 = charset2.newDecoder();
			CharBuffer charBuffer2 = charsetDecoder2.decode(buffer2);
			context.put("file2", charBuffer2.toString());
		}
		catch (Exception e) {
			System.out.println(e.toString());
		}
		return context;
	}

	/**
	 * 查询一个project中所有的包克隆关系
	 * @return
	 */
	@GetMapping("/file/double/json")
	@ResponseBody
	public JSONObject DoubleFilesStructureJson(@RequestParam("fileId1") long fileId1, @RequestParam("fileId2") long fileId2) {
		ProjectFile file1 = nodeService.queryFile(fileId1);
		ProjectFile file2 = nodeService.queryFile(fileId2);
		List<ProjectFile> fileList = new ArrayList<>();
		fileList.add(file1);
		fileList.add(file2);
		return containRelationService.doubleFileStructure(fileList);
	}

	@GetMapping("/file/double")
	public String DoubleFilesStructure(@RequestParam("file1Id") long file1Id, @RequestParam("file2Id") long file2Id,
									   @RequestParam("cloneType") String cloneType, @RequestParam("linesSize1") int linesSize1,
									   @RequestParam("linesSize2") int linesSize2, @RequestParam("loc1") int loc1,
									   @RequestParam("loc2") int loc2, @RequestParam("value") double value,
									   @RequestParam("cochange") int cochange, @RequestParam("filePath1") String filePath1,
									   @RequestParam("filePath2") String filePath2, @RequestParam("cochangeId") long cochangeId,
									   HttpServletRequest request) {
		request.setAttribute("file1Id", file1Id);
		request.setAttribute("file2Id", file2Id);
		request.setAttribute("linesSize1", linesSize1);
		request.setAttribute("linesSize2", linesSize2);
		request.setAttribute("loc1", loc1);
		request.setAttribute("loc2", loc2);
		request.setAttribute("value", value);
		request.setAttribute("cochange", cochange);
		request.setAttribute("cochangeId", cochangeId);
		return "doublefilestructure";
	}
}
