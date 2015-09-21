var editor;
$(document).ready(function() {
	editor = new SchemaEditor();
	$("#btn-add-schema").click(function() { editor.triggerAddSchema(); });
});

var SchemaEditor = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.link.delete",
	                          "~eu.dariah.de.minfba.common.link.edit",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                          "~eu.dariah.de.minfba.schereg.button.editor",
	                          "~eu.dariah.de.minfba.schereg.dialog.confirm_detete",
	                          "~eu.dariah.de.minfba.schereg.model.schema.description",
	                          "~eu.dariah.de.minfba.schereg.model.schema.label",
	                          "~eu.dariah.de.minfba.schereg.model.schema.draft",
	                          "~eu.dariah.de.minfba.schereg.notification.deleted.head",
	                          "~eu.dariah.de.minfba.schereg.notification.deleted.body"]);
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
							"bSearchable": true,
							"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return editor.renderBadgeColumn(data, type, full.entity); }
						 },
		                 {	"aTargets": [1],
		                 	"mData": "entity.pojo.label",
		                 	"sWidth" : "100%"
						 }/*,
						 {	"aTargets": [2], 
							"mData": "entity.type", 
		                 	"sClass": "td-no-wrap"
						 }*/]
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
	
	if (id==null || id=="") {
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
	        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.head"), 
	        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.body"));
	        }
		});
		
		$.ajax({
	        url: window.location.pathname + "/async/getElements/" + id,
	        type: "GET",
	        dataType: "json",
	        success: function(data) { _this.renderSchemaElementsTab(id, data); },
	        error: function(textStatus) {
	        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
	        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.head"), 
	        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.body"));
	        }
		});
	}
};

SchemaEditor.prototype.renderSchemaMetadataTab = function(id, data) {
	$("#schema-metadata").html("");
	
	
	var buttonBarContainer = $("<div class=\"row\">");
	var buttonBar = $("<div class=\"tab-buttons col-xs-9 col-md-8 col-xs-offset-3 col-md-offset-4\">");
	
	if (true || data.write || data.own) {
		buttonBar.append(
			"<button onclick='editor.triggerEditSchema(\"" + id + "\");'class='btn btn-default btn-sm' type='button'><span class='glyphicon glyphicon-edit'></span> " + 
				__translator.translate("~eu.dariah.de.minfba.common.link.edit") + 
			"</button> ");
		buttonBar.append(
			"<button onclick='editor.triggerDeleteSchema(\"" + id + "\");' class='btn btn-danger btn-sm' type='button'><span class='glyphicon glyphicon-trash'></span> " +
				__translator.translate("~eu.dariah.de.minfba.common.link.delete") +
			"</button>");
	} else {
		buttonBar.append(
			"<button class='btn btn-default btn-sm disabled' type='button'><span class='glyphicon glyphicon-lock'></span> " + 
				__translator.translate("~eu.dariah.de.minfba.common.link.edit") + 
			"</button> ");
		buttonBar.append(
			"<button class='btn btn-danger btn-sm disabled' type='button'><span class='glyphicon glyphicon-lock'></span> " +
				__translator.translate("~eu.dariah.de.minfba.common.link.delete") +
			"</button>");
	}
	
	
	
	buttonBarContainer.append(buttonBar);
	
	$("#schema-metadata").append(buttonBarContainer);
	
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderSchemaMetadataTabDetail( __translator.translate("~eu.dariah.de.minfba.common.model.id"), data.pojo.id));
	details.append(this.renderSchemaMetadataTabDetail( __translator.translate("~eu.dariah.de.minfba.schereg.model.schema.label"), data.pojo.label));
	details.append(this.renderSchemaMetadataTabDetail( __translator.translate("~eu.dariah.de.minfba.schereg.model.schema.description"), data.pojo.description));
	details.append(this.renderSchemaMetadataTabDetail( __translator.translate("~eu.dariah.de.minfba.schereg.model.schema.draft"), data.draft));
		
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
	var buttonBar = $("<div class=\"tab-buttons col-xs-9 col-md-8 col-xs-offset-3 col-md-offset-4\">");
	
	// TODO: Move import behavior
	/*buttonBar.append(
			"<button onclick='editor.triggerUploadFile(\"" + id + "\");'class='btn btn-default btn-sm' type='button'><span class='glyphicon glyphicon-edit'></span> " + 
				__translator.translate("~eu.dariah.de.minfba.schereg.button.import") + 
			"</button> ");*/
	buttonBar.append(
			"<a href='" + __util.getBaseUrl() + "schema/editor/" + id + "' class='btn btn-link btn-sm' type='button'>" + 
				__translator.translate("~eu.dariah.de.minfba.schereg.button.editor") + 
			" <span class='glyphicon glyphicon-new-window'></span></a> ");
	
	buttonBarContainer.append(buttonBar);
	
	
	
	
	$("#schema-elements").append(buttonBarContainer);
};

SchemaEditor.prototype.renderBadgeColumn = function(data, type, full) {
	var result = "";	
	if (type=="display") {
		if (full.pojo.type=="BaseSchema") {
			result += '<span class="label label-warning">Stub</span> ';
		}		
		if (full.draft) {
			result += '<span class="label label-info">Draft</span> ';
		}
	} else {
		if (full.draft) {
			result += 'draft ';
		}
	}
	
	return result;
};


SchemaEditor.prototype.triggerAddSchema = function () {
	this.triggerEditSchema();
};

SchemaEditor.prototype.triggerEditSchema = function(schemaId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-schema-" + schemaId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: (schemaId!=undefined ? ("/forms/edit/" + schemaId) : "/forms/add"),
		identifier: form_identifier,
		additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.triggerDeleteSchema = function(schemaId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_detete"), schemaId), function(result) {
		if(result) {
			$.ajax({
		        url: window.location.pathname + "/async/delete/" + schemaId,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	__notifications.showMessage(NOTIFICATION_TYPES.INFO, 
		        			__translator.translate("~eu.dariah.de.minfba.schereg.notification.deleted.head"), 
		        			String.format(__translator.translate("~eu.dariah.de.minfba.schereg.notification.deleted.body"), schemaId));
		        	_this.refresh();
		        },
		        error: function(textStatus) {
		        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
		        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.head"), 
		        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.body"));
		        }
			});
		}
	});
};