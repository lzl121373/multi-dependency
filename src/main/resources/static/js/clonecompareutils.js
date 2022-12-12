var showtree = function(divId, zNodes) {
    var setting = {
        check : {
            enable : false
        },
        data : {
            simpleData : {
                enable : true
            }
        }
    };
    console.log(zNodes);
    $.fn.zTree.init($("#" + divId), setting, zNodes);
}

var doublePackagesCloneShowMain = function(id1,id2,path1, path2, clonePairs, cloneNodesCount1, cloneNodesCount2, allNodesCount1, allNodesCount2, cloneMatchRate, cloneNodesLoc1, cloneNodesLoc2, allNodesLoc1, allNodesLoc2, cloneLocRate, cloneNodesCoChangeTimes, allNodesCoChangeTimes, cloneCoChangeRate, cloneType1Count, cloneType2Count, cloneType3Count, cloneType, cloneSimilarityValue, cloneSimilarityRate) {
    var html = "";
    html += "</table>";
    html += "<table class='table table-bordered'>";
    html += "<tr><th>目录</th><th>克隆文件占比</th><th>克隆CoChange占比</th><th>克隆Loc占比</th><th>克隆相似度</th><th>type_1数量</th><th>type_2数量</th><th>type_3数量</th><th>type</th><th>克隆对数</th></tr>";
    html += "<tr>";
    html += "<td>";
    html += "<a target='_blank' href='/relation/package/" + id1 + "'>" + path1;
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += "(" + cloneNodesCount1 + "+" + cloneNodesCount2 + ")/(" + allNodesCount1 + "+" + allNodesCount2 + ")=" + cloneMatchRate.toFixed(2);;
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += cloneNodesCoChangeTimes  + "/" + allNodesCoChangeTimes  + "=" + cloneCoChangeRate.toFixed(2);
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += "(" + cloneNodesLoc1 + "+" + cloneNodesLoc2 + ")/(" + allNodesLoc1 + "+" + allNodesLoc2 + ")=" + cloneLocRate.toFixed(2);
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += cloneSimilarityValue.toFixed(2) + "/(" + cloneType1Count + "+" + cloneType2Count + "+" + cloneType3Count + ")=" + cloneSimilarityRate.toFixed(2);
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += cloneType1Count;
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += cloneType2Count;
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += cloneType3Count;
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += cloneType;
    html += "</td>";
    html += "<td rowspan='2' style='vertical-align: middle'>";
    html += clonePairs;
    html += "</td>";
    html += "</tr>";
    html += "<tr>";
    html += "<td>";
    html += "<a target='_blank' href='/relation/package/" + id2 + "'>" + path2;
    html += "</td>";
    html += "</tr>";
    html += "</table>";
    $("#package_clone_detail").html(html);
}

/*
采用矩阵形式展示跨包克隆包内详细信息
*/
var doublePackagesCloneShowMatrix = function (pck1Id, pck2Id) {
    $.ajax({
        type: "get",
        url: "/clone/package/double/matrix?package1=" + pck1Id + "&package2=" + pck2Id,
        success: function(result) {
            console.log("success");
            var fileClone = result.fileClone;
            var cloneFiles1 = result.cloneFiles1;
            var cloneFiles2 = result.cloneFiles2;
            var matrix = result.matrix;
            var row = cloneFiles1.length;
            var col = cloneFiles2.length;
            var html = "";
            html += "<div style='overflow: auto;' width='100%' id='matrix'>";
            html += "<table class='table table-bordered' id='matrix_2'>";
            html += "<tr>";
            html += "<th style='background-color: #FFFFFF;'></th>";
            html += "<th style='background-color: #FFFFFF;'>file2</th>";
            for(var k = 1; k <= col; k++) {
                html += "<th style='background-color: #FFFFFF;'>";
                html += k;
                html += "</th>";
            }
            html += "</tr>";
            html += "<tr>";
            html += "<th style='background-color: #FFFFFF;'>file1</th>";
            html += "<th style='background-color: #FFFFFF;'></th>";
            for(var g = 0; g < col; g++) {
                html += "<th style='background-color: #FFFFFF;'>";
                html += "<a target='_blank' href='/relation/file/" + cloneFiles2[g].id + "'>" + cloneFiles2[g].name + "</a>(" + cloneFiles2[g].lines + ")";
                html += "</th>";
            }
            html += "</tr>";
            for(var i = 0; i < row; i++) {
                html += "<tr>";
                for(var j = -2; j < col; j++) {
                    html += "<td style='background-color: #FFFFFF;'>";
                    if(j === -2) {
                        html += i + 1;
                    }
                    else if(j === -1) {
                        html += "<a target='_blank' href='/relation/file/" + cloneFiles1[i].id + "'>" + cloneFiles1[i].name + "</a>(" + cloneFiles1[i].lines + ")";
                    }
                    else if(matrix[i][j] === true){
                        var key = cloneFiles1[i].name + "_" + cloneFiles2[j].name;
                        var type = fileClone[key].fileClone.cloneType;
                        var value = fileClone[key].fileClone.value.toFixed(2);
                        var cochangeId = fileClone[key].cochange == null ? -1 : fileClone[key].cochange.id;
                        var cochangeTimes = cochangeId === -1 ? 0 : fileClone[key].cochange.times;
                        var linesSize1 = fileClone[key].fileClone.linesSize1;
                        var linesSize2 = fileClone[key].fileClone.linesSize2;
                        var loc1 = fileClone[key].fileClone.loc1;
                        var loc2 = fileClone[key].fileClone.loc2;
                        html += "(";
                        html += "<a target='_blank' href='/clone/file/double" +
                            "?file1Id=" + cloneFiles1[i].id +
                            "&file2Id=" + cloneFiles2[j].id +
                            "&cloneType=" + type +
                            "&linesSize1=" + linesSize1 +
                            "&linesSize2=" + linesSize2 +
                            "&loc1=" + loc1 +
                            "&loc2=" + loc2 +
                            "&value=" + value +
                            "&cochange=" + cochangeTimes +
                            "&filePath1=" + cloneFiles1[i].path +
                            "&filePath2=" + cloneFiles2[j].path +
                            "&cochangeId=" + cochangeId +
                            "'>" + type + "</a>";
                        html += ", ";
                        html += "<a target='_blank' href='/clone/compare?id1=" + cloneFiles1[i].id + "&id2=" + cloneFiles2[j].id +"'>" + value + "</a>";
                        html += ", ";
                        html += "<a class='cochangeTimes' target='_blank' href='/commit/cochange?cochangeId=" + cochangeId + "'>" + cochangeTimes + "</a>";
                        html += ")";
                    }
                    html += "</td>";
                }
                html += "</tr>";
            }
            html += "</table>";
            html += "</div>";
            $("#package_files_clone_matrix").html(html);
            $("#matrix").scroll(function(){
                $("#matrix tr th").css({"position":"relative","top":$("#matrix").scrollTop(),"z-index":"2","border":"1"});
                $("#matrix tr td:nth-child(1)").css({"position":"relative","left":$("#matrix").scrollLeft(),"z-index":"1","border":"1"});
                $("#matrix tr td:nth-child(2)").css({"position":"relative","left":$("#matrix").scrollLeft(),"z-index":"1","border":"1"});
                $("#matrix tr th:nth-child(1)").css({"position":"relative","top":$("#matrix").scrollTop(),"left":$("#matrix").scrollLeft(),"z-index":"3","border":"1"});
                $("#matrix tr th:nth-child(2)").css({"position":"relative","top":$("#matrix").scrollTop(),"left":$("#matrix").scrollLeft(),"z-index":"3","border":"1"});
            });
        }
    })
}

