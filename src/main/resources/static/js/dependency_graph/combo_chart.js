let data = {};
let projectList_global; //存放选定项目列表
let projectList_filter; //存放正在筛选的项目列表
let projectId_global = ""; //存放当前展示的项目的ID
let package_filter_global = []; //存放当前筛选后的包路径
let in_out_list = []; //存放出度入度节点
let actual_edges = []; //存放原有的线以及拆分后的线ID
let present_edge_data = []; //存放当前的连线数据
let present_smell_type = ""; //存放当前异味类型
let present_smell_node_data = []; //存放当前的异味筛选后的节点数据
let smell_data_global = []; //存放异味信息
let smell_data_single_type = []; //存放当前某种类型的异味数据
let smell_data_single_group = []; //存放当前某个组的异味数据
let smell_data_single_group_id; //存放当前某个组的异味数据
let smell_data_flag = ""; //存放当前的异味类型
let smell_info_global = []; //存放异味统计信息
let smell_hover_nodes = []; //存放当前鼠标悬停异味节点数组
let present_smell_data = [];
let reliable_dependency_list = [];//存放可确定依赖关系
let unreliable_dependency_list = [];//存放不可信依赖关系
let evt_global; //存放当前的鼠标点击事件
let last_click_node = "";
let repaint_flag = false; //判断是否为重新绘制
let filter_smell_layer; //存放筛选异味弹窗
let smell_filter_condition = {};

const REGULAR_NODE_SIZE = 25;
const loading_div = $("#loading_div");
const COLOR_DEPENDSON = '#1b78d4';
const COLOR_CLONE = '#B04206';
const COLOR_COCHANGE = '#f9ac20';
// const COLOR_LINK_NORMAL = '#f7c8ca';
const COLOR_LINK_NORMAL = '#9aa6d2';
const COLOR_LINK_INNER = '#d8d5d5';
// const COLOR_LINK_CLICK = '#bd0303';
const COLOR_LINK_CLICK = '#1515ff';
const COLOR_SMELL_NORMAL = '#f19083';
const COLOR_SMELL_CLICK = '#bd0303';

const EDGE_CLICK_MODEL = {
    style: {
        stroke: COLOR_LINK_CLICK,
        lineWidth: 2,
        endArrow: {
            path: G6.Arrow.vee(5, 8, 3),
            d: 3,
            fill: COLOR_LINK_CLICK,
        },
    },
};
const EDGE_NORMAL_MODEL = {
    style: {
        stroke: COLOR_LINK_NORMAL,
        endArrow: {
            path: G6.Arrow.vee(5, 8, 3),
            d: 3,
            fill: COLOR_LINK_NORMAL,
        },
    },
};
const EDGE_INNER_MODEL = {
    style: {
        stroke: COLOR_LINK_INNER,
        endArrow: {
            path: G6.Arrow.vee(5, 8, 3),
            d: 3,
            fill: COLOR_LINK_INNER,
        },
    },
};

// 注册自定义名为 pie-node 的节点类型
G6.registerNode('pie-node', {
    draw: (cfg, group) => {
        let linkTypeNum = [];

        if(cfg.hasOwnProperty("pienode")){
            cfg.pienode.forEach(item => {
                if(item.source === last_click_node || item.target === last_click_node){
                    // console.log(item);
                    for(let key in item.dependency){
                        switch (key){
                            case "dependsonDegree":
                                if(typeof(linkTypeNum.find(n => n === COLOR_DEPENDSON)) === "undefined"){
                                    linkTypeNum.push(COLOR_DEPENDSON);
                                }
                                break;
                            case "cloneDegree":
                                if(typeof(linkTypeNum.find(n => n === COLOR_CLONE)) === "undefined"){
                                    linkTypeNum.push(COLOR_CLONE);
                                }
                                break;
                            case "cochangeDegree":
                                if(typeof(linkTypeNum.find(n => n === COLOR_COCHANGE)) === "undefined"){
                                    linkTypeNum.push(COLOR_COCHANGE);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            })
        }

        const radius = cfg.size / 2 - 1; // node radius

        group.addShape('circle', {
            attrs: {
                "r": radius + 0.5,
                "lineWidth": 4,
                "stroke": '#5f95ff',
                "fill": '#ffffff',
            }
        });

        if(typeof linkTypeNum)
        switch (linkTypeNum.length) {
            case 0:
                return group.addShape('circle', {
                    attrs: {
                        "r": radius + 2.5,
                        "lineWidth": 4,
                        "stroke": '#5f95ff',
                        "fill": '#ffffff',
                    }
                });
            case 1:
                return group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', radius, 0],
                            ['A', radius, radius, 0, 1, 0, radius, 0.001],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[Object.keys(linkTypeNum)[0]],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape',
                });
            case 2:
                let inPercentage = 0.5;
                const inAngle = inPercentage * Math.PI * 2; // the anble for the indegree fan
                const inArcEnd = [radius * Math.cos(inAngle), -radius * Math.sin(inAngle)]; // the end position for the in-degree fan
                // fan shape for the in degree
                const fanIn = group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', radius, 0],
                            ['A', radius, radius, 0, 0, 0, inArcEnd[0], inArcEnd[1]],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[Object.keys(linkTypeNum)[0]],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape',
                });
                // draw the fan shape
                group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', inArcEnd[0], inArcEnd[1]],
                            ['A', radius, radius, 0, 0, 0, radius, 0],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[Object.keys(linkTypeNum)[1]],
                        cursor: "pointer",
                    },
                    name: 'out-fan-shape',
                });
                // 返回 keyshape
                return fanIn;
            case 3:
                let inPercentage_1 = 1 / 3;
                let inPercentage_2 = 2 / 3;

                const inAngle_1 = inPercentage_1 * Math.PI * 2; // the anble for the indegree fan
                const inArcEnd_1 = [radius * Math.cos(inAngle_1), -radius * Math.sin(inAngle_1)]; // the end position for the in-degree fan
                let isInBigArc_1 = 0,
                    isOutBigArc_1 = 1;
                if (inAngle_1 > Math.PI) {
                    isInBigArc_1 = 1;
                    isOutBigArc_1 = 0;
                }

                const inAngle_2 = inPercentage_2 * Math.PI * 2; // the anble for the indegree fan
                const inArcEnd_2 = [radius * Math.cos(inAngle_2), -radius * Math.sin(inAngle_2)]; // the end position for the in-degree fan
                // fan shape for the in degree
                const fanIn_1 = group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', radius, 0],
                            ['A', radius, radius, 0, 0, 0, inArcEnd_1[0], inArcEnd_1[1]],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[Object.keys(linkTypeNum)[0]],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape-1',
                });

                group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', inArcEnd_1[0], inArcEnd_1[1]],
                            ['A', radius, radius, 0, 0, 0, inArcEnd_2[0], inArcEnd_2[1]],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[Object.keys(linkTypeNum)[1]],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape-1',
                });
                // draw the fan shape
                group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', inArcEnd_2[0], inArcEnd_2[1]],
                            ['A', radius, radius, 0, 0, 0, radius, 0],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[Object.keys(linkTypeNum)[2]],
                        cursor: "pointer",
                    },
                    name: 'out-fan-shape',
                });
                // 返回 keyshape
                return fanIn_1;
        }
    },
});

const grid = new G6.Grid();
const tooltip = new G6.Tooltip({
    offsetX: 70,
    offsetY: 150,
    getContent(e) {
        $(".g6-component-tooltip").css({"left": e.clientX + 20 + "px", "top": e.clientY + 20 + "px"});
        const outDiv = document.createElement('div');
        outDiv.style.width = '100%';
        let tooltip_html = ``;
        let group = e.item.getModel().group_type;
        let model = e.item.getModel();
        if(group === 'combo'){
            tooltip_html =
                `<b class="combo_label">${model.name}</b>
                  <ul>
                    <li>ID: ${model.id}</li>
                  </ul>`;
        }else if(group === 'node'){
            tooltip_html =
                `<b class="combo_label">${model.name}</b>
                  <ul>
                    <li>ID: ${model.id}</li>
                    <li>Path: ${model.long_name}</li>
                    <li>NOC: ${model.noc}</li>
                    <li>NOM: ${model.nom}</li>
                    <li>LOC: ${model.loc}</li>
                    <li>Score: ${model.score.toFixed(2)}</li>
                  </ul>`;
        }else{
            tooltip_html =
                `<b class="combo_label">${model.id}</b>
                <ul>
                  <li>source: ${model.source_name}</li>
                  <li>source_id: ${model.source}</li>
                  <li>target: ${model.target_name}</li>
                  <li>target_id: ${model.target}</li></ul>`;

            if(model.inner_edge === 1){
                tooltip_html += `<ul>dependsOnTypesMap:`;
                model.dependsOnTypesMap.forEach(item =>{
                    tooltip_html += `<ul>{</ul>
                                     <ul>    dependsOnType: ` + item.dependsOnType + `</ul>`;
                    tooltip_html += `<ul>    dependsOnTime: ` + item.dependsOnTime + `</ul><ul>}</ul>`;
                })
                tooltip_html += `</ul>`;
            }

        }

        outDiv.innerHTML = tooltip_html;
        return outDiv
    },
    itemTypes: ['node', 'combo', 'edge']
});  //鼠标悬停提示项
const container = document.getElementById('combo_chart');
const width = container.scrollWidth;
const height = container.scrollHeight || 500;

