var schemaEditor;
$(document).ready(function() {
	schemaEditor = new SchemaEditor();
	
	$("#btn-add-schema").click(function() { 
		schemaEditor.triggerAdd(); 
	});
});

var SchemaEditor = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.link.delete",
	                          "~eu.dariah.de.minfba.common.link.edit",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                          ]);
	this.createTable();
};

SchemaEditor.prototype = new BaseTable(__util.getBaseUrl() + "schema/async/getData", "#schema-table-container");

SchemaEditor.prototype.createTable = function() {
	this._base.table = $('#schema-table').DataTable($.extend(true, {
		"order": [[1, "asc"]],
		"columnDefs": [
	       {
	           "targets": [0],
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return schemaEditor.renderBadgeColumn(row, type, val, meta); }
	       }, {
	    	   "targets": [1],
	    	   "data" : "entity.pojo.label",
	    	   "width" : "100%"
	       }, {
	    	   "targets": [2],
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return schemaEditor.renderActionColumn(row, type, val, meta); }
	       }
	   ]
	}, this.baseSettings));
};

/* Overrides the base abstract method */
SchemaEditor.prototype.handleSelection = function(id) {
	console.log("schema handle " + id);
};

SchemaEditor.prototype.renderBadgeColumn = function(row, type, val, meta) {
	var result = "";	
	if (type==="display") {
		if (row.entity.pojo.type=="BaseSchema") {
			result += '<span class="label label-warning">Stub</span> ';
		}		
		if (row.entity.draft) {
			result += '<span class="label label-info">Draft</span> ';
		}
	} else if (type==="filter" || type==="sort") {
		if (row.entity.draft) {
			result += 'draft ';
		}
	}
	return result;
};

SchemaEditor.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Edit metadata..."><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button> ';
		result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Open in schema editor..."><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span></button> ';
		if (row.entity.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span></button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> ';
		}
		
	}
	return result;
};