/*
跨包克隆包内详细信息展示
*/
var doublePackagesCloneShowDetail = function(pck1Id, pck2Id) {
    $.ajax({
        type: "get",
        url: "/clone/package/double/cochange?package1=" + pck1Id + "&package2=" + pck2Id,
        success: function(result) {
            console.log("success");
            var html = "";
            html += "<table class='table table-bordered'>" + "<tr><th>file1</th><th>file2</th><th>type</th><th>value</th><th>cochange</th></tr>";
            var children = result.children;
            for(var i = 0; i < children.length; i++) {
                var cochangeId = children[i].cochange == null ? -1 : children[i].cochange.id;
                html += "<tr>";
                html += "<td>";
                html += "<a target='_blank' href='/relation/file/" + children[i].file1.id + "'>" + children[i].file1.path + " (" + children[i].file1.lines + ")</a>";
                html += "</td>";
                html += "<td>";
                html += "<a target='_blank' href='/relation/file/" + children[i].file2.id + "'>" + children[i].file2.path + " (" + children[i].file2.lines + ")</a>";
                html += "</td>";
                html += "<td>";
                var type = children[i].fileClone.cloneType;
                var linesSize1 = children[i].fileClone.linesSize1;
                var linesSize2 = children[i].fileClone.linesSize2;
                var loc1 = children[i].fileClone.loc1;
                var loc2 = children[i].fileClone.loc2;
                var value = children[i].fileClone.value;
                html += "<a target='_blank' href='/clone/file/double" +
                    "?file1Id=" + children[i].file1.id +
                    "&file2Id=" + children[i].file2.id +
                    "&cloneType=" + type +
                    "&linesSize1=" + linesSize1 +
                    "&linesSize2=" + linesSize2 +
                    "&loc1=" + loc1 +
                    "&loc2=" + loc2 +
                    "&value=" + value +
                    "&cochange=" + children[i].cochangeTimes +
                    "&filePath1=" + children[i].file1.path +
                    "&filePath2=" + children[i].file2.path +
                    "&cochangeId=" + cochangeId +
                    "'>" + type + "</a>";
                html += "</td>";
                html += "<td>";
                html += "<a target='_blank' href='/clone/compare?id1=" + children[i].file1.id + "&id2=" + children[i].file2.id +"'>" + children[i].fileClone.value + "</a>";
                html += "</td>";
                html += "<td>";
                html += "<a class='cochangeTimes' target='_blank' href='/commit/cochange?cochangeId=" + cochangeId + "'>" + children[i].cochangeTimes + "</a>";
                html += "</td>";
                html += "</tr>";
            }
            html += "</table>";
            $("#package_files_clone").html(html);
        }
    })
}

var doublePackagesCloneShowTree = function(pck1Id, pck2Id) {
    $.ajax({
        type: "get",
        url: "/clone/package/double/filetree?package1=" + pck1Id + "&package2=" + pck2Id,
        success: function (result) {
            console.log("success");
            var html = "";
            html += "<div class='div_file1'>";
            html += "<ul id='tree_file1' class='ztree'>";
            html += "</ul>";
            html += "</div>";
            html += "<div class='div_file2'>";
            html += "<ul id='tree_file2' class='ztree'>"
            html += "</ul>";
            html += "</div>";
            $("#package_files_tree").html(html);
            var data1 = result[pck1Id];
            showtree("tree_file1", data1);
            var data2 = result[pck2Id];
            showtree("tree_file2", data2);
        }
    });
}

/*
显示跨包克隆包内克隆文件圆形图对比
*/
var cloneGroupToGraphShowDetail = function (pck1Id, pck2Id) {
    $.ajax({
        type: "get",
        url: "/clone/package/double/graph?package1=" + pck1Id + "&package2=" + pck2Id,
        success: function(result) {
            console.log("success");
            cloneGroupToGraph(result, "fileClonesGraph");
        }
    });
}

