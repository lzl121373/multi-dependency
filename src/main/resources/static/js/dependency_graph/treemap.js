var FILE_NO_SMELL_COLOR = "#fcfdbf";
var FILE_SMELL_COLOR = "#ee9576";
var FILE_CHOSEN_COLOR = "#af5247";

const CLONE_LOW_COLOR = "#f48989";
const CLONE_MEDIUM_COLOR = "#e90c0c";
const CLONE_HIGH_COLOR = "#9a2002";
const DEPENDSON_LOW_COLOR = "#0799d4";
const DEPENDSON_MEDIUM_COLOR = "#0566b0";
const DEPENDSON_HIGH_COLOR = "#012c7b";
const COCHANGE_COLOR = "#1bbb51";

const LEGEND_DATA = [
    {
        "name" : "文件匹配度 = 1",
        "id" : "clone_high",
        "color":CLONE_HIGH_COLOR
    },
    {
        "name" : "0.9 <= 文件匹配度 < 1",
        "id" : "clone_medium",
        "color":CLONE_MEDIUM_COLOR
    },
    {
        "name" : "文件匹配度 < 0.9",
        "id" : "clone_low",
        "color" : CLONE_LOW_COLOR
    },
    {
        "name" : "0.8 <= 依赖强度 < 1",
        "id" : "dependson_high",
        "color":DEPENDSON_HIGH_COLOR
    },
    {
        "name" : "0.5 <= 依赖强度 < 0.8",
        "id" : "dependson_medium",
        "color":DEPENDSON_MEDIUM_COLOR
    },
    {
        "name" : "0 < 依赖强度 < 0.5",
        "id" : "dependson_low",
        "color":DEPENDSON_LOW_COLOR
    },
    {
        "name" : "Co-Change",
        "id" : "cochange",
        "color":COCHANGE_COLOR
    }
];

var svg_global;
var projectList_global; //存放选定项目列表
var links_global; //存放所有连线数据
var linksCurrent_global; //存放当前显示连线数组
var fileIdCurrent_click; //存放当前点击文件ID
var fileSmellCurrent_click;  //存放当前点击文件smellID
var fileFlag_click = 0; //存放是否有被点击文件的flag
var smell_global; //存放所有smell数据
var smell_type_global; //存放smell筛选条件
var smell_level_global; //存放smell筛选条件
var vis_global; //存放d3画布
var link_condition_global = {}; //存放连线筛选条件

