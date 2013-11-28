$(document).ready(function() {
	getMyRequests();
});

function getMyRequests() {
	$.ajax({
		url: "http://10.211.55.8/ibmconnect/approvals.nsf/api.xsp/myrequests",
		dataType: 'json',
		cache: false,
		success: function(data) {
			try {
				var items = [];
				var popovers = "";
				var html = "<table class=\"table table-condensed\">";
				html += "<thead><tr><th class=\"actioncol\"></th><th class=\"createdcol\">Created</th><th>Created By</th><th>Type</th><th>Status</th><th>Details</th></tr></thead><tbody>"
				$.each(data, function(key, val) {
					console.log("Adding row for " + this.ID);
					html += "<tr id=\"" + this.ID + "\">";
					html += "<td>";
					if (this.Status == "New" || this.Status == "In Progress"){
						html += "<div class=\"btn-group\"><button type=\"button\" class=\"btn\">Action</button>";
						html += "<button type=\"button\" class=\"btn dropdown-toggle\" data-toggle=\"dropdown\">";
						html += "<span class=\"caret\"></span><span class=\"sr-only\">Toggle Dropdown</span>";
						html += "</button><ul class=\"dropdown-menu\" role=\"menu\">";
						if (this.Status == "New") {
							html += "<li><a href=\"#\" onClick=\"changeStatus('" + this.ID + "', 'In Progress')\">Mark In Progress</a></li>";
							html += "<li><a href=\"#\" onClick=\"changeStatus('" + this.ID + "', 'Completed')\">Mark Complete</a></li>";
						} else if (this.Status == "In Progress") {
							html += "<li><a href=\"#\" onClick=\"changeStatus('" + this.ID + "', 'Completed')\">Mark Complete</a></li>";
						} else {

						}
						html += "</ul></div>"
					}
					html += "</td>";
					html += "<td>" + formatDate(this.Date) + "</td>";
					html += "<td>" + this.CreatedBy + "</td>";
					html += "<td>" + this.Type + "</td>";
					html += "<td>" + this.Status + "</td>";
					html += "<td>" + this.RequestDetails.trunc(25, true) + "</td>";

					html += "</tr>";
				});
				html += "</tbody></table>";
				$("#container").html(html);
			} catch (e) {

			}
		}
	});
}

function changeStatus(id, newstatus) {
	$.ajax({
		type: "POST",
		url: "http://10.211.55.8/ibmconnect/approvals.nsf/api.xsp/" + id,
		data: JSON.stringify({"status":newstatus}), 
		cache: false, 
		complete: getMyRequests,
		headers: { "Content-Type": "application/json"},
		dataType: "json",
		error: function(request, error) {
			console.log(arguments);
		}
	});
}

function formatDate(thedate) {
	var d = new Date(thedate);
	var curr_date = d.getDate();
	var curr_month = d.getMonth();
	curr_month++;
	var curr_year = d.getFullYear();
	return (curr_date + "-" + curr_month + "-" + curr_year);
}

String.prototype.trunc =
     function(n,useWordBoundary){
         var toLong = this.length>n,
             s_ = toLong ? this.substr(0,n-1) : this;
         s_ = useWordBoundary && toLong ? s_.substr(0,s_.lastIndexOf(' ')) : s_;
         return  toLong ? s_ + '&hellip;' : s_;
      };