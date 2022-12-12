let CHART_MODE = "package";
const loading_div = $("#loading_div");
let I75 = 0.0;
let data = {};
let selected_packages = [];
let present_packages = [];
let data_stack = []
let current_path = '/';
const INSTABILITY_COLOR1 = "#642924";
const INSTABILITY_COLOR2 = "#8B3830";
const INSTABILITY_COLOR3 = "#B2463C";
const INSTABILITY_COLOR4 = "#C86259";
const INSTABILITY_COLOR5 = "#D6857E";
const COLOR_LINK_NORMAL = '#8e99c7';
const COLOR_LINK_HIGH_INTENSITY = '#3657cd';
const COLOR_LINK_LOW_INTENSITY = '#b5c2f1';
const COLOR_LINK_SPECIAL = '#f25f3f';
const COLOR_LINK_EXTENDS_AND_IMPLEMENTS = '#4393ee';

const container = document.getElementById('coupling_chart');
const width = container.scrollWidth;
const height = container.scrollHeight || 500;

const LOW_INTENSITY_EDGE_MODEL = {
    style:{
        stroke: COLOR_LINK_LOW_INTENSITY,
        lineWidth: 0.7,
    }
}

const HIGH_INTENSITY_EDGE_MODEL = {
    style:{
        stroke: COLOR_LINK_HIGH_INTENSITY,
        lineWidth: 1.8,
    }
}

function main() {
    return {
        init : function() {
            showLoadingWindow("加载中...");
            instabilityAjax();
        }
    }
}

function instabilityAjax(){
    $.ajax({
        type : "GET",
        url : "/coupling/group/top_level_packages",
        success : function(result) {
            data = result;
            // let nodes_data = json_data["nodes"];
            // let edges_data = json_data["edges"];
            //
            // data["nodes"] = json_data["nodes"];
            // data["edges"] = json_data["edges"];

            loadGraph();
            console.log(graph)
        }
    })
}

const tooltip = new G6.Tooltip({
    offsetX: 10,
    offsetY: 10,
    itemTypes: ['node', 'edge'],
    getContent: (e) => {
        const outDiv = document.createElement('div');
        outDiv.style.width = 'fit-content';
        if(e.item._cfg.type === "node"){
            if(e.item._cfg.model.nodeType === "file"){
                outDiv.innerHTML = `
              <h4>${e.item._cfg.id}</h4>
              <ul>
                <li>name: ${e.item.getModel().name}</li>
              </ul>
              <ul>
                <li>path: ${e.item.getModel().path}</li>
              </ul>
              <ul>
                <li>instability: ${e.item.getModel().instability}</li>
              </ul>`;
            }else if(e.item._cfg.model.nodeType === "package"){
                outDiv.innerHTML = `
              <h4>${e.item._cfg.id}</h4>
              <ul>
                <li>name: ${e.item.getModel().name}</li>
              </ul>
              <ul>
                <li>path: ${e.item.getModel().path}</li>
              </ul>
              <ul>
                <li>files num: ${e.item.getModel().LOF}</li>
              </ul>
              <ul>
                <li>instability: ${e.item.getModel().instability}</li>
              </ul>`;
            }
        }else if(e.item._cfg.type === "edge"){
            outDiv.innerHTML = `
              <h4>${e.item.getModel().source}_${e.item.getModel().target}</h4>
              <ul>
                <li>dependsOnTypes: ${e.item.getModel().dependsOnTypes}</li>
              </ul>
              <ul>
                <li>耦合强度(I): ${e.item.getModel().I}</li>
              </ul>
              <ul>
                <li>dist: ${e.item.getModel().dist}</li>
              </ul>`;
        }
        return outDiv;
    },
});

