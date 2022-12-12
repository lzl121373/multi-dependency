package cn.edu.fudan.se.multidependency.controller;

import cn.edu.fudan.se.multidependency.service.query.FCAMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class FCAController {
    @Autowired
    private FCAMetricService fcaMetricService;

    @GetMapping("/fca/metric/package")
    @ResponseBody
    public void getFCAPackageDependsOnMetric() throws IOException {
        fcaMetricService.getFCAPackageDependsOnMetric();
    }

    @GetMapping("/fca/metric/file")
    @ResponseBody
    public void getFCAFileDependsOnMetric() throws IOException {
        fcaMetricService.getFCAFileDependsOnMetric();
    }
}
