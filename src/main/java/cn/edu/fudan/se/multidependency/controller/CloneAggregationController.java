package cn.edu.fudan.se.multidependency.controller;

import java.io.OutputStream;
import java.util.*;

import cn.edu.fudan.se.multidependency.service.query.aggregation.HotspotPackagePairDetector;
import cn.edu.fudan.se.multidependency.service.query.aggregation.data.HotspotPackagePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/cloneaggregation")
public class CloneAggregationController {

    @Autowired
    private HotspotPackagePairDetector hotspotPackagePairDetector;

    @GetMapping(value = {""})
    public String graph() {
        return "cloneaggregation";
    }

    /**
     * java项目两个包之间的克隆聚合
     * @param threshold
     * @param percentage
     * @return
     */
    @GetMapping("/show/java")
    @ResponseBody
    public List<HotspotPackagePair> showHotspotPackagesOfJava(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        return hotspotPackagePairDetector.getHotspotPackagePairWithFileCloneByParentId(-1, -1, "java");
    }

    /**
     * cpp项目两个包之间的克隆聚合
     * @param threshold
     * @param percentage
     * @return
     */
    @GetMapping("/show/cpp")
    @ResponseBody
    public List<HotspotPackagePair> showHotspotPackagesOfCpp(@RequestParam("threshold") int threshold, @RequestParam("percentage") double percentage) {
        return hotspotPackagePairDetector.getHotspotPackagePairWithFileCloneByParentId(-1, -1, "cpp");
    }

    @GetMapping("/package/export")
    @ResponseBody
    public void exportSimilarPackages(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.addHeader("Content-Disposition", "attachment;filename=similar_packages.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            OutputStream stream = response.getOutputStream();
            hotspotPackagePairDetector.exportHotspotPackages(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/details")
    public String showDetails(@RequestParam("id1") long id1,
                              @RequestParam("id2") long id2,
                              @RequestParam("path1") String path1,
                              @RequestParam("path2") String path2,
                              @RequestParam("clonePairs") int clonePairs,
                              @RequestParam("cloneNodesCount1") int cloneNodesCount1,
                              @RequestParam("cloneNodesCount2") int cloneNodesCount2,
                              @RequestParam("allNodesCount1") int allNodesCount1,
                              @RequestParam("allNodesCount2") int allNodesCount2,
                              @RequestParam("cloneMatchRate") double cloneMatchRate,
                              @RequestParam("cloneNodesLoc1") int cloneNodesLoc1,
                              @RequestParam("cloneNodesLoc2") int cloneNodesLoc2,
                              @RequestParam("allNodesLoc1") int allNodesLoc1,
                              @RequestParam("allNodesLoc2") int allNodesLoc2,
                              @RequestParam("cloneLocRate") double cloneLocRate,
                              @RequestParam("cloneNodesCoChangeTimes") int cloneNodesCoChangeTimes,
                              @RequestParam("allNodesCoChangeTimes") int allNodesCoChangeTimes,
                              @RequestParam("cloneCoChangeRate") double cloneCoChangeRate,
                              @RequestParam("cloneType1Count") int cloneType1Count,
                              @RequestParam("cloneType2Count") int cloneType2Count,
                              @RequestParam("cloneType3Count") int cloneType3Count,
                              @RequestParam("cloneType") String cloneType,
                              @RequestParam("cloneSimilarityValue") double cloneSimilarityValue,
                              @RequestParam("cloneSimilarityRate") double cloneSimilarityRate,
                              HttpServletRequest request) {
        request.setAttribute("id1", id1);
        request.setAttribute("id2", id2);
        request.setAttribute("clonePairs", clonePairs);
        request.setAttribute("cloneNodesCount1", cloneNodesCount1);
        request.setAttribute("cloneNodesCount2", cloneNodesCount2);
        request.setAttribute("allNodesCount1", allNodesCount1);
        request.setAttribute("allNodesCount2", allNodesCount2);
        request.setAttribute("cloneMatchRate", cloneMatchRate);
        request.setAttribute("cloneNodesLoc1", cloneNodesLoc1);
        request.setAttribute("cloneNodesLoc2", cloneNodesLoc2);
        request.setAttribute("allNodesLoc1", allNodesLoc1);
        request.setAttribute("allNodesLoc2", allNodesLoc2);
        request.setAttribute("cloneLocRate", cloneLocRate);
        request.setAttribute("cloneNodesCoChangeTimes", cloneNodesCoChangeTimes);
        request.setAttribute("allNodesCoChangeTimes", allNodesCoChangeTimes);
        request.setAttribute("cloneCoChangeRate", cloneCoChangeRate);
        request.setAttribute("cloneType1Count", cloneType1Count);
        request.setAttribute("cloneType2Count", cloneType2Count);
        request.setAttribute("cloneType3Count", cloneType3Count);
        request.setAttribute("cloneSimilarityValue", cloneSimilarityValue);
        request.setAttribute("cloneSimilarityRate", cloneSimilarityRate);
        return "details";
    }
}
