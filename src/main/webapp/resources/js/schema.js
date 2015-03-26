var editor;
$(document).ready(function() {
	editor = new SchemaEditor();
	$("#btn-add-schema").click(function() { editor.triggerAddSchema(); });
});

var SchemaEditor = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.id",
	                          "~eu.dariah.de.minfba.schereg.schemas.model.description",
	                          "~eu.dariah.de.minfba.schereg.schemas.model.label",
	                          "~eu.dariah.de.minfba.common.view.common.delete",
	                          "~eu.dariah.de.minfba.common.view.common.edit",
	                          "~eu.dariah.de.minfba.schereg.schemas.button.editor",
	                          "~eu.dariah.de.minfba.schereg.view.async.servererror.head",
	                          "~eu.dariah.de.minfba.schereg.view.async.servererror.body",
	                          "~eu.dariah.de.minfba.schereg.schemas.dialog.confirm_detete",
	                          "~eu.dariah.de.minfba.schereg.schemas.notification.deleted.head",
	                          "~eu.dariah.de.minfba.schereg.schemas.notification.deleted.body"]);
	this.createTable();
	this.assignTableEvents();
};


SchemaEditor.prototype = new BaseEditor();

SchemaEditor.prototype.createTable = function() {
	this._base.table = $('#schema-table').DataTable({
		"aaSorting": [[ 1, "asc" ]],
		"aoColumnDefs": [{	"aTargets": [0], 
							"mData": "entity.id",
							"bSortable": false,
							"bSearchable": false,
							"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return editor.renderBadgeColumn(data, type, full.entity); }
						 },
		                 {	"aTargets": [1],
		                 	"mData": "entity.label",
		                 	"sWidth" : "100%"
						 },
						 {	"aTargets": [2], 
							"mData": "entity.type", 
		                 	"sClass": "td-no-wrap"
						 }]
	});
};

SchemaEditor.prototype.refresh = function() {
	var _this = this;
	
	if (!this.error && this.table!=null) {
		var selected = [];
		this._base.table.$("tr.selected").each(function() {
			selected.push($(this).prop("id"));
		});
				
		this.table.ajax.reload(function() {
			var hasSelected = false;
			if (selected.length>0) {
				for (var i=0; i<selected.length; i++) {
					$("#"+selected[i]).each(function() {
						$(this).addClass("selected");
						// Only executed if the row (id) still exists
						_this.handleSelection(selected[i]);
						hasSelected = true;
					});
				}
			} 
			if (!hasSelected) {
				_this.handleSelection(null);
			}
		});
	}
};

SchemaEditor.prototype.assignTableEvents = function() {
	var _this = this;
	
    $('.data-table-filter input').on('keyup click', function () {
    	$('#schema-table').DataTable().search($('.data-table-filter input').val(), false, false).draw();
    });
    $(".data-table-filter input").trigger("keyup");
    
    $('.data-table-count select').on('change', function () {
    	var len = parseInt($('.data-table-count select').val());
    	if (isNaN(len)) {
    		len = -1; // Show all
    	}    	
    	_this._base.table.page.len(len).draw();
    });
    $(".data-table-count select").trigger("change");
    		
	$("#schema-table tbody").on("click", "tr", function () {
        if ($(this).hasClass("selected")) {
            $(this).removeClass("selected");
            _this.handleSelection(null);
        } else {
        	_this._base.table.$("tr.selected").removeClass("selected");
            $(this).addClass("selected");
            _this.handleSelection($(this).prop("id"));
        }
    });
};

SchemaEditor.prototype.handleSelection = function(id) {
	var _this = this;
	
	if (id==null) {
		$('#tab-schema-activity a').tab('show');
		$("#tab-schema-metadata").addClass("hide");
		$("#tab-schema-elements").addClass("hide");
		$("#schema-metadata").html("");
		$("#schema-elements").html("");
	} else {
		$("#tab-schema-metadata").removeClass("hide");
		$("#tab-schema-elements").removeClass("hide");
		
		/*if ($("#tab-schema-activity").hasClass("active")) {
			$('#tab-schema-metadata a').tab('show');
		}*/
		
		$("#schema-metadata").html("<div class=\"loader\">");
		$("#schema-elements").html("<div class=\"loader\">");
		
		$.ajax({
	        url: window.location.pathname + "/async/getData/" + id,
	        type: "GET",
	        dataType: "json",
	        success: function(data) { _this.renderSchemaMetadataTab(id, data); },
	        error: function(textStatus) {
	        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
	        			__translator.translate("~eu.dariah.de.minfba.schereg.view.async.servererror.head"), 
	        			__translator.translate("~eu.dariah.de.minfba.schereg.view.async.servererror.body"));
	        }
		});
		
		$.ajax({
	        url: window.location.pathname + "/async/getElements/" + id,
	        type: "GET",
	        dataType: "json",
	        success: function(data) { _this.renderSchemaElementsTab(id, data); },
	        error: function(textStatus) {
	        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
	        			__translator.translate("~eu.dariah.de.minfba.schereg.view.async.servererror.head"), 
	        			__translator.translate("~eu.dariah.de.minfba.schereg.view.async.servererror.body"));
	        }
		});
	}
};

