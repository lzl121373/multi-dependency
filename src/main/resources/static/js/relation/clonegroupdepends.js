var rclonegroupdepends = function (name, cytoscapeutil) {

    var _clonegroup = function() {

        dependsmatrix(name);

        dependedmatrix(name);

        dependfiledetail(name);

        dependedfiledetail(name);
    };

    var dependsmatrix = function(name) {
        $.ajax({
            type: "GET",
            url: "/relation/dependsdetail/" + name + "/dependsmatrix",
            success: function(result) {
                if(result.dependsnodes.length > 0) {
                    var html = "";
                    html += "<div  style='overflow: auto;' width='100%' id='matrix_1'>";
                    html += "<table  class = 'table table-bordered'>"
                    html += "<tr>";
                    html += "<th style='background-color: #FFFFFF;'></th>";
                    for(var i = 0; i < result.dependsnodes.length; i++){
                        html += "<th style='background-color: #FFFFFF;'>" + (i + 1) + "</th>";
                    }
                    html += "</tr>";
                    for(var i = 0; i < result.nodes.length; i++){
                        html += "<tr>";
                        html += "<td style='background-color: #FFFFFF;'><a target='_blank' href='/relation/file/" + result.nodes[i].id + "'>" + result.nodes[i].path + "(" + result.nodes[i].loc + ")" + "</a></td>";
                        for(var j = 0;j < result.dependsnodes.length;j++){
                            html += "<td style='background-color: #FFFFFF;'>";
                            if(result.matrix[i][j] != null){
                                html +=  result.matrix[i][j];
                            }
                            html += "</td>";
                        }
                        html += "</tr>";
                    }
                    html += "</table>";
                    html += "</div>";
                    $("#dependmatrix").html(html);
                    $("#matrix_1").scroll(function(){
                        $("#matrix_1 tr th").css({"position":"relative","top":$("#matrix_1").scrollTop(),"z-index":"2"});
                        $("#matrix_1 tr td:nth-child(1)").css({"position":"relative","left":$("#matrix_1").scrollLeft(),"z-index":"1"});
                        $("#matrix_1 tr th:nth-child(1)").css({"position":"relative","top":$("#matrix_1").scrollTop(),"left":$("#matrix_1").scrollLeft(),"z-index":"3"});
                    });
                }
            }
        })
    }

    var dependedmatrix = function(name) {
        $.ajax({
            type: "GET",
            url: "/relation/dependsdetail/" + name + "/dependedmatrix",
            success: function(result) {
                if(result.dependsnodes.length > 0) {
                    var html = "";
                    html += "<div  style='overflow: auto;' width='100%' id='matrix_2'>";
                    html += "<table  class = 'table table-bordered'>"
                    html += "<tr>";
                    html += "<th  style='background-color: #FFFFFF;'></th>";
                    for(var i = 0; i < result.dependsnodes.length; i++){
                        html += "<th style='background-color: #FFFFFF;'>" + (i + 1) + "</th>";
                    }
                    html += "</tr>";
                    for(var i = 0; i < result.nodes.length; i++){
                        html += "<tr>";
                        html += "<td style='background-color: #FFFFFF;'><a target='_blank' href='/relation/file/" + result.nodes[i].id + "'>" + result.nodes[i].path + "(" + result.nodes[i].loc + ")" + "</a></td>";
                        for(var j = 0;j < result.dependsnodes.length;j++){
                            html += "<td style='background-color: #FFFFFF;'>";
                            if(result.matrix[i][j] != null){
                                html +=  result.matrix[i][j];
                            }
                            html += "</td>";
                        }
                        html += "</tr>";
                    }
                    html += "</table>";
                    html += "</div>";
                    $("#dependedmatrix").html(html);
                    $("#matrix_2").scroll(function(){
                        $("#matrix_2 tr th").css({"position":"relative","top":$("#matrix_2").scrollTop(),"z-index":"2"});
                        $("#matrix_2 tr td:nth-child(1)").css({"position":"relative","left":$("#matrix_2").scrollLeft(),"z-index":"1"});
                        $("#matrix_2 tr th:nth-child(1)").css({"position":"relative","top":$("#matrix_2").scrollTop(),"left":$("#matrix_2").scrollLeft(),"z-index":"3"});
                    });
                }
            }
        })
    }

    var dependfiledetail = function(name) {
        $.ajax({
                type: "GET",
                url: "/relation/dependsdetail/" + name + "/alldependsonnodes",
                success: function(result) {
                    var html = "<ol>";
                    for(var i = 0;i < result.length;i++){
                        html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>"
                            + "(Score: " + (result[i].score).toFixed(2) + "): " +result[i].path + "(" + result[i].loc + ")" + "</a></li>";
                    }
                    html += "</ol>";
                    $("#dependfiledetail").html(html);
                }
            })
    }

    var dependedfiledetail = function(name) {
        $.ajax({
            type: "GET",
            url: "/relation/dependsdetail/" + name + "/alldependednodes",
            success: function(result) {
                var html = "<ol>";
                for(var i = 0;i < result.length;i++){
                    html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>"
                        + "(Score: " + (result[i].score).toFixed(2) + "): " +result[i].path + "(" + result[i].loc + ")" + "</a></li>";
                }
                html += "</ol>";
                $("#dependedfiledetail").html(html);
            }
        })
    }

    return {
        init: function(){
            _clonegroup();
        }
    }
}