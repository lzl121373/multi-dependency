package cn.edu.fudan.se.multidependency.service.query.smell.impl;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.smell.Smell;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellLevel;
import cn.edu.fudan.se.multidependency.model.node.smell.SmellType;
import cn.edu.fudan.se.multidependency.repository.smell.SmellRepository;
import cn.edu.fudan.se.multidependency.repository.smell.UnusedIncludeASRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.smell.SmellUtils;
import cn.edu.fudan.se.multidependency.service.query.smell.UnusedIncludeDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnusedInclude;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

@Service
public class UnusedIncludeDetectorImpl implements UnusedIncludeDetector {

    @Autowired
    private CacheService cache;

    @Autowired
    private UnusedIncludeASRepository unusedIncludeASRepository;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private SmellRepository smellRepository;

    @Override
    public Map<Long, List<UnusedInclude>> queryFileUnusedInclude() {
        String key = "fileUnusedInclude";
        if (cache.get(getClass(), key) != null) {
            return cache.get(getClass(), key);
        }

        Map<Long, List<UnusedInclude>> result = new HashMap<>();
        List<Smell> smells = new ArrayList<>(smellRepository.findSmellsByType(SmellType.UNUSED_INCLUDE));
        SmellUtils.sortSmellByName(smells);
        for (Smell smell : smells) {
            long projectId = smell.getProjectId();
            List<UnusedInclude> unusedIncludeList = result.getOrDefault(projectId, new ArrayList<>());
            unusedIncludeList.add(smellRepository.getUnusedIncludeWithSmellId(smell.getId()));
            result.put(projectId, unusedIncludeList);
        }
        for (Map.Entry<Long, List<UnusedInclude>> entry : result.entrySet()) {
            long projectId = entry.getKey();
            List<UnusedInclude> unusedIncludeList = result.getOrDefault(projectId, new ArrayList<>());
            result.put(projectId, unusedIncludeList);
        }
        cache.cache(getClass(), key, result);
        return result;
    }

