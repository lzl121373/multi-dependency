var doublefile = function() {

    var showtree = function(divId, zNodes) {
        var zTreeObj;
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
        zTreeObj = $.fn.zTree.init($("#" + divId), setting, zNodes);
    }

    var loaddata = function(file1Id,file2Id,cloneType,linesSize1,linesSize2,loc1,loc2,value,cochange,filePath1,filePath2,cochangeId) {
        $.ajax({
            type : "GET",
            url : "/clone/file/double/json?fileId1="+ file1Id +"&fileId2=" + file2Id,
            success : function(result) {
                var html = ""
                html += "<table class = 'table table-bordered'>"
                html += "<tr><th>ID</th><th>filePath</th><th>linesSize</th><th>loc</th><th>type</th><th>value</th><th>cochange</th></tr>"
                html += "<tr>";
                html += "<td>"+ file1Id + "</td>";
                html += "<td>" + "<a target='_blank' href='/relation/file/" + file1Id + "'>" + filePath1 + "</a>" + "</td>";
                html += "<td>" + linesSize1 +"</td>";
                html += "<td>" + loc1 +"</td>";
                html += "<td rowspan='2' style='vertical-align: middle'>" + cloneType + "</td>";
                html += "<td rowspan='2' style='vertical-align: middle'><a target='_blank' href='/clone/compare?id1=" + file1Id + "&id2=" + file2Id + "'>" + value + "</a></td>";
                html += "<td rowspan='2' style='vertical-align: middle'><a target='_blank' href='/commit/cochange?cochangeId=" + cochangeId + "'>" + cochange + "</a></td>";
                html += "</tr>"
                html += "<tr>";
                html += "<td>" + file2Id + "</td>";
                html += "<td>" + "<a target='_blank' href='/relation/file/" + file2Id + "'>" + filePath2 + "</a>" + "</td>"
                html += "<td>" + linesSize2 +"</td>";
                html += "<td>" + loc2 +"</td>";
                html += "</tr>";
                html += "</table>";
                $("#file_table").html(html);
                var data1 = result[file1Id];
                showtree("tree_file1", data1);
                var data2 = result[file2Id];
                showtree("tree_file2", data2);

            }
        })
    }

    var depends = function (file1Id, file2Id, filePath1, filePath2) {
        var html1 = "";
        html1 += "<a target='_blank' href='/relation/file/" + file1Id + "'>" + filePath1 + "</a>";
        $("#file1").html(html1);
        var html2 = "";
        html2 += "<a target='_blank' href='/relation/file/" + file2Id + "'>" + filePath2 + "</a>";
        $("#file2").html(html2);
        $.ajax({
            type: "get",
            url: "/relation/file/double/" + file1Id + "/" + file2Id + "/file1DependsOn",
            success: function(result) {
                console.log(result);
                var html = "<ol>";
                for (var i = 0; i < result.length; i++) {
                    html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                }
                html += "</ol>";
                $("#file1DependsOn").html(html);
            }
        })
        $.ajax({
            type: "get",
            url: "/relation/file/double/" + file2Id + "/" + file1Id + "/file1DependsOn",
            success: function(result) {
                console.log(result);
                var html = "<ol>";
                for (var i = 0; i < result.length; i++) {
                    html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                }
                html += "</ol>";
                $("#file2DependsOn").html(html);
            }
        })
        $.ajax({
            type: "get",
            url: "/relation/file/double/" + file1Id + "/" + file2Id + "/commonDependsOn",
            success: function(result) {
                console.log(result);
                var html = "<ul style='text-align: center'>";
                for(var i = 0; i < result.length; i++) {
                    html += "<li>" + (i+1) + "." + "<a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                }
                html += "</ul>";
                $("#commonDependsOn").html(html);
            }
        })
        $.ajax({
            type: "get",
            url: "/relation/file/double/" + file1Id + "/" + file2Id + "/file1DependedOnBy",
            success: function(result) {
                console.log(result);
                var html = "<ol>";
                for(var i = 0; i < result.length; i++) {
                    html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                }
                html += "</ol>";
                $("#file1DependedBy").html(html);
            }
        })
        $.ajax({
            type: "get",
            url: "/relation/file/double/" + file2Id + "/" + file1Id + "/file1DependedOnBy",
            success: function(result) {
                console.log(result);
                var html = "<ol>";
                for(var i = 0; i < result.length; i++) {
                    html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                }
                html += "</ol>";
                $("#file2DependedBy").html(html);
            }
        })
        $.ajax({
            type: "get",
            url: "/relation/file/double/" + file1Id + "/" + file2Id + "/commonDependedOnBy",
            success: function(result) {
                console.log(result);
                var html = "<ul style='text-align: center'>";
                for(var i = 0; i < result.length; i++) {
                    html += "<li>" + (i+1) + "." +  "<a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                }
                html += "</ul>";
                $("#commonDependedBy").html(html);
            }
        });
    }

    return {
        init : function(file1Id,file2Id,cloneType,linesSize1,linesSize2,loc1,loc2,value,cochange,filePath1,filePath2,cochangeId) {
            depends(file1Id,file2Id,filePath1,filePath2);
            loaddata(file1Id,file2Id,cloneType,linesSize1,linesSize2,loc1,loc2,value,cochange,filePath1,filePath2,cochangeId);
        }
    }
}