/*
跨包克隆包内克隆文件圆形图对比
*/
var cloneGroupToGraph = function(result, divId) {
    //设置数组
    var clonedata = result

    var diameter = 1800,
        radius = diameter / 2,
        innerRadius = radius - 300;

    var cluster = d3.layout.cluster()
        .size([360, innerRadius])
        .sort(null)
        .value(function(d) { return d.size; });

    var bundle = d3.layout.bundle();

    var line = d3.svg.line.radial()
        .interpolate("bundle")
        .tension(.85)
        .radius(function(d) { return d.y; })
        .angle(function(d) { return d.x / 180 * Math.PI; });


    var svg = d3.select("#" + divId).append("svg")
        .attr("width", diameter)
        .attr("height", diameter)
        .attr("id", "svg1")
        .append("g")
        .attr("transform", "translate(" + radius + "," + radius + ")");


    var link = svg.append("g").selectAll(".link"),
        node = svg.append("g").selectAll(".node");

    //设置数组读取数据
    var nodes = cluster.nodes(packageHierarchy(clonedata)),
        links = packageImports(nodes);
    link = link
        .data(bundle(links))
        .enter().append("path")
        .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
        .attr("class", "link")
        .attr("d", line);

    node = node
        .data(nodes.filter(function(n) { return !n.children; }))
        .enter().append("text")
        .attr("class", "node")
        .attr("dy", ".31em")
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
        .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
        .text(function(d) { return d.key; })
        .on("mouseover", mouseovered)
        .on("mouseout", mouseouted)
        .call(text => text.append("title").text(function(d) { return d.key; }));

    String.prototype.replaceAt=function(index, replacement) {
        return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
    }

    String.prototype.replaceAll = function(search, replacement) {
        var target = this;
        return target.replace(new RegExp(search, 'g'), replacement);
    };

    var legendRectSize = 18;
    var legendSpacing = 4;
    var legend = d3.select('svg')
        .append("g")
        .selectAll("g")
        .append('g')
        .attr('class', 'legend')
        .attr('transform', function(d, i) {
            var height = legendRectSize;
            var x = 0;
            var y = (i+1) * height;
            return 'translate(' + x + ',' + y + ')';
        });
    d3.select('svg')
        .select("g:nth-child(0)").append('text').text("Component Colors:");

    legend.append('rect')
        .attr('width', legendRectSize)
        .attr('height', legendRectSize)
    legend.append('text')
        .attr('x', legendRectSize + legendSpacing)
        .attr('y', legendRectSize - legendSpacing)
        .text(function(d) { return d; });

    function mouseovered(d) {
        node
            .each(function(n) { n.target = n.source = false; });
        link
            .classed("link--target", function(l) { if (l.target === d) return l.source.source = true; })
            .classed("link--source", function(l) { if (l.source === d) return l.target.target = true; })
            .filter(function(l) { return l.target === d || l.source === d; })
            .style("stroke", "#e0230a")
            .each(function() { this.parentNode.appendChild(this); });
        node
            .classed("node--target", function(n) { return n.target; })
            .classed("node--source", function(n) { return n.source; });
    }

    function mouseouted(d) {
        link
            .classed("link--target", false)
            .classed("link--source", false)
            .style("stroke", 'DarkGray');

        node
            .classed("node--target", false)
            .classed("node--source", false);

    }
    d3.select(self.frameElement).style("height", diameter + "px");

    function packageHierarchy(classes) {
        var map = {};
        function find(name, data) {
            var node = map[name], i;
            if (!node) {
                node = map[name] = data || {name: name, children: []};
                if (name.length) {
                    node.parent = find(name.substring(0, i = name.lastIndexOf("/")));
                    node.parent.children.push(node);
                    node.key = name.substring(i + 1);
                }
            }
            return node;
        }
        classes.forEach(function(d) {
            find(d.name, d);
        });

        return map[""];
    }

    function packageImports(nodes) {
        var map = {},
            imports = [];
        nodes.forEach(function(d) {
            map[d.name] = d;
        });
        nodes.forEach(function(d) {
            if (d.imports) d.imports.forEach(function(i) {
                imports.push({source: map[d.name], target: map[i]});
            });
        });

        return imports;
    }
}
/*
跨包克隆包内克隆文件圆形图对比2
*/
var cloneGroupToGraph2 = function(result,divId){
    vis = divId;
    const config = { titleHeight: 45, titlePosition: [18, 28] },
        data = {},
        results = {};

    /* mutable */
    var width,
        height,
        clicked = -1;

    /*
     * Colors & CSS
     */
    const colorCategory = [
        { name: "purple", hex: "#db2872" },
        { name: "green", hex: "#b5c92d" },
        { name: "blue", hex: "#3e9ad6" },
        { name: "orange", hex: "#e45c1e" }
    ].map(e => e.hex);

    function svgcss() {
        return `
svg text, button {
    -webkit-user-select: none;
       -moz-user-select: none;
        -ms-user-select: none;
            user-select: none;
}
svg text::selection {
    background: none;
}

    text { font-family: flama, sans-serif; font-weight: bold; }

    #maintitle rect, .maintitle rect { fill: none; }
    #maintitle text, .maintitle text { fill: #555; font-size: 1em; font-family: flama, sans-serif; font-weight: bold;  }

    g.tooltip {}
    g.tooltip rect.background {fill: #000000; stroke: #333333; fill-opacity: 0.8}
    g.tooltip text.text {font-family: flama, sans-serif; font-weight: bold; font-size: 12px;}
    g.tooltip text.text tspan.title {fill: #ffffff;}
    g.tooltip text.text tspan.detail {fill: #aaaaaa;}

`;
    }
    function svgshadowfilter() {
        return `
        <filter id="drop-shadow">
          <feDropShadow dx="1" dy="1" stdDeviation="1" flood-color="#000000" flood-opacity="0.5">
          </feDropShadow>
        </filter>
`;
    }

    /*
     * Load and parse data
     */

    async function loadRawData() {
        return await Promise.all([
            // d3.text(`data/science2005links_mo.txt`),
            // d3.text(`data/science2005nodes_mo.txt`),
            // d3.text(`data/science2005tree_mo.txt`)
            // d3.text(`data/links.txt`),
            // d3.text(`data/nodes.txt`),
            // d3.text(`data/ptree.txt`)
            // d3.text(`data/2links.txt`),
            // d3.text(`data/2nodes.txt`),
            // d3.text(`data/2ptree.txt`)
            // d3.json("../static/data/testpackages.json")
            d3.select("#" + vis)
        ]);
    }

    (async function() {
        let _links;
        let ptree = "", _nodes2 = "";
        _links = await loadRawData();

        var clonedata = result
        var nametoid = new Map();
        const projectid = new Map();
        const packageid = new Map();
        const fileid = new Map();
        const treelist = new Set();
        const packagetofile = new Map();
        let proid = 1;
        let pckid = 1;
        let _links2 = "";
        const project = new Set();
        const package = new Set();
        clonedata.forEach(function(d) {
            let splitlist = d.name.split("/");
            _nodes2 += d.id + "," + d.name + "," + d.name + "\n";
            project.add(splitlist[1]);
            package.add(d.name.replace(splitlist[splitlist.length - 1],""));
            nametoid.set(splitlist[splitlist.length - 1], d.id);
        });

        project.forEach(function (d) {
            projectid.set(d, proid);
            proid += 1;
        })

        package.forEach(function (d) {
            const id = projectid.get(d.split("/")[1]);
            packageid.set(d, id + ":" + pckid);
            pckid += 1;
        })


        clonedata.forEach(function(d) {
            let splitlist = d.name.split("/");
            const packagename = d.name.replace(splitlist[splitlist.length - 1],"");
            const packagetofileid = packagetofile.get(packagename);

            if(!packagetofileid){
                fileid.set(d.name, packageid.get(packagename) + ":" + 1);
                packagetofile.set(packagename, 1);
            }else{
                const id = packagetofileid + 1;
                fileid.set(d.name, packageid.get(packagename) + ":" + id);
                packagetofile.set(packagename, id);
            }
        });

        clonedata.forEach(function(d) {
            if (d.imports)d.imports.forEach(function (c) {
                let splitlist = c.split("/");
                _links2 += d.id + "," + nametoid.get(splitlist[splitlist.length - 1]) + "\n";
            })
        });

        fileid.forEach(function (value,key) {
            let splitlist = key.split("/");
            let projectname = splitlist[1];
            let filename = splitlist[splitlist.length - 1];
            let packagename = key.replace(filename,"");
            let id1 = projectid.get(projectname);
            let id2 = packageid.get(packagename);

            for (i = 0; i < id1; i++){
                if (!treelist.has(id1)){
                    ptree += id1 + ",0.1," + projectname + "\n";
                    treelist.add(id1);
                }
            }
            for (j = 0; j < Number(id2.split(":")[1]); j++){
                if (!treelist.has(id2)){
                    ptree += id2 + ",0.1," + packagename + "\n";
                    treelist.add(id2);
                }
            }
            ptree += value + ",0.1," + key + "," + key + "\n";
        })

        // _links2 = d3.map(x,function(d){return d.id})
        // console.log(_links2)

        // data.flowEdges = flowEdges(_links);
        // data.ids = ids(_nodes);
        // data.IDsByName = IDsByName(data.ids);
        // data.tree = tree(_tree, data.IDsByName);
        // console.log(_links)
        // console.log(_nodes)
        // console.log(_tree)

        data.flowEdges = flowEdges(_links2);
        data.ids = ids(_nodes2);
        data.IDsByName = IDsByName(data.ids);
        data.tree = tree(ptree, data.IDsByName);

        // data.IDsByName = nametoid;

        d3.select(window).on("resize", redraw);
        redraw();
    })();

    var wait = 20;
    function redraw() {
        width = window.innerWidth;
        height = Math.min(window.innerHeight, width);

        if (typeof buildchart !== "function") {
            console.log("Waiting for buildchart to load…", wait);
            if (wait-- > 0) setTimeout(redraw, 1000);
        } else {
            buildchart();
        }
    }

    /*
     * Parsing
     */
    function flowEdges(links) {
        const l = d3
            .dsvFormat(",")
            .parseRows(links)
            .map(d => ({
                source: d[0],
                target: d[1],
                weight: 0.1
            }));
        const maxEdgeWeight = l.reduce(
            (a, c) => ("weight" in c && c.weight > a ? c.weight : a),
            0
        );
        return l.map(e => {
            if ("weight" in e) {
                e.normalizedWeight =
                    Math.log(1 + (e.weight / maxEdgeWeight) * 10) / Math.log(11);
            }
            return e;
        });
    }

    function ids(_nodes) {
        return d3
            .dsvFormat(",")
            .parseRows(_nodes)
            .map(e => {
                return {
                    id: e[0],
                    name: e[1],
                    longName: e[2]
                };
            });
    }

    function IDsByName(ids) {
        return ids.reduce((a, c) => {
            a.set(c.name, c.id);
            return a;
        }, new Map());
    }

    function tree(_tree, _idsByName) {
        const root = [{ path: "_", label: "root", value: 1 }];
        const tree = d3
            .dsvFormat(",")
            .parseRows(_tree)
            .map(e => {
                const cell = {
                    path: e[0],
                    label: e[2]
                };
                if (e.length == 4) {
                    cell.longLabel = e[3];
                    cell.eigenfactor = parseFloat(e[1]);
                } else {
                    cell.parentEigenfactor = parseFloat(e[1]);
                }
                cell.id = _idsByName.get(cell.label)
                    ? _idsByName.get(cell.label)
                    : "p_" + cell.path.split(":").join(",");
                cell.parentPath = cell.path
                    .split(":")
                    .slice(0, -1)
                    .join(":");
                if (cell.parentPath === "") cell.parentPath = "_";
                return cell;
            })
            .concat(root);
        const maxEigenfactor = tree.reduce(
            (a, c) => ("eigenfactor" in c && c.eigenfactor > a ? c.eigenfactor : a),
            0
        );
        return tree.map(e => {
            if ("eigenfactor" in e) {
                e.weight = e.eigenfactor / maxEigenfactor;
                e.logWeight = Math.log(1 + e.weight * 10) / Math.log(11);
            }
            return e;
        });
    }

    /*
     * Utilities
     */
// We cannot use format = d3.format(".6f"), because the original implementation is slighlty different
    function cutAfter(n, d) {
        let s = n.toString();
        let dotPos = s.indexOf(".");
        if (dotPos === -1) {
            s += ".";
            dotPos = s.length - 1;
        }
        return s.length - dotPos < d
            ? s + "0".repeat(d - s.length + dotPos + 1)
            : s.substring(0, dotPos + d + 1);
    }

// https://github.com/observablehq/notebook-stdlib/blob/master/src/dom/uid.js
    DOM = (function() {
        var count = 0;
        function uid(name) {
            return new Id("O-" + (name == null ? "" : name + "-") + ++count);
        }
        function Id(id) {
            this.id = id;
            this.href = window.location.href + "#" + id;
        }
        Id.prototype.toString = function() {
            return "url(" + this.href + ")";
        };
        return { uid };
    })();

    /* Inspired by https://github.com/d3/d3-interpolate/blob/master/src/rgb.js */
    function interpolateRgbFloor(a, b, t) {
        function linear(a, d) {
            return function(t) {
                return a + t * d;
            };
        }
        function constant(x) {
            return function() {
                return x;
            };
        }
        function nogamma(a, b) {
            var d = b - a;
            return d ? linear(a, d) : constant(isNaN(a) ? b : a);
        }
        function rgb(start, end) {
            var r = nogamma((start = d3.rgb(start)).r, (end = d3.rgb(end)).r),
                g = nogamma(start.g, end.g),
                b = nogamma(start.b, end.b),
                opacity = nogamma(start.opacity, end.opacity);
            return function(t) {
                start.r = Math.floor(r(t));
                start.g = Math.floor(g(t));
                start.b = Math.floor(b(t));
                start.opacity = opacity(t);
                return start + "";
            };
        }
        return rgb(a, b, t);
    }

    function getColorByIndexAndWeight({
                                          index,
                                          weight = 0,
                                          MIN_SAT = 0.3,
                                          MAX_SAT = 0.9,
                                          MIN_BRIGHTNESS = 0.85,
                                          MAX_BRIGHTNESS = 0.5
                                      }) {
        const baseColor = colorCategory[index - 1];
        const hue = d3.hsl(baseColor).h;
        const w = Math.max(0, Math.min(1, weight));
        minColor = hue => d3.hsv(hue, MIN_SAT, MIN_BRIGHTNESS);
        maxColor = hue => d3.hsv(hue, MAX_SAT, MAX_BRIGHTNESS);
        /* We have to use a custom interpolation function to reproduce
         * the Flare RGB color interpolation, since they approximate the
         * RGB channels with the "floor" function, and d3 with the "round"
         * function. */
        const palette = interpolateRgbFloor(minColor(hue), maxColor(hue));
        return palette(w);
    }

    function tooltip(
        id,
        w,
        h,
        title,
        text1 = "",
        text2 = "",
        value1 = "",
        value2 = ""
    ) {
        const cursor = d3.mouse(d3.select("svg").node());
        d3.selectAll(".tooltip").remove();
        const tooltip = d3
            .select("g#tooltip")
            .append("g")
            .classed("tooltip", true)
            .attr("id", id);

        const rect = tooltip
            .append("rect")
            .classed("background", true)
            .attr("x", 0)
            .attr("y", 0)
            .style("filter", "url(#drop-shadow)");

        const text = tooltip
            .append("text")
            .classed("text", true)
            .attr("x", 0)
            .attr("y", 0);

        function appendTspan(t, c, x, dy, text) {
            if (text !== "")
                t.append("tspan")
                    .classed(c, true)
                    .attr("x", x)
                    .attr("dy", dy)
                    .text(text);
        }

        appendTspan(text, "title", 5, "1em", title);
        appendTspan(text, "detail", 5, "1.2em", text1);
        appendTspan(text, "detail", "4.5em", 0, value1);
        appendTspan(text, "detail", 5, "1em", text2);
        appendTspan(text, "detail", "4.5em", 0, value2);

        /* Position */
        const bbox = text.node().getBBox();
        rect
            .attr("width", bbox.width + 10)
            .attr("height", bbox.height + 11)
            .attr("y", -4);

        /* Manage the bottom and right edges */
        let x = cursor[0];
        let y = cursor[1] + 26;
        if (x + bbox.width + 8 + 2 > w) x = x - bbox.width - 10;
        if (y + bbox.height + 26 + 2 > h) y = y - bbox.height - 10 - 26;
        tooltip.attr("transform", `translate(${x},${y})`);
    }


    const svgcssradial = `
g#links path.link { fill: none; pointer-events: none; }
g#links path.link.colour { mix-blend-mode: multiply }
g#labels text.label { font-weight: normal; font-size: 10px; mix-blend-mode: darken; }

g#innerArcs path.innerArc.clicked { fill: #222222 }
g#innerArcs path.innerArc.unlinked { fill: #DDDDDD }
g#innerArcs path.innerArc:hover { fill: #444444; }

g#outerArcs path.outerArc.clicked { fill: #222222 }
g#outerArcs path.outerArc:hover { fill: #444444; }
g#outerArcs path.outerArc.unlinked { fill: #DDDDDD; }
`;

    config.titleHeight = 35;

    /*
     * Build the chart
     */
    function buildchart() {
        results.radius = Math.min(width, height - config.titleHeight) / 2 - 120;
        const initialAngle = Math.PI / 5; /* TODO: understand why */
        results.maxLinks = 1000;

        results.radialData = centerChildNodes(
            stratifyTree(data.tree).sum(d => d.eigenfactor)
        );
        results.radialData = addAngleAndRadius(
            results.radialData,
            results.radius,
            initialAngle,
            results.radialData.value
        );
        results.radialData = results.radialData.each(addNodeColor);

        results.leavesData = results.radialData.leaves();
        results.groupsData = results.radialData
            .descendants()
            .filter(d => d.depth == 2);

        results.radialDataLookup = results.radialData.descendants().reduce((a, c) => {
            a[c.data.id] = c;
            return a;
        }, {});
        results.linksData = data.flowEdges.map(link => {
            return {
                source: results.radialDataLookup[link.source],
                target: results.radialDataLookup[link.target],
                weight: link.weight,
                normalizedWeight: link.normalizedWeight
            };
        });
        results.linksLookup = new Map();

        const svg = d3
            .select("svg")
            .attr("width", width)
            .attr("height", height + config.titleHeight)
            .html(
                `
      <defs>
        <style type="text/css">${svgcss()}${svgcssradial}</style>
        ${svgshadowfilter()}
      </defs>
      `
            );

        svg
            .append("rect")
            .attr("id", "main")
            .attr("width", "100%")
            .attr("height", "100%")
            .attr("fill", "#f0f0f0")
            .attr("fill-opacity", 0)
            .on("click", goToNormalState);

        const graph = d3.select("#" + vis)
            .append("g")
            .attr("id", "vis") // was: "radial"
            .attr("transform", `translate(${[width / 2, height / 2]})`);

        graph.append("g").attr("id", "all");
        // graph.append("g").attr("id", "tooltip");

        const title = svg.append("g").attr("id", "maintitle");
        title.append("rect").attr("height", config.titleHeight);
        title
            .append("text")
            .attr("letter-spacing", ".05em")
            .attr("transform", `translate(${config.titlePosition})`);

        svg.append("g").attr("id", "tooltip");

        goToNormalState();

        return svg.node();
    }

    /*
     * Interaction functions
     */

    function handleMouseOver(d, i) {
        // Ugly, will fix later
        const label = "longLabel" in d.data ? d.data.longLabel : d.data.label;
        const value =
            "eigenfactor" in d.data ? d.data.eigenfactor : d.data.parentEigenfactor;

        if (clicked === -1 || clicked.data.id === d.data.id) {
            tooltip(
                "t-" + i,
                width,
                height,
                "Project: " + label.split("/")[1],
                label
            );
        } else {
            const inout = getInOut(d, clicked);
            tooltip(
                "t-" + i,
                width,
                height,
                label
            );
        }
    }

    function calcInDepth3(source, target) {
        const v = data.flowEdges
            .filter(l => l.source === source.data.id && l.target === target.data.id)
            .map(l => l.normalizedWeight);
        return v.length === 1 ? v[0] : 0;
    }

    function getInOut(source, target) {
        const lo = results.linksLookup;
        const sId = source.data.id;
        const tId = target.data.id;

        if (!lo.has(sId)) lo.set(sId, new Map());

        if (!lo.get(sId).has(tId)) {
            if (lo.has(tId) && lo.get(tId).has(sId)) {
                lo.get(sId).set(tId, {
                    in: lo.get(tId).get(sId).out,
                    out: lo.get(tId).get(sId).in
                });
            } else if (source.depth === 3 && target.depth === 3) {
                lo.get(sId).set(tId, {
                    in: calcInDepth3(source, target),
                    out: calcInDepth3(target, source)
                });
            } else if ("children" in target) {
                lo.get(sId).set(
                    tId,
                    target.children.reduce(
                        (a, c) => {
                            const inout = getInOut(source, c);
                            a.in += inout.in;
                            a.out += inout.out;
                            return a;
                        },
                        { in: 0, out: 0 }
                    )
                );
            } else {
                const inout = getInOut(target, source);
                lo.get(sId).set(tId, {
                    in: inout.out,
                    out: inout.in
                });
            }
        }

        return lo.get(sId).get(tId);
    }

    function handleMouseOut(d, i) {
        d3.select("#t-" + i).remove();
    }

    function handleClick(arc, i) {
        /* TODO: Be more consistent and careful with the state management.
         * Maybe use an array, or a null node placeholder */
        if (clicked !== -1 && arc.data.id === clicked.data.id) {
            goToNormalState();
        } else {
            goToSelectedState(arc);
        }
    }

    function goToNormalState() {
        clicked = -1;
        setTitle("");

        const g = d3.select("g#vis");
        g.select("g#innerArcs").remove();
        g.select("g#outerArcs").remove();
        g.select("g#labels").remove();
        g.select("g#links").remove();

        drawOuterArcs(
            g.append("g").attr("id", "outerArcs"),
            results.groupsData,
            results.radius
        );
        drawInnerArcs(
            g.append("g").attr("id", "innerArcs"),
            results.leavesData,
            results.radius
        );
        drawOuterArcsLabels(
            g.append("g").attr("id", "labels"),
            results.groupsData
                .filter(
                    group =>
                        group.data.parentEigenfactor > 0.005 &&
                        group.data.label !== "NO SUGGESTION"
                ) // show label if large enough, and there is in fact one
                .map(d => {
                    return {
                        angle: (((180 / Math.PI) * (d.startAngle + d.endAngle)) / 2) % 360,
                        text: d.data.label,
                        fill: "#888888"
                    };
                }),
            results.radius
        );
        drawLinks(
            g.append("g").attr("id", "links"),
            results.linksData
                .sort((a, b) => b.normalizedWeight > a.normalizedWeight)
                .slice(0, results.maxLinks),
            getGrayLinkColor
        );
    }

    function goToSelectedState(arc) {
        if (arc.depth === 3) selectInnerArc(arc);
        else selectOuterArc(arc);
        clicked = arc;
    }

    function selectInnerArc(arc) {
        setTitle(arc.data.longLabel);

        const innerArcs = d3.selectAll("svg .innerArc");
        innerArcs.classed("clicked", d => d.data.id === arc.data.id);

        const localWeights = new Map([[arc.data.id, 1]]);
        data.flowEdges
            .filter(link => link.source === arc.data.id)
            .forEach(l =>
                localWeights.set(
                    l.target,
                    (localWeights.has(l.target) ? localWeights.get(l.target) : 0) +
                    l.normalizedWeight
                )
            );
        data.flowEdges
            .filter(link => link.target === arc.data.id)
            .forEach(l =>
                localWeights.set(
                    l.source,
                    (localWeights.has(l.source) ? localWeights.get(l.source) : 0) +
                    l.normalizedWeight
                )
            );
        innerArcs.classed(
            "unlinked",
            d => !localWeights.has(d.data.id) && d.data.id !== arc.data.id
        );
        innerArcs.filter(d => localWeights.has(d.data.id)).attr("fill", d => {
            return getColorByIndexAndWeight({
                index: +d.parent.parent.id,
                weight: localWeights.get(d.data.id),
                MIN_SAT: 0.4,
                MAX_SAT: 0.95,
                MIN_BRIGHTNESS: 0.8,
                MAX_BRIGHTNESS: 0.5
            });
        });

        /* Outer arcs */
        d3.select("g#outerArcs")
            .selectAll(".outerArc")
            .classed("unlinked", true);

        /* Links */
        const links = d3.select("g#links").remove();
        drawLinks(
            d3
                .select("g#vis")
                .append("g")
                .attr("id", "links"),
            results.linksData.filter(
                l => l.source.data.id === arc.data.id || l.target.data.id === arc.data.id
            ),
            link => {
                const color = d3.rgb(
                    getColorByIndexAndWeight({
                        index: +link.source.parent.parent.id,
                        weight: link.normalizedWeight,
                        MIN_SAT: 0.4,
                        MAX_SAT: 0.95,
                        MIN_BRIGHTNESS: 0.8,
                        MAX_BRIGHTNESS: 0.5
                    })
                );
                color.opacity = 0.3 + 0.6 * link.normalizedWeight;
                return color;
            },
            true
        );

        /* Labels */
        d3.select("g#labels").remove();
        drawInnerArcsLabels(
            d3
                .select("g#vis")
                .append("g")
                .attr("id", "labels"),
            results.leavesData
                .filter(d => {
                    return localWeights.has(d.data.id) || d.data.id === arc.data.id;
                })
                .map(d => {
                    const brightness =
                        221 - Math.min(153, Math.floor(localWeights.get(d.data.id) * 153));
                    const fill = d3.rgb(brightness, brightness, brightness).toString();
                    return {
                        angle: (((180 / Math.PI) * (d.startAngle + d.endAngle)) / 2) % 360,
                        text: d.data.label,
                        fill: d.data.id === arc.data.id ? "#222222" : fill
                    };
                }),
            results.radius
        );
    }

    function selectOuterArc(arc) {
        setTitle(arc.data.label);

        const childrenIds = arc.children.map(e => e.data.id);

        const innerArcs = d3.selectAll("g#innerArcs .innerArc");
        innerArcs.classed("clicked", false);

        const localWeights = new Map([[arc.data.id, 1]]);
        data.flowEdges
            .filter(link => childrenIds.includes(link.source))
            .forEach(l =>
                localWeights.set(
                    l.target,
                    (localWeights.has(l.target) ? localWeights.get(l.target) : 0) +
                    l.normalizedWeight
                )
            );
        data.flowEdges
            .filter(link => childrenIds.includes(link.target))
            .forEach(l =>
                localWeights.set(
                    l.source,
                    (localWeights.has(l.source) ? localWeights.get(l.source) : 0) +
                    l.normalizedWeight
                )
            );
        innerArcs.classed("unlinked", d => !localWeights.has(d.data.id));
        innerArcs.filter(d => localWeights.has(d.data.id)).attr("fill", d => {
            return getColorByIndexAndWeight({
                index: +d.parent.parent.id,
                weight: localWeights.get(d.data.id),
                MIN_SAT: 0.4,
                MAX_SAT: 0.95,
                MIN_BRIGHTNESS: 0.8,
                MAX_BRIGHTNESS: 0.5
            });
        });

        // Outer arcs
        d3.select("g#outerArcs")
            .selectAll(".outerArc")
            .classed("unlinked", d => d.data.id !== arc.data.id)
            .classed("clicked", d => d.data.id === arc.data.id);

        // Links
        const links = d3.select("g#links").remove();
        const linksData = results.linksData.filter(
            l =>
                childrenIds.includes(l.source.data.id) ||
                childrenIds.includes(l.target.data.id)
        );

        drawLinks(
            d3
                .select("g#vis")
                .append("g")
                .attr("id", "links"),
            linksData,
            link => {
                const color = d3.rgb(
                    getColorByIndexAndWeight({
                        index: +link.source.parent.parent.id,
                        weight: link.normalizedWeight,
                        MIN_SAT: 0.4,
                        MAX_SAT: 0.95,
                        MIN_BRIGHTNESS: 0.8,
                        MAX_BRIGHTNESS: 0.5
                    })
                );
                color.opacity = 0.3 + 0.6 * link.normalizedWeight;
                return color;
            },
            true
        );

        // Labels
        d3.select("g#labels").remove();
        const g = d3
            .select("g#vis")
            .append("g")
            .attr("id", "labels");
        drawOuterArcsLabels(
            g,
            results.groupsData.filter(d => d.data.id === arc.data.id).map(d => {
                return {
                    angle: (((180 / Math.PI) * (d.startAngle + d.endAngle)) / 2) % 360,
                    text: d.data.label,
                    fill: "#222222"
                };
            }),
            results.radius
        );
        drawInnerArcsLabels(
            g,
            results.leavesData
                .filter(d => {
                    return localWeights.has(d.data.id) && d.parent.data.id !== arc.data.id;
                })
                .map(d => {
                    const brightness =
                        221 - Math.min(153, Math.floor(localWeights.get(d.data.id) * 153));
                    const fill = d3.rgb(brightness, brightness, brightness).toString();
                    return {
                        angle: (((180 / Math.PI) * (d.startAngle + d.endAngle)) / 2) % 360,
                        text: d.data.label,
                        fill: fill
                    };
                }),
            results.radius
        );
    }

    function setTitle(title) {
        const text = d3.select("svg #maintitle text");
        text.text(title);
        const w = text.node().getBBox().width;
        d3.select("svg #maintitle rect").attr("width", !w ? 0 : w + 2 * 9);
    }

    /*
     * Graphical functions
     */

    function drawInnerArcs(g, data, radius) {
        return g
            .selectAll("path")
            .data(data)
            .enter()
            .append("path")
            .classed("innerArc", true)
            .attr("id", d => d.data.id)
            .attr("d", innerArc(radius))
            .attr("fill", d => d.color)
            .on("mousemove", handleMouseOver)
            .on("mouseout", handleMouseOut)
            .on("click", handleClick);
    }

    function drawOuterArcs(g, data, radius) {
        return g
            .selectAll("path")
            .data(data)
            .enter()
            .append("path")
            .classed("outerArc", true)
            .attr("id", d => d.data.id)
            .attr("d", outerArc(radius))
            .attr("fill", d => d.color)
            .on("mousemove", handleMouseOver)
            .on("mouseout", handleMouseOut)
            .on("click", handleClick);
    }

    function drawLinks(g, linksData, colorFn, colourClass = false) {
        return g
            .selectAll("path")
            .data(linksData)
            .enter()
            .append("path")
            .attr("class", "link")
            .attr("d", link => {
                const path = moveEdgePoints(link.source.path(link.target));
                return line(path);
            })
            .attr(
                "stroke-width",
                d => (1 + 5 * d.normalizedWeight) / (colourClass ? 1 : 2)
            )
            .attr("stroke", d => colorFn(d))
            .classed("colour", colourClass);
    }

    function moveEdgePoints(path) {
        const source = path[0];
        const target = path[path.length - 1];
        let delta = ((source.centerAngle - target.centerAngle) / (2 * Math.PI)) % 1;
        if (delta < 0) delta += 1;
        path[0] = {
            radius: source.radius,
            centerAngle: source.centerAngle + source.angleWidth * (delta - 0.5)
        };
        path[path.length - 1] = {
            radius: target.radius,
            centerAngle: target.centerAngle + target.angleWidth * (0.5 - delta)
        };
        return path;
    }

    function drawInnerArcsLabels(g, labels, radius) {
        drawLabels(g, labels, radius);
    }
    function drawOuterArcsLabels(g, labels, radius) {
        drawLabels(g, labels, radius + 14);
    }
    function drawLabels(g, labels, radius) {
        return g
            .selectAll("text")
            .data(labels)
            .enter()
            .append("text")
            .attr("class", "label")
            .attr("dy", "0.31em")
            .attr("transform", function(d) {
                return (
                    "rotate(" +
                    (d.angle - 90) +
                    ")translate(" +
                    (radius + 28) +
                    ",0)" +
                    (d.angle < 180 ? "" : "rotate(180)")
                );
            }) /* TODO: set the exact radius */
            .attr("text-anchor", function(d) {
                return d.angle < 180 ? "start" : "end";
            })
            .attr("fill", d => d.fill)
            .text(function(d) {
                return d.text;
            });
    }

    /* Graphical functions for basic elements */

    function innerArc(radius) {
        return d3
            .arc()
            .outerRadius(radius + 10)
            .innerRadius(radius);
    }

    function outerArc(radius) {
        return d3
            .arc()
            .outerRadius(radius + 21)
            .innerRadius(radius + 11);
    }

    const line = d3
        .radialLine()
        .curve(d3.curveBundle.beta(0.8))
        .radius(d => d.radius)
        .angle(d => d.centerAngle);

    /*
     * DATA
     *
     * Build the hierarchical edge bundling graph
     * https://bl.ocks.org/mbostock/7607999
     *
     */
    function addAngleAndRadius(node, radius, startAngle, maxValue) {
        /* Add angles and radius to current node */
        node.angleWidth = (node.value / maxValue) * Math.PI;
        node.padAngle = node.angleWidth > 0.003 ? 0.0015 : node.angleWidth;
        node.startAngle = startAngle;
        node.endAngle = startAngle + 2 * node.angleWidth;
        node.centerAngle = (node.endAngle + node.startAngle) / 2;
        node.radius = (radius * node.depth) / (node.depth + node.height);

        /* Descend in the tree */
        if ("children" in node) {
            node.children = node.children.map((n, i) =>
                addAngleAndRadius(
                    n,
                    radius,
                    i === 0 ? startAngle : node.children[i - 1].endAngle,
                    maxValue
                )
            );
        }

        return node;
    }

    function addNodeColor(node) {
        if (node.depth === 0) return node;
        node.color = getColorByIndexAndWeight({
            index: node.depth === 3 ? +node.parent.parent.id : +node.parent.id,
            weight: node.depth === 3 ? node.data.weight : node.value,
            MIN_SAT: 0.4,
            MAX_SAT: 0.95,
            MIN_BRIGHTNESS: 0.8,
            MAX_BRIGHTNESS: 0.5
        });
        if (node.depth === 2) node.color = fadeColor(node.color);

        return node;
    }

    function getGrayLinkColor(link) {
        const brightness = 56 - Math.floor(56 * Math.sqrt(link.normalizedWeight));
        const alpha = Math.sqrt(link.normalizedWeight) * 0.3 + 0.02;
        return d3.rgb(brightness, brightness, brightness, alpha).toString();
    }

    function fadeColor(color) {
        const hsvColor = d3.hsv(color);
        return d3.hsv(hsvColor.h, 0.2, 0.8);
    }

    const stratifyTree = d3
        .stratify()
        .id(d => d.path)
        .parentId(d => d.parentPath);

    function centerChildNodes(nodes) {
        nodes.each(node => {
            if ("children" in node) {
                const newChildren = [];
                let i = 1;
                while (node.children.length > 0) {
                    if (i < node.children.length) {
                        // voodoo!
                        newChildren.push(
                            node.children.splice(
                                Math.max(0, node.children.length - 1 - i),
                                1
                            )[0]
                        );
                    } else {
                        newChildren.push(node.children.shift());
                    }
                    i++;
                }
                node.children = newChildren;
            }
        });
        return nodes;
    }

}
/*
跨包克隆包内克隆文件对比
*/
var setFilesContext = function (file1AbsolutePath, file2AbsolutePath, decoder1, decoder2) {
    $.ajax({
        type : "GET",
        url : "/clone/compare/files?file1AbsolutePath=" + file1AbsolutePath +"&file2AbsolutePath=" + file2AbsolutePath + "&decoder1=" + decoder1 + "&decoder2=" + decoder2,
        success : function(result) {
            console.log("success");
            $('#compare').mergely({
                width: 'auto',
                height: 'auto',
                cmsettings: {readOnly: false, lineNumbers: true},
                lhs: function (setValue) {
                    setValue(result.file1);
                },
                rhs: function (setValue) {
                    setValue(result.file2);
                }
            });
        }
    })
}

