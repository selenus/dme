var schemaTable;
$(document).ready(function() {
	schemaTable = new SchemaTable();
	
	$("#btn-add-schema").click(function() { 
		schemaTable.triggerAdd(); 
	});
});

var SchemaTable = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.link.delete",
	                          "~eu.dariah.de.minfba.common.link.edit",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                          ]);
	this.createTable();
};

SchemaTable.prototype = new BaseTable(__util.getBaseUrl() + "schema/async/getData", "#schema-table-container");

SchemaTable.prototype.createTable = function() {
	this._base.table = $('#schema-table').DataTable($.extend(true, {
		"order": [[1, "asc"]],
		"columnDefs": [
	       {
	           "targets": [0],
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return schemaTable.renderBadgeColumn(row, type, val, meta); }
	       }, {
	    	   "targets": [1],
	    	   "data" : "entity.pojo.label",
	    	   "width" : "100%"
	       }, {
	    	   "targets": [2],
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return schemaTable.renderActionColumn(row, type, val, meta); }
	       }
	   ]
	}, this.baseSettings));
};

/* Overrides the base abstract method */
SchemaTable.prototype.handleSelection = function(id) {
	console.log("schema handle " + id);
};

SchemaTable.prototype.renderBadgeColumn = function(row, type, val, meta) {
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

SchemaTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		result += '<button class="btn btn-xs btn-default hint-tooltip" onclick="schemaTable.triggerEdit(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Edit metadata..."><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button> ';
		result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Open in schema editor..."><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span></button> ';
		if (row.entity.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span></button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> ';
		}
		
	}
	return result;
};

SchemaTable.prototype.triggerAdd = function () {
	this.triggerEdit();
};

SchemaTable.prototype.triggerEdit = function(schemaId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-schema-" + schemaId;
	var url = __util.getBaseUrl() + "schema/" + (schemaId!=undefined ? ("forms/edit/" + schemaId) : "forms/add");
	
	modalFormHandler = new ModalFormHandler({
		formFullUrl: url,
		identifier: form_identifier,
		additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};