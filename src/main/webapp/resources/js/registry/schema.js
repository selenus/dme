var schemaEditor;
$(document).ready(function() {
	schemaEditor = new SchemaEditor();
	
	$("#btn-add-schema").click(function() { 
		schemaEditor.triggerAdd(); 
	});
});

var SchemaEditor = function() {
	this.containerSelector = "#schema-table-container";
	
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

	//this.loadChanges();
	this.createTable();
	//this.assignTableEvents();
};


SchemaEditor.prototype = new BaseEditor(__util.getBaseUrl() + "schema/async/getData");

SchemaEditor.prototype.createTable = function() {
	this._base.table = $('#schema-table').DataTable($.extend(true, {}, this.baseSettings, {
		"aaSorting": [[ 1, "asc" ]],
		"aoColumnDefs": [{	"aTargets": [0], 
							"mData": "entity.id",
							"bSortable": false,
							"bSearchable": true,
							"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return schemaEditor.renderBadgeColumn(data, type, full.entity); }
						 },
		                 {	"aTargets": [1],
		                 	"mData": "entity.pojo.label",
		                 	"sWidth" : "100%"
						 },
						 {	"aTargets": [2], 
							"mData": "entity.id", 
		                 	"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return schemaEditor.renderActionColumn(data, type, full.entity); }
						 }]
	}));
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
						//_this.handleSelection(selected[i]);
						hasSelected = true;
					});
				}
			} 
			if (!hasSelected) {
				//_this.handleSelection(null);
			}
		});
	}
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

SchemaEditor.prototype.renderActionColumn = function(data, type, full) {
	var result = "";	
	if (type=="display") {
		result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Edit metadata..."><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button> ';
		result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Open in schema editor..."><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span></button> ';
		if (full.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span></button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> ';
		}
		
	}
	return result;
};