const contextMenu = new G6.Menu({
    getContent(evt) {
        let item = evt.item._cfg;
        let html = `<ul class="combo_ul">${item.model.id}_${item.model.name}</ul>`;
        if(item.model.smellType === "CyclicDependency" || item.model.smellType === "UnusedInclude"){
            html += `<ul class="combo_li">
            <a class="combo_a" href="/as/smellgraph/${item.model.smellId}" target="_blank">打开异味详情</a>
            </ul>`;
        }else if(item.model.smellType === "Clone"){
            html += `<ul class="combo_li">
            <a class="combo_a" href="/clonegroup/detail?clonegroupName=${item.model.smellName}" target="_blank">打开克隆组详情</a>
            </ul>`;
        }
        html += `<ul class="combo_li">
            <a class="combo_a" href="/relation/file/${item.model.id}" target="_blank">打开文件详情</a>
            </ul>`;
        return html;
    },
    offsetX: 0,
    offsetY: 0,
    itemTypes: ['node'],
});

const graph = new G6.Graph({
    container: 'combo_chart',
    width,
    height,
    fitCenter: true,
    fitView: true,
    defaultNode: {
        type: 'circle',
        size: 10,
        style: {
            cursor: "pointer",
            // fill: "#cce9f8",
            // stroke: "#a0d6f4",
            fill: "rgb(129,236,236)",
            stroke: "rgb(113,151,234)",

        },
    },
    nodeStateStyles: {
        smell_normal: {
            fill: "#ffffff",
            lineWidth: 2.5,
            shadowBlur: 10,
            shadowColor: COLOR_SMELL_NORMAL,
            stroke: COLOR_SMELL_NORMAL,
            "text-shape": {
                fontWeight: 500
            }
        },
        smell_hover: {
            fill: "#e6714f",
            lineWidth: 2.5,
            shadowBlur: 10,
            shadowColor: COLOR_SMELL_CLICK,
            stroke: COLOR_SMELL_CLICK,
            "text-shape": {
                fontWeight: 500
            }
        },
        unreliable: {
            fill: "#ffffff",
            lineWidth: 2.5,
            shadowBlur: 10,
            shadowColor: "#df0e23",
            stroke: "#df0e23",
        },
        selected: {
            fill: "#ffffff",
            lineWidth: 2.5,
            shadowBlur: 10,
            shadowColor: "#5F95FF",
            stroke: "#5F95FF",
        }
    },
    groupByTypes: false,
    modes: {
        default: [{
            type: 'drag-combo',
            enableDelegate: true,
        }, 'drag-canvas', 'collapse-expand-combo', 'zoom-canvas', 'click-select'],
    },
    defaultCombo: {
        type: 'rect',
        size: [50, 50],
        labelCfg: {
            position: 'top',
        },
        style: {
            lineWidth : 1,
            stroke : '#2d3436'
        }
    },
    defaultEdge: {
        // type: 'quadratic',
        type: 'cubic-vertical',
        size: 1,
        labelCfg: {
            style: {
                fontSize: 5,
            },
        },
        style: {
            stroke: COLOR_LINK_NORMAL,
            lineWidth: 1.15,
            endArrow: {
                path: G6.Arrow.vee(5, 8, 3),
                d: 3,
                fill: COLOR_LINK_NORMAL,
            },
            cursor: "pointer"
        },
    },
    plugins: [tooltip, contextMenu],
    minZoom: 0.05,
});

function main(projectId) {
    return {
        init : function() {
            if(projectId !== ""){
                projectId_global = projectId;
                $(".selectpicker").selectpicker('val', projectId_global);
                $('#multipleProjectsButton').css('background-color', '#f84634');
                showLoadingWindow("调用数据接口...");
                projectGraphAjax(projectId);
            }

            loadPageData();
        }
    }
}

//加载数据
function loadPageData() {
    var projectlist = [];

    $.ajax({
        type : "GET",
        url : "/project/all/name",
        success : function(result) {
            for(let i = 0; i < result.length; i++){
                let name_temp = {};
                name_temp["id"] = result[i].id;
                name_temp["name"] = result[i].name;
                projectlist.push(name_temp);

                let html_loadproject = "";
                html_loadproject += "<div>项目：<select id = \"multipleProjectSelect\" class=\"selectpicker\">";
                for(let i = 0; i < projectlist.length; i++) {
                    if(projectId_global === ""){
                        if (i === 0) {
                            html_loadproject += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                        } else {
                            html_loadproject += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                        }
                    }else{
                        if(projectlist[i].id.toString() === projectId_global){
                            html_loadproject += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                        }else{
                            html_loadproject += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                        }
                    }
                }
                html_loadproject += "</select>";
                html_loadproject += "<br><button id = \"multipleProjectsButton\" type=\"button\" class='combo_button layui-btn layui-btn-primary' " +
                    "style='margin-top: 15px;' onclick= showMultipleButton()>加载项目</button>" +

                    "<button id = \"pckFilterButton\" type=\"button\" class='combo_button layui-btn layui-btn-primary' " +
                    "style='margin-top: 15px' onclick= showFilterWindow()>筛选项目</button>" +

                    "<button id = \"clearFilterButton\" type=\"button\" class='combo_button layui-btn layui-btn-primary' " +
                    "style='margin-top: 15px;' onclick= clearFilter()>重置筛选</button></div>";

                let html_loadlink = generateFilterHtml("");

                let html_loadsmell = "";

                // html_loadsmell += "<p class='combo_p'><label class = \"combo_title\" style = \"font-size: 16px; margin-right: 3px\">Smell Type：</label>";
                //
                // html_loadsmell += "<select id = \"smellTypeSelect\" class=\"selectpicker\">";
                // html_loadsmell += "<option value=\"Clone\">Clone</option>";
                // html_loadsmell += "<option value=\"CyclicDependency\">Cyclic Dependency</option>";
                // html_loadsmell += "<option value=\"HubLikeDependency\">Hublike Dependency</option>";
                // html_loadsmell += "<option value=\"UnstableDependency\">Unstable Dependency</option>";
                // html_loadsmell += "<option value=\"UnutilizedAbstraction\">Unutilized Abstraction</option>";
                // html_loadsmell += "<option value=\"ImplicitCrossModuleDependency\">Implicit Cross Module Dependency</option>";
                // html_loadsmell += "<option value=\"GodComponent\">God Component</option>";
                // html_loadsmell += "<option value=\"UnusedInclude\">Unused Include</option>";
                // html_loadsmell += "</select></p>";

                // html_loadsmell += "<p class='combo_p'><button class = \"combo_button layui-btn layui-btn-primary\" type=\"button\" onclick= loadSmellByButton()>加载异味</button>" +
                //     "<button class = \"combo_button layui-btn layui-btn-primary\" style='margin-left: ' type=\"button\" onclick= deleteSmell('delete')>删除异味</button>" +
                //     "<button class = \"combo_button layui-btn layui-btn-primary\" style='margin-left: ' type=\"button\" onclick= filterSmellLayer()>筛选异味</button></p>";

                html_loadsmell += "<p class='combo_p'>" +
                    "<button class = \"combo_button layui-btn layui-btn-primary\" style='margin-left: ' type=\"button\" onclick= deleteSmell('delete')>删除异味</button>" +
                    "<button class = \"combo_button layui-btn layui-btn-primary\" style='margin-left: ' type=\"button\" onclick= filterSmellLayer()>筛选异味</button></p>";

                $("#load_dependencylink").html(html_loadlink);
                $("#load_project").html(html_loadproject);
                $("#analyse_smell").html(html_loadsmell);
                $(".selectpicker").selectpicker({
                    actionsBox:true,
                    countSelectedText:"已选中{0}项",
                    selectedTextFormat:"count > 2"
                })
            }
        }
    })
}

//调用接口请求数据
function projectGraphAjax(projectIds){
    let projectList = {};
    let projectIds_array = [];
    let tempId = {};
    tempId["id"] = projectIds;
    projectIds_array.push(tempId);

    // for(let i = 0; i < projectIds.length; i++){
    //     let tempId = {};
    //     tempId["id"] = projectIds[i];
    //     projectIds_array.push(tempId);
    // }

    projectList["projectIds"] = projectIds_array;

    $.ajax({
        type:"POST",
        url : "/project/has/combo",
        contentType: "application/json",
        dataType:"json",
        data:JSON.stringify(projectList),
        success : function(result) {
            DrawComboChart(result);
        }
    });
}

function showMultipleButton(){
    projectId_global = $('#multipleProjectSelect').val();
    $('#multipleProjectsButton').css('background-color', '#f84634');
    projectList_global = [];
    projectList_global = projectId_global;
    showLoadingWindow("调用数据接口...");
    projectGraphAjax(projectId_global);

}

