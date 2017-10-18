SchemaEditor.prototype.importSchema = function(elementId) {
	var _this = this;
	var form_identifier = "upload-file-" + this.schema.id;
	
	var data = elementId===undefined ? undefined : { elementId : elementId };
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "forms/import/",
		data: data,
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		additionalModalClasses: "wide-modal",
		completeCallback: function() { 
			_this.reloadPage();
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
	if (data==null || data.pojo==null || data.pojo.elements==null || data.pojo.elements.length==0) {
		rootSelector.prop("disabled", "disabled");
		$("#btn-submit-schema-elements").prop("disabled", "disabled");
		$("#importer-options").addClass("hide");
		return;
	}
	
	$("#importer-type").text(data.pojo.importerMainType);
	$("#importer-subtype").text(data.pojo.importerSubtype);
	
	if (data.pojo.keepIdsAllowed===true) {
		$("#importer-keep-ids").removeClass("hide");
	} else {
		$("#importer-keep-ids").addClass("hide");
	}
	
	$("#importer-options").removeClass("hide");
	
	
	if (data.pojo.elements[0].namespace===undefined) {
		$("#schema_root_qn").val(data.pojo.elements[0].name);
	} else {
		$("#schema_root_qn").val("{" + data.pojo.elements[0].namespace + "}:" + data.pojo.elements[0].name);
	}
	$("#schema_root_type").val(data.pojo.elements[0].type);
	rootSelector.val(data.pojo.elements[0].name);
	
	if (data.pojo.elements.length==1) {
		return;
	} 
	
	rootSelector.removeProp("disabled");
	
	var _this = editor;
	var elements = new Bloodhound({
		  datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
		  queryTokenizer: Bloodhound.tokenizers.whitespace,
		  identify: function(obj) { return (obj.namespace + ":" + obj.name); },
		  local: data.pojo.elements
	});
	
	_this.registerTypeahead(rootSelector, "importedelements", elements, "name", 8, 
			function(e) {
				if (e.namespace!==undefined && e.namespace!==null && e.namespace!=="") {
					return '<p><strong>' + e.name + '</strong><br/ >' + e.namespace + '<p>';
				} else if (e.simpleType!==undefined && e.simpleType!==null && e.simpleType!=="") {
					return '<p><strong>' + e.name + '</strong> ' +
							'<small><em>' + __translator.translate("~de.unibamberg.minf.common.model.types." + e.simpleType.toLowerCase()) + '</em></small>' +
						'<p>';
				} else {
					return '<p><strong>' + e.name + '</strong>';
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
				for (var i=0; i<data.pojo.elements.length; i++) {
					if (value===data.pojo.elements[i].name) {
						if (data.pojo.elements[i].namespace===undefined) {
							$("#schema_root_qn").val(data.pojo.elements[i].name);
						} else {
							$("#schema_root_qn").val("{" + data.pojo.elements[i].namespace + "}:" + data.pojo.elements[i].name);
						}
						$("#schema_root_type").val(data.pojo.elements[i].type);
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
