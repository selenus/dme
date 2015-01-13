var modelViewRefresher = null;

$(document).ready(function() {
	modelViewRefresher = new ModelViewRefresher();
	
	$("#btn-new-mapping").click(function(e) {
		createModalForm(-1, "new-mapping");
		e.preventDefault();
		return false;
	});
});

function refreshModel() {
	modelViewRefresher.refreshModel();
};

function deleteMapping(id) {
	$.ajax({
        url: window.location.pathname + "/ajax/delete?id=" + id,
        type: "GET",
        dataType: "json",
        success: function(data) {
        	if (data.success==true) { 
        		modelViewRefresher.refreshModel();
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
		setupCallback: refreshModel
	});
	modalFormHandler.translations = 
		[{placeholder: "~*servererror.head", key: "~crosswalkRegistry.dialogs.new.servererror.head", defaultText: "Problem interacting with server"},
		 {placeholder: "~*servererror.body", key: "~crosswalkRegistry.dialogs.new.servererror.body", defaultText: "Could not interact with server. Please check the internet connectivity of your computer, try again or inform the administrator if this problem pertains."},
		 {placeholder: "~*importsuccessful.head", key: "~crosswalkRegistry.dialogs.new.importsuccessful.head"},
		 {placeholder: "~*importsuccessful.body", key: "~crosswalkRegistry.dialogs.new.importsuccessful.body"},
		 {placeholder: "~*validationerrors.head", key: "~crosswalkRegistry.dialogs.new.validationerrors.head"},
		 {placeholder: "~*validationerrors.body", key: "~crosswalkRegistry.dialogs.new.validationerrors.body"}
		];
	
	modalFormHandler.show(form_identifier);
};