/*
显示跨包克隆聚合详细信息
*/
var showDetails = function (id1, id2, path1, path2, clonePairs, cloneNodesCount1, cloneNodesCount2, allNodesCount1, allNodesCount2, cloneMatchRate, cloneNodesLoc1, cloneNodesLoc2, allNodesLoc1, allNodesLoc2, cloneLocRate, cloneNodesCoChangeTimes, allNodesCoChangeTimes, cloneCoChangeRate, cloneType1Count, cloneType2Count, cloneType3Count, cloneType, cloneSimilarityValue, cloneSimilarityRate) {
    doublePackagesCloneShowMain(id1,id2,path1, path2, clonePairs, cloneNodesCount1, cloneNodesCount2, allNodesCount1, allNodesCount2, cloneMatchRate, cloneNodesLoc1, cloneNodesLoc2, allNodesLoc1, allNodesLoc2, cloneLocRate, cloneNodesCoChangeTimes, allNodesCoChangeTimes, cloneCoChangeRate, cloneType1Count, cloneType2Count, cloneType3Count, cloneType, cloneSimilarityValue, cloneSimilarityRate);
    doublePackagesCloneShowDetail(id1, id2);
    doublePackagesCloneShowTree(id1, id2);
    // cloneGroupToGraphShowDetail(id1, id2);
    doublePackagesCloneShowMatrix(id1, id2);
}