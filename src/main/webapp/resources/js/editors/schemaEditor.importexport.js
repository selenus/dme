SchemaEditor.prototype.importSchema = function(elementId) {
	var _this = this;
	var form_identifier = "upload-file-" + this.schema.id;
	
	var data = elementId===undefined ? undefined : { elementId : elementId };
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "forms/import/",
		data: data,
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		additionalModalClasses: "wide-modal",
		completeCallback: function() { 
			_this.reloadElementHierarchy(function() {
				_this.area.expandFromElement(elementId, true)
			});
		}
	});
	
	modalFormHandler.fileUploadElements.push({
		selector: "#schema_source",				// selector for identifying where to put widget
		formSource: "forms/fileupload",			// where is the form
		uploadTarget: "async/upload" + (elementId===undefined ? "": "/" + elementId), 			// where to we upload the file(s) to
		multiFiles: false, 						// one or multiple files
		elementChangeCallback: _this.setupRootSelection
	});
		
	modalFormHandler.show(form_identifier);
};


SchemaEditor.prototype.setupRootSelection = function(data) {
	var rootSelector = $("#schema_root");
	rootSelector.typeahead("destroy");
	
	
	rootSelector.val("");
	
	// No root elements
	if (data==null || data.pojo==null || data.pojo.length==0) {
		select.prop("disabled", "disabled");
		$("#btn-submit-schema-elements").prop("disabled", "disabled");
		return;
	}
	
	if (data.pojo.length==1) {
		$("#schema_root_qn").val("{" + data.pojo[0].namespace + "}:" + data.pojo[0].name);
		rootSelector.val(data.pojo[0].name);
		return;
	}
	
	rootSelector.removeProp("disabled");
	
	var _this = editor;
	var elements = new Bloodhound({
		  datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
		  queryTokenizer: Bloodhound.tokenizers.whitespace,
		  identify: function(obj) { return (obj.namespace + ":" + obj.name); },
		  local: data.pojo
	});
	
	_this.registerTypeahead(rootSelector, "importedelements", elements, "name", 8, 
			function(e) { 
				if (e.namespace===undefined || e.namespace===null || e.namespace==="") {
					return '<p><strong>' + e.name + '</strong> ' +
							'<small><em>' + __translator.translate("~eu.dariah.de.minfba.common.model.types." + e.simpleType.toLowerCase()) + '</em></small>' +
						   '<p>';
				} else {
					return '<p><strong>' + e.name + '</strong><br/ >' + e.namespace + '<p>';
				}
			},
			function(t, suggestion) {
				if (suggestion.namespace===undefined) {
					$("#schema_root_qn").val(suggestion.name);
				} else {
					$("#schema_root_qn").val("{" + suggestion.namespace + "}:" + suggestion.name);
				}
				$("#schema_root_type").val(suggestion.type);
			},
			function(t, value) {
				for (var i=0; i<data.pojo.length; i++) {
					if (value===data.pojo[i].name) {
						if (data.pojo[i].namespace===undefined) {
							$("#schema_root_qn").val(data.pojo[i].name);
						} else {
							$("#schema_root_qn").val("{" + data.pojo[i].namespace + "}:" + data.pojo[i].name);
						}
						$("#schema_root_type").val(data.pojo[i].type);
						return;
					}
				}
				$("#schema_root_qn").val("");
				$("#schema_root_type").val("");
			}			
	);

	rootSelector.focus();
	$("#btn-submit-schema-elements").removeProp("disabled");
	
};

SchemaEditor.prototype.exportSchema = function() {
	var _this = this;

	$.ajax({
	    url: _this.pathname + "/async/export",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	blob = new Blob([JSON.stringify(data.pojo)], {type: "application/json; charset=utf-8"});
	    	saveAs(blob, "schema_" + _this.schema.id + ".json");
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.exportSubtree = function(elementId) {
	var _this = this;
	
	$.ajax({
	    url: _this.pathname + "/async/exportSubtree",
	    data: { elementId: elementId },
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	blob = new Blob([JSON.stringify(data.pojo)], {type: "application/json; charset=utf-8"});
	    	saveAs(blob, "subtree_" + _this.schema.id + ".json");
	    },
	    error: __util.processServerError
	});
};