var TreeMap = function (data_list) {
    var result = data_list[0].result;
    links_global = data_list[1].links;
    smell_global = data_list[2].smell;
    var HEADER, OFFSET, h, height, treemap, w, width, zoom, zoomable_layer;

    OFFSET = 1;

    HEADER = 8;

    svg_global = d3.select("#treemap");

    width = svg_global
        .node()
        .getBoundingClientRect().width;

    height = svg_global
        .node()
        .getBoundingClientRect().height;

    w = width - 40;

    h = height - 40;

    treemap = d3.layout.treemap()
        .size([w, h]).value(function(node) {
            return node.size;
        })
        .padding([OFFSET + HEADER, OFFSET, OFFSET, OFFSET])
        .sort(function(a, b) {
            h = d3.ascending(a.height, b.height);
            if (h === 0) {
                return d3.ascending(a.size, b.size);
            }
            return h;
        });

    svg_global.attr({
        viewBox: (-width / 2) + " " + (-height / 2) + " " + width + " " + height
    });

    zoomable_layer = svg_global.append('g');

    zoom = d3.behavior
        .zoom()
        .scaleExtent([-Infinity, Infinity])
        .on('zoom', function() {
        return zoomable_layer.attr({
            transform: "translate(" + (zoom.translate()) + ")scale(" + (zoom.scale()) + ")"
        });
    });

    svg_global.call(zoom);

    vis_global = zoomable_layer.append('g').attr({
        transform: "translate(" + (-w / 2) + "," + (-h / 2) + ")"
    });

// .range([d3.hcl(320, 0, 20), d3.hcl(200, 70, 80)])

    var aggregate, cells, compute_height, compute_heights, data, labels;

    aggregate = function(node) {
        if (node.children != null) {
            node.children.forEach(aggregate);
            return node.size = d3.sum(node.children, function(d) {
                return d.size;
            });
        }
    };

    aggregate(result);

    compute_height = function(node) {
        if (node.children != null) {
            node.children.forEach(compute_height);
            return node.height = 1 + d3.max(node.children, function(d) {
                return d.height;
            });
        } else {
            return node.height = 0;
        }
    };

    compute_height(result);

    data = treemap.nodes(result);

    compute_heights = function(node) {
        var bchildren, bmax, rchildren, rmax;
        if (node.children != null) {
            node.children.forEach(compute_heights);

            rmax = d3.max(node.children, function(c) {
                return c.x + c.dx;
            });

            rchildren = node.children.filter(function(d) {
                return (d.x + d.dx) >= rmax;
            });

            node.height_r = 1 + d3.max(rchildren, function(d) {
                return d.height_r;
            });

            bmax = d3.max(node.children, function(c) {
                return c.y + c.dy;
            });

            bchildren = node.children.filter(function(d) {
                return (d.y + d.dy) >= bmax;
            });

            return node.height_b = 1 + d3.max(bchildren, function(d) {
                return d.height_b;
            });
        } else {
            node.height_r = 0;
            return node.height_b = 0;
        }
    };

    compute_heights(result);

    data.sort(function(a, b) {
        return d3.ascending(a.depth, b.depth);
    });

    cells = vis_global.selectAll('.cell').data(data);

    cells.enter().append('rect')
        .attr({
            "class": 'cell',
            x: function(d) {
                return d.x;
            },
            y: function(d) {
                return d.y;
            },
            width: function(d) {
                var width = d.dx - 2 * OFFSET * d.height_r;
                return width >= 0 ? width : 0.1;
            },
            height: function(d) {
                var height = d.dy - 2 * OFFSET * d.height_b;
                return height < 0 ? 0.1 : height;
            },
            fill: function(d) {
                // return d.hasOwnProperty("children") ? color(d.depth) : d.clone ? "#ea7d5f" : "#fcfdbf";
                return d.hasOwnProperty("children") ? colorTreemap(d.depth) : FILE_NO_SMELL_COLOR;
            },
            bottom: function (d) {
                return d.hasOwnProperty("children") ? 0 : 1;
            },
            stroke: function(d) {
                // console.log(d.children.length);
                // console.log(d.children);
                // return "#fcfdbf";
                return colorTreemap(6.1);
            }
        })
        .attr("id", function (d) {
            return d.id;
        })
        .attr("onmouseover", "showSmellGroupOnMouseOver(-1, \"\", \"\")")
        .attr("onclick", function (d) {
            return "showSmellGroupOnClick(\"" + d.id + "\", -1, \"\", \"\", 0)";
        })
        .classed('leaf', function(d) {
            return (d.children == null) || d.children.length === 0;
        })
        .call(text => text.append("title").text(function(d) {
            return d.hasOwnProperty("children") ? "ID：" + d.id.split("_")[1] + "\nPath：" + d.name + "\nDepth：" + d.depth
                : "ID：" + d.id.split("_")[1] + "\nPath：" + d.long_name + "\nDepth：" + d.depth;
        }));

    labels = vis_global.selectAll('.label').data(data.filter(function(d) {
        return (d.children != null) && d.children.length > 0;
    }));

    $('#multipleProjectsButton').css('background-color', '#efefef');

    var defs = vis_global.append("defs");

    LEGEND_DATA.forEach(function (item){
        var path_start = (defs.append("marker")
            .attr("id", item.id + "_start")
            .attr("markerUnits", "strokeWidth")
            .attr("markerWidth", "8")
            .attr("markerHeight", "8")
            .attr("viewBox", "0 0 8 8")
            .attr("refX", "4")
            .attr("refY", "4")
            .attr("orient", "auto"))
            .append("path")
            .attr("d", "M6,2 L2,4 L6,6 L4,4 L6,2")
            .style("fill", item.color);

        var path_end = (defs.append("marker")
            .attr("id", item.id + "_end")
            .attr("markerUnits", "strokeWidth")
            .attr("markerWidth", "8")
            .attr("markerHeight", "8")
            .attr("viewBox", "0 0 8 8")
            .attr("refX", "4")
            .attr("refY", "4")
            .attr("orient", "auto"))
            .append("path")
            // .attr("d", "M2,2 L10,6 L2,10 L6,6 L2,2")
            .attr("d", "M2,2 L6,4 L2,6 L4,4 L2,2")
            .style("fill", item.color);
    })

    vis_global.append("line")
        .attr("x1", 0)
        .attr("y1", 0)
        .attr("x2", 55)
        .attr("y2", 47)
        .attr("stroke", "black")
        .attr("stroke-width", "2px");

    return labels.enter().append('text').text(function(d) {
        return ((d.dx - 2 * OFFSET * d.height_r) >= 30 && (d.dy - 2 * OFFSET * d.height_b) >= 8) ? d.name.split("/")[d.name.split("/").length - 2] : null;
    }).attr({
        "class": 'label',
        x: function(d) {
            return d.x;
        },
        y: function(d) {
            return d.y;
        },
        dx: 2,
        dy: '1em'
    });
}

