var staticDependGraph = function() {
    var mainUrl = "/ar";
    $.ajax({
        type: "get",
        url: mainUrl + "/dependJson/static",
        success: function(result) {
            console.log(result);
            dependencyToGraph(result.result, "static_depend_graph");
        }
    });
};

var dynamicDependGraph = function() {
    var mainUrl = "/ar";
    $.ajax({
        type: "get",
        url: mainUrl + "/dependJson/dynamic",
        success: function(result) {
            console.log(result);
            dependencyToGraph(result.result, "dynamic_depend_graph");
        }
    });
};

var cochangeDependGraph = function() {
    var mainUrl = "/ar";
    $.ajax({
        type: "get",
        url: mainUrl + "/dependJson/cochange",
        success: function(result) {
            console.log(result);
            dependencyToGraph(result.result, "cochange_depend_graph");
        }
    });
};


var dependencyToGraph = function(result,divId) {
    //设置数组
    var data = result

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
    var nodes = cluster.nodes(packageHierarchy(data)),
        links = packageImports(nodes);
    // var nodes = cluster.nodes(packageClone(classes)),
    //     links = packageCloneImports(nodes);

    console.log(nodes)

    link = link
        .data(bundle(links))
        .enter().append("path")
        .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
        .attr("class", "link")
        .attr("d", line);

    node = node
        .data(nodes.filter(function(n) { return !n.children; }))
        .enter().append("text")
        // .style("fill", function (d) { if (checkChangeType(d.key, changes)== 3) { return '#b47500';}
        //                               if (checkChangeType(d.key, changes)== 4) { return '#00b40a';}})
        .attr("class", "node")
        .attr("dy", ".31em")
        .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
        .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
        .text(function(d) { return d.key; })
        .on("mouseover", mouseovered)
        .on("mouseout", mouseouted)
        .call(text => text.append("title").text(function(d) { return d.key; }));
    // .call(text => text.append("title").text(d => `${node.data.name}
    // ${d.outgoing.length} outgoing
    // ${d.incoming.length} incoming`));


    /*
    *从json中读取数组
     */
    // d3.json("../data/link.json", function(error, classes) {
    // d3.json("../static/data/link.json", function(error,  classes) {
    // d3.json("../static/data/2.json", function(error, classes) {
    // d3.json("../static/data/flare.json", function(error, classes) {
    // d3.json("../static/data/testpackages.json", function(error, classes) {
    //     if (error) throw error;
    //
    //     var nodes = cluster.nodes(packageHierarchy(classes)),
    //         links = packageImports(nodes);
    //     // var nodes = cluster.nodes(packageClone(classes)),
    //     //     links = packageCloneImports(nodes);
    //
    //     console.log(nodes)
    //
    //     link = link
    //         .data(bundle(links))
    //         .enter().append("path")
    //         .each(function(d) { d.source = d[0], d.target = d[d.length - 1]; })
    //         .attr("class", "link")
    //         .attr("d", line);
    //
    //     node = node
    //         .data(nodes.filter(function(n) { return !n.children; }))
    //         .enter().append("text")
    //         // .style("fill", function (d) { if (checkChangeType(d.key, changes)== 3) { return '#b47500';}
    //         //                               if (checkChangeType(d.key, changes)== 4) { return '#00b40a';}})
    //         .attr("class", "node")
    //         .attr("dy", ".31em")
    //         .attr("transform", function(d) { return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)"); })
    //         .style("text-anchor", function(d) { return d.x < 180 ? "start" : "end"; })
    //         .text(function(d) { return d.key; })
    //         .on("mouseover", mouseovered)
    //         .on("mouseout", mouseouted)
    //         .call(text => text.append("title").text(function(d) { return d.key; }));
    //         // .call(text => text.append("title").text(d => `${node.data.name}
    //         // ${d.outgoing.length} outgoing
    //         // ${d.incoming.length} incoming`));
    // });

    String.prototype.replaceAt=function(index, replacement) {
        return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
    }

    String.prototype.replaceAll = function(search, replacement) {
        var target = this;
        return target.replace(new RegExp(search, 'g'), replacement);
    };


    var width = 360;
    var height = 360;
    var radius = Math.min(width, height) / 2;
    var donutWidth = 75;
    var legendRectSize = 18;                                  // NEW
    var legendSpacing = 4;

    var legend = d3.select('svg')
        .append("g")
        .selectAll("g")
        // .data(color.domain())
        //.enter()
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
    //.attr('transform', 'translate(0,0)');


    legend.append('rect')
        .attr('width', legendRectSize)
        .attr('height', legendRectSize)
    // .style('fill', color)
    // .style('stroke', color);

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
            // .style("stroke", function (l) { if (checkOldLink(l, old_links)) { return '#b400ad';}})
            .style("stroke", function (l) {if (l.target === d) return "#2ca02c"; else return "#d62728"; })
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

    // Lazily construct the package hierarchy from class names.
    function packageHierarchy(classes) {
        var map = {};

        function find(name, data) {
            var node = map[name], i;
            if (!node) {
                node = map[name] = data || {name: name, children: []};
                console.log(node)
                if (name.length) {
                    node.parent = find(name.substring(0, i = name.lastIndexOf("/")));
                    node.parent.children.push(node);
                    node.key = name.substring(i + 1);
                }
            }
            return node;
        }

        // classes.result.forEach(function(d) {
        classes.forEach(function(d) {
            console.log(d)
            find(d.name, d);
        });

        return map[""];
    }// Return a list of imports for the given array of nodes.
    function packageImports(nodes) {
        var map = {},
            imports = [];

        // Compute a map from name to node.
        nodes.forEach(function(d) {
            map[d.name] = d;
        });

        // For each import, construct a link from the source to target node.
        nodes.forEach(function(d) {
            if (d.imports) d.imports.forEach(function(i) {
                imports.push({source: map[d.name], target: map[i]});
            });
        });

        return imports;
    }
};

