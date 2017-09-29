MappingEditor.prototype.importMapping = function() {
	var _this = this;
	var form_identifier = "upload-file-" + this.mappingId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "forms/import/",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		additionalModalClasses: "wide-modal",
		completeCallback: function() { _this.reloadAll(); }
	});
	
	modalFormHandler.fileUploadElements.push({
		selector: "#mapping_source",			// selector for identifying where to put widget
		formSource: "forms/fileupload",			// where is the form
		uploadTarget: "async/upload", 			// where to we upload the file(s) to
		multiFiles: false, 						// one or multiple files
		//elementChangeCallback: _this.setupRootSelection
	});
		
	modalFormHandler.show(form_identifier);
};

MappingEditor.prototype.exportMapping = function() {
	var _this = this;

	$.ajax({
	    url: _this.pathname + "/async/export",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	blob = new Blob([JSON.stringify(data.pojo)], {type: "application/json; charset=utf-8"});
	    	saveAs(blob, "mapping_" + _this.mappingId + ".json");
	    },
	    error: __util.processServerError
	});
};