var mappingTable;
$(document).ready(function() {
	mappingTable = new MappingTable();
	
	$("#btn-add-mapping").click(function() { 
		mappingTable.triggerAdd(); 
	});
});

var MappingTable = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.link.delete",
	                          "~eu.dariah.de.minfba.common.link.edit",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                          ]);
	this.createTable();
};

MappingTable.prototype = new BaseTable(__util.getBaseUrl() + "mapping/async/getData", "#mapping-table-container");

MappingTable.prototype.createTable = function() {
	this._base.table = $('#mapping-table').DataTable($.extend(true, {
		"order": [[1, "asc"]],
		"columnDefs": [
	       {
	           "targets": [0],
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return mappingTable.renderBadgeColumn(row, type, val, meta); }
	       }, {	
	    	   "targets": [1],
	    	   "data": "entity.pojo.sourceId",
	    	   "width" : "100%"
	       }, {	
	    	   "targets": [2],
	    	   "data": "entity.pojo.targetId",
	    	   "width" : "100%"
	       }, {
	    	   "targets": [3],
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return mappingTable.renderActionColumn(row, type, val, meta); }
	       }
	   ]
	}, this.baseSettings));
};

/* Overrides the base abstract method */
MappingTable.prototype.handleSelection = function(id) {
	console.log("mapping handle " + id);
};

MappingTable.prototype.renderBadgeColumn = function(row, type, val, meta) {
	var result = "";	
	if (type=="display") {
		if (row.entity.draft) {
			result += '<span class="label label-info">Draft</span> ';
		} else {
			result += '<span class="label label-success">Ok</span> ';
		}
	}
	return result;
};

MappingTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	if (type=="display") {
		result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button> ';
		result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span></button> ';
		if (row.entity.draft) {
			result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-export" aria-hidden="true"></span></button> ';
			result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> ';
		}
	}
	return result;
};