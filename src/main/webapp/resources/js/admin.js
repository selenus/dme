$(document).ready(function() {
	setTimeout(function() { refreshAsync(); }, 2000);
});

function refreshAsync() {
	var latestID = 0;	
	
	setTimeout(function() { refreshAsync(); }, 2000);
	
	if (!$("#auto_refresh").is(':checked')) {
		return;
	}
	
	$(".model-list tbody tr").first().each(function() {
		latestID = this.id.replace(/logEntry_/g, '');
	});
	
	$.ajax({
        url: window.location.pathname + '/refresh',
        type: "GET",
        data: {currentLatestItem: latestID },
        dataType: "html",
        success: function(data) { setContent(data); },
        error: function(textStatus) { $("#refresh_message").text(" (Last refresh failed)"); }
  });
}

function setContent(data) {
	
	$("#refresh_message").text(" (Last refresh at " + new Date() + ")");
	
	if (data != null && data != "") {
		$('.model-list > tbody:first').prepend(data);
		
		var max = 30;
		var removeTr = [];
		
		$("#log_view tbody tr").each(function() {
			if (max <= 0) {
				removeTr.push(this);
			}
			max--;
		});
		
		$(removeTr).each(function() {
			$(this).remove();
		});
	}
}