var main = function () {
    return {
        init : function() {
            loadPageData();
        }
    }
}

//加载数据
var loadPageData = function () {
    var projectlist = [];
    var projectIds = [];

    $.ajax({
        type : "GET",
        url : "/project/all/name",
        success : function(result) {
            for(var i = 0; i < result.length; i++){
                var name_temp = {};
                // console.log(x);
                name_temp["id"] = result[i].id;
                name_temp["name"] = result[i].name;
                projectlist.push(name_temp);

                var html = ""
                html += "<div class = \"treemap_div\"><select id = \"multipleProjectSelect\" class=\"selectpicker\" multiple>";
                for(var i = 0; i < projectlist.length; i++) {
                    if (i === 0) {
                        html += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                    } else {
                        html += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                    }
                }
                html += "</select>";
                html += "<br><button id = \"multipleProjectsButton\" type=\"button\" class='common_button' style='margin-top: 15px' onclick= showMultipleButton()>加载项目</button></div>";

                html += "<div class = \"treemap_div\">"+
                    "<form role=\"form\">" +

                    "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Smell ：</label>" +

                    "<label class = \"treemap_label\" >" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_Clone\" value='Clone'> Clone " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_CyclicDependency\" value='CyclicDependency'> Cyclic Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_HublikeDependency\" value='HubLikeDependency'> Hublike Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnstableDependency\" value='UnstableDependency'> Unstable Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnutilizedAbstraction\" value='UnutilizedAbstraction'> Unutilized Abstraction " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_ImplicitCrossModuleDependency\" value='ImplicitCrossModuleDependency'> Implicit Cross Module Dependency " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_GodComponent\" value='GodComponent'> God Component " +
                    "</label>" +

                    "</p>";

                html += "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Level ：</label>" +

                    "<label class = \"treemap_label\" >" +
                    "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Module\" value='Module'> Module " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Package\" value='Package'> Package " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_File\" value='File'> File " +
                    "</label>" +

                    "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_\" value='Type'> Type " +
                    "</label>" +

                    // "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    // "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Function\" value='Function'> Function " +
                    // "</label>" +
                    // "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
                    // "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Snippet\" value='Snippet'> Snippet " +
                    // "</label>" +

                    "</p>";

                html += "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Dependency ：</label>" +

                    "<label class = \"treemap_label\" >" +
                    "<input style = \"margin-right:4px; \" type=\"checkbox\" id=\"checkbox_dependency_dependson\"> Depends On " +
                    "</label>" +

                    "<label class = \"treemap_label\" >" +
                    "<input style = \"margin-right:4px; margin-left: 40px;\" type=\"checkbox\" id=\"checkbox_dependency_clone\"> Clone " +
                    "</label>" +

                    "<label class = \"treemap_label\" >" +
                    "<input style = \"margin-right:4px; margin-left: 40px;\" type=\"checkbox\" id=\"checkbox_dependency_cochange\"> Co-change " +
                    "</label>" +

                    "</p>";

                html += "<p><div style=\"margin-top: 10px;\">" +
                    "<button class = \"common_button\" type=\"button\" onclick= loadSmell() >加载异味</button>" +
                    "</div></p>";

                html += "</form>" +
                    "</div>";

                $("#treemap_util").html(html);
                $(".selectpicker").selectpicker({
                    actionsBox:true,
                    countSelectedText:"已选中{0}项",
                    selectedTextFormat:"count > 2"
                })
            }
        }
    })
}

//多选下拉框，加载多项目
var showMultipleButton = function(){
    var value = $('#multipleProjectSelect').val();
    $('#multipleProjectsButton').css('background-color', '#f84634');
    projectList_global = [];
    projectList_global = value;
    projectGraphAjax(value);
}