function DrawComboChart(json_data){
    data = {};
    smell_data_global = [];
    last_click_node = "";
    in_out_list = [];
    actual_edges = [];
    let package_data = json_data[0]["result"]["nodes"];
    let link_data = json_data[1]["links"];
    filterSmellData(json_data[2]["smell_data"]);
    smell_info_global = json_data[2]["smell_info"];

    let temp_nodes = [];
    let temp_combos = [];

    package_data.forEach(function (item){
        let temp_combo = {};
        temp_combo["id"] = item.id;
        temp_combo["label"] = item.name;
        temp_combo["name"] = item.name;
        temp_combo["fanIn"] = 0;
        temp_combo["fanOut"] = 0;
        temp_combo["node_num"] = item.children.length;
        temp_combo["group_type"] = 'combo';
        temp_combos.push(temp_combo);

        item.children.forEach(function (d){
            let temp_node = {};
            temp_node["id"] = d.id;
            temp_node["name"] = d.name;
            temp_node["long_name"] = d.long_name;
            temp_node["noc"] = d.noc;
            temp_node["nom"] = d.nom;
            temp_node["loc"] = d.loc;
            temp_node["score"] = d.score;
            temp_node["size"] = d.size;
            temp_node["inOutNode"] = 0;
            temp_node["group_type"] = 'node';
            temp_node["comboId"] = item.id;
            temp_node["inDegree"] = 80;
            temp_node["degree"] = 160;
            temp_node["index"] = 2;
            temp_node["outerNode"] = 0;
            temp_node["pienode"] = [];
            temp_node["smellId"] = 0;
            temp_node["smellType"] = "";
            temp_nodes.push(temp_node);
        });
    });

    link_data.forEach(function (link){
        let temp_link = {};
        temp_link["id"] = link.source_id + "_" + link.target_id + "_" + link.type;
        temp_link["source"] = link.source_id;
        temp_link["target"] = link.target_id;
        temp_link["source_name"] = link.source_name;
        temp_link["target_name"] = link.target_name;
        temp_link["source_path"] = link.source_path;
        temp_link["target_path"] = link.target_path;
        temp_link["dependsOnTypesMap"] = link.dependsOnTypesMap;
        temp_link["link_type"] = link.type;
        temp_link["group_type"] = 'edge';
        temp_link["inner_edge"] = 0;
        temp_link["visible"] = link.type === "dependson";
        temp_link["label"] = link.type === "dependson" ? link.dependsOnTypes : "";

        switch (link.type){
            case "dependson":
                temp_link["dependsOnTypes"] = link.dependsOnTypes;
                temp_link["dependsOnTimes"] = link.dependsOnTimes;
                temp_link["dependsOnWeightedTimes"] = link.dependsOnWeightedTimes;
                break;
            case "clone":
                temp_link["value"] = link.value;
                temp_link["cloneRelationType"] = link.cloneRelationType;
                temp_link["cloneType"] = link.cloneType;
                break;
            case "cochange":
                temp_link["coChangeTimes"] = link.coChangeTimes;
                temp_link["node1ChangeTimes"] = link.node1ChangeTimes;
                temp_link["node2ChangeTimes"] = link.node2ChangeTimes;
                break;
            default:
                break;
        }

        const source_node = temp_nodes.find((n) => n.id === link.source_id);
        const target_node = temp_nodes.find((n) => n.id === link.target_id);
        if(source_node != null && target_node != null) {
            temp_link["source_comboId"] = source_node.comboId;
            temp_link["target_comboId"] = target_node.comboId;

            if(typeof(source_node) !== "undefined" && typeof(target_node) !== "undefined"){
                if((source_node["outerNode"] === 0 || target_node["outerNode"] === 0) && source_node["comboId"] !== target_node["comboId"]){
                    source_node["outerNode"] = 1;
                    target_node["outerNode"] = 1;
                }else if(source_node["comboId"] === target_node["comboId"]){
                    temp_link["inner_edge"] = 1;
                }
                actual_edges.push(temp_link);
            }
        }
    })

    data["nodes"] = temp_nodes;
    data["combos"] = temp_combos;
    present_edge_data = splitLinks(actual_edges);
    data["edges"] = present_edge_data;

    let sum = data["nodes"].length + data["combos"].length + data["edges"].length;

    if(sum >= 15000){
        if(confirm("当前已选中顶层目录 " + data["combos"].length + " 个，"
            + "文件 " + data["nodes"].length + " 个，"
            + "关系 " + data["edges"].length + " 条，"
            + "元素总计 " + sum + " 个。\n"
            + "建议总元素不超过15,000个，请减少选中目录数！")){
            $('#multipleProjectsButton').css('background-color', '#efefef');
            closeLoadingWindow();
            showFilterWindow();
        }else{
            $('#multipleProjectsButton').css('background-color', '#efefef');
            closeLoadingWindow();
            showFilterWindow();
        }
    }else{
        autoLayout();
        console.log(data);
        paintCombo();
        const edge_list = graph.getEdges();
        edge_list.forEach(function (item){
            if(item._cfg.model.inner_edge === 1){
                graph.updateItem(item, EDGE_INNER_MODEL);
            }
        });

        const nodes = graph.getNodes();
        nodes.forEach((node) => {
            node.toFront();
        });
        graph.paint();

        $('#multipleProjectsButton').css('background-color', '#efefef');

        if(repaint_flag === false){
            graph.on('node:mouseenter', (evt) => {
                const { item } = evt;
                const smellId = item._cfg.model.smellId;

                if(smellId !== 0){
                    smell_data_global.forEach(smell => {
                        if(smell.id === smellId){
                            smell.nodes.forEach(node_data => {
                                const node = graph.findById(node_data.id.split("_")[1]);
                                if(typeof(node) !== "undefined"){
                                    smell_hover_nodes.push(node);
                                    graph.setItemState(node, 'smell_hover', true);
                                }
                            })
                        }
                    })
                }
            });

            graph.on('node:mouseleave', (evt) => {
                const { item } = evt;

                const smellId = item._cfg.model.smellId;

                if(smellId !== 0) {
                    smell_hover_nodes.forEach(node => {
                        graph.setItemState(node, 'smell_hover', false);
                    })

                    smell_hover_nodes = [];
                }
            });

            graph.on('node:contextmenu', (evt) => {
                $(".g6-component-contextmenu").css({"left": evt.clientX + 20 + "px", "top": evt.clientY + 20 + "px"});
            });

            graph.on('canvas:click', (evt) => {
                graph.getCombos().forEach((combo) => {
                    graph.clearItemStates(combo);
                });
            });

            graph.on('dragend', (evt) => {
                const nodes = graph.getNodes();
                nodes.forEach((node) => {
                    node.toFront();
                });
            });


            //节点点击函数
            graph.on('node:click', (evt) => {
                const { item: node_click } = evt;
                if(last_click_node === ""){
                    console.log(node_click);
                    showRelevantNodeAndEdge(node_click);
                }else if(last_click_node === node_click._cfg.id){
                    last_click_node = "";
                    cancelRelevantNodeAndEdge(node_click);
                }else{
                    console.log(node_click);
                    const lastClickNode = graph.findById(last_click_node);

                    cancelRelevantNodeAndEdge(lastClickNode);
                    showRelevantNodeAndEdge(node_click);
                }

                // graph.setItemState(node_click, 'active', true);
            });
            graph.on('edge:click', (evt) => {
                const { item: edge_click } = evt;
                console.log(edge_click);

                // graph.setItemState(node_click, 'active', true);
            });

            graph.on('combo:click', (evt) => {
                const { item: item } = evt;
                console.log(item);
            });

            repaint_flag = true;
        }

        closeLoadingWindow();

        showHistogram();
    }
}

//显示与该节点相关的连线和节点
function showRelevantNodeAndEdge(node_click){
    last_click_node = node_click._cfg.id;
    graph.setItemState(node_click, 'selected', true);

    const node_edges = node_click.getEdges();
    console.log(node_edges);

    node_edges.forEach(function (edge){
        if(edge._cfg.model.inner_edge === 1 || node_click._cfg.model.inOutNode === 1){
            const node = getOtherEndNode(edge._cfg.model, node_click._cfg.id);
            updatePieNode(node);
            graph.setItemState(node, 'selected', true);
            graph.updateItem(edge, EDGE_CLICK_MODEL);
        }else{
            edge._cfg.model.children.forEach(link => {
                const node = getOtherEndNode(link, node_click._cfg.id);
                graph.setItemState(node, 'selected', true);
                updatePieNode(node);
                if(link.link_type === "dependson") {
                    link.split_edges.forEach(n => {
                        graph.updateItem(n.id, EDGE_CLICK_MODEL);
                    })
                }
            })
        }
    });
}

function highlightEdge(edge){
    const edge_model = edge._cfg.model;
    const source_node = graph.findById(edge_model.source);
    const target_node = graph.findById(edge_model.target);

    if(edge_model.inner_edge === 1 || source_node._cfg.model.inOutNode === 1){
        // graph.setItemState(source_node, 'selected', true);
        // graph.setItemState(target_node, 'selected', true);
        graph.updateItem(edge, EDGE_CLICK_MODEL);
    }else{
        // console.log(item2);
        edge_model.children.forEach(link => {
            // graph.setItemState(source_node, 'selected', true);
            // graph.setItemState(target_node, 'selected', true);
            link.split_edges.forEach(n => {
                graph.updateItem(n.id, EDGE_CLICK_MODEL);
            })
        })
    }
}

//取消与该节点相关的连线和节点的点击状态
function cancelRelevantNodeAndEdge(node_click){
    graph.setItemState(node_click, 'selected', false);
    const node_edges = node_click.getEdges();

    node_edges.forEach(function (edge){
        if(edge._cfg.model.inner_edge === 1 || node_click._cfg.model.inOutNode === 1){
            const node = getOtherEndNode(edge._cfg.model, node_click._cfg.id);
            graph.setItemState(node, 'selected', false);
            deletePieNode(node);
            if(node_click._cfg.model.inOutNode === 1){
                graph.updateItem(edge, EDGE_NORMAL_MODEL);
            }else{
                graph.updateItem(edge, EDGE_INNER_MODEL);
            }

        }else{
            edge._cfg.model.children.forEach(link => {
                const node = getOtherEndNode(link, node_click._cfg.id);
                graph.setItemState(node, 'selected', false);
                deletePieNode(node);
                link.split_edges.forEach(n => {
                    graph.updateItem(n.id, EDGE_NORMAL_MODEL);
                })
            })
        }
    });
}

function clearCombo(){
    const nodes = graph.getNodes();
    nodes.forEach((node) => {
        graph.setItemState(node, 'selected', false);
        deletePieNode(node);
    });

    if(last_click_node !== ""){
        const lastClickNode = graph.findById(last_click_node);
        cancelRelevantNodeAndEdge(lastClickNode);
    }
}

