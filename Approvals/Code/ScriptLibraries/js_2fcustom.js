$(document).ready( function() {
	// Handler for .ready() called.
	$("#searchfield").keypress( function(event) {
		if (event.which == 13) {
			event.preventDefault();
			doSearch();
		}
	})

});


function doSearch() {
	var query = $("#searchfield").val();
	if (query != ""){
		window.location.href="allrequests.xsp?query=" + query;
	}
}