//调用接口请求数据
var projectGraphAjax = function(projectIds){
    d3.selectAll("svg > *").remove();
    var projectList = {};
    var projectIds_array = [];

    for(var i = 0; i < projectIds.length; i++){
        var tempId = {};
        tempId["id"] = projectIds[i];
        projectIds_array.push(tempId);
    }

    projectList["projectIds"] = projectIds_array;

    $.ajax({
        type:"POST",
        url : "/project/has/treemap",
        contentType: "application/json",
        dataType:"json",
        data:JSON.stringify(projectList),
        success : function(result) {
            TreeMap(result);
        }
    });
}

//鼠标悬停时，显示属于同一组的文件
var showSmellGroupOnMouseOver = function (smellId, type, level){
    if((smell_level_global === level && smell_type_global === type) || smellId < 0){
        if(fileFlag_click){
            $("rect[smell=1]").not("[smellId=" + fileSmellCurrent_click + "]").attr("fill", FILE_SMELL_COLOR);
        }else{
            $("rect[smell=1]").attr("fill", FILE_SMELL_COLOR);
        }
    }
    if(smellId > 0){
        $("rect[smellId=" + smellId + "]").attr("fill", FILE_CHOSEN_COLOR);
    }
}

//鼠标点击方块时，显示属于同一组的文件，并加载连线
var showSmellGroupOnClick = function (fileId, smellId, type, level, smell_flag){
    if(fileFlag_click){
        if(fileIdCurrent_click === fileId){
            if(smellId > 0){
                $("rect[smellId=" + smellId + "]").attr("fill", FILE_SMELL_COLOR);
            }else if(smellId < 0){
                $("rect[id=\"" + fileId + "\"]").attr("fill", FILE_NO_SMELL_COLOR);
            }
            fileIdCurrent_click = "";
            fileSmellCurrent_click = 0;
            fileFlag_click = 0;
        }else{
            if(fileSmellCurrent_click > 0){
                $("rect[smellId=" + fileSmellCurrent_click + "]").attr("fill", FILE_SMELL_COLOR);
            }else if(fileSmellCurrent_click < 0){
                $("rect[id=\"" + fileIdCurrent_click + "\"]").attr("fill", FILE_NO_SMELL_COLOR);
            }

            if(smellId > 0){
                $("rect[smellId=" + smellId + "]").attr("fill", FILE_CHOSEN_COLOR);
            }else if(smellId < 0){
                $("rect[id=\"" + fileId + "\"]").attr("fill", FILE_CHOSEN_COLOR);
            }

            fileSmellCurrent_click = smellId;
            fileIdCurrent_click = fileId;
        }
    }else{
        if(smellId > 0){
            $("rect[smellId=" + smellId + "]").attr("fill", FILE_CHOSEN_COLOR);
        }else if(smellId < 0){
            $("rect[id=\"" + fileId + "\"]").attr("fill", FILE_CHOSEN_COLOR);
        }

        fileFlag_click = 1;
        fileIdCurrent_click = fileId;
        fileSmellCurrent_click = smellId;
    }

    // loadLink();

    if(smell_flag === 1){
        showSideInformation(smellId);
    }else{
        $("#side_information").html("");
    }
}

//点击按钮，加载smell方块以及连线
var loadSmell = function (){
    smell_type_global = $("input[name='smell_radio']:checked").val();
    smell_level_global = $("input[name='level_radio']:checked").val();

    link_condition_global["dependson"] = $("#checkbox_dependency_dependson").prop("checked") ? 1 : 0;
    link_condition_global["clone"] = $("#checkbox_dependency_clone").prop("checked") ? 1 : 0;
    link_condition_global["cochange"] = $("#checkbox_dependency_cochange").prop("checked") ? 1 : 0;

    // $('rect[bottom=1]').css('fill', FILE_NO_SMELL_COLOR);
    refreshTreemap();

    smell_global.forEach(function (item){
        if(item.smell_type === smell_type_global && item.smell_level === smell_level_global){
            var nodes = item.nodes;
            nodes.forEach(function (node){
                var smell = d3.select("#" + node.id)
                    .attr("fill", FILE_SMELL_COLOR)
                    .attr("smell", 1)
                    .attr("smellId", item.id)
                    .attr("smell_type", item.smell_type)
                    .attr("smell_level", item.smell_level)
                    .attr("onmouseover", "showSmellGroupOnMouseOver(" + item.id + ", \"" + item.smell_type + "\", \"" + item.smell_level + "\")")
                    .attr("onclick", "showSmellGroupOnClick(\"" + node.id + "\", " + item.id + ", \"" + item.smell_type + "\", \"" + item.smell_level + "\", 1)")
                    .attr("smellGroup", item.name);
            });
        }
    });

    // loadLink();
}