const toolbar = new G6.ToolBar({
    // container: tc,
    className: 'g6-toolbar-ul',
    getContent: () => {
        return `
      <ul>
        <li code='back'>返回上一层</li>
        <li code='choose'>选择</li>
        <li code='unfold'>展开</li>
        <li code='unfoldFile'>展开到文件页面</li>
      </ul>
    `;
    },
    handleClick: (code, graph) => {
        if (code === 'unfold') {
            if(selected_packages.length === 0){
                confirm("当前未选中节点！");
            }else{
                let json = {};
                let unfoldPcks = [];
                let otherPcks = [];

                selected_packages.forEach(node => {
                    unfoldPcks.push({
                        "id": node._cfg.id,
                        "instability": node._cfg.model.instability
                    })
                })

                present_packages.forEach(node => {
                    let flag = true;
                    selected_packages.forEach(node2 => {
                        if(node === node2){
                            flag = false;
                        }
                    })
                    if(flag){
                        otherPcks.push({
                            "id": node._cfg.id,
                            "instability": node._cfg.model.instability
                        })
                    }
                })

                json["unfoldPcks"] = unfoldPcks;
                json["otherPcks"] = otherPcks;
                showLoadingWindow("加载中...");
                console.log(json)

                $.ajax({
                    url: "/coupling/group/one_step_child_packages",
                    type: "POST",
                    contentType: "application/json",
                    dataType: "json",
                    data: JSON.stringify(json),
                    success: function (result) {
                        if(result["code"] === 200){
                            data_stack.push(graph.save())
                            data = result;
                            loadGraph();
                        }else if(result["code"] === -1){
                            confirm("错误！\n" + result["pck"]["directoryPath"] + "\n已无法再展开！");
                            closeLoadingWindow();
                        }
                    }
                });
            }
        }else if(code === 'choose'){
            if(selected_packages.length === 0){
                confirm("当前未选中节点！");
            }else{
                if(graph.save().nodes.length !== 1) {
                    data_stack.push(graph.save());
                }
                present_packages.length = 0;
                let deleteNodes = [];
                // present_packages = selected_packages;

                selected_packages.forEach(node =>{
                    present_packages.push(node);
                    graph.clearItemStates(node);
                })

                graph.getNodes().forEach(node => {
                    let flag = true;
                    present_packages.forEach(node2 => {
                        if(node._cfg.id === node2._cfg.id){
                            flag = false;
                        }
                    })

                    if(flag){
                        let edges = node.getEdges();
                        deleteNodes.push(node);

                        edges.forEach(edge => {
                            graph.removeItem(edge, false);
                        })
                    }
                })
                deleteNodes.forEach(node => {
                    graph.removeItem(node, false);
                })

                selected_packages.length = 0;

                loadPanel();
            }
        }else if(code === 'unfoldFile'){
            let json = {};
            let pckIds = [];
            CHART_MODE = "file";

            selected_packages.forEach(node => {
                pckIds.push({
                    "id": node._cfg.id
                })
            })
            console.log(json)
            showLoadingWindow("加载中...");

            json["pckIds"] = pckIds;
            $.ajax({
                url: "/coupling/group/files_of_packages",
                type: "POST",
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(json),
                success: function (result) {
                    data_stack.push(graph.save());
                    data = result;
                    loadGraph();
                }
            });
        } else if (code === 'back') {
            if (data_stack.length !== 0) {
                data = data_stack.pop();
                loadGraph();
            }
        }
    },
});


const graph = new G6.Graph({
    container: 'coupling_chart',
    width,
    height,
    fitView: true,
    modes: {
        default: ['drag-canvas', 'drag-node', 'zoom-canvas', 'click-select'],
    },
    // layout: {
    //     type: 'dagre',
    //     sortByCombo: true,
    //     ranksep: 10,
    //     nodesep: 20,
    // },
    defaultNode: {
        type: 'circle',
        size: 20,
        style: {
            cursor: "pointer",
            // fill: "#cce9f8",
            // stroke: "#a0d6f4",
            fill: "rgb(129,236,236)",
            stroke: "rgb(113,151,234)",

        },
        labelCfg: {
            style: {
                fill: '#1890ff',
                fontSize: 5,
                background: {
                    fill: '#ffffff',
                    stroke: '#9EC9FF',
                    padding: [2, 2, 2, 2],
                    radius: 2,
                },
            },
            position: 'bottom',
        }
    },
    defaultEdge: {
        type: 'quadratic',
        labelCfg: {
            autoRotate: true,
        },
        // type: 'cubic-vertical',
        // size: 1,
        // labelCfg: {
        //     style: {
        //         fontSize: 5,
        //     },
        // },
        style: {
            stroke: COLOR_LINK_NORMAL,
            lineWidth: 1,
            endArrow: {
                path: G6.Arrow.vee(5, 8, 3),
                d: 3,
                fill: COLOR_LINK_NORMAL,
            },
            cursor: "pointer"
        },
    },
    defaultCombo: {
        type: 'rect',
        style: {
            fill: "#C4E3B2",
            stroke: "#C4E3B2",
            fillOpacity: 0.1,
        },
    },
    edgeStateStyles: {
        highlight: {
            stroke: '#bc2704',
        },
        reverse: {
            stroke: COLOR_LINK_SPECIAL,
        },
    },
    plugins: [tooltip, toolbar],
    minZoom: 0.05,
});

