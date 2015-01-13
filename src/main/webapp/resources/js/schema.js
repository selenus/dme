var modelViewRefresher = null;

$(document).ready(function() {
	modelViewRefresher = new ModelViewRefresher();
	
	$("#btn-new-schema").click(function(e) {
		createModalForm(-1, "new-schema");
		e.preventDefault();
		return false;
	});
});

function editSchema(id) {
	createModalForm(id, "edit-schema-" + id);
}

function refreshModel() {
	modelViewRefresher.refreshModel();
}

function deleteSchema(id) {
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
		[{placeholder: "~*servererror.head", key: "~schemaRegistry.dialogs.new.servererror.head", defaultText: "Problem interacting with server"},
		 {placeholder: "~*servererror.body", key: "~schemaRegistry.dialogs.new.servererror.body", defaultText: "Could not interact with server. Please check the internet connectivity of your computer, try again or inform the administrator if this problem pertains."},
		 {placeholder: "~*importsuccessful.head", key: "~schemaRegistry.dialogs.new.importsuccessful.head"},
		 {placeholder: "~*importsuccessful.body", key: "~schemaRegistry.dialogs.new.importsuccessful.body"},
		 {placeholder: "~*validationerrors.head", key: "~schemaRegistry.dialogs.new.validationerrors.head"},
		 {placeholder: "~*validationerrors.body", key: "~schemaRegistry.dialogs.new.validationerrors.body"},
		 {placeholder: "~*file.validationsucceeded.head", key: "~schemaRegistry.dialogs.file.validationsucceeded.head"},
		 {placeholder: "~*file.validationsucceeded.body", key: "~schemaRegistry.dialogs.file.validationsucceeded.body"},
		 {placeholder: "~*file.servererror.head", key: "~schemaRegistry.dialogs.new.servererror.head", defaultText: "Problem interacting with server"},
		 {placeholder: "~*file.servererror.body", key: "~schemaRegistry.dialogs.new.servererror.body", defaultText: "Could not interact with server. Please check the internet connectivity of your computer, try again or inform the administrator if this problem pertains."},
		 {placeholder: "~*file.generalerror.head", key: "~schemaRegistry.dialogs.file.generalerror.head"},
		 {placeholder: "~*file.generalerror.body", key: "~schemaRegistry.dialogs.file.generalerror.body"},
		 {placeholder: "~*file.uploaderror.head", key: "~schemaRegistry.dialogs.file.uploaderror.head"},
		 {placeholder: "~*file.uploaderror.body", key: "~schemaRegistry.dialogs.file.uploaderror.body"},
		 {placeholder: "~*file.deletesucceeded.head", key: "~schemaRegistry.dialogs.file.deletesucceeded.head"},
		 {placeholder: "~*file.deletesucceeded.body", key: "~schemaRegistry.dialogs.file.deletesucceeded.body"},
		 {placeholder: "~*file.uploadcomplete.head", key: "~schemaRegistry.dialogs.file.uploadcomplete.head"},
		 {placeholder: "~*file.uploadcomplete.body", key: "~schemaRegistry.dialogs.file.uploadcomplete.body"}
		];
	
	modalFormHandler.fileUploadElements.push({
		selector: "#schema_source",				// selector for identifying where to put widget
		formSource: "/ajax/getSourceSelectionForm",	// where is the form
		uploadTarget: "/ajax/prepareSchema", 	// where to we upload the file(s) to
		multiFiles: false, 						// one or multiple files
	});

	modalFormHandler.show(form_identifier);
}