//刷新treemap样式
var refreshTreemap = function (){
    var refresh = d3.selectAll("rect")
        .attr("fill", function (d){
            return d.hasOwnProperty("children") ? colorTreemap(d.depth) : FILE_NO_SMELL_COLOR;
        })
        .attr("smell", 0)
        .attr("smellId", "")
        .attr("smell_type", "")
        .attr("smell_level", "")
        .attr("onmouseover", "showSmellGroupOnMouseOver(-1, \"\", \"\")")
        .attr("onclick", function (d){
            return "showSmellGroupOnClick(\"" + d.id + "\", -1, \"\", \"\", 0)";
        })
        .attr("smellGroup", "");
}

//treemap颜色方法
var colorTreemap = function (depth) {
    var color = d3.scale
        .linear()
        .domain([0, 7])
        .range([d3.rgb("#b73779"), d3.rgb("#fcfdbf")])
        .interpolate(d3.interpolateHcl);

    return color(depth);
}

//加载连线
var loadLink = function (){
    var local_links = [];
    if (link_condition_global["dependson"] === 1){
        local_links = links_global["dependson_links"].concat();
    }
    if(link_condition_global["clone"] === 1){
        local_links = list_concat(local_links, links_global["clone_links"]);
    }
    if(link_condition_global["cochange"] === 1){
        local_links = list_concat(local_links, links_global["cochange_links"]);
    }

    local_links.forEach(function(item, index){
        if(!item.hasOwnProperty("deplicate_num")){
            var num = 1;
            var index_list = [];
            local_links.forEach(function(item2, index2){
                if(item2.pair_id === item.source_id.split("_")[1] + "_" + item.target_id.split("_")[1]){
                    item2["duplicate_num"] = num;
                    item2["line_direction"] = true;
                    num++;
                    index_list.push(index2);
                }else if(item2.pair_id === item.target_id.split("_")[1] + "_" + item.source_id.split("_")[1]){
                    item2["duplicate_num"] = num;
                    item2["line_direction"] = false;
                    num++;
                    index_list.push(index2);
                }
            });

            index_list.forEach(function(d){
                local_links[d]["duplicate_all"] = num - 1;
            });
            // console.log(num);
        }
    });

    var svg1 = d3.select(".packageLink") .remove();
    vis_global.selectAll("rect")
        .style("stroke",colorTreemap(6.1));
    var circleCoordinate = [];

    console.log(local_links);

    var links = vis_global.append('g')
        .style('stroke', '#aaa')
        .attr("class", "packageLink")
        .selectAll('path')
        .data(local_links)
        .enter().append('path')
        .attr("stroke", function (d){
            return getTypeColor(d)[0];
        })
        .attr("fill", "none")
        .attr("type", function (d){
            return d.type;
        })
        .attr("id", function (d){
            return d.pair_id;
        })
        .attr("onclick", function(d){
            if(!d.bottom_package){
                // return "drawChildrenLinks(\"" + d.pair_id + "\", \"" + d.type + "\")";
                return "drawChildrenLinks(\"" + d.pair_id + "\")";
            }
        })
        .attr("marker-end",function (d){
            if(d.type === "dependson"){
                if(d.dependsOnTimes === 0){
                    return null;
                }else{
                    return "url(#" + getTypeColor(d)[1] + "_end)";
                }
            }else{
                return "url(#" + getTypeColor(d)[1] + "_end)";
            }
        })
        .attr("marker-start",function (d){
            if(d.type === "dependson") {
                if (d.two_way || d.dependsOnTimes === 0) {
                    return "url(#" + getTypeColor(d)[1] + "_start)";
                } else {
                    return null;
                }
            }else{
                return null;
            }
        })
        .call(text => text.append("title").text(function(d) {
            if(d.type === "clone"){
                return "Package1: " + d.source_name + "\nPackage2: " + d.target_name
                    + "\ndepth: " + d.depth
                    + "\ncloneType: " + d.cloneType
                    + "\ncloneMatchRate: " + d.cloneMatchRate.toFixed(2)
                    + "\ncloneCoChangeRate: " + d.cloneCoChangeRate.toFixed(2)
                    + "\ncloneLocRate: " + d.cloneLocRate.toFixed(2)
                    + "\ncloneSimilarityRate: " + d.cloneSimilarityRate.toFixed(2)
                    + "\nallNodesCoChangeTimes: " + d.allNodesCoChangeTimes
                    + "\ncloneNodesCoChangeTimes: " + d.cloneNodesCoChangeTimes
                    + "\nclonePairs: " + d.clonePairs;
            }else if(d.type === "dependson"){
                var temp_title =  "Package1: " + d.source_name + "\nPackage2: " + d.target_name
                    + "\ndepth: " + d.depth
                    + "\ndependsOnTypes: " + d.dependsOnTypes
                    + "\ndependsByTypes: " + d.dependsByTypes
                    + "\ndependsOnTimes: " + d.dependsOnTimes
                    + "\ndependsByTimes: " + d.dependsByTimes
                    + "\ndependsOnIntensity: " + d.dependsOnIntensity
                    + "\ndependsByIntensity: " + d.dependsByIntensity;

                if(d.dependsByTypesMap.length > 0){
                    temp_title += "\ndependsByTypesMap: [";
                    for(var index in d.dependsByTypesMap){
                        temp_title += "\n\t{\n\t\tdependsByType: " + d.dependsByTypesMap[index].dependsByType;
                        temp_title += "\n\t\tdependsByTime: " + d.dependsByTypesMap[index].dependsByTime + "\n\t}";
                    }
                    temp_title += "\n]";
                }

                if(d.dependsOnTypesMap.length > 0){
                    temp_title += "\ndependsOnTypesMap: [";
                    for(var index in d.dependsOnTypesMap){
                        temp_title += "\n\t{\n\tdependsOnType: " + d.dependsOnTypesMap[index].dependsOnType;
                        temp_title += "\n\tdependsOnTime: " + d.dependsOnTypesMap[index].dependsOnTime + "\n\t}";
                    }
                    temp_title += "\n]";
                }

                return temp_title;
            }else if(d.type === "cochange"){
                return "Package1: " + d.source_name + "\nPackage2: " + d.target_name
                    + "\ndepth: " + d.depth
                    + "\ncoChangeTimes: " + d.coChangeTimes
                    + "\nnode1ChangeTimes: " + d.node1ChangeTimes
                    + "\nnode2ChangeTimes: " + d.node2ChangeTimes;
            }
        }));

    local_links.forEach(function (d){
        var k;
        var k_flag;
        var inner_flag;
        d3.select("#" + d.source_id)
            .style("stroke",function (e){
                return getTypeColor(d)[0];
            })
            .style("stroke-width","1.5px")

        d3.select("#" + d.target_id)
            .style("stroke",function (e){
                return getTypeColor(d)[0];
            })
            .style("stroke-width","1.5px")

        //获取两个rect的坐标 及长宽
        var x1 = parseFloat(d3.select("#" + d.source_id).attr("x"));
        var y1 = parseFloat(d3.select("#" + d.source_id).attr("y"));
        var x2 = parseFloat(d3.select("#" + d.target_id).attr("x"));
        var y2 = parseFloat(d3.select("#" + d.target_id).attr("y"));

        var width_1 = parseFloat(d3.select("#" + d.source_id).attr("width"));
        var height_1 = parseFloat(d3.select("#" + d.source_id).attr("height"));
        var width_2 = parseFloat(d3.select("#" + d.target_id).attr("width"));
        var height_2 = parseFloat(d3.select("#" + d.target_id).attr("height"));

        var temp_coordinate = {};
        temp_coordinate["id"] = d.source_id + "_" + d.target_id;
        temp_coordinate["x1"] = x1 + width_1 / 2;
        temp_coordinate["y1"] = y1 + height_1 / 2;
        temp_coordinate["x2"] = x2 + width_2 / 2;
        temp_coordinate["y2"] = y2 + height_2 / 2;
        circleCoordinate.push(temp_coordinate);
    })

    function getTranslateX1(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        // console.log(link_id);
        // console.log(circleCoordinate.find((n) => n.id === link_id))
        return circleCoordinate.find((n) => n.id === link_id).x1;
    }

    function getTranslateY1(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).y1;
    }

    function getTranslateX2(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).x2;
    }

    function getTranslateY2(source_id, target_id){
        var link_id = source_id + "_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).y2;
    }

    // links.attr("x1", function (d) {
    //     return getTranslateX1(d.source_id, d.target_id) + diameter_global / 2;
    // })
    //     .attr("y1", function (d) {
    //         return getTranslateY1(d.source_id, d.target_id) + diameter_global / 2;
    //     })
    //     .attr("x2", function (d) {
    //         return getTranslateX2(d.source_id, d.target_id) + diameter_global / 2;
    //     })
    //     .attr("y2", function (d) {
    //         return getTranslateY2(d.source_id, d.target_id) + diameter_global / 2;
    //     });

    links.attr("d", function (d) {
        var x1 = getTranslateX1(d.source_id, d.target_id);
        var y1 = getTranslateY1(d.source_id, d.target_id);
        var x2 = getTranslateX2(d.source_id, d.target_id);
        var y2 = getTranslateY2(d.source_id, d.target_id);
        // var length = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));

        // if(length > 650){
        //     // var ratio = length / 650;
        //     var ratio = 2;
        // }else{
        //     var ratio = 2;
        // }
        var ratio = 2;
        // console.log(d.duplicate_all)
        switch(d.duplicate_all){
            case 1:
                return "M" + x1 + " " + y1 + " L" + x2 + " " + y2;
            case 2:
                switch(d.duplicate_num){
                    case 1:
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio + 1) / (ratio * 2)) * x1 + ((ratio - 1) / (ratio * 2)) * x2)
                            + " " + (((ratio - 1) / (ratio * 2)) * y1 + ((ratio + 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                    case 2:
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio + 1) / (ratio * 2)) * x1 + ((ratio - 1) / (ratio * 2)) * x2)
                            + " " + (((ratio - 1) / (ratio * 2)) * y1 + ((ratio + 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                }
            case 3:
                switch(d.duplicate_num){
                    case 1:
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio + 1) / (ratio * 2)) * x1 + ((ratio - 1) / (ratio * 2)) * x2)
                            + " " + (((ratio - 1) / (ratio * 2)) * y1 + ((ratio + 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                    case 2:
                        return "M" + x1 + " " + y1 + " L" + x2 + " " + y2;
                    case 3:
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio + 1) / (ratio * 2)) * x1 + ((ratio - 1) / (ratio * 2)) * x2)
                            + " " + (((ratio - 1) / (ratio * 2)) * y1 + ((ratio + 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                }
            case 4:
                switch(d.duplicate_num){
                    case 1:
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio + 1) / (ratio * 2)) * x1 + ((ratio - 1) / (ratio * 2)) * x2)
                            + " " + (((ratio - 1) / (ratio * 2)) * y1 + ((ratio + 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                    case 2:
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio + 1) / (ratio * 2)) * x1 + ((ratio - 1) / (ratio * 2)) * x2)
                            + " " + (((ratio - 1) / (ratio * 2)) * y1 + ((ratio + 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                    case 3:
                        ratio = ratio * 2;
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                    case 4:
                        ratio = ratio * 2;
                        return d.line_direction ? ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2) : ("M" + x1 + " " + y1 +
                            " Q" + (((ratio - 1) / (ratio * 2)) * x1 + ((ratio + 1) / (ratio * 2)) * x2)
                            + " " + (((ratio + 1) / (ratio * 2)) * y1 + ((ratio - 1) / (ratio * 2)) * y2)
                            + " " + x2 + " " + y2);
                }
        }
    })
}

