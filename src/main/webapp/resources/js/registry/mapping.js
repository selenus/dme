var mappingEditor;
$(document).ready(function() {
	mappingEditor = new MappingEditor();
	
	$("#btn-add-mapping").click(function() { 
		mappingEditor.triggerAdd(); 
	});
});

var MappingEditor = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.link.delete",
	                          "~eu.dariah.de.minfba.common.link.edit",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                          ]);
	
	//this.loadChanges();
	this.createTable();
	//this.assignTableEvents();
};


MappingEditor.prototype = new BaseEditor(__util.getBaseUrl() + "mapping/async/getData");

MappingEditor.prototype.createTable = function() {
	this._base.table = $('#mapping-table').DataTable($.extend(true, {}, this.baseSettings, {
		"aaSorting": [[ 1, "asc" ]],
		"aoColumnDefs": [{	"aTargets": [0], 
							"mData": "entity.id",
							"bSortable": false,
							"bSearchable": true,
							"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return mappingEditor.renderBadgeColumn(data, type, full.entity); }
						 },
		                 {	"aTargets": [1],
		                 	"mData": "entity.pojo.sourceId",
		                 	"sWidth" : "100%"
						 },
						 {	"aTargets": [2],
		                 	"mData": "entity.pojo.targetId",
		                 	"sWidth" : "100%"
						 },
						 {	"aTargets": [3], 
							"mData": "entity.id", 
		                 	"sClass": "td-no-wrap",
		                 	"bSortable": false,
							"bSearchable": true,
							"mRender": function (data, type, full) { return mappingEditor.renderActionColumn(data, type, full.entity); }
						 }]
	}));
};

MappingEditor.prototype.renderBadgeColumn = function(data, type, full) {
	var result = "";	
	if (type=="display") {
		if (full.draft) {
			result += '<span class="label label-info">Draft</span> ';
		} else {
			result += '<span class="label label-success">Ok</span> ';
		}
	}
	return result;
};

MappingEditor.prototype.renderActionColumn = function(data, type, full) {
	var result = "";	
	if (type=="display") {
		result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-pencil" aria-hidden="true"></span></button> ';
		result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span></button> ';
		if (full.draft) {
			result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-export" aria-hidden="true"></span></button> ';
			result += '<button class="btn btn-xs btn-default"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></button> ';
		}
		
	}
	return result;
};