package cn.edu.fudan.se.multidependency.service.query.ar;

import com.alibaba.fastjson.JSONArray;


public interface ShowService {
    JSONArray staticDependGraph();

    JSONArray dynamicDependGraph();

    JSONArray cochangeDependGraph();
}
