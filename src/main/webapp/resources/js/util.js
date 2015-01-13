$(document).ready(function() {
	checkFormsForErrors();
	
	$(function () {
        $("[rel='tooltip']").tooltip();
    });
});

function checkFormsForErrors() {
	$("form .control-group").each(function() {
		if ($(this).find(".error").size()>0) {
			$(this).addClass("error");
		}
	});
}

String.prototype.format = function() {
  var args = arguments;
  return this.replace(/{(\d+)}/g, function(match, number) { 
    return typeof args[number] != 'undefined'
      ? args[number]
      : match
    ;
  });
};

function deleteObject(id) {
	$.ajax({
        url: window.location.pathname + "/ajax/delete?id=" + id,
        type: "GET",
        dataType: "json",
        success: function(data) {
        	if (data.success==true) { 
        		$("#model_id_" + id).remove();
        		modalMessage.showMessage(data.message_type, data.message_head, data.message_body);
        	} else {
        		modalMessage.showMessage(data.message_type, data.message_head, data.message_body);
        	}
        },
        error: function(textStatus) { 
        	alert(textStatus);
        }
	});
}

function createModalForm(id, form_identifier) {

	modalFormHandler = new ModalFormHandler({
		formUrl: "/ajax/getNewForm?id=" + id,
		translationsUrl: "/ajax/getTranslations",
		identifier: form_identifier,
		//callback: refreshModel
	});
	
	modalFormHandler.show(form_identifier);
}

function showChangeLog() {
	$.ajax({
		url: window.location.pathname + "/changeLog",
        type: "GET",
        dataType: "text",
        success: function(data) {
        	$(data).modal('show');
        },
        error: function(textStatus) { }
 	});
}
