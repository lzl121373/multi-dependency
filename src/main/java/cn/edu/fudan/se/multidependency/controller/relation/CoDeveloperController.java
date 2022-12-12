package cn.edu.fudan.se.multidependency.controller.relation;

import java.util.List;
import java.util.stream.Collectors;

import cn.edu.fudan.se.multidependency.model.MetricType;
import cn.edu.fudan.se.multidependency.service.query.CoDeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/codeveloper")
public class CoDeveloperController {

    @Autowired
    private CoDeveloperService coDeveloperService;

    @GetMapping("")
    @ResponseBody
    public List<String> getRelatedfiles(@RequestParam("fileId")String fileId, @RequestParam("type")String type){
        List<String> result;
        if(type.equals(MetricType.CREATOR)){
            result = coDeveloperService.findCoCreatorFileList(Long.parseLong(fileId)).stream()
                                            .map(file -> file.getId().toString()).collect(Collectors.toList());
        }else if(type.equals(MetricType.LAST_UPDATOR)){
            result = coDeveloperService.findCoLastUpdatorFileList(Long.parseLong(fileId)).stream()
                    .map(file -> file.getId().toString()).collect(Collectors.toList());
        }else{
            result = coDeveloperService.findCoMostUpdatorFileList(Long.parseLong(fileId)).stream()
                    .map(file -> file.getId().toString()).collect(Collectors.toList());
        }
        return result;
    }
}