SchemaEditor.prototype.renderSchemaMetadataTab = function(id, data) {
	$("#schema-metadata").html("");
	
	
	var buttonBarContainer = $("<div class=\"row\">");
	var buttonBar = $("<div class=\"schema-metadata-buttons col-xs-9 col-md-8 col-xs-offset-3 col-md-offset-4\">");
	buttonBar.append(
		"<button onclick='editor.triggerEditSchema(\"" + id + "\");'class='btn btn-default btn-sm' type='button'><span class='glyphicon glyphicon-edit'></span> " + 
			__translator.translate("~eu.dariah.de.minfba.common.view.common.edit") + 
		"</button> ");
	buttonBar.append(
		"<button onclick='editor.triggerDeleteSchema(\"" + id + "\");' class='btn btn-danger btn-sm' type='button'><span class='glyphicon glyphicon-trash'></span> " +
			__translator.translate("~eu.dariah.de.minfba.common.view.common.delete") +
		"</button>");
	buttonBarContainer.append(buttonBar);
	
	$("#schema-metadata").append(buttonBarContainer);
	
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderSchemaMetadataTabDetail( __translator.translate("~eu.dariah.de.minfba.common.id"), data.id));
	details.append(this.renderSchemaMetadataTabDetail( __translator.translate("~eu.dariah.de.minfba.schereg.schemas.model.label"), data.label));
	details.append(this.renderSchemaMetadataTabDetail( __translator.translate("~eu.dariah.de.minfba.schereg.schemas.model.description"), data.description));
		
	$("#schema-metadata").append(details);

};

SchemaEditor.prototype.renderSchemaMetadataTabDetail = function(label, data) {
	var detail = $("<div class=\"row\">");
	detail.append("<div class=\"schema-metadata-label col-xs-3 col-md-4\">" + label + ":</div>");
	detail.append("<div class=\"schema-metadata-data col-xs-9 col-md-8\">" + data + "</div>");
	
	return detail;
};

SchemaEditor.prototype.renderSchemaElementsTab = function(id, data) {
	$("#schema-elements").html("");
	
	var buttonBarContainer = $("<div class=\"row\">");
	var buttonBar = $("<div class=\"schema-elements-buttons col-xs-9 col-md-8 col-xs-offset-3 col-md-offset-4\">");
	
	// TODO: Move import behavior
	/*buttonBar.append(
			"<button onclick='editor.triggerUploadFile(\"" + id + "\");'class='btn btn-default btn-sm' type='button'><span class='glyphicon glyphicon-edit'></span> " + 
				__translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.import") + 
			"</button> ");*/
	buttonBar.append(
			"<a href='" + __util.getBaseUrl() + "schema/editor/" + id + "' class='btn btn-link btn-sm' type='button'>" + 
				__translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.editor") + 
			" <span class='glyphicon glyphicon-new-window'></span></a> ");
	
	buttonBarContainer.append(buttonBar);
	
	
	
	
	$("#schema-elements").append(buttonBarContainer);
};

SchemaEditor.prototype.renderBadgeColumn = function(data, type, full) {
	var result = "";
	
	if (full.type=="BaseSchema") {
		result = '<span class="label label-warning">Stub</span> ';
	}	
	return result;
};


SchemaEditor.prototype.triggerAddSchema = function () {
	this.triggerEditSchema();
};

SchemaEditor.prototype.triggerUploadFile = function(schemaId) {
	var _this = this;
	var form_identifier = "upload-file-" + schemaId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/forms/import/" + schemaId,
		identifier: form_identifier,
		//additionalModalClasses: "wider-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.schereg.view.async.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.schereg.view.async.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
	
	modalFormHandler.fileUploadElements.push({
		selector: "#schema_source",				// selector for identifying where to put widget
		formSource: "/forms/fileupload",		// where is the form
		uploadTarget: "/async/upload", 			// where to we upload the file(s) to
		multiFiles: false, 						// one or multiple files
		elementChangeCallback: _this.handleFileValidatedOrFailed
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.handleFileValidatedOrFailed = function(data) {
	var select = $("#schema_root");
	select.html("");
	if (data==null || data.pojo==null || data.pojo.length==0) {
		select.prop("disabled", "disabled");
		$("#btn-submit-schema-elements").prop("disabled", "disabled");
		return;
	}
	
	var option;
	for (var i=0; i<data.pojo.length; i++) {
		option = "<option value='" + i + "'>" + data.pojo[i].name + " <small>(" + data.pojo[i].namespace + ")</small>" + "</option>";
		select.append(option);
	}
	select.removeProp("disabled");
	$("#btn-submit-schema-elements").removeProp("disabled");
};

SchemaEditor.prototype.triggerEditSchema = function(schemaId) {
	var _this = this;
	var form_identifier = "edit-schema-" + schemaId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: (schemaId!=undefined ? ("/forms/edit/" + schemaId) : "/forms/add"),
		identifier: form_identifier,
		//additionalModalClasses: "wider-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.schereg.view.async.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.schereg.view.async.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.triggerDeleteSchema = function(schemaId) {
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.schemas.dialog.confirm_detete"), schemaId), function(result) {
		if(result) {
			$.ajax({
		        url: window.location.pathname + "/async/delete/" + schemaId,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	__notifications.showMessage(NOTIFICATION_TYPES.INFO, 
		        			__translator.translate("~eu.dariah.de.minfba.schereg.schemas.notification.deleted.head"), 
		        			String.format(__translator.translate("~eu.dariah.de.minfba.schereg.schemas.notification.deleted.body"), schemaId));
		        	_this.refresh();
		        },
		        error: function(textStatus) {
		        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
		        			__translator.translate("~eu.dariah.de.minfba.schereg.view.async.servererror.head"), 
		        			__translator.translate("~eu.dariah.de.minfba.schereg.view.async.servererror.body"));
		        }
			});
		}
	}); //
};