var rPackage = function (packageId, cytoscapeutil) {
    var _package = function() {
        containFile(packageId);
        metric(packageId);
        depends(packageId);
        developer(packageId);
        // issues(packageId);
        // commits(packageId);
    };

    var depends = function(packageId) {
        $.ajax({
            type: "get",
            url: "/relation/package/" + packageId + "/dependedBy",
            success: function(result) {
                console.log(result);
                var html = "<ol>";
                for(var i = 0; i < result.length; i++) {
                    html += "<li><a target='_blank' href='/relation/package/" + result[i].startNode.id + "'>"
                        + result[i].startNode.directoryPath + ":  " + "</a>"
                        + "<a target='_blank' href='/dependon?pck1=" + result[i].endNode.id + "&pck2=" + result[i].startNode.id + "'>"
                        + result[i].dependsOnType + "(" + + result[i].times + ", " + (result[i].weightedTimes).toFixed(2) + ")"
                        + "</a></li>";
                }
                html += "</ol>";
                $("#dependedBy_content").html(html);
            }
        })
        $.ajax({
            type: "get",
            url: "/relation/package/" + packageId + "/dependsOn",
            success: function(result) {
                console.log(result);
                var html = "<ol>";
                for(var i = 0; i < result.length; i++) {
                    html += "<li><a target='_blank' href='/relation/package/" + result[i].endNode.id + "'>"
                        + result[i].endNode.directoryPath + ":  " + "</a>"
                        + "<a target='_blank' href='/dependon?pck1=" + result[i].startNode.id + "&pck2=" + result[i].endNode.id + "'>"
                        + result[i].dependsOnType + "(" + + result[i].times + ", " + (result[i].weightedTimes).toFixed(2) + ")"
                        + "</a></li>";
                }
                html += "</ol>";
                $("#dependsOn_content").html(html);
            }
        });
    }

    var metric = function(packageId) {
        $.ajax({
            type: "get",
            url: "/relation/package/" + packageId + "/metric",
            success: function(result) {
                console.log(result);
                var html = "<table class='table table-bordered'>";
                html += "<tr>";
                html += "<td width='12.5%'>NOF（文件数）</td>";
                html += "<td width='12.5%'>NOM（方法数）</td>";
                html += "<td width='12.5%'>LOC（代码行）</td>";
                html += "<td width='12.5%'>Lines（文件总行数）</td>";
                html += "<td width='12.5%'>Ca（afferent couplings）</td>";
                html += "<td width='12.5%'>Ce（efferent couplings）</td>";
                html += "<td width='12.5%'>instability</td>";
                html += "</tr>";
                html += "<tr>";
                html += "<td>" + result.metricValues.NOF + "</td>";
                html += "<td>" + result.metricValues.NOM + "</td>";
                html += "<td>" + result.metricValues.LOC + "</td>";
                html += "<td>" + result.metricValues.Lines + "</td>";
                html += "<td>" + result.metricValues.FanIn + "</td>";
                html += "<td>" + result.metricValues.FanOut + "</td>";
                let instability = result.metricValues.Instability;
                html += "<td>" + (instability > 0 ? (instability).toFixed(2) : 0.0) + "</td>";
                html += "</tr>";
                html += "</table>";
                $("#metric_content").html(html);
            }
        })
    }

    var containFile = function(packageId) {
        $.ajax({
            type: "get",
            url: "/relation/package/" + packageId + "/contain/file",
            success: function(result) {
                var html = "<ul>";
                console.log(result);
                for(var i = 0; i < result.length; i++) {
                    html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "' >";
                    html += "(Score: " + (result[i].score).toFixed(2) + "): ";
                    html += result[i].name;
                    html += "</a></li>";
                }
                html += "</ul>";
                $("#contain_file_content").html(html);
            }
        });
    }

    var developer = function(packageId) {
        $.ajax({
           type: "get",
            url: "/relation/package/" + packageId + "/developerstimes",
            success: function (result) {
               var html = "";
               console.log(result);
               if(result.length == 0){
                   html += "No Developer For This Package!"
               }else{
                   html += "<ul>";
                   for(let i = 0; i < result.length; i++){
                       html += "<li><a target='_blank' href='/developer/packages?developerId=" + result[i].developer.id + "'>"
                           + result[i].developer.name + " (" + result[i].times + ")</li>"
                   }
                   html += "</ul>";
               }
                $("#developerstimes").html(html);
            }
        });
    }

    return {
        init: function(){
            _package();
        }
    }
}