var staticClusterGraph = function () {
    var mainUrl = "/ar";
    $.ajax({
        type : "GET",
        url : mainUrl + "/clusterJson/static",
        success : function(result) {
            projectToGraph(result,"graph");
        }
    })
}

var staticClusterGraph = function () {
    var mainUrl = "/ar";
    $.ajax({
        type : "GET",
        url : mainUrl + "/clusterJson/static",
        success : function(result) {
            projectToGraph(result,"graph");
        }
    })
}

var dynamicClusterGraph = function () {
    var mainUrl = "/ar";
    $.ajax({
        type : "GET",
        url : mainUrl + "/clusterJson/dynamic",
        success : function(result) {
            projectToGraph(result,"graph");
        }
    })
}

var cochangeClusterGraph = function () {
    var mainUrl = "/ar";
    $.ajax({
        type : "GET",
        url : mainUrl + "/clusterJson/cochange",
        success : function(result) {
            projectToGraph(result,"graph");
        }
    })
}

var projectToGraph = function(result,divId){
    var projectdata = result[0].result
    console.log(projectdata)
    var svg = d3.select("#" + divId)
            .attr("width", 1800)
            .attr("height", 1800),
        margin = 20,
        diameter = +svg.attr("width"),
        g = svg.append("g").attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

    var color = d3.scaleLinear()
        .domain([-1, 5])
        .range(["hsl(152,80%,80%)", "hsl(228,30%,40%)"])
        .interpolate(d3.interpolateHcl);

    var pack = d3.pack()
        .size([diameter - margin, diameter - margin])
        .padding(2);

    root = d3.hierarchy(projectdata)
        .sum(function(d) { return d.size; })
        .sort(function(a, b) { return b.value - a.value; });


    var focus = root,
        nodes = pack(root).descendants(),
        view;

    var circle = g.selectAll("circle")
        .data(nodes)
        .enter().append("circle")
        .attr("class", function(d) { return d.parent ? d.children ? "circlepacking_node" : "circlepacking_node circlepacking_node--leaf" : "circlepacking_node circlepacking_node--root"; })
        .style("fill", function(d) {return d.children ? color(d.depth) : null; })
        .on("click", function(d) { if (focus !== d) zoom(d), d3.event.stopPropagation(); })
        .call(text => text.append("title").text(function(d) { return d.data.name; }));

    var text = g.selectAll("text")
        .data(nodes)
        .enter().append("text")
        .attr("class", "circlepacking_label")
        .style("fill-opacity", function(d) { return d.parent === root ? 1 : 0; })
        .style("display", function(d) { return d.parent === root ? "inline" : "none"; })
        .style("font-size", function(d) { return d.children ? "18px" : "13px"; })
        .text(function(d) { return d.data.name; });

    var node = g.selectAll("circle,text");

    svg
        .style("background", "white")
        .on("click", function() { zoom(root); });

    zoomTo([root.x, root.y, root.r * 2 + margin]);

    function zoom(d) {
        if(!d.children){
            d = d.parent;
        }
        var focus0 = focus; focus = d;

        var transition = d3.transition()
            .duration(d3.event.altKey ? 7500 : 750)
            .tween("zoom", function(d) {
                var i = d3.interpolateZoom(view, [focus.x, focus.y, focus.r * 2 + margin]);
                return function(t) { zoomTo(i(t)); };
            });

        transition.selectAll("text")
            .filter(function(d) { return d.parent === focus || this.style.display === "inline"; })
            .style("fill-opacity", function(d) { return d.parent === focus ? 1 : 0; })
            .on("start", function(d) { if (d.parent === focus) this.style.display = "inline"; })
            .on("end", function(d) { if (d.parent !== focus) this.style.display = "none"; });
    }

    function zoomTo(v) {
        var k = diameter / v[2]; view = v;
        node.attr("transform", function(d) { return "translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")"; });
        circle.attr("r", function(d) { return d.r * k; });
    }
};