function levelLayout(){
    let nodelist = graph.getNodes();

    let list1 = [], list2 = [], list3 = [], list4 = [], list5 = [], allList = [];
    nodelist.forEach(node =>{
        if(node._cfg.model.instability >= 0.8){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR1
                }
            });
            list1.push(node);
        }else if(node._cfg.model.instability >= 0.6){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR2
                }
            });
            list2.push(node);
        }else if(node._cfg.model.instability >= 0.4){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR3
                }
            });
            list3.push(node);
        }else if(node._cfg.model.instability >= 0.2){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR4
                }
            });
            list4.push(node);
        }else{
            node.update({
                style:{
                    fill:INSTABILITY_COLOR5
                }
            });
            list5.push(node);
        }
    })

    allList.push(list1);
    allList.push(list2);
    allList.push(list3);
    allList.push(list4);
    allList.push(list5);

    let startIndex = 0;
    let minParentPckId = "0";
    for(let i = 0; i < allList.length; i++){
        if(allList[i].length !== 0){
            minParentPckId = allList[i][0]._cfg.model.parentPckId;
            allList[i].sort(function(a,b){return a._cfg.model.parentPckId - b._cfg.model.parentPckId});
            allList[i].forEach((node, index) =>{
                minParentPckId = Math.min(minParentPckId, node._cfg.model.parentPckId);
                node.updatePosition({
                    // x: (index - (list1.length / 2)) * 70,
                    x: index * 70,
                    y: 70 * (i + 1) - ((node._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
                });
            })
            startIndex = i + 1;
            break;
        }
    }

    if(startIndex < allList.length){
        for(let i = startIndex; i < allList.length; i++){
            let sortedList = [];
            let nullList = [];

            allList[i].forEach(node =>{
                let parentPosX = 0.0;
                let parentPosSum = 0.0;
                let neighbors = node.getNeighbors();

                neighbors.forEach(neighbor =>{
                    if(neighbor._cfg.model.instability > node._cfg.model.instability){
                        parentPosX += neighbor._cfg.model.x;
                        parentPosSum += 1;
                    }
                })

                if(parentPosSum === 0){
                    if(neighbors.length > 0){
                        node._cfg.model.barycenter = 0;
                        sortedList.push(node);
                    }else{
                        nullList.push(node);
                    }
                }else{
                    node._cfg.model.barycenter = parentPosX / parentPosSum;
                    sortedList.push(node);
                }
            })

            sortedList.sort(function(a,b){return a._cfg.model.barycenter - b._cfg.model.barycenter});


            sortedList.forEach((node2, index) =>{
                let offset = i % 2 === 0 ? -35 : 0;
                node2.updatePosition({
                    // x: (index - (sortedList.length / 2)) * 70,
                    x: -100 + offset + index * 70,
                    y: 70 * (i + 1) - ((node2._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
                });
            })

            nullList.forEach((nullNode, index) => {
                nullNode.updatePosition({
                    // x: (index - (sortedList.length / 2)) * 70,
                    x: (index - Math.floor(index / 15) * 15 - 3) * 60,
                    y: 70 * (i + 2 + Math.floor(index / 15)) - ((nullNode._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
                });
            })
        }
    }

    graph.refresh();
    graph.fitCenter();
    graph.fitView(80);
}

function handleReverseEdgesAndExtends(){
    let edges = graph.getEdges();
    let nodes = graph.getNodes();

    edges.forEach(edge =>{
        let startLevel = 0;
        let endLevel = 0;

        nodes.forEach(node =>{
            if(node._cfg.id === edge._cfg.model.source){
                startLevel = node._cfg.model.y;
            }
            if(node._cfg.id === edge._cfg.model.target){
                endLevel = node._cfg.model.y;
            }
        })

        if(startLevel > endLevel){
            if(edge._cfg.model.isExtendOrImplements){
                let model = {
                    style:{
                        endArrow: {
                            path: G6.Arrow.triangleRect(10, 10, 10, 2, 4),
                            fill: COLOR_LINK_EXTENDS_AND_IMPLEMENTS,
                        }
                    }
                }
                edge.update(model);
            }else{
                edge.setState('reverse', true);
            }
        }else{
            if(edge._cfg.model.isTwoWayDependsOn){
                let tmpedge = graph.findById(edge._cfg.model.target + "_" + edge._cfg.model.source);
                if(tmpedge._cfg.model.isExtendOrImplements){
                    edge.setState('reverse', true);
                }
            }
        }
    })
}

function savePresentNodes(){
    present_packages.length = 0;
    graph.getNodes().forEach(node => {
        present_packages.push(node);
    })
}

function loadPanel(){
    let html = "";
    let nodes = graph.getNodes();
    let edges = graph.getEdges();
    let LOF = 0;
    let LOC = 0;
    let IList = [];
    let Isum = 0.0;
    let Imax = 0.0;
    let Imin = 10000.0;

    nodes.forEach(node => {
        LOF += node._cfg.model.LOF;
        LOC += node._cfg.model.LOC;
    })
    if(CHART_MODE === "package"){
        html += "<p>包数：" + nodes.length + "</p>";
        html += "<p>文件数：" + LOF + "</p>";
    }else if(CHART_MODE === "file"){
        html += "<p>文件数：" + nodes.length + "</p>";
    }

    html += "<p>代码行数：" + LOC + "</p>";
    html += "<br />";
    if(edges.length > 0){
        edges.forEach(edge => {
            IList.push(edge._cfg.model.I);
            Isum += edge._cfg.model.I;
            Imax = Math.max(Imax, edge._cfg.model.I);
            Imin = Math.min(Imin, edge._cfg.model.I);
        })
        IList.sort();
        I75 = IList[parseInt(IList.length * 0.75)];

        html += "<p>耦合强度(I) 平均值：" + (Isum / edges.length).toFixed(3) + "</p>";
        html += "<p>耦合强度(I) max：" + Imax.toFixed(3) + "</p>";
        html += "<p>耦合强度(I) 90分位值：" + IList[parseInt(IList.length * 0.9)].toFixed(3) + "</p>";
        html += "<p>耦合强度(I) 85分位值：" + IList[parseInt(IList.length * 0.85)].toFixed(3) + "</p>";
        html += "<p>耦合强度(I) 80分位值：" + IList[parseInt(IList.length * 0.8)].toFixed(3) + "</p>";
        html += "<p>耦合强度(I) 75分位值(Q3)：" + IList[parseInt(IList.length * 0.75)].toFixed(3) + "</p>";
        html += "<p>耦合强度(I) 中位值(Q2)：" + IList[parseInt(IList.length * 0.5)].toFixed(3) + "</p>";
        html += "<p>耦合强度(I) 25分位值(Q1)：" + IList[parseInt(IList.length * 0.25)].toFixed(3) + "</p>";
        html += "<p>耦合强度(I) min：" + Imin.toFixed(3) + "</p>";
        html += "<br />";

        let reverseNum = graph.findAllByState("edge", "reverse").length;
        html += "<p>逆向依赖数：" + reverseNum + "</p>";
        html += "<p>总依赖数：" + edges.length + "</p>";
        html += "<p>逆向依赖数 / 总依赖数：" + (reverseNum / edges.length).toFixed(3) + "</p>";
    }
    $("#data_panel").html(html);
}

function handleEdgesWidth(){
    graph.getEdges().forEach(edge => {
        if(edge._cfg.model.I >= 1){
            edge.update(HIGH_INTENSITY_EDGE_MODEL);
        }else if(edge._cfg.model.I < I75){
            edge.update(LOW_INTENSITY_EDGE_MODEL);
        }
    })
}

function loadGraph(){
    graph.data(data);
    graph.render();
    levelLayout();
    handleReverseEdgesAndExtends()
    savePresentNodes();
    loadPanel();
    handleEdgesWidth();
    closeLoadingWindow();
}

graph.on('node:dragend', evt => {
    let edges = evt.item.getEdges();

    edges.forEach(edge =>{
        edge.clearStates('reverse');
        if(edge._cfg.source._cfg.model.y > edge._cfg.target._cfg.model.y){
            if(!edge._cfg.model.isExtendOrImplements)
                edge.setState('reverse', true);
        }else{
            if(edge._cfg.model.isTwoWayDependsOn){
                let edgeTmp = graph.findById(edge._cfg.model.target + "_" + edge._cfg.model.source);
                if(edgeTmp._cfg.model.isExtendOrImplements){
                    edge.setState('reverse', true);
                }
            }
        }
    })
    loadPanel();
})

graph.on('nodeselectchange', (e) => {
    // selected_packages = e.selectedItems.nodes;
    selected_packages.length = 0;
    e.selectedItems.nodes.forEach(node =>{
        selected_packages.push(node);
    })
});

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

if (typeof window !== 'undefined')
    window.onresize = () => {
        if (!graph || graph.get('destroyed')) return;
        if (!container || !container.scrollWidth || !container.scrollHeight) return;
        graph.changeSize(container.scrollWidth, container.scrollHeight);
    };