    @Override
    public Map<Long, List<UnusedInclude>> detectFileUnusedInclude() {
        Map<Long, List<UnusedInclude>> result = new HashMap<>();
        Set<UnusedInclude> codeFileUnusedIncludeSet = new HashSet<>();
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".c"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".cc"));
        codeFileUnusedIncludeSet.addAll(unusedIncludeASRepository.findUnusedIncludeWithSuffix(".cpp"));
        List<UnusedInclude> codeFileUnusedIncludeList = new ArrayList<>(codeFileUnusedIncludeSet);
        sortFileUnusedIncludeBySizeAndPath(codeFileUnusedIncludeList);
        for (UnusedInclude codeUnusedInclude : codeFileUnusedIncludeList) {
            Project project = containRelationService.findFileBelongToProject(codeUnusedInclude.getCoreFile());
            if (project != null) {
                List<UnusedInclude> unusedIncludeList = result.getOrDefault(project.getId(), new ArrayList<>());
                unusedIncludeList.add(codeUnusedInclude);
                result.put(project.getId(), unusedIncludeList);
            }
        }
        return result;
    }

    @Override
    public JSONObject getFileUnusedIncludeJson(Long projectId, String smellName) {
        Smell smell = smellRepository.findProjectSmellsByName(projectId, smellName);
        return getFileUnusedIncludeJson(smell);
    }

    @Override
    public JSONObject getFileUnusedIncludeJson(Long smellId) {
        Smell smell = smellRepository.findSmell(smellId);
        return getFileUnusedIncludeJson(smell);
    }

    @Override
    public Boolean exportUnusedInclude(Project project) {
        Map<Long, List<UnusedInclude>> fileUnusedIncludeMap = queryFileUnusedInclude();
        List<UnusedInclude> fileUnusedIncludeList = fileUnusedIncludeMap.getOrDefault(project.getId(), new ArrayList<>());
        try {
            exportUnusedInclude(project, fileUnusedIncludeList);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void exportUnusedInclude(Project project, List<UnusedInclude> fileUnusedIncludeList) {
        Workbook workbook = new XSSFWorkbook();
        exportFileUnusedInclude(workbook, fileUnusedIncludeList);
        OutputStream outputStream = null;
        try {
            String fileName = SmellType.UNUSED_INCLUDE + "_" + project.getName() + "(" + project.getLanguage() + ")" + ".xlsx";
            outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void exportFileUnusedInclude(Workbook workbook, List<UnusedInclude> fileUnusedIncludeList) {
        Sheet sheet = workbook.createSheet(SmellLevel.FILE);
        ThreadLocal<Integer> rowKey = new ThreadLocal<>();
        rowKey.set(0);
        Row row = sheet.createRow(rowKey.get());
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Cell cell;
        cell = row.createCell(0);
        cell.setCellValue("Index");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("CoreFile");
        cell.setCellStyle(style);
        cell = row.createCell(2);
        cell.setCellValue("Number");
        cell.setCellStyle(style);
        cell = row.createCell(3);
        cell.setCellValue("UnusedIncludeFiles");
        cell.setCellStyle(style);
        int startRow;
        int endRow;
        int index = 1;
        for (UnusedInclude fileUnusedInclude : fileUnusedIncludeList) {
            startRow = rowKey.get() + 1;
            ProjectFile coreFile = fileUnusedInclude.getCoreFile();
            Set<ProjectFile> unusedIncludeFiles = new HashSet<>(fileUnusedInclude.getUnusedIncludeFiles());
            for (ProjectFile unusedIncludeFile : unusedIncludeFiles) {
                rowKey.set(rowKey.get() + 1);
                row = sheet.createRow(rowKey.get());
                cell = row.createCell(0);
                cell.setCellValue(index);
                style.setAlignment(HorizontalAlignment.CENTER);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                cell.setCellStyle(style);
                cell = row.createCell(1);
                cell.setCellValue(coreFile.getPath());
                style.setAlignment(HorizontalAlignment.LEFT);
                cell = row.createCell(2);
                cell.setCellValue(unusedIncludeFiles.size());
                cell.setCellStyle(style);
                cell = row.createCell(3);
                cell.setCellValue(unusedIncludeFile.getPath());
                style.setAlignment(HorizontalAlignment.LEFT);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                cell.setCellStyle(style);
            }
            endRow = rowKey.get();
            if (endRow - startRow > 0) {
                CellRangeAddress indexRegion = new CellRangeAddress(startRow, endRow, 0, 0);
                sheet.addMergedRegion(indexRegion);
                CellRangeAddress coreFileRegion = new CellRangeAddress(startRow, endRow,  1, 1);
                sheet.addMergedRegion(coreFileRegion);
                CellRangeAddress numberRegion = new CellRangeAddress(startRow, endRow,  2, 2);
                sheet.addMergedRegion(numberRegion);
            }
            index ++;
        }
    }

    private JSONObject getFileUnusedIncludeJson(Smell smell) {
        JSONObject result = new JSONObject();
        JSONArray nodesJson = new JSONArray();
        JSONArray edgesJson = new JSONArray();
        JSONArray smellsJson = new JSONArray();
        List<ProjectFile> files = new ArrayList<>();
        Long coreFileId = 0L;
        if (smell != null) {
            Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
            Iterator<Node> iterator = containedNodes.iterator();
            ProjectFile coreFile = null;
            if (iterator.hasNext()) {
                coreFile = (ProjectFile) iterator.next();
            }
            if (coreFile != null) {
                String key = smell.getId().toString();
                if (cache.get(getClass(), key) != null) {
                    return cache.get(getClass(), key);
                }
                files.add(coreFile);
                coreFileId = coreFile.getId();
                JSONObject smellJson = new JSONObject();
                smellJson.put("name", smell.getName());
                Set<Node> relateToNodes = new HashSet<>(smellRepository.findRelateToNodesBySmellId(smell.getId()));
                List<ProjectFile> smellFiles = new ArrayList<>();
                for (Node relateToNode : relateToNodes) {
                    smellFiles.add((ProjectFile) relateToNode);
                }
                files.addAll(smellFiles);
                JSONArray smellNodesJson = new JSONArray();
                for (ProjectFile smellFile : smellFiles) {
                    if (!files.contains(smellFile)) {
                        files.add(smellFile);
                    }
                    JSONObject smellNodeJson = new JSONObject();
                    smellNodeJson.put("index", files.indexOf(smellFile) + 1);
                    smellNodeJson.put("path", smellFile.getPath());
                    smellNodesJson.add(smellNodeJson);
                }
                smellJson.put("nodes", smellNodesJson);
                smellJson.put("coreFilePath", coreFile.getPath());
                smellsJson.add(smellJson);
                int length = files.size();
                for (int i = 0; i < length; i ++) {
                    ProjectFile file = files.get(i);
                    JSONObject nodeJson = new JSONObject();
                    nodeJson.put("id", file.getId().toString());
                    nodeJson.put("name", file.getName());
                    nodeJson.put("path", file.getPath());
                    nodeJson.put("label", i + 1);
                    nodeJson.put("size", SmellUtils.getSizeOfNodeByLoc(file.getLoc()));
                    nodesJson.add(nodeJson);
                    if (i > 0) {
                        JSONObject edgeJson = new JSONObject();
                        edgeJson.put("id", i);
                        edgeJson.put("source", coreFile.getId().toString());
                        edgeJson.put("target", file.getId().toString());
                        edgeJson.put("source_name", coreFile.getName());
                        edgeJson.put("target_name", file.getName());
                        edgeJson.put("source_label", 1);
                        edgeJson.put("target_label", i + 1);
                        edgesJson.add(edgeJson);
                    }
                }
            }
        }
        result.put("smellType", SmellType.UNUSED_INCLUDE);
        result.put("coreNode", coreFileId.toString());
        result.put("nodes", nodesJson);
        result.put("edges", edgesJson);
        result.put("smells", smellsJson);
        if (smell != null) {
            Set<Node> containedNodes = new HashSet<>(smellRepository.findContainedNodesBySmellId(smell.getId()));
            Iterator<Node> iterator = containedNodes.iterator();
            ProjectFile coreFile = null;
            if (iterator.hasNext()) {
                coreFile = (ProjectFile) iterator.next();
            }
            if (coreFile != null) {
                String key = smell.getId().toString();
                cache.cache(getClass(), key, result);
            }
        }
        return result;
    }

    private void sortFileUnusedIncludeBySizeAndPath(List<UnusedInclude> fileUnusedIncludeList) {
        fileUnusedIncludeList.sort((unusedInclude1, unusedInclude2) -> {
            int sizeCompare = Integer.compare(unusedInclude2.getUnusedIncludeFiles().size(), unusedInclude1.getUnusedIncludeFiles().size());
            if (sizeCompare == 0) {
                return unusedInclude1.getCoreFile().getPath().compareTo(unusedInclude2.getCoreFile().getPath());
            }
            return sizeCompare;
        });
    }
}