//工具方法，合并两个数组
var list_concat = function (arr1, arr2){
    var arr3 = [];

    for (var i = 0; i < arr1.length; i++) {
        arr3.push(arr1[i]);
    }

    for (var j = 0; j < arr2.length; j++) {
        arr3.push(arr2[j]);
    }

    return arr3;
}

//获取连线颜色
var getTypeColor = function(d){
    if(d.type === "clone") {
        return d.cloneMatchRate === 1 ? [CLONE_HIGH_COLOR, "clone_high"] : d.cloneMatchRate >= 0.9
            ? [CLONE_MEDIUM_COLOR, "clone_medium"] : [CLONE_LOW_COLOR, "clone_low"];
    }else if(d.type === "dependson"){
        return Math.max(d.dependsOnIntensity, d.dependsByIntensity) > 0.8 ? [DEPENDSON_HIGH_COLOR, "dependson_high"] : Math.max(d.dependsOnIntensity, d.dependsByIntensity) >= 0.5
            ? [DEPENDSON_MEDIUM_COLOR, "dependson_medium"] : [DEPENDSON_LOW_COLOR, "dependson_low"];
    }else if(d.type === "cochange"){
        return [COCHANGE_COLOR, "cochange"];
    }
}

//加载右侧信息栏
var showSideInformation = function (smellId){
    $.ajax({
        type : "GET",
        url : "/smell/get_metric?smellId=" + smellId,
        success : function(result) {
            var metricValues = result["metricValues"];
            var html = "";

            html += "<p><label class = \"treemap_information_title\" style = \"margin-right: 30px\">" + result.name + "</label></p>";

            for(var key in metricValues){
                html += "<p><label class = \"treemap_title\" style = \"margin-right: 20px; width: 60%\">" + key + " : " +
                    "</label>" +

                    "<label class = \"treemap_information_label\" style = \"margin-left: 40px\">" + metricValues[key] +
                    "</label></p>";
            }

            $("#side_information").html(html);

            // for(var i = 0; i < result.length; i++){
            //     var name_temp = {};
            //     // console.log(x);
            //     name_temp["id"] = result[i].id;
            //     name_temp["name"] = result[i].name;
            //     projectlist.push(name_temp);
            //
            //     var html = ""
            //     html += "<div class = \"treemap_div\"><select id = \"multipleProjectSelect\" class=\"selectpicker\" multiple>";
            //     for(var i = 0; i < projectlist.length; i++) {
            //         if (i === 0) {
            //             html += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
            //         } else {
            //             html += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
            //         }
            //     }
            //     html += "</select>";
            //     html += "<br><button id = \"multipleProjectsButton\" type=\"button\" class='common_button' style='margin-top: 15px' onclick= showMultipleButton()>加载项目</button></div>";
            //
            //     html += "<div class = \"treemap_div\">"+
            //         "<form role=\"form\">" +
            //
            //         "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Smell ：</label>" +
            //
            //         "<label class = \"treemap_label\" >" +
            //         "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_Clone\" value='Clone'> Clone " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_CyclicDependency\" value='CyclicDependency'> Cyclic Dependency " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_HublikeDependency\" value='HubLikeDependency'> Hublike Dependency " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnstableDependency\" value='UnstableDependency'> Unstable Dependency " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnutilizedAbstraction\" value='UnutilizedAbstraction'> Unutilized Abstraction " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_ImplicitCrossModuleDependency\" value='ImplicitCrossModuleDependency'> Implicit Cross Module Dependency " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_GodComponent\" value='GodComponent'> God Component " +
            //         "</label>" +
            //
            //         "</p>";
            //
            //     html += "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Level ：</label>" +
            //
            //         "<label class = \"treemap_label\" >" +
            //         "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Module\" value='Module'> Module " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Package\" value='Package'> Package " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_File\" value='File'> File " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_\" value='Type'> Type " +
            //         "</label>" +
            //
            //         // "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         // "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Function\" value='Function'> Function " +
            //         // "</label>" +
            //         // "<label class = \"treemap_label\" style = \"margin-left: 40px\">" +
            //         // "<input name=\"level_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_level_Snippet\" value='Snippet'> Snippet " +
            //         // "</label>" +
            //
            //         "</p>";
            //
            //     html += "<p><label class = \"treemap_title\" style = \"margin-right: 30px\">Dependency ：</label>" +
            //
            //         "<label class = \"treemap_label\" >" +
            //         "<input style = \"margin-right:4px; \" type=\"checkbox\" id=\"checkbox_dependency_dependson\"> Depends On " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" >" +
            //         "<input style = \"margin-right:4px; margin-left: 40px;\" type=\"checkbox\" id=\"checkbox_dependency_clone\"> Clone " +
            //         "</label>" +
            //
            //         "<label class = \"treemap_label\" >" +
            //         "<input style = \"margin-right:4px; margin-left: 40px;\" type=\"checkbox\" id=\"checkbox_dependency_cochange\"> Co-change " +
            //         "</label>" +
            //
            //         "</p>";
            //
            //     html += "<p><div style=\"margin-top: 10px;\">" +
            //         "<button class = \"common_button\" type=\"button\" onclick= loadSmell() >加载异味</button>" +
            //         "</div></p>";
            //
            //     html += "</form>" +
            //         "</div>";
            //
            //     $("#treemap_util").html(html);
            //     $(".selectpicker").selectpicker({
            //         actionsBox:true,
            //         countSelectedText:"已选中{0}项",
            //         selectedTextFormat:"count > 2"
            //     })
            // }
        }
    })

}