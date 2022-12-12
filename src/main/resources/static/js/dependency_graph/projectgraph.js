const CLONE_LOW_COLOR = "#f48989";
const CLONE_MEDIUM_COLOR = "#e90c0c";
const CLONE_HIGH_COLOR = "#9a2002";
const DEPENDSON_LOW_COLOR = "#0799d4";
const DEPENDSON_MEDIUM_COLOR = "#0566b0";
const DEPENDSON_HIGH_COLOR = "#012c7b";
const COCHANGE_COLOR = "#f88705";

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

var cloneLinks_global = [];
var dependsonLinks_global = [];
var cochangeLinks_global = [];
var allLinks_global = [];

var filter_global = {};

var linksCurrent_global = [];
var linksCurrentAfterExtract_global = [];
var linksBefore_global = [];

var pairIdBefore_global = "";  //存放当前连线的母线ID
var pairIdCurrent_global = "";
var circleId_global = "";

var typeBefore_global;
var typeCurrent_global;
var linksCurrent_flag = true;
var linksVisiable_flag = false;
var linksOfCircleVisiable_flag = false;

var diameter_global;
var svg_global;
var g_global;
var projectList_global;

var projectgraph = function () {
    return {
        init : function() {
            loadPageData();
        }
    }
}

//加载数据
var loadPageData = function () {
    var projectlist = [];

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
            }

            var html = ""
            html += "<div id = \"ProjectSelect\"><select id = \"multipleProjectSelect\" class=\"selectpicker\" multiple>";
            for(var i = 0; i < projectlist.length; i++) {
                if (i === 0) {
                    html += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                } else {
                    html += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                }
            }
            html += "</select>";
            html += "<br><button id = \"multipleProjectsButton\" type=\"button\" style='margin-top: 15px' onclick= showMultipleButton()>加载项目</button></div>";

            html += "<div id = \"AttributionSelect\">" +
                "<form role=\"form\">" +
                "<p><label class = \"AttributionSelectTitle\" style = \"margin-right: 44px\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOn\" onclick=\"CancelChildrenChecked('dependsOn')\">Dependency：" +
                "</label>" +

                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsIntensity\" name = \"dependsOn_children\">" +
                "<input  class = \"AttributionSelectInput\" id=\"intensitybelow\" value=\"0.8\">" +

                "<select class = \"AttributionSelectSingleSelect\" id=\"intensityCompareSelectBelow\">" +
                "<option value=\"<=\" selected = \"selected\"><=</option>" +
                "<option value=\"<\"><</option></select>" +

                "<label class = \"AttributionSelectLabel\"> &nbsp;Intensity</label>" +

                "<select class = \"AttributionSelectSingleSelect\" id=\"intensityCompareSelectHigh\">" +
                "<option value=\"<=\"><=</option>" +
                "<option value=\"<\" selected = \"selected\"><</option></select>" +

                "<input  class = \"AttributionSelectInput\" id=\"intensityhigh\" value=\"1\">" +

                "<label class = \"AttributionSelectLabel\" style = \"margin-left: 80px\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOnTimes\" name = \"dependsOn_children\"> Times >= " +
                "<input  id=\"dependencyTimes\" class = \"AttributionSelectInput\" style='margin-right: 80px' value=\"3\">" +
                "</label>" +

                "<label class = \"AttributionSelectLabel\" style = \"margin-right:10px;\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOnType\" name = \"dependsOn_children\"> Dependency Type: " +
                "</label>" +

                "<select id = \"dependsTypeSelect\" class=\"selectpicker\" multiple>" +
                "<option value=\"IMPORT\">IMPORT</option>" +
                "<option value=\"INCLUDE\">INCLUDE</option>" +
                "<option value=\"EXTENDS\">EXTENDS</option>" +
                "<option value=\"IMPLEMENTS\">IMPLEMENTS</option>" +
                // "<option value=\"GLOBAL_VARIABLE\">GLOBAL_VARIABLE</option>" +
                "<option value=\"MEMBER_VARIABLE\">MEMBER_VARIABLE</option>" +
                "<option value=\"LOCAL_VARIABLE\">LOCAL_VARIABLE</option>" +
                "<option value=\"CALL\">CALL</option>" +
                "<option value=\"ANNOTATION\">ANNOTATION</option>" +
                "<option value=\"CAST\">CAST</option>" +
                "<option value=\"CREATE\">CREATE</option>" +
                "<option value=\"USE\">USE</option>" +
                "<option value=\"PARAMETER\">PARAMETER</option>" +
                "<option value=\"THROW\">THROW</option>" +
                "<option value=\"RETURN\">RETURN</option>" +
                "<option value=\"IMPLEMENTS_C\">IMPLEMENTS_C</option>" +
                "<option value=\"IMPLLINK\">IMPLLINK</option>" +
                "</select>" +
                "</p>";

            html += "<p><label class = \"AttributionSelectTitle\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"clone\" onclick=\"CancelChildrenChecked('clone')\">Clone：" +
                "</label>" +

                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cloneSimilarity\" name = \"clone_children\">" +
                "<input  class = \"AttributionSelectInput\" id=\"similaritybelow\" value=\"0.7\">" +

                "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectBelow\">" +
                "<option value=\"<=\" selected = \"selected\"><=</option>" +
                "<option value=\"<\"><</option></select>" +

                "<label class = \"AttributionSelectLabel\"> &nbsp;Clone Files / All Files</label>" +

                "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectHigh\">" +
                "<option value=\"<=\"><=</option>" +
                "<option value=\"<\" selected = \"selected\"><</option></select>" +

                "<input  class = \"AttributionSelectInput\" id=\"similarityhigh\" value=\"1\">" +

                "<label class = \"AttributionSelectLabel\" style = \"margin-left: 80px\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cloneTimes\" name = \"clone_children\">CloneTimes >=</label>" +
                "<input  class = \"AttributionSelectInput\" id=\"clonetimes\" value=\"3\">" +
                "<button id = \"hideBottomPackageButton\" type=\"button\" style=\"margin-left: 30px\" onclick=HideBottomPackageButton() >仅显示聚合</button></p>";

            html += "<p><label class = \"AttributionSelectTitle\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"coChange\" onclick=\"CancelChildrenChecked('coChange')\">Co-change：" +
                "</label>" +
                "<label class = \"AttributionSelectLabel\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cochangeTimes\" name = \"coChange_children\"> Times >= " +
                "<input class = \"AttributionSelectInput\" id=\"cochangetimes\" value=\"3\">" +
                "</label></p>";

            html += "<p>" +
                "<label for=\"slider_range\" style=\"font-size: 18px; margin-left: 22px; margin-right: 30px; font-weight: 500;\">Depth Range：</label>\n" +
                "<input type=\"text\" id=\"slider_range\" style=\"border:0; color:#f6931f; font-weight:bold; font-size: 18px; margin-left: 30px; margin-right: 30px\">\n" +
                "</p>" +
                "<div id = projectToGraph_slider style=\"margin-left: 30px; margin-right: 30px\">" +
                "</div>";

            html += "<p><div style=\"margin-top: 10px;\">" +
                // "<button type=\"button\" onclick= showLineButton()>加载连线</button>" +
                "<button type=\"button\" onclick= FilterLinks() style = \"margin-left: 30px\">筛选连线</button>" +
                "<button type=\"button\" onclick= clearLink() style = \"margin-left: 30px\">删除连线</button>" +
                "<button type=\"button\" onclick= HideLink() style = \"margin-left: 30px\">隐藏连线</button>" +
                "<button type=\"button\" onclick= RecoverLink() style = \"margin-left: 30px\">恢复连线</button>" +
                "<button type=\"button\" onclick= reDo() style = \"margin-left: 30px\">撤销操作</button>" +
                "</div></p>" +
                "</form>" +
                "</div>";

            // console.log(html)
            $("#projectToGraph_util").html(html);
            $(".selectpicker").selectpicker({
                actionsBox:true,
                countSelectedText:"已选中{0}项",
                selectedTextFormat:"count > 2"
            })
            // $('.selectpicker').selectpicker();

            $( "#projectToGraph_slider" ).slider({
                range: "min",
                min: 1,
                max: 5,
                value: 5,
                slide: function( event, ui ) {
                    $( "#slider_range" ).val("1 - " + ui.value );
                    Change_Depth(ui.value);
                    // console.log("slider")
                }
            });
            $( "#slider_range" ).val("1 - " + $( "#projectToGraph_slider" ).slider( "value") );
        }
    })
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
        url : "/project/has",
        contentType: "application/json",
        dataType:"json",
        data:JSON.stringify(projectList),
        success : function(result) {
            projectToGraph(result,"projectToGraphSvg");
        }
    })
}

//绘制气泡图
var projectToGraph = function(result,divId){
    var projectdata = result[0].result;
    cloneLinks_global = result[1].links.clone_links;
    dependsonLinks_global = result[1].links.dependson_links;
    cochangeLinks_global = result[1].links.cochange_links;
    allLinks_global = allLinks_global.concat(cloneLinks_global);
    allLinks_global = allLinks_global.concat(dependsonLinks_global);
    allLinks_global = allLinks_global.concat(cochangeLinks_global);

    var svg = d3.select("#" + divId)
            .attr("width", 1500)
            .attr("height", 1500),
        margin = 20,
        diameter = +svg.attr("width"),
        g_remove = svg.selectAll("g").remove();
        g = svg.append("g").attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

    svg_global = svg;
    g_global = g;
    diameter_global = diameter;

    var color = d3.scaleLinear()
        .domain([0, 1])
        .range(["hsl(152,80%,80%)", "hsl(228,30%,40%)"])
        .interpolate(d3.interpolateHcl);

    var color_clone = d3.scaleLinear()
        .domain([0, 1])
        .range(["hsl(0,30%,90%)", "hsl(15,70%,70%)"])
        .interpolate(d3.interpolateHcl);

    var pack = d3.pack()
        .size([diameter - margin, diameter - margin])
        .padding(2);

    root = d3.hierarchy(projectdata)
        .sum(function(d) { return d.size; })
        .sort(function(a, b) { return b.value - a.value; });

    var nodes = pack(root).descendants(),
        view;

    var circle = g.selectAll("circle")
        .data(nodes)
        .enter().append("circle")
        .attr("class", function(d) {
            return d.parent ? d.children ? "circlepacking_node"
                : "circlepacking_node circlepacking_node--leaf" : "circlepacking_node circlepacking_node--root";
        })
        .style("fill", function(d) {
            if(d.children){
                return color(d.depth/(d.depth+10));
            }else{
                // var ratio = getCloneRatioByName(projectdata,d.data.id)[1];
                // var id = getCloneRatioByName(projectdata,d.data.id)[0];
                // if(ratio === 0){
                //     return null;
                // }else{
                //     return color_clone(ratio);
                // }
                return null;
            }
        })
        .attr("id", function (d) {
            return d.data.id;
        })
        .attr("onclick", function (d) {
            return "FocusOnCircleLinks(\"" + d.data.id + "\")";
        })
        .call(text => text.append("title").text(function(d) {
            return d.parent ? d.data.name + "\n所属包：" + d.parent.data.name  + "\nID：" + d.data.id : d.data.name + "\nDepth：" + d.data.depth;
        }));

    var text = g.selectAll("text")
        .data(nodes)
        .enter().append("text")
        .attr("class", "circlepacking_label")
        .style("fill-opacity", function(d) {
            return d.parent === root ? 1 : 0;
        })
        .style("display", function(d) {
            return d.parent === root ? "inline" : "none";
        })
        .text(function(d) {
            return d.data.name;
        });

    var node = g.selectAll("circle,text");

    zoomTo([root.x, root.y, root.r * 2 + margin]);

    function zoomTo(v) {
        var k = diameter / v[2]; view = v;
        node.attr("transform", function(d) {
            return "translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")";
        });
        circle.attr("r", function(d) {
            return d.r * k;
        });
    }

    function getCloneRatioByName(data,id){
        var result = [];
        if(data.id === id){
            result.push(data.id,data.clone_ratio);
            return result;
        }else{
            if(data.children){
                for(var i = 0; i < data.children.length; i++) {
                    if (data.children[i].id === id) {
                        result.push(data.children[i].id,data.children[i].clone_ratio);
                        return result;
                    } else {
                        var findResult = getCloneRatioByName(data.children[i], id);
                        if(findResult) {
                            return findResult;
                        }
                    }
                }
            }
        }
    }

    var defs = svg.append("defs");

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

    var legend = svg.selectAll(".legend")
        // .data(["A", "B", "C", "D", "E"])
        .data(LEGEND_DATA)
        .enter().append("g")
        .attr("class", "legend")
        .attr("transform", function(d, i) { return "translate(-30," + (i * 20 + 30) + ")"; });

    legend.append("rect")
        .attr("x", (diameter / 160) * 157)
        .attr("y", 7)
        .attr("width", 40)
        .attr("height", 4)
        .style("fill", function(d){
            return d.color;
        });

    legend.append("text")
        .attr("x", (diameter / 40) * 39)
        .attr("y", 9)
        .attr("dy", ".35em")
        .style("text-anchor", "end")
        .text(function(d) { return d.name;});

    $('#multipleProjectsButton').css('background-color', '#efefef');
}

