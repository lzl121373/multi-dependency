function base64(content) {
	return window.btoa(unescape(encodeURIComponent(content)));
}
function tableToExcel(tableID, fileName) {
	var excelContent = $("#" + tableID).html();
	// 		alert(excelContent);
	var excelFile = "<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:x='urn:schemas-microsoft-com:office:excel' xmlns='http://www.w3.org/TR/REC-html40'>";
	excelFile += "<head><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet><x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]--></head>";
	excelFile += "<body><table width='10%'  border='1'>";
	excelFile += excelContent;
	excelFile += "</table></body>";
	excelFile += "</html>";
	var link = "data:application/vnd.ms-excel;base64," + base64(excelFile);
	var a = document.createElement("a");
	a.download = fileName + ".xlsx";
	a.href = link;
	a.click();
}
