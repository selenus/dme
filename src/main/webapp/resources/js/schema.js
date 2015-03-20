var editor;
$(document).ready(function() {
	editor = new SchemaEditor();
	$("#btn-add-schema").click(function() { editor.triggerAddSchema(); });
});

var SchemaEditor = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.schereg.view.async.servererror.head",
	                          "~eu.dariah.de.minfba.schereg.view.async.servererror.body",
	                          "~eu.dariah.de.minfba.schereg.schemas.dialog.confirm_detete",
	                          "~eu.dariah.de.minfba.schereg.schemas.notification.deleted.head",
	                          "~eu.dariah.de.minfba.schereg.schemas.notification.deleted.body"]);
	this.createTable();
};


SchemaEditor.prototype = new BaseEditor();

SchemaEditor.prototype.createTable = function() {
	this._base.table = $('#schema-table').dataTable({
		"aaSorting": [[ 1, "asc" ]],
		"aoColumnDefs": [{	"aTargets": [0], 
							"mData": "id",
							"bSortable": false,
							"bSearchable": false,
							"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return editor.renderBadgeColumn(data, type, full); }
						 },
		                 {	"aTargets": [1],
		                 	"mData": "label", 
		                 	"sClass": "td-no-wrap",
		                 	"sWidth" : "100%"
						 },
						 {	"aTargets": [2], 
							"mData": "type", 
						 },
		                 {	"aTargets": [3], 
							"mData": "id", 
							"bSortable": false,
							"bSearchable": false,
							"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return editor.renderActionColumn(data, type, full);}
						 }]
	});
};

SchemaEditor.prototype.renderBadgeColumn = function(data, type, full) {
	var result = "";
	
	if (full.type=="BaseSchema") {
		result = '<span class="label label-warning">Stub</span> ';
	}	
	return result;
};

SchemaEditor.prototype.renderActionColumn = function(data, type, full) {
	return 	"<button onclick='editor.triggerEditSchema(\"" + data + "\");'class='btn btn-default btn-xs' type='button'><span class='glyphicon glyphicon-edit'></span></button> " +
			"<button onclick='editor.triggerDeleteSchema(\"" + data + "\");' class='btn btn-default btn-xs' type='button'><span class='glyphicon glyphicon-trash'></span></button>";
};

SchemaEditor.prototype.triggerAddSchema = function () {
	this.triggerEditSchema();
};

SchemaEditor.prototype.triggerEditSchema = function(schemaId) {
	var _this = this;
	var form_identifier = "edit-schema-" + schemaId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: (schemaId!=undefined ? ("/async/edit/" + schemaId) : "/async/add"),
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