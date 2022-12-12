package cn.edu.fudan.se.multidependency.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CompareController {

    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public String index(@RequestParam(name="leftFilePath", required=false, defaultValue="") String leftFilePath, 
    		@RequestParam(name="rightFilePath", required=false, defaultValue="") String rightFilePath,
    		HttpServletRequest request) {
    	request.setAttribute("leftFilePath", leftFilePath);
    	request.setAttribute("rightFilePath", rightFilePath);
        return "compare";
    }
}