function getOtherEndNode(model, id){
    let node;
    if(model.source === id){
        node = graph.findById(model.target);
    }else{
        node = graph.findById(model.source);
    }

    return node;
}

function filterComboLinks(){
    if(last_click_node !== ""){
        const lastClickNode = graph.findById(last_click_node);
        cancelRelevantNodeAndEdge(lastClickNode);
    }
    const filter = getFilterCondition("");
    let temp_edges = filterLinks(filter, "");
    present_edge_data = splitLinks(temp_edges);
    data["edges"] = present_edge_data;
    paintCombo();
    const edge_list = graph.getEdges();
    edge_list.forEach(function (item){
        if(item._cfg.model.inner_edge === 1){
            graph.updateItem(item, EDGE_INNER_MODEL);
        }
    });

    const nodes = graph.getNodes();
    nodes.forEach((node) => {
        node.toFront();
    });
    graph.paint();
}

function filterLinks(filter, suffix){
    let unreliable_nodes = [];
    let temp_edges = [];

    actual_edges.forEach(edge =>{
        // reliable_dependency_list.forEach(item => {
        //     if((edge.source === item.node1 && edge.target === item.node2) ||
        //         (edge.source === item.node2 && edge.target === item.node1)){
        //         edge.visible = false;
        //     }
        // })

        if(suffix === ""){
            unreliable_dependency_list.forEach(item => {
                if((edge.source_path === item.node1 && edge.target_path === item.node2) ||
                    (edge.source_path === item.node2 && edge.target_path === item.node1)){
                    console.log(item);
                    const node1 = graph.findById(edge.source);
                    const node2 = graph.findById(edge.target);
                    unreliable_nodes.push(node1);
                    unreliable_nodes.push(node2);
                    // graph.setItemState(node1, 'unreliable', true);
                    // graph.setItemState(node2, 'unreliable', true);
                }
            })
        }

        if (filter["dependson"]["checked"] && edge.link_type ==="dependson") {
            let dependson_flag = true;
            if (filter["dependson"]["dependsIntensity"]) {
                let intensityhigh = parseFloat(filter["dependson"]["intensityhigh"]).toFixed(2);
                let intensitybelow = parseFloat(filter["dependson"]["intensitybelow"]).toFixed(2);
                let intensity = edge.dependsOnWeightedTimes/ (edge.dependsOnWeightedTimes + 10.0);
                let temp_flag_intensity = false;

                if (filter["dependson"]["intensityCompareSelectBelow"] === "<=" && intensity >= intensitybelow) {
                    if (filter["dependson"]["intensityCompareSelectHigh"] === "<=" &&
                        intensity <= intensityhigh) {
                        temp_flag_intensity = true;
                    } else if (filter["dependson"]["intensityCompareSelectHigh"] === "<" &&
                        intensity < intensityhigh) {
                        temp_flag_intensity = true;
                    }
                } else if (filter["dependson"]["intensityCompareSelectBelow"] === "<" && intensity > intensitybelow) {
                    if (filter["dependson"]["intensityCompareSelectHigh"] === "<=" &&
                        intensity <= intensityhigh) {
                        temp_flag_intensity = true;
                    } else if (filter["dependson"]["intensityCompareSelectHigh"] === "<" &&
                        intensity < intensityhigh) {
                        temp_flag_intensity = true;
                    }
                }

                if (temp_flag_intensity === false) {
                    dependson_flag = false;
                }
            }

            if (filter["dependson"]["dependsOnTimes"] &&
                filter["dependson"]["dependencyTimes"] > edge.dependsOnTimes &&
                dependson_flag === true) {
                dependson_flag = false;
            }

            if (filter["dependson"]["dependsOnType"] && dependson_flag === true) {
                let value = filter["dependson"]["dependsTypeSelect"];
                let type_list = edge.dependsOnTypes.split("__");
                let temp_dependsType_flag = false;

                if (value.length === 0) {
                    alert("未选中任何类型！");
                }else{
                    value.forEach(type1 => {
                        type_list.forEach(type2 =>{
                            if(type1 === type2){
                                temp_dependsType_flag = true;
                            }
                        })
                    })
                }

                if (temp_dependsType_flag === false) {
                    dependson_flag = false;
                }
            }

            if(dependson_flag === true){
                if(suffix === "" || (suffix === "_smell" && judgeSmellLink(edge))){
                    temp_edges.push(edge);
                }
            }
        }

        if (filter["clone"]["checked"] && edge.link_type ==="clone") {
            let clone_flag = true;
            if (filter["clone"]["cloneSimilarity"]) {
                let temp_flag_clonesimilarity = false;
                let cloneValue = edge.value;
                let similarityhigh = parseFloat(filter["clone"]["similarityhigh"]).toFixed(2);
                let similaritybelow = parseFloat(filter["clone"]["similaritybelow"]).toFixed(2);

                if (filter["clone"]["similarityCompareSelectBelow"] === "<=" &&
                    cloneValue >= similaritybelow) {
                    if (filter["clone"]["similarityCompareSelectHigh"] === "<=" &&
                        cloneValue <= similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    } else if (filter["clone"]["similarityCompareSelectHigh"] === "<" &&
                        cloneValue < similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    }
                } else if (filter["clone"]["similarityCompareSelectBelow"] === "<" &&
                    cloneValue > similaritybelow) {
                    if (filter["clone"]["similarityCompareSelectHigh"] === "<=" &&
                        cloneValue <= similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    } else if (filter["clone"]["similarityCompareSelectHigh"] === "<" &&
                        cloneValue < similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    }
                }

                if (temp_flag_clonesimilarity === false) {
                    clone_flag = false;
                }
            }

            if(clone_flag === true){
                if(suffix === "" || (suffix === "_smell" && judgeSmellLink(edge))) {
                    temp_edges.push(edge);
                }
            }
        }

        if (filter["cochange"]["checked"] && edge.link_type ==="cochange") {
            if (smell_filter_condition["cochange"]["cochangeTimes"] && edge.coChangeTimes >= filter["cochange"]["cochangetimes"]) {
                if(suffix === "" || (suffix === "_smell" && judgeSmellLink(edge))) {
                    temp_edges.push(edge);
                }
            }
            // if (filter["cochange"]["cochangetimes"] >= 3) {
            //
            // } else {
            //     alert("Cochange Times 需大于等于 3！");
            // }
        }
    });

    if(suffix === ""){
        unreliable_nodes.forEach(item =>{
            graph.setItemState(item, 'unreliable', true);
        })
    }

    return temp_edges;
}