//根据筛选规则绘制气泡图连线
var showLine = function(links_local, type){
    checkDuplicateLink();
    loadLink(linksCurrent_global);
}

//加载连线
function loadLink(jsonLinks) {
    var svg1 = d3.select(".packageLink") .remove();
    g_global.selectAll("circle")
        .style("stroke","")
        .style("stroke-width","");
    var circleCoordinate = [];

    // console.log(jsonLinks);

    var links = svg_global.append('g')
        .style('stroke', '#aaa')
        .attr("class", "packageLink")
        .selectAll('path')
        .data(jsonLinks)
        .enter().append('path')
        .attr("stroke-dasharray", function (d){
            // console.log(d);
            return d.bottom_package ? "20,2" : null;
        })
        .attr("stroke", function (d){
            return getTypeColor(d)[0];
        })
        .attr("fill", "none")
        .attr("type", function (d){
            return d.type;
        })
        .attr("id", function (d){
            return "id_" + d.pair_id;
        })
        .attr("onclick", function(d){
            if(!d.bottom_package){
                // return "drawChildrenLinks(\"" + d.pair_id + "\", \"" + d.type + "\")";
                return "drawChildrenLinks(\"id_" + d.pair_id + "\")";
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

    jsonLinks.forEach(function (d){
        var k;
        var k_flag;
        var inner_flag;
        d3.select("#id_" +  d.source_id)
            .style("stroke",function (e){
                return getTypeColor(d)[0];
            })
            .style("stroke-width","1.5px")

        d3.select("#id_" +  d.target_id)
            .style("stroke",function (e){
                return getTypeColor(d)[0];
            })
            .style("stroke-width","1.5px")

        //获取两个圆的transform属性（包含坐标信息）和半径
        var source_transform = d3.select("#id_" +  d.source_id).attr("transform");
        var target_transform = d3.select("#id_" +  d.target_id).attr("transform");
        var r1 = parseFloat(d3.select("#id_" +  d.source_id).attr("r"));
        var r2 = parseFloat(d3.select("#id_" +  d.target_id).attr("r"));

        //求初始情况下的两个圆心坐标
        var x1 = parseFloat(source_transform.slice(source_transform.indexOf("(") + 1, source_transform.indexOf(",")));
        var y1 = parseFloat(source_transform.slice(source_transform.indexOf(",") + 1, source_transform.indexOf(")")));
        var x2 = parseFloat(target_transform.slice(target_transform.indexOf("(") + 1, target_transform.indexOf(",")));
        var y2 = parseFloat(target_transform.slice(target_transform.indexOf(",") + 1, target_transform.indexOf(")")));

        //求斜率(考虑斜率正无穷问题)
        if(x1.toFixed(6) !== x2.toFixed(6)){
            k = (y2 - y1) / (x2 - x1);
            k_flag = true;
        }else{
            k_flag = false;
        }

        var r_max = Math.max(r1, r2);
        if(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)) < r_max){
            inner_flag = true;
        }else{
            inner_flag = false;
        }

        if(k_flag){
            //求偏移量
            var x1_offset = Math.sqrt((r1 * r1) / (k * k + 1));
            var y1_offset = Math.sqrt((r1 * r1) / (k * k + 1)) * k;
            var x2_offset = Math.sqrt((r2 * r2) / (k * k + 1));
            var y2_offset = Math.sqrt((r2 * r2) / (k * k + 1)) * k;


            if(x1 > x2 && inner_flag === false){
                x1 -= x1_offset;
                y1 -= y1_offset;
                x2 += x2_offset;
                y2 += y2_offset;
            }else if (x1 < x2 && inner_flag === false){
                x1 += x1_offset;
                y1 += y1_offset;
                x2 -= x2_offset;
                y2 -= y2_offset;
            }else if (x1 > x2 && inner_flag){
                x1 -= x1_offset;
                y1 -= y1_offset;
                x2 -= x2_offset;
                y2 -= y2_offset;
            }else if (x1 < x2 && inner_flag){
                x1 += x1_offset;
                y1 += y1_offset;
                x2 += x2_offset;
                y2 += y2_offset;
            }
        }else{
            if(y1 > y2 && inner_flag === false){
                y1 -= r1;
                y2 += r2;
            }else if(y1 < y2 && inner_flag === false){
                y1 += r1;
                y2 -= r2;
            }else if (y1 > y2 && inner_flag){
                y1 -= r1;
                y2 -= r2;
            }else if (y1 < y2 && inner_flag){
                y1 += r1;
                y2 += r2;
            }
        }

        var temp_coordinate = {};
        temp_coordinate["id"] = "id_" + d.source_id + "_id_" + d.target_id;
        temp_coordinate["x1"] = x1;
        temp_coordinate["y1"] = y1;
        temp_coordinate["x2"] = x2;
        temp_coordinate["y2"] = y2;
        circleCoordinate.push(temp_coordinate);
    })

    function getTranslateX1(source_id, target_id){
        var link_id = "id_" + source_id + "_" + "id_" +  target_id;
        // console.log(link_id);
        // console.log(circleCoordinate.find((n) => n.id === link_id))
        return circleCoordinate.find((n) => n.id === link_id).x1;
    }

    function getTranslateY1(source_id, target_id){
        var link_id = "id_" + source_id + "_" + "id_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).y1;
    }

    function getTranslateX2(source_id, target_id){
        var link_id = "id_" + source_id + "_" + "id_" + target_id;
        return circleCoordinate.find((n) => n.id === link_id).x2;
    }

    function getTranslateY2(source_id, target_id){
        var link_id = "id_" + source_id + "_" + "id_" + target_id;
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
        var x1 = getTranslateX1(d.source_id, d.target_id) + diameter_global / 2;
        var y1 = getTranslateY1(d.source_id, d.target_id) + diameter_global / 2;
        var x2 = getTranslateX2(d.source_id, d.target_id) + diameter_global / 2;
        var y2 = getTranslateY2(d.source_id, d.target_id) + diameter_global / 2;
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

    linksVisiable_flag = true;
    // flag = false;
}

//删除连线
function clearLink(){
    // drawTableBelow(clone_table_global, "project_clone");
    g_global.selectAll("circle")
        .style("stroke","")
        .style("stroke-width","");
    var clearlink = d3.select(".packageLink") .remove();
    var cleartable = d3.selectAll("table").remove();
    linksCurrent_global = [];
    linksVisiable_flag = false;
}

//隐藏连线
function hideLink(){
    // drawTableBelow(clone_table_global, "project_clone");
    g_global.selectAll("circle")
        .style("stroke","")
        .style("stroke-width","");
    var clearlink = d3.select(".packageLink") .remove();
    var cleartable = d3.selectAll("table").remove();
    linksVisiable_flag = false;
}

//检查重复连线，以便做弧线处理
function checkDuplicateLink(){
    linksCurrent_global.forEach(function(item, index){
        if(!item.hasOwnProperty("deplicate_num")){
            var num = 1;
            var index_list = [];
            linksCurrent_global.forEach(function(item2, index2){
                if(item2.pair_id === item.source_id + "_" + item.target_id){
                    item2["duplicate_num"] = num;
                    item2["line_direction"] = true;
                    num++;
                    index_list.push(index2);
                }else if(item2.pair_id === item.target_id + "_" + item.source_id){
                    item2["duplicate_num"] = num;
                    item2["line_direction"] = false;
                    num++;
                    index_list.push(index2);
                }
            });

            index_list.forEach(function(d){
                linksCurrent_global[d]["duplicate_all"] = num - 1;
            });
            // console.log(num);
        }
    });
}

//绘制下方表格
function drawTableBelow(link_id, linksdata, type){
    // console.log(linksBefore_global);
    var cleartable = d3.selectAll("table").remove();
    let html = "";

    if(type === "all" || type === "clone" || type === "dependson" || type === "cochange"){
        var links_local = linksCurrent_global.concat();
    }else if(type === "single"){
        var links_local = linksdata.concat();
    }else{
        var links_local = linksCurrentAfterExtract_global.concat();
    }

    if(type === "all" || type === "single" || type === "clone" || type === "extract"){
        let html_clone_table_body = "";
        let html_clone_table_parent = "";
        let html_clone_table_head = "<table class = \"gridtable\">"
            + "<tr><th>目录1</th><th>目录2</th>"
            + "<th>克隆文件占比</th><th>克隆Cochange占比</th><th>克隆Loc占比</th><th>克隆相似度</th><th>type</th><th>克隆对数</th></tr>";

        if(link_id !== "default"){
            cloneLinks_global.forEach(function (item){
                if(item.pair_id === link_id){
                    html_clone_table_parent += "<tr>"
                        + "<th>" + item.source_name + "</th>"
                        + "<th>" + item.target_name + "</th>"
                        + "<th>" + "(" + item.cloneNodesCount1 + "+" + item.cloneNodesCount2 + ")/(" + item.allNodesCount1 + "+"
                        + item.allNodesCount2 + ")=" + item.cloneMatchRate.toFixed(2) + "</th>"
                        + "<th>" + item.cloneNodesCoChangeTimes  + "/" + item.allNodesCoChangeTimes  + "=" + item.cloneCoChangeRate.toFixed(2) + "</th>"
                        + "<th>" + "(" + item.cloneNodesLoc1 + "+" + item.cloneNodesLoc2 + ")/(" + item.allNodesLoc1 + "+" + item.allNodesLoc2 + ")=" + item.cloneLocRate.toFixed(2) + "</th>"
                        + "<th>" + item.cloneSimilarityValue.toFixed(2) + "/(" + item.cloneType1Count + "+" + item.cloneType2Count + "+" + item.cloneType3Count + ")=" + item.cloneSimilarityRate.toFixed(2) + "</th>"
                        + "<th>" + item.cloneType + "</th>"
                        + "<th>" + item.clonePairs + "</th></tr>";
                }
            })
        }

        links_local.forEach(function(item){
            if(item.type === "clone"){
                var allNodesCoChangeTimes = item.allNodesCoChangeTimes;
                var cloneNodesCoChangeTimes = item.cloneNodesCoChangeTimes;
                var clonePairs = item.clonePairs;
                var cloneRate = "(" + item.cloneNodesCount1 + "+" + item.cloneNodesCount2 + ")/("
                    + item.allNodesCount1 + "+" + item.allNodesCount2 + ")=" +
                    ((item.cloneNodesCount1 + item.cloneNodesCount2 + 0.0)
                        / (item.allNodesCount1 + item.allNodesCount2)).toFixed(2);

                var cochangeRate = "";
                if(allNodesCoChangeTimes < 3){
                    cochangeRate = cloneNodesCoChangeTimes + "/" + allNodesCoChangeTimes + "=0.00";
                }else {
                    cochangeRate = cloneNodesCoChangeTimes  + "/" + allNodesCoChangeTimes  + "="
                        + ((cloneNodesCoChangeTimes  + 0.0) / allNodesCoChangeTimes ).toFixed(2);
                }

                html_clone_table_body += "<tr><td>|---" + item.source_name + "</td>"
                    + "<td>|---" + item.target_name + "</td>"
                    + "<td>" + "(" + item.cloneNodesCount1 + "+" + item.cloneNodesCount2 + ")/(" + item.allNodesCount1 + "+"
                    + item.allNodesCount2 + ")=" + item.cloneMatchRate.toFixed(2) + "</td>"
                    + "<td>" + item.cloneNodesCoChangeTimes  + "/" + item.allNodesCoChangeTimes  + "=" + item.cloneCoChangeRate.toFixed(2) + "</td>"
                    + "<td>" + "(" + item.cloneNodesLoc1 + "+" + item.cloneNodesLoc2 + ")/(" + item.allNodesLoc1 + "+" + item.allNodesLoc2 + ")=" + item.cloneLocRate.toFixed(2) + "</td>"
                    + "<td>" + item.cloneSimilarityValue.toFixed(2) + "/(" + item.cloneType1Count + "+" + item.cloneType2Count + "+" + item.cloneType3Count + ")=" + item.cloneSimilarityRate.toFixed(2) + "</td>"
                    + "<td>" + item.cloneType + "</td>"
                    + "<td>";
                if(clonePairs > 0) {
                    html_clone_table_body += "<a target='_blank' class='package' href='/cloneaggregation/details" +
                        "?id1=" + item.source_id +
                        "&id2=" + item.target_id +
                        "&path1=" + item.source_name +
                        "&path2=" + item.target_name +
                        "&clonePairs=" + clonePairs +
                        "&cloneNodesCount1=" + item.cloneNodesCount1 +
                        "&cloneNodesCount2=" + item.cloneNodesCount2 +
                        "&allNodesCount1=" + item.allNodesCount1 +
                        "&allNodesCount2=" + item.allNodesCount2 +
                        "&cloneMatchRate=" + item.cloneMatchRate +
                        "&cloneNodesLoc1=" + item.cloneNodesLoc1 +
                        "&cloneNodesLoc2=" + item.cloneNodesLoc2 +
                        "&allNodesLoc1=" + item.allNodesLoc1 +
                        "&allNodesLoc2=" + item.allNodesLoc2 +
                        "&cloneLocRate=" + item.cloneLocRate +
                        "&cloneNodesCoChangeTimes=" + item.cloneNodesCoChangeTimes +
                        "&allNodesCoChangeTimes=" + item.allNodesCoChangeTimes +
                        "&cloneCoChangeRate=" + item.cloneCoChangeRate +
                        "&cloneType1Count=" + item.cloneType1Count +
                        "&cloneType2Count=" + item.cloneType2Count +
                        "&cloneType3Count=" + item.cloneType3Count +
                        "&cloneType=" + item.cloneType +
                        "&cloneSimilarityValue=" + item.cloneSimilarityValue +
                        "&cloneSimilarityRate=" + item.cloneSimilarityRate +
                        "'>" + clonePairs + "</a>";
                }else {
                    html_clone_table_body += clonePairs;
                }
                html_clone_table_body += "</td></tr>";
            }
        })

        // if(nonclonefiles.hasOwnProperty("nonclonefiles1")){
        //     var nonclonefiles1 = nonclonefiles.nonclonefiles1;
        //     var nonclonefiles2 = nonclonefiles.nonclonefiles2;
        //
        //     if(nonclonefiles1.length > 0){
        //         nonclonefiles1.forEach(function (item){
        //             html_clone_table_body += "<tr><td>|---" + item.name + "</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>";
        //         })
        //     }
        //
        //     if(nonclonefiles2.length > 0){
        //         nonclonefiles2.forEach(function (item){
        //             html_clone_table_body += "<tr><td></td><td>|---" + item.name + "</td><td></td><td></td><td></td><td></td><td></td><td></td></tr>";
        //         })
        //     }
        // }

        html_clone_table_body += "</table>";
        html += html_clone_table_head + html_clone_table_parent + html_clone_table_body;
    }

    if(type === "all" || type === "single" || type === "dependson" || type === "extract"){
        let html_dependson_table = "";

        html_dependson_table += "<table class = \"gridtable\">"
            + "<tr><th>目录1</th><th>目录1依赖类型(次数)</th><th>目录2</th><th>目录2依赖类型(次数)</th>"
            + "<th>依赖强度</th><th>被依赖强度</th><th>详细信息</th></tr>";

        links_local.forEach(function(d){
            if(d.type === "dependson"){
                html_dependson_table += "<tr><td><a target='_blank' href='/relation/package/" + d.source_id + "'>" + d.source_name + "</a></td><td>"
                    + (d.dependsOnTypes === "" ? ""
                    : (d.dependsOnTypes + "(" + d.dependsOnTimes + "," + d.dependsOnWeightedTimes.toFixed(2) + ")")) + "</td><td>"
                    + "<a target='_blank' href='/relation/package/" + d.target_id + "'>" +  d.target_name + "</td><td>"
                    + (d.dependsByTypes === "" ? ""
                    : (d.dependsByTypes + "(" + d.dependsByTimes + "," + d.dependsByWeightedTimes.toFixed(2) + ")")) + "</td><td>"
                    + d.dependsOnIntensity.toFixed(2) + "</td><td>"
                    + d.dependsByIntensity.toFixed(2) + "</td><td>"
                    + "<a target='_blank' href='/dependon?pck1=" + d.source_id + "&pck2=" + d.target_id
                    + "'>detail</a></td></tr>";
            }
        })

        html_dependson_table += "</table>"
        html += html_dependson_table;
    }
    if(type === "all" || type === "single" || type === "cochange" || type === "extract"){
        let html_cochange_table = "";

        html_cochange_table += "<table class = \"gridtable\">"
            + "<tr><th>目录1</th><th>目录1更改次数</th><th>目录2</th><th>目录2更改次数</th>"
            + "<th>CoChange Times</th></tr>";

        links_local.forEach(function(d){
            if(d.type === "cochange"){
                html_cochange_table += "<tr><td>" + d.source_name + "</td><td>"
                    + d.node1ChangeTimes + "</td><td>"
                    + d.target_name + "</td><td>"
                    + d.node2ChangeTimes + "</td><td>"
                    + d.coChangeTimes + "</td></tr>";
            }
        })

        html_cochange_table += "</table>"
        html += html_cochange_table;
    }

    $("#projectCloneTable").html(html);
}

//点击连线，获取子包关系,绘制图下方表格
function drawChildrenLinks(pair_id, type){
    linksBefore_global = linksCurrent_global.concat();
    var temp_links_all = []
    var temp_links = []
    clearLink();
    // console.log(result);
    // switch (type){
    //     case "clone":
    //         temp_links_all = cloneLinks_global.concat();
    //         break;
    //     case "dependson":
    //         temp_links_all = dependsonLinks_global.concat();
    //         break;
    //     case "cochange":
    //         temp_links_all = cochangeLinks_global.concat();
    //         break;
    // }

    temp_links_all = allLinks_global.concat();
    var filter = GetFilterCondition();

    for(var i = temp_links_all.length; i > 0; i--){
        if(temp_links_all[i - 1].parent_pair_id === pair_id){
            if((temp_links_all[i - 1].type === "dependson" && filter["dependson"]["checked"] === 1) ||
                (temp_links_all[i - 1].type === "clone" && filter["clone"]["checked"] === 1) ||
                (temp_links_all[i - 1].type === "cochange" && filter["cochange"]["checked"] === 1)){
                temp_links.push(temp_links_all[i - 1])
            }
        }
    }

    // console.log(temp_links)

    linksCurrent_global = temp_links.concat();
    checkDuplicateLink();
    loadLink(linksCurrent_global);
    pairIdBefore_global = pairIdCurrent_global;
    typeBefore_global = typeCurrent_global;
    pairIdCurrent_global = pair_id;
    typeCurrent_global = type;
    drawTableBelow(pair_id, [], "all");
}

//多选下拉框，加载多项目
var showMultipleButton = function(){
    var value = $('#multipleProjectSelect').val();
    $('#multipleProjectsButton').css('background-color', '#e27575');
    projectList_global = [];
    projectList_global = value;
    // console.log(projectList_global);
    var table_clear = d3.selectAll("table").remove();
    projectGraphAjax(value);
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

//筛选框子控件随着母控件一同取消点选
var CancelChildrenChecked = function(parent_name){
    if(!$("#" + parent_name).is(":checked")){
        $("input[name = '" + parent_name + "_children" + "']").prop("checked", false);
    }
}

//取消显示底层包按钮
var HideBottomPackageButton = function(){
    if($("#clone").prop("checked")){
        if(linksCurrent_flag){
            var linksWithoutBottomPackages = linksCurrent_global.concat();
            for(var i = linksWithoutBottomPackages.length; i > 0; i--){
                if(linksWithoutBottomPackages[i - 1].hasOwnProperty("bottom_package")
                    && linksWithoutBottomPackages[i - 1].bottom_package){
                    linksWithoutBottomPackages.splice(i - 1, 1);
                }
            }

            loadLink(linksWithoutBottomPackages);

            linksCurrent_flag  = false;
            document.getElementById("hideBottomPackageButton").innerHTML = "取消 仅显示聚合";
        }else{
            loadLink(linksCurrent_global);
            linksCurrent_flag  = true;
            document.getElementById("hideBottomPackageButton").innerHTML = "仅显示聚合";
        }
    }
}

//隐藏连线
var HideLink = function(){
    g_global.selectAll("circle")
        .style("stroke","")
        .style("stroke-width","");
    var clearlink = d3.select(".packageLink") .remove();
    linksVisiable_flag  = false;
}

//恢复连线
var RecoverLink = function(){
    if(linksCurrent_global.length > 0){
        loadLink(linksCurrent_global);
        linksVisiable_flag  = true;
    }else{
        alert("当前无连线数据！");
    }
}

//筛选连线
var extractLink = function(){
    if(linksCurrent_global.length === 0){
        alert("当前无连线！")
    }else{
        hideLink();
        console.log()
        linksCurrentAfterExtract_global = linksCurrent_global.concat();
        for(var i = linksCurrentAfterExtract_global.length; i > 0; i--) {
            if (linksCurrentAfterExtract_global[i - 1].depth > $( "#projectToGraph_slider" ).slider( "value")) {
                linksCurrentAfterExtract_global.splice(i - 1, 1);
            }
        }

        if (linksCurrentAfterExtract_global.length > 0) {
            loadLink(linksCurrentAfterExtract_global);
            drawTableBelow("default", [], "extract")
        }
    }
}

//撤销操作
var reDo = function(){
    if(linksBefore_global.length > 0){
        linksCurrent_global = [];
        linksCurrent_global = linksBefore_global.concat();
        checkDuplicateLink();
        loadLink(linksCurrent_global);
        drawTableBelow(pairIdBefore_global, [], typeBefore_global);
        pairIdBefore_global = "";
        typeBefore_global = "";
        linksBefore_global = [];
    }else{
        alert("无法撤销！")
    }
}

//点击气泡，隐藏或显示与气泡无关连线
var FocusOnCircleLinks = function(circleId){
    if(!linksOfCircleVisiable_flag || circleId !== circleId_global){
        var temp_links = linksCurrent_global.concat();
        // console.log(linksCurrent_global);
        circleId_global = circleId;
        linksOfCircleVisiable_flag = true;

        for(var i = temp_links.length; i > 0; i--){
            if("id_" + temp_links[i - 1].source_id !== circleId
                && "id_" + temp_links[i - 1].target_id !== circleId ){
                temp_links.splice(i - 1, 1);
            }
        }

        if(temp_links.length > 0){
            checkDuplicateLink();
            loadLink(temp_links);
            drawTableBelow("", temp_links, "single");
        }else{
            alert("没有与此节点相关连线！");
            return;
        }

    }else{
        // linksCurrent_global = linksBefore_global.concat();
        // linksBefore_global = [];
        checkDuplicateLink();
        loadLink(linksCurrent_global);
        var temp_pair = pairIdCurrent_global;
        drawTableBelow(temp_pair, [], "all");
        linksOfCircleVisiable_flag = false;
    }
}

//滑块筛选深度
var Change_Depth = function(value){
    // console.log("Change_Depth")
    if(linksCurrent_global.length === 0){
        alert("当前无连线！")
    }else{
        hideLink();
        console.log(linksCurrent_global.length)
        var linksInDepthRange = linksCurrent_global.concat();
        for(var i = linksInDepthRange.length; i > 0; i--) {
            if (linksInDepthRange[i - 1].depth > value) {
                linksInDepthRange.splice(i - 1, 1);
            }
        }

        if (linksInDepthRange.length > 0) {
            loadLink(linksInDepthRange);
        }
    }
}

//获取当前筛选条件
var GetFilterCondition = function(){
    var filter = {}
    var temp_dependson = {}
    var temp_clone = {}
    var temp_cochange = {}

    temp_dependson["checked"] = $("#dependsOn").prop("checked") ? 1 : 0;
    temp_clone["checked"] = $("#clone").prop("checked") ? 1 : 0;
    temp_cochange["checked"] = $("#coChange").prop("checked") ? 1 : 0;

    temp_dependson["dependsIntensity"] = $("#dependsIntensity").prop("checked") ? 1 : 0;
    temp_dependson["intensityCompareSelectBelow"] = $("#intensityCompareSelectBelow").val();
    temp_dependson["intensityCompareSelectHigh"] = $("#intensityCompareSelectHigh").val();
    temp_dependson["intensitybelow"] = $("#intensitybelow").val();
    temp_dependson["intensityhigh"] = $("#intensityhigh").val();

    temp_dependson["dependsOnTimes"] = $("#dependsOnTimes").prop("checked") ? 1 : 0;
    temp_dependson["dependencyTimes"] = $("#dependencyTimes").val();

    temp_dependson["dependsOnType"] = $("#dependsOnType").prop("checked") ? 1 : 0;
    temp_dependson["dependsTypeSelect"] = $("#dependsTypeSelect").val();

    temp_clone["cloneSimilarity"] = $("#cloneSimilarity").prop("checked") ? 1 : 0;
    temp_clone["similarityCompareSelectBelow"] = $("#similarityCompareSelectBelow").val();
    temp_clone["similarityCompareSelectHigh"] = $("#similarityCompareSelectHigh").val();
    temp_clone["similarityhigh"] = $("#similarityhigh").val();
    temp_clone["similaritybelow"] = $("#similaritybelow").val();

    temp_clone["cloneTimes"] = $("#cloneTimes").prop("checked") ? 1 : 0;
    temp_clone["clonetimes"] = $("#clonetimes").val();

    temp_cochange["cochangeTimes"] = $("#cochangeTimes").prop("checked") ? 1 : 0;
    temp_cochange["cochangetimes"] = $("#cochangetimes").val();

    filter["dependson"] = temp_dependson;
    filter["clone"] = temp_clone;
    filter["cochange"] = temp_cochange;

    filter["depth"] = $( "#projectToGraph_slider" ).slider( "value");

    return filter;
}

//根据筛选条件筛选连线
var FilterLinks = function() {
    var filter = GetFilterCondition();
    // cmp(filter,filter_global);
    pairIdCurrent_global = "";
    pairIdBefore_global = "";

    var links_local = [];
    linksCurrent_flag = true;
    document.getElementById("hideBottomPackageButton").innerHTML = "仅显示聚合";
    clearLink();

    console.log(filter["dependson"]["checked"]);
    console.log(filter["clone"]["checked"]);
    console.log(filter["cochange"]["checked"]);

    if (filter["dependson"]["checked"]) {
        links_local = links_local.concat(dependsonLinks_global);
    }
    if (filter["clone"]["checked"]) {
        links_local = links_local.concat(cloneLinks_global);
    }
    if (filter["cochange"]["checked"]) {
        links_local = links_local.concat(cochangeLinks_global);
    }

    for (var i = links_local.length; i > 0; i--) {
        var source_project = links_local[i - 1].source_projectBelong;
        var target_project = links_local[i - 1].target_projectBelong;
        var relation_type = links_local[i - 1].type;

        var flag_delete = false;
        var temp_flag_source = false;
        var temp_flag_target = false;

        for (var j = 0; j < projectList_global.length; j++) {
            if (source_project === projectList_global[j]) {
                temp_flag_source = true;
                break;
            }
        }

        for (var k = 0; k < projectList_global.length; k++) {
            if (target_project === projectList_global[k]) {
                temp_flag_target = true;
                break;
            }
        }

        if (temp_flag_source === false || temp_flag_target === false || links_local[i - 1].parent_pair_id !== "default") {
            links_local.splice(i - 1, 1);
            flag_delete = true;
        }

        if (relation_type === "clone" && flag_delete === false) {
            var cloneMatchRate = links_local[i - 1].cloneMatchRate.toFixed(2);
            var similarityhigh = parseInt(filter["clone"]["similarityhigh"]).toFixed(2)
            var similaritybelow = parseInt(filter["clone"]["similaritybelow"]).toFixed(2)
            if (filter["clone"]["cloneSimilarity"]) {
                var temp_flag_clonesimilarity = false;

                if (filter["clone"]["similarityCompareSelectBelow"] === "<=" &&
                    cloneMatchRate >= similaritybelow) {
                    if (filter["clone"]["similarityCompareSelectHigh"] === "<=" &&
                        cloneMatchRate <= similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    } else if (filter["clone"]["similarityCompareSelectHigh"] === "<" &&
                        cloneMatchRate < similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    }
                } else if (filter["clone"]["similarityCompareSelectBelow"] === "<" &&
                    cloneMatchRate > similaritybelow) {
                    if (filter["clone"]["similarityCompareSelectHigh"] === "<=" &&
                        cloneMatchRate <= similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    } else if (filter["clone"]["similarityCompareSelectHigh"] === "<" &&
                        cloneMatchRate < similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    }
                }

                if (temp_flag_clonesimilarity === false) {
                    links_local.splice(i - 1, 1);
                    flag_delete = true;
                }
            }

            if (filter["clone"]["cloneTimes"] && links_local[i - 1].cloneNodesCoChangeTimes < filter["clone"]["clonetimes"] && flag_delete === false) {
                links_local.splice(i - 1, 1);
                flag_delete = true;
            }
        }

        if (relation_type === "dependson" && flag_delete === false) {
            var intensity = Math.max(links_local[i - 1].dependsOnIntensity, links_local[i - 1].dependsByIntensity);
            if (filter["dependson"]["dependsIntensity"]) {
                var temp_flag_intensity = false;

                if (filter["dependson"]["intensityCompareSelectBelow"] === "<=" && intensity >= filter["dependson"]["intensitybelow"]) {
                    if (filter["dependson"]["intensityCompareSelectHigh"] === "<=" &&
                        intensity <= filter["dependson"]["intensityhigh"]) {
                        temp_flag_intensity = true;
                    } else if (filter["dependson"]["intensityCompareSelectHigh"] === "<" &&
                        intensity < filter["dependson"]["intensityhigh"]) {
                        temp_flag_intensity = true;
                    }
                } else if (filter["dependson"]["intensityCompareSelectBelow"] === "<" && intensity > filter["dependson"]["intensitybelow"]) {
                    if (filter["dependson"]["intensityCompareSelectHigh"] === "<=" &&
                        intensity <= filter["dependson"]["intensityhigh"]) {
                        temp_flag_intensity = true;
                    } else if (filter["dependson"]["intensityCompareSelectHigh"] === "<" &&
                        intensity < filter["dependson"]["intensityhigh"]) {
                        temp_flag_intensity = true;
                    }
                }

                if (temp_flag_intensity === false) {
                    links_local.splice(i - 1, 1);
                    flag_delete = true;
                }
            }

            if (filter["dependson"]["dependsOnTimes"] &&
                filter["dependson"]["dependencyTimes"] > links_local[i - 1].dependsOnTimes &&
                filter["dependson"]["dependencyTimes"] > links_local[i - 1].dependsByTimes && flag_delete === false) {
                links_local.splice(i - 1, 1);
                flag_delete = true;
            }

            if (filter["dependson"]["dependsOnType"] && flag_delete === false) {
                var value = filter["dependson"]["dependsTypeSelect"];
                if (value.length === 0) {
                    alert("未选中任何类型！");
                }

                var dependsByTypesMap = links_local[i - 1].dependsByTypesMap;
                var dependsOnTypesMap = links_local[i - 1].dependsOnTypesMap;
                var temp_dependsType_flag = false;

                for (var k = 0; k < value.length; k++) {
                    if (temp_dependsType_flag === true) {
                        break;
                    }

                    if (dependsByTypesMap.length > 0) {
                        for (var item1 in dependsByTypesMap) {
                            if (dependsByTypesMap[item1].dependsByType === value[k]) {
                                temp_dependsType_flag = true;
                                break;
                            }
                        }
                    }

                    if (dependsOnTypesMap.length > 0) {
                        for (var item2 in dependsOnTypesMap) {
                            if (dependsOnTypesMap[item2].dependsOnType === value[k]) {
                                temp_dependsType_flag = true;
                                break;
                            }
                        }
                    }
                }

                if (temp_dependsType_flag === false) {
                    links_local.splice(i - 1, 1);
                    flag_delete = true;
                }
            }
        }

        if (relation_type === "cochange" && flag_delete === false && filter["cochange"]["cochangeTimes"]) {
            if (filter["cochange"]["cochangetimes"] >= 3) {
                if (links_local[i - 1].coChangeTimes < filter["cochange"]["cochangetimes"]) {
                    links_local.splice(i - 1, 1);
                    flag_delete = true;
                }
            } else {
                alert("Cochange Times 需大于等于 3！");
                return;
            }
        }
    }

    filter_global = filter;
    linksCurrent_global = links_local.concat();
    checkDuplicateLink();
    // console.log(linksCurrent_global)
    loadLink(linksCurrent_global);
    drawTableBelow("", [], "all");

    if(filter["depth"] !== 5){
        hideLink();
        linksCurrentAfterExtract_global = linksCurrent_global.concat();

        for (var i = linksCurrentAfterExtract_global.length; i > 0; i--) {
            if (linksCurrentAfterExtract_global[i - 1].depth > filter["depth"]) {
                linksCurrentAfterExtract_global.splice(i - 1, 1);
            }
        }

        if (linksCurrentAfterExtract_global.length > 0) {
            loadLink(linksCurrentAfterExtract_global);
            drawTableBelow("default", [], "extract")
        }
    }
}

//判断两个对象是否相等
var cmp = function (x, y) {
    for (var p in x) {
        console.log(p)
        if (x.hasOwnProperty(p)) {
            if (!y.hasOwnProperty(p)) {
                return false;
            }

            if (x[p] === y[p]) {
                continue;
            }

            if (x[p] !== y[p]) {
                return false;
            }
        }
    }

    for (var k in y) {
        console.log(k)
        // allows x[ p ] to be set to undefined
        if (y.hasOwnProperty(k) && !x.hasOwnProperty(k)) {
            return false;
        }
    }
    return true;
}


