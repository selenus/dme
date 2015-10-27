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


SchemaEditor.prototype = new BaseEditor(__util.getBaseUrl() + "schema/async/getData", "#schema-table-container");

SchemaEditor.prototype.handleSelection = function(id) {
	console.log("schema handle " + id);
};

SchemaEditor.prototype.createTable = function() {
	this._base.table = $('#schema-table').DataTable($.extend(true, {
		"order": [[ 1, "asc" ]],
		"columnDefs": [
	       {
	           "targets": [ 0 ],
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return schemaEditor.renderBadgeColumn(val, type, row.entity, meta); }
	       }, {
	    	   "targets": [ 1 ],
	    	   "data" : "entity.pojo.label",
	    	   "width" : "100%"
	       }, {
	    	   "targets": [ 2 ],
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return schemaEditor.renderActionColumn(val, type, row.entity, meta); }
	       }
	   ]
	}, this.baseSettings));
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

SchemaEditor.prototype.renderBadgeColumn = function(data, type, entity, meta) {
	var result = "";	
	if (type==="display") {
		if (entity.pojo.type=="BaseSchema") {
			result += '<span class="label label-warning">Stub</span> ';
		}		
		if (entity.draft) {
			result += '<span class="label label-info">Draft</span> ';
		}
	} else if (type==="filter" || type==="sort") {
		if (entity.draft) {
			result += 'draft ';
		}
	}
	return result;
};

SchemaEditor.prototype.renderActionColumn = function(data, type, entity, meta) {
	var result = "";	
	
	if (type==="display") {
		result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Edit metadata..."><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button> ';
		result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Open in schema editor..."><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span></button> ';
		if (entity.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span></button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> ';
		}
		
	}
	return result;
};