//拆分连线为三段
function splitLinks(links_data){
    let temp_nodes = data["nodes"];
    let temp_combos = data["combos"];
    let temp_edges = [];
    links_data.forEach(edge =>{
        const source_node = temp_nodes.find((n) => n.id === edge.source);
        const target_node = temp_nodes.find((n) => n.id === edge.target);

        let source_pienode = source_node["pienode"];
        let target_pienode = target_node["pienode"];

        let temp_source_pienode = source_pienode.find(n => (n.source === edge.source && n.target === edge.target));
        let temp_target_pienode = target_pienode.find(n => (n.source === edge.source && n.target === edge.target));

        if(typeof(temp_source_pienode) === "undefined"){
            let temp = {
                source: edge.source,
                target: edge.target,
                dependency: {}
            };

            temp.dependency[edge.link_type + "Degree"] = 1;
            source_pienode.push(temp);
        }else{
            temp_source_pienode.dependency[edge.link_type + "Degree"] = 1;
        }

        if(typeof(temp_target_pienode) === "undefined"){
            let temp = {
                source: edge.source,
                target: edge.target,
                dependency: {}
            };

            temp.dependency[edge.link_type + "Degree"] = 1;
            target_pienode.push(temp);
        }else{
            temp_target_pienode.dependency[edge.link_type + "Degree"] = 1;
        }

        if(edge.inner_edge === 0){
            let out_edge = temp_edges.find(n =>n.id === edge.source + "_" + edge.source_comboId + "_out");
            let in_edge = temp_edges.find(n =>n.id === edge.target_comboId + "_in" + "_" + edge.target);
            let in_combo = temp_combos.find((n) => n.id === edge.target_comboId);
            let out_combo = temp_combos.find((n) => n.id === edge.source_comboId);

            in_combo.fanIn += 1;
            out_combo.fanOut += 1;

            if(typeof(out_edge) === "undefined"){
                let out_edge_model = {
                    source: edge.source,
                    target: edge.source_comboId + "_out",
                    id: edge.source + "_" + edge.source_comboId + "_out",
                    inner_edge: edge.inner_edge,
                    visible: false,
                    style: {
                        endArrow: false,
                    },
                    children: [
                        {
                            edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                            source: edge.source,
                            target: edge.target,
                            source_name: edge.source_name,
                            target_name: edge.target_name,
                            link_type: edge.link_type,
                            group_type: edge.group_type,
                            inner_edge: edge.inner_edge,
                            visible: edge.visible,
                            label: edge.label,
                            source_comboId: edge.source_comboId,
                            target_comboId: edge.target_comboId,
                            split_edges: [
                                {
                                    id: edge.target_comboId + "_in" + "_" + edge.target,
                                    source: edge.target_comboId + "_in",
                                    target: edge.target,
                                },
                                {
                                    id: edge.source + "_" + edge.source_comboId + "_out",
                                    source: edge.source,
                                    target: edge.source_comboId + "_out",
                                },
                                {
                                    id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                                    source: edge["source_comboId"] + "_out",
                                    target: edge["target_comboId"] + "_in",
                                }
                            ]
                        }
                    ],
                };

                if(edge.visible){
                    out_edge_model.visible = true;
                }

                temp_edges.push(out_edge_model);
            }else{
                out_edge.children.push({
                    edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                    source: edge.source,
                    target: edge.target,
                    source_name: edge.source_name,
                    target_name: edge.target_name,
                    link_type: edge.link_type,
                    group_type: edge.group_type,
                    inner_edge: edge.inner_edge,
                    visible: edge.visible,
                    label: edge.label,
                    source_comboId: edge.source_comboId,
                    target_comboId: edge.target_comboId,
                    split_edges: [
                        {
                            id: edge.target_comboId + "_in" + "_" + edge.target,
                            source: edge.target_comboId + "_in",
                            target: edge.target,
                        },
                        {
                            id: edge.source + "_" + edge.source_comboId + "_out",
                            source: edge.source,
                            target: edge.source_comboId + "_out",
                        },
                        {
                            id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                            source: edge["source_comboId"] + "_out",
                            target: edge["target_comboId"] + "_in",
                        }
                    ]
                });
            }

            if(typeof(in_edge) === "undefined"){
                let in_edge_model = {
                    source: edge.target_comboId + "_in",
                    target: edge.target,
                    id: edge.target_comboId + "_in" + "_" + edge.target,
                    inner_edge: edge.inner_edge,
                    visible: false,
                    children: [{
                        edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                        source: edge.source,
                        target: edge.target,
                        source_name: edge.source_name,
                        target_name: edge.target_name,
                        link_type: edge.link_type,
                        group_type: edge.group_type,
                        inner_edge: edge.inner_edge,
                        visible: edge.visible,
                        label: edge.label,
                        source_comboId: edge.source_comboId,
                        target_comboId: edge.target_comboId,
                        split_edges: [
                            {
                                id: edge.target_comboId + "_in" + "_" + edge.target,
                                source: edge.target_comboId + "_in",
                                target: edge.target,
                            },
                            {
                                id: edge.source + "_" + edge.source_comboId + "_out",
                                source: edge.source,
                                target: edge.source_comboId + "_out",
                            },
                            {
                                id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                                source: edge["source_comboId"] + "_out",
                                target: edge["target_comboId"] + "_in",
                            }
                        ]
                    }],
                };

                if(edge.visible){
                    in_edge_model.visible = true;
                }

                temp_edges.push(in_edge_model);

                temp_edges.push();
            }else{
                in_edge.children.push({
                    edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                    source: edge.source,
                    target: edge.target,
                    source_name: edge.source_name,
                    target_name: edge.target_name,
                    link_type: edge.link_type,
                    group_type: edge.group_type,
                    inner_edge: edge.inner_edge,
                    visible: edge.visible,
                    label: edge.label,
                    source_comboId: edge.source_comboId,
                    target_comboId: edge.target_comboId,
                    split_edges: [
                        {
                            id: edge.target_comboId + "_in" + "_" + edge.target,
                            source: edge.target_comboId + "_in",
                            target: edge.target,
                        },
                        {
                            id: edge.source + "_" + edge.source_comboId + "_out",
                            source: edge.source,
                            target: edge.source_comboId + "_out",
                        },
                        {
                            id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                            source: edge["source_comboId"] + "_out",
                            target: edge["target_comboId"] + "_in",
                        }
                    ]
                });
            }

            if(typeof(temp_edges.find((n) => (n.target === (edge.target_comboId + "_in")
                && (n.source === edge.source_comboId + "_out")))) === "undefined"){
                let temp_edge = {};
                temp_edge["id"] = edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in";
                temp_edge["source"] = edge["source_comboId"] + "_out";
                temp_edge["target"] = edge["target_comboId"] + "_in";
                temp_edge["visible"] = false;
                temp_edge["style"] = {
                    lineWidth: 2
                };

                if(edge.visible){
                    temp_edge.visible = true;
                }

                temp_edges.push(temp_edge);
            }
        }else{
            temp_edges.push(edge);
        }
    })

    return temp_edges;
}

function loadSmellByButton(){
    let smell_data = [];

    let smell_type_filter = $('#smellTypeSelect').val();
    smell_data_global.forEach(smell => {
        if(smell.smell_type === smell_type_filter) {
            smell_data.push(smell);
        }
    })

    smell_data_single_type = smell_data;
    smell_data_flag = "type";

    loadSmell(smell_data);
    loadSmellTable(smell_data);
}

function loadSmell(smell_data){
    present_smell_data = smell_data;
    clearCombo();
    deleteSmell("reload");
    if (!isEmptyObject(smell_filter_condition)) {
        filterSmell();
    }

    smell_data.forEach(smell => {
        smell.nodes.forEach(node_data => {
            const node = graph.findById(node_data.id.split("_")[1]);
            if (typeof (node) !== "undefined") {
                let node_model = node._cfg.model;
                node_model.smellId = smell.id;
                node_model.smellType = smell.smell_type;
                node_model.smellName = smell.name;
                graph.setItemState(node, 'smell_normal', true);
            }
        })
    })
}

function deleteSmell(type){
    const nodes = graph.findAllByState('node', 'smell_normal');
    nodes.forEach((node) => {
        node._cfg.model.smellId = 0;
        node._cfg.model.smellType = "";
        node._cfg.model.smellName = "";
        graph.setItemState(node, 'smell_normal', false);
    });
    if(type === "delete"){
        smell_data_flag = "";
        present_smell_data = [];
        present_smell_node_data = [];
        smell_filter_condition = {};
        present_smell_type = "";
        data["edges"] = present_edge_data;
        paintCombo();

        const nodes = graph.getNodes();
        nodes.forEach((node) => {
            node.toFront();
        });
        graph.paint();
    }
}

function filterSmell(){
    let smellOnlyDependsOn = $("#smellOnlyDependsOn").prop("checked") ? 1 : 0;
    let smellOnlyClone = $("#smellOnlyClone").prop("checked") ? 1 : 0;
    let smellOnlyCoChange = $("#smellOnlyCoChange").prop("checked") ? 1 : 0;

    closeFilterSmellLayer();
    showLoadingWindow();

    let smell_data = [];
    console.log(smell_data_single_type);

    for(let i = 0; i < smell_data_single_type.length; i++){
        let nodes = smell_data_single_type[i].nodes;
        edge:
        for(let j = 0; j < actual_edges.length; j++){
            let edge = actual_edges[j];
            if((edge.link_type === "dependson" && smellOnlyDependsOn === 1) ||
                (edge.link_type === "clone" && smellOnlyClone === 1) ||
                (edge.link_type === "cochange" && smellOnlyCoChange === 1)){
                for(let k = 0; k < nodes.length; k++){
                    if(nodes[k].id.split("_")[1] === actual_edges[j].source){
                        for(let m = 0; m < nodes.length; m++){
                            if(nodes[m].id.split("_")[1] === actual_edges[j].target){
                                smell_data.push(smell_data_single_type[i]);
                                break edge;
                            }
                        }
                    }else if(nodes[k].id.split("_")[1] === actual_edges[j].target){
                        for(let m = 0; m < nodes.length; m++){
                            if(nodes[m].id.split("_")[1] === actual_edges[j].source){
                                smell_data.push(smell_data_single_type[i]);
                                break edge;
                            }
                        }
                    }
                }
            }
            // console.log(actual_edges[j]);
        }
    }

    console.log(smell_data);

    loadSmell(smell_data);
    loadSmellTable(smell_data);

    closeLoadingWindow();
}

function filterSmellLayer(){
    if(smell_data_flag !== "type"){
        layui.use('layer', function(){
            let layer = layui.layer;
            if(smell_data_flag === "group"){
                layer.alert('当前显示为单个异味，请选择一种异味！');
            }else{
                layer.alert('当前未选择异味！');
            }
        });
    }else{
        layui.use('layer', function(){
            filter_smell_layer = layui.layer;

            let html_link = "<p class='combo_p'><label class = \"AttributionSelectLabel\">" +
                "当前异味类型： " + "<b>" + present_smell_type + "</b>" +
                "</label></p><hr>" +

                "<p class='combo_p'><label class = \"AttributionSelectLabel\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"smellOnlyDependsOn\">" +
                "只显示有Depends-On关系的节点" +
                "</label></p><hr>" +

                "<p class='combo_p'><label class = \"AttributionSelectLabel\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"smellOnlyClone\">" +
                "只显示有Clone关系的节点" +
                "</label></p><hr>" +

                "<p class='combo_p'><label class = \"AttributionSelectLabel\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"smellOnlyCoChange\">" +
                "只显示有Co-Change关系的节点" +
                "</label></p><hr>" +

                "<p><div style=\"margin-top: 20px;\">" +
                "<button class = \"layui-btn layui-btn-fluid\" type=\"button\" onclick= filterSmell() >确定</button>" +
                "</div></p>";

            let html_condition = "<p class='combo_p'><label class = \"AttributionSelectLabel\">" +
                "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"smellConditionCommonDependency\">" +
                "只显示节点间的共同依赖(只针对单个异味)" +
                "</label></p><hr>" +

                "<p><div style=\"margin-top: 20px;\">" +
                "<button class = \"layui-btn layui-btn-fluid\" type=\"button\" onclick= filterSmell() >确定</button>" +
                "</div></p>";

            filter_smell_layer.tab({
                move: false,
                tab: [{
                    title: '关系筛选',
                    content: html_link
                }, {
                    title: '条件筛选',
                    content: html_condition
                }]
            });

            $(".selectpicker").selectpicker({
                actionsBox:true,
                countSelectedText:"已选中{0}项",
                selectedTextFormat:"count > 2"
            })
        });
    }
}

function closeFilterSmellLayer(){
    layui.use('layer', function(){
        filter_smell_layer = layui.layer;
        layer.close(filter_smell_layer.index);
    });
}

function loadSmellTable(smell_data){
    let table_data = [];

    smell_data.forEach(smell => {
        let tmp = {};
        tmp.smellId = smell.id;
        tmp.smellInfo = "\"" + smell.nodes[0].name + "\"等 " + smell.nodes.length + " 个文件";
        table_data.push(tmp);
    })

    layui.use('table', function(){
        let table = layui.table;

        table.render({
            elem: '#table_smell',
            height: 500,
            data: table_data,
            page: true,
            cols: [[
                {field: 'smellId', title: 'Smell ID', width:100, sort: true, fixed: 'left'},
                {field: 'smellInfo', title: 'Smell Info', width:300}]]
        });

        table.on('row(table_smell)', function(obj){
            let smell_data_single = [];
            // console.log(obj.data) //得到当前行数据

            if(obj.data.smellId !== smell_data_single_group_id){
                smell_data_global.forEach(smell => {
                    if(smell.id === obj.data.smellId){
                        smell_data_single.push(smell);
                        smell_data_single_group_id = obj.data.smellId;
                    }
                })

                smell_data_single_group = smell_data_single;
                smell_data_flag = "group";

                if(smell_data_single.length > 1){

                }

                loadSmell(smell_data_single);
            }else{
                smell_data_single_group_id = -1;
                smell_data_single_group = [];
                smell_data_flag = "type";
                loadSmell(smell_data_single_type);
            }

        });

    });
}

//选中某一组异味的时候，显示与该组节点都依赖的节点
function showCommonDependency(smell_data){
    let link_data = [];
    if (!isEmptyObject(smell_filter_condition)) {
        link_data = present_edge_data.concat();
    }else{
        link_data = present_edge_data.concat();
    }

    smell_data.forEach(smell => {

    })
}

//获取当前连线筛选条件
function getFilterCondition(suffix){
    let filter = {}
    let temp_dependson = {}
    let temp_clone = {}
    let temp_cochange = {}
    let temp_smell_condition_filter = {}

    temp_dependson["checked"] = $("#dependsOn" + suffix).prop("checked") ? 1 : 0;
    temp_clone["checked"] = $("#clone" + suffix).prop("checked") ? 1 : 0;
    temp_cochange["checked"] = $("#coChange" + suffix).prop("checked") ? 1 : 0;

    temp_dependson["dependsIntensity"] = $("#dependsIntensity" + suffix).prop("checked") ? 1 : 0;
    temp_dependson["intensityCompareSelectBelow"] = $("#intensityCompareSelectBelow" + suffix).val();
    temp_dependson["intensityCompareSelectHigh"] = $("#intensityCompareSelectHigh" + suffix).val();
    temp_dependson["intensitybelow"] = $("#intensitybelow" + suffix).val();
    temp_dependson["intensityhigh"] = $("#intensityhigh" + suffix).val();

    temp_dependson["dependsOnTimes"] = $("#dependsOnTimes" + suffix).prop("checked") ? 1 : 0;
    temp_dependson["dependencyTimes"] = $("#dependencyTimes" + suffix).val();

    temp_dependson["dependsOnType"] = $("#dependsOnType" + suffix).prop("checked") ? 1 : 0;
    temp_dependson["dependsTypeSelect"] = $("#dependsTypeSelect" + suffix).val();

    temp_clone["cloneSimilarity"] = $("#cloneSimilarity" + suffix).prop("checked") ? 1 : 0;
    temp_clone["similarityCompareSelectBelow"] = $("#similarityCompareSelectBelow" + suffix).val();
    temp_clone["similarityCompareSelectHigh"] = $("#similarityCompareSelectHigh" + suffix).val();
    temp_clone["similarityhigh"] = $("#similarityhigh" + suffix).val();
    temp_clone["similaritybelow"] = $("#similaritybelow" + suffix).val();

    temp_clone["cloneTimes"] = $("#cloneTimes" + suffix).prop("checked") ? 1 : 0;
    temp_clone["clonetimes"] = $("#clonetimes" + suffix).val();

    temp_cochange["cochangeTimes"] = $("#cochangeTimes" + suffix).prop("checked") ? 1 : 0;
    temp_cochange["cochangetimes"] = $("#cochangetimes" + suffix).val();

    filter["dependson"] = temp_dependson;
    filter["clone"] = temp_clone;
    filter["cochange"] = temp_cochange;

    if(suffix === "_smell"){
        temp_smell_condition_filter["smellConditionCommonDependency"] = $("#smellConditionCommonDependency").prop("checked") ? 1 : 0;
        filter["smell_condition_filter"] = temp_smell_condition_filter;
    }

    return filter;
}

function generateFilterHtml(suffix){
    let html_loadlink = "";
    html_loadlink += "<div>" +
        "<form role=\"form\">" +

        "<p class='combo_p'><label class = \"AttributionSelectTitle\">" +
        "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOn" + suffix + "\" " +
        "onclick=\"CancelChildrenChecked('dependsOn" + suffix + "')\">Dependency：" +
        "</label></p>" +

        "<p class='combo_p'><input style = \"margin-left:25px;\" type=\"checkbox\" id=\"dependsIntensity" + suffix + "\" " +
        "onclick=\"setParentChecked('dependsOn" + suffix + "', 'dependsIntensity" + suffix + "')\" " +
        "name = \"dependsOn" + suffix + "_children\">" +

        "<input  class = \"AttributionSelectInput layui-input-block\" id=\"intensitybelow" + suffix + "\" value=\"0.8\">" +

        "<select class = \"AttributionSelectSingleSelect layui-input-block\" id=\"intensityCompareSelectBelow" + suffix + "\">" +
        "<option value=\"<=\" selected = \"selected\"><=</option>" +
        "<option value=\"<\"><</option></select>" +

        "<label class = \"AttributionSelectLabel\"> &nbsp;Intensity</label>" +

        "<select class = \"AttributionSelectSingleSelect layui-input-block\" id=\"intensityCompareSelectHigh" + suffix + "\">" +
        "<option value=\"<=\"><=</option>" +
        "<option value=\"<\" selected = \"selected\"><</option></select>" +

        "<input  class = \"AttributionSelectInput layui-input-block\" id=\"intensityhigh" + suffix + "\" value=\"1\"></p>" +

        "<p class='combo_p'><label class = \"AttributionSelectLabel\" style = \"margin-left:25px\">" +
        "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOnTimes" + suffix + "\" " +
        "onclick=\"setParentChecked('dependsOn" + suffix + "', 'dependsOnTimes" + suffix + "')\" " +
        "name = \"dependsOn" + suffix + "_children\"> Times >= " +
        "<input  id=\"dependencyTimes" + suffix + "\" class = \"AttributionSelectInput layui-input-block\" " +
        "style='margin-right: 80px' value=\"3\">" +
        "</label></p>" +

        "<p class='combo_p'><label class = \"AttributionSelectLabel\" style = \"margin-left:25px; margin-right:20px;\">" +
        "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOnType" + suffix + "\" " +
        "onclick=\"setParentChecked('dependsOn" + suffix + "', 'dependsOnType" + suffix + "')\" " +
        "name = \"dependsOn" + suffix + "_children\"> Dependency Type: " +
        "</label>" +

        "<select id = \"dependsTypeSelect" + suffix + "\" class=\"selectpicker\" multiple>" +
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
        "</p><hr>";

    html_loadlink += "<p class='combo_p'><label class = \"AttributionSelectTitle\">" +
        "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"clone" + suffix + "\" " +
        "onclick=\"CancelChildrenChecked('clone" + suffix + "')\">Clone：" +
        "</label></p>" +

        "<p class='combo_p'><input style = \"margin-right:10px; margin-left:25px; \" type=\"checkbox\" " +
        "id=\"cloneSimilarity" + suffix + "\" " +
        "onclick=\"setParentChecked('clone" + suffix + "', 'cloneSimilarity" + suffix + "')\" " +
        "name = \"clone" + suffix + "_children\">" +
        "<input  class = \"AttributionSelectInput\" id=\"similaritybelow" + suffix + "\" value=\"0.7\">" +

        "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectBelow" + suffix + "\">" +
        "<option value=\"<=\" selected = \"selected\"><=</option>" +
        "<option value=\"<\"><</option></select>" +

        "<label class = \"AttributionSelectLabel\"> &nbsp;Clone Value</label>" +

        "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectHigh" + suffix + "\">" +
        "<option value=\"<=\"><=</option>" +
        "<option value=\"<\" selected = \"selected\"><</option></select>" +

        "<input  class = \"AttributionSelectInput\" id=\"similarityhigh" + suffix + "\" value=\"1\"></p><hr>";

    html_loadlink += "<p class='combo_p'><label class = \"AttributionSelectTitle\">" +
        "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"coChange" + suffix + "\" " +
        "onclick=\"CancelChildrenChecked('coChange" + suffix + "')\">Co-change：" +
        "</label></p>" +

        "<p class='combo_p'><label class = \"AttributionSelectLabel\">" +
        "<input style = \"margin-right:10px; margin-left:25px;\" type=\"checkbox\" id=\"cochangeTimes" + suffix + "\" " +
        "onclick=\"setParentChecked('coChange" + suffix + "', 'cochangeTimes" + suffix + "')\" " +
        "name = \"coChange" + suffix + "_children\"> Times >= " +
        "<input class = \"AttributionSelectInput\" id=\"cochangetimes" + suffix + "\" value=\"3\">" +
        "</label></p>";

    if(suffix === ""){
        html_loadlink += "<p><div style=\"margin-top: 10px;\">" +
            "<button class = \"combo_button layui-btn layui-btn-primary\" type=\"button\" onclick= filterComboLinks() >筛选连线</button>" +
            "<button id = \"unreliableDependencyFile\" class = \"combo_button layui-btn layui-btn-primary\" type=\"button\" " +
            "onclick= loadUnreliableDependency()>加载不可依赖关系</button>" +
            "<input type=\"file\" accept=\".json\" id=\"unreliable_dependency_file\" " +
            "onchange='setUnreliableDependency()' style=\"display:none\">" +
            "</div></p>";
    }

    html_loadlink += "</form>" +
        "</div>";

    return html_loadlink;
}

function updatePieNode(node){
    const model = {
        type: 'pie-node',
    };

    node.update(model);
}

function deletePieNode(node){
    const model = {
        type: 'node',
    };

    node.update(model);
}

//自动布局
function autoLayout(){
    let combo_list = data["combos"];
    let node_list = data["nodes"];
    let combo_num = combo_list.length;
    let radius = node_list.length * REGULAR_NODE_SIZE / 6.28 * 0.5;
    let cord = radius * 0.5;
    let left = [], right = [];
    let node_index = 0;

    combo_list.forEach((item, index) => {
        if(index < combo_list.length / 2){
            left.push(item);
        }else{
            right.push(item);
        }
    })

    left.sort(function(a,b){
        return a.fanOut / (a.fanIn + a.fanOut) - b.fanOut / (b.fanIn + b.fanOut);
    });
    right.sort(function(a,b){
        return b.fanOut / (b.fanIn + b.fanOut) - a.fanOut / (a.fanIn + a.fanOut);
    });

    let combo_list_sort = left.concat(right);
    data["combos"] = combo_list_sort;

    combo_list_sort.forEach((item, index) => {
        let combo_cord;
        let outerNodeLineIndex = 1;
        let innerNodeLineIndex = 1;
        let line_node_num = parseInt(Math.sqrt(item.node_num) * 1.5);
        let combo_width = REGULAR_NODE_SIZE * 1.5 * line_node_num;
        let combo_height = REGULAR_NODE_SIZE * 1.5 * line_node_num;
        let combo_radio = index / combo_num; //当前combo处于总布局圆圈的比例
        let model1x, model1y, model2x, model2y;

        if(index !== combo_num){
            let stretch_radio;
            if(combo_radio < 0.5){
                stretch_radio = (0.25 - combo_radio) / 0.25;
            }else{
                stretch_radio = (combo_radio - 0.75) / 0.25;
            }
            combo_cord = [radius * Math.cos(combo_radio * Math.PI * 2) - combo_width * 0.5 + (1 + stretch_radio * 1.6) * cord,
                (-radius) * Math.sin(combo_radio * Math.PI * 2) - combo_height * 0.5 + cord];
        }else{
            combo_cord = [radius - combo_width * 0.5 + cord,  -(combo_height * 0.5) + cord];
        }

        node_list.forEach(node =>{
            if(node.comboId === item.id){
                let innerLineIndex = Math.floor(innerNodeLineIndex / line_node_num);
                let outerLineIndex = Math.floor(outerNodeLineIndex / line_node_num);
                // console.log(innerLineIndex);
                // console.log(outerLineIndex);
                if(node.outerNode === 0){ //内部节点
                    if(combo_radio <= 0.5){//放置在上面
                        if(combo_radio <= 0.25){
                            node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * innerLineIndex - combo_height;
                            innerNodeLineIndex++;
                        }else{
                            node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * innerLineIndex - combo_height;
                            innerNodeLineIndex++;
                        }
                    }else{
                        if(combo_radio <= 0.75){
                            node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * innerLineIndex + combo_height;
                            innerNodeLineIndex++;
                        }else{
                            node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * innerLineIndex + combo_height;
                            innerNodeLineIndex++;
                        }

                    }
                }else{ //外部节点
                    if(combo_radio <= 0.5) {
                        if(combo_radio <= 0.25) {
                            node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                            outerNodeLineIndex++;
                        }else{
                            node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                            outerNodeLineIndex++;
                        }
                    }else{
                        if(combo_radio <= 0.75) {
                            node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                            outerNodeLineIndex++;
                        }else{
                            node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                            node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                            outerNodeLineIndex++;
                        }
                    }
                }
            }
        })

        // for(let i = 0; i < item.node_num; i++){
        //     // if(node_list[node_index].comboId === item.id){
        //     let temp_node = node_list[node_index];
        //     let innerLineIndex = Math.ceil(innerNodeLineIndex / line_node_num);
        //     let outerLineIndex = Math.ceil(outerNodeLineIndex / line_node_num);
        //     // console.log(innerLineIndex);
        //     // console.log(outerLineIndex);
        //     if(temp_node.outerNode === 0){ //内部节点
        //         if(combo_radio <= 0.5){//放置在上面
        //             if(combo_radio <= 0.25){
        //                 temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * innerLineIndex - combo_height;
        //                 innerNodeLineIndex++;
        //             }else{
        //                 temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * innerLineIndex - combo_height;
        //                 innerNodeLineIndex++;
        //             }
        //         }else{
        //             if(combo_radio <= 0.75){
        //                 temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * innerLineIndex + combo_height;
        //                 innerNodeLineIndex++;
        //             }else{
        //                 temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * innerLineIndex + combo_height;
        //                 innerNodeLineIndex++;
        //             }
        //
        //         }
        //     }else{ //外部节点
        //         if(combo_radio <= 0.5) {
        //             if(combo_radio <= 0.25) {
        //                 temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
        //                 outerNodeLineIndex++;
        //             }else{
        //                 temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
        //                 outerNodeLineIndex++;
        //             }
        //         }else{
        //             if(combo_radio <= 0.75) {
        //                 temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
        //                 outerNodeLineIndex++;
        //             }else{
        //                 temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
        //                 temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
        //                 outerNodeLineIndex++;
        //             }
        //         }
        //     }
        //     node_index++;
        //     // }
        // }

        if(combo_radio <= 0.5){
            if(combo_radio <= 0.25){
                model1x = combo_cord[0] + combo_width * 0.5;
                model1y = combo_cord[1] + REGULAR_NODE_SIZE;
                model2x = combo_cord[0] - REGULAR_NODE_SIZE;
                model2y = combo_cord[1] - combo_height * 0.3;
            }else{
                model1x = combo_cord[0] - combo_width * 0.5;
                model1y = combo_cord[1] + REGULAR_NODE_SIZE;
                model2x = combo_cord[0] + REGULAR_NODE_SIZE;
                model2y = combo_cord[1] - combo_height * 0.3;
            }
        }else{
            if(combo_radio <= 0.75){
                model1x = combo_cord[0] - combo_width * 0.5;
                model1y = combo_cord[1] - REGULAR_NODE_SIZE;
                model2x = combo_cord[0] + REGULAR_NODE_SIZE;
                model2y = combo_cord[1] + combo_height * (1 / 3);
            }else{
                model1x = combo_cord[0] + combo_width * 0.5;
                model1y = combo_cord[1] - REGULAR_NODE_SIZE;
                model2x = combo_cord[0] - REGULAR_NODE_SIZE;
                model2y = combo_cord[1] + combo_height * (1 / 3);
            }
        }

        let model1 = {
            id: item.id + "_in",
            size: REGULAR_NODE_SIZE / 3,
            inOutNode: 1,
            comboId: item.id,
            x: model1x,
            y: model1y,
            style: {
                fill: "#f18c6f",
                stroke: "#f3623a"
            }
        }

        let model2 = {
            id: item.id + "_out",
            size: REGULAR_NODE_SIZE / 3,
            inOutNode: 1,
            comboId: item.id,
            x: model2x,
            y: model2y,
            style: {
                fill: "#f18c6f",
                stroke: "#f3623a"
            }
        }

        node_list.push(model1);
        node_list.push(model2);
        //
        // graph.addItem('node', model1);
        // graph.addItem('node', model2);
    })
}

//重置筛选
function clearFilter(){
    $.ajax({
        url: "/project/clearfilter",
        type: "GET",
        success: function (result) {
            if (result.result === "success") {
                alert("重置成功");
                projectList_filter = [];
                package_filter_global = [];
            } else {
                alert("重置失败！");
            }
        }
    });
}

//筛选项目
function showFilterWindow(){
    let html = "<div class=\"div_treeview\">" +
        "<h4>项目结构</h4><button class=\"btn pull-right\" id=\"buttonPackageFilter\">设置</button>" +
        "<p><i id='iconProject'></i></p>" +
        "<div class=\"div_treeview_content\">" +
        "<!-- 包括项目、项目的基本结构 -->" +
        "<ul id=\"treeProjects\" class=\"ztree\"></ul>" +
        "</div>" +
        "<div id=\"treeProjectsPage\" style=\"text-align: center;\">" +
        "</div>" +
        "</div>";

    let win = new Window({

        width: 800, //宽度
        height: 600, //高度
        title: '筛选项目', //标题
        content: html, //内容
        isMask: true, //是否遮罩
        isDrag: true, //是否移动
    });

    _project();

    //筛选文件目录按钮
    $("#buttonPackageFilter").click(function() {
        let projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
        let checkCount = projectZTreeObj.getCheckedNodes(true);
        let ids = [];
        let j = 0;
        for (let i = 0; i < checkCount.length; i++) {
            if (checkCount[i].type === "Package") {
                ids[j++] = {
                    type: "pck",
                    id: checkCount[i].id,
                };
            } else if (checkCount[i].type === "FileList") {
                ids[j++] = {
                    type: "FileList",
                    id: checkCount[i].getParentNode().id
                };
            }else if(checkCount[i].type === "PckList"){
                ids[j++] = {
                    type: "PckList",
                    id: checkCount[i].getParentNode().id
                };
            }
        }
        $.ajax({
            url: "/project/pckfilter",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(ids),
            success: function (result) {
                if (result.result === "success") {
                    let paths = "";
                    package_filter_global = result.path;
                    for(let i = 0; i < result.length; i ++) {
                        paths += (i+1) + "." + result.path[i] + '\n';
                    }
                    alert("设置成功 " + result.length + " 个路径！\n" +
                        "分别为：\n" + paths);
                    projectList_filter = $('#multipleProjectSelect').val();
                } else {
                    alert("设置失败！");
                }
            }
        });
    })
}

//加载不可依赖关系
function loadUnreliableDependency(){
    document.getElementById("unreliable_dependency_file").click();
}

//设置不可依赖关系
function setUnreliableDependency(){
    let resultFile = $('#unreliable_dependency_file')[0].files[0];
    let reader = new FileReader();
    reader.readAsText(resultFile, "utf8");
    reader.onload = function(){
        let temp_list = JSON.parse(this.result);
        temp_list.forEach(function (item){
            unreliable_dependency_list.push(item);
        });
        console.log(unreliable_dependency_list);
    }
}

//筛选项目框内项目树结构
function showZTree(nodes, container = $("#ztree")) {
    let setting = {
        check: {
            enable: true,
            chkStyle: "checkbox",
            chkboxType: {
                "Y":"","N":"s"
            }
        },
        data: {
            keep: {
                parent: true
            }
        },
        callback: {
            onClick: function(event, treeId, treeNode) {
                let id = treeNode.id;
                if(id <= 0) {
                    return ;
                }
                let type = treeNode.type;
                if(type == "Project") {
                    window.open("/project/index?id=" + id);
                }
            },
            onCheck: function(event, treeId, treeNode) {
                let type = treeNode.type;
                if(treeNode.checked == true){
                    if(type == "Package"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.setChkDisabled(child, true, false, true);
                        })
                        let pckList = treeNode.getParentNode();
                        if(pckList.getParentNode() != null){
                            treeObj.setChkDisabled(pckList.getParentNode(), true, true, false);
                        }
                    }else if(type == "PckList"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        treeObj.setChkDisabled(treeNode.getParentNode(), true, true, false);
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.checkNode(child, true, true, true);
                        })
                    }else if(type == "FileList"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        treeObj.setChkDisabled(treeNode.getParentNode(), true, true, false);
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.checkNode(child, true, true, false);
                        })
                    }
                }else{
                    if(type == "Package"){
                    let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                    let children = treeNode.children;
                    children.forEach(child =>{
                        treeObj.setChkDisabled(child, false, false, true);
                    });
                    let noCheckedChildren = true;
                    let cousins = treeNode.getParentNode().children;
                    cousins.forEach(cousin =>{
                        if(cousin.checked == true){
                            noCheckedChildren = false;
                        }
                    });
                    if(noCheckedChildren == true){
                        treeObj.setChkDisabled(treeNode.getParentNode(), false, true, false);
                    }
                    let pckListNode = treeNode.getParentNode();
                    if(pckListNode != null){
                        if(pckListNode.checked == true){
                            treeObj.checkNode(pckListNode, false, false, false);
                        }
                    }
                }else if(type == "PckList"){
                    let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                    let children = treeNode.children;
                    children.forEach(child =>{
                        treeObj.checkNode(child, false, true, true);
                    });
                    let noCheckedChildren = true;
                    let cousins = treeNode.getParentNode().children;
                    cousins.forEach(cousin =>{
                        if(cousin.checked == true){
                            noCheckedChildren = false;
                        }
                    });
                    if(noCheckedChildren == true){
                        treeObj.setChkDisabled(treeNode.getParentNode(), false, true, false);
                    }
                }else if(type == "FileList"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.checkNode(child, false, true, true);
                        })
                        let noCheckedChildren = true;
                        let cousins = treeNode.getParentNode().children;
                        cousins.forEach(cousin =>{
                            if(cousin.checked == true){
                                noCheckedChildren = false;
                            }
                        });
                        if(noCheckedChildren == true){
                            treeObj.setChkDisabled(treeNode.getParentNode(), false, true, false);
                        }
                    }
                }

            }
        }

    };
    let zNodes = nodes;
    $.fn.zTree.init(container, setting, zNodes);
}

//项目树结构
function _project() {

    let showProjectZTree = function(projectIds) {
        $("#iconProject").text("搜索中...");

        $.ajax({
            type:"POST",
            url : "/project/all/ztree/project",
            contentType: "application/json",
            dataType:"json",
            data:JSON.stringify(projectIds),
            success : function(result) {
                if(result.result === "success") {
                    showZTree(result.values, $("#treeProjects"));
                    $("#iconProject").text("");
                }
            }
        });
    }

    let project_list = [];
    let value = $('#multipleProjectSelect').val();
    project_list.push(value);
    showProjectZTree(project_list);
}

//筛选框子控件随着母控件一同取消点选
function CancelChildrenChecked(parent_name){
    if(!$("#" + parent_name).is(":checked")){
        $("input[name = '" + parent_name + "_children" + "']").prop("checked", false);
    }
}

//筛选框母控件随着子控件一同点选
function setParentChecked(parent_name, children_id){
    if($("#" + children_id).is(":checked") && !$("#" + parent_name).is(":checked")){
        $("#" + parent_name).prop("checked", true);
    }
}

function histogram(data, divId) {
    // $("#" + divId).css({'width': width * 0.25 + "px"});
    let histogramChart = echarts.init(document.getElementById(divId));
    let option = {
        dataZoom: [{
            type: 'slider',
            show: true,
            xAxisIndex: [0],
            left: '9%',
            bottom: -5,
            start: 0,
            end: 100
        }],
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [{
            name: "Smell Type",
            type: 'category',
            data: data.xAxis,
            axisLabel: {
                interval:0,
                rotate:40
            }
        }
        ],
        yAxis: [{
            type: 'value'
        }
        ],
        series: [{
            name: "Smell Files",
            type: 'bar',
            stack: 'smellFiles',
            data: data.yAxis
        }
        ]
    };
    histogramChart.setOption(option);
    histogramChart.off('click');
    histogramChart.on('click', function(params) {
        showLoadingWindow("加载异味中...");

        let smell_data = [];
        smell_data_global.forEach(smell => {
            if(smell.smell_type === params.name && smell.project_belong.toString() === projectId_global){
                smell_data.push(smell);
            }
        })

        smell_data_single_type = smell_data;
        present_smell_type = params.name;
        present_smell_node_data = smell_data;
        smell_data_flag = "type";

        loadSmell(smell_data);
        loadSmellTable(smell_data);

        closeLoadingWindow();
    });
}

function showHistogram(){
    if(smell_info_global.projectId.toString() === projectId_global){
        let data = {};
        let xAxis = [];
        let yAxis = [];

        smell_info_global.project_smell_info.forEach(item => {
            xAxis.push(item.smell_type);
            yAxis.push(item.smell_num);
        })

        data.xAxis = xAxis;
        data.yAxis = yAxis;
        histogram(data, "histogram_smell");
    }
}

function judgeSmellLink(edge){
    // console.log(edge);
    let smell_data = [];
    let node1_flag = false;
    let node2_flag = false;
    if(smell_data_flag === "type"){
        smell_data = smell_data_single_type.concat();
    }else if(smell_data_flag === "group"){
        smell_data = smell_data_single_group.concat();
    }

    smell_data.forEach(smell => {
        smell.nodes.forEach(node => {
            if(node.id.split("_")[1] === edge.source){
                node1_flag = true;
            }else if(node.id.split("_")[1] === edge.target){
                node2_flag = true;
            }
        })
    })

    return node1_flag && node2_flag;
}

//绘制combo
function paintCombo(){
    graph.data(data);
    graph.render();
}

function isEmptyObject(obj) {
    for (let key in obj) {
        return false;
    }
    return true;
}

//加载弹窗
function showLoadingWindow(tip){
    let html = "<div style=\"position:fixed;height:100%;width:100%;z-index:10000;background-color: #5a6268;opacity: 0.5\">" +
        "<div class='loading_window' id='Id_loading_window' " +
        "style=\"left: " + (width - 215) / 2 + "px; top:" + (height - 61) / 2 + "px;\">" + tip + "</div>" +
        "</div>";
    loading_div.html(html);
}

//关闭加载弹窗
function closeLoadingWindow(){
    loading_div.html("");
}

//筛选异味数据，使之与项目筛选吻合
function filterSmellData(data) {
    data.forEach(smell => {
        if (package_filter_global.length > 0) {
            let smell_flag = false;
            package_filter_global.forEach(pck => {
                smell.nodes.forEach(node => {
                    if (node.path.indexOf(pck) !== -1) {
                        smell_flag = true;
                    }
                })
            })

            if (smell_flag) {
                smell_data_global.push(smell);
            }
        }else{
            smell_data_global.push(smell);
        }
    })
}

if (typeof window !== 'undefined'){
    window.onresize = () => {
        if (!graph || graph.get('destroyed')) return;
        if (!container || !container.scrollWidth || !container.scrollHeight) return;
        graph.changeSize(container.scrollWidth, container.scrollHeight);
    };
}

