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
	                          "~eu.dariah.de.minfba.common.link.publish",
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
	    	   "data": "entity.pojo.sourceLabel",
	    	   "width" : "50%"
	       }, {	
	    	   "targets": [2],
	    	   "data": "entity.pojo.targetLabel",
	    	   "width" : "50%"
	       }, {	
	    	   "targets": [3],
	    	   "data": "entity.pojo.sourceId",
	    	   "visible" : false
	       }, {	
	    	   "targets": [4],
	    	   "data": "entity.pojo.targetId",
	    	   "visible" : false
	       }, {
	    	   "targets": [5],
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return mappingTable.renderActionColumn(row, type, val, meta); }
	       }
	   ]
	}, this.baseSettings));
};

/* Overrides the base abstract method */
MappingTable.prototype.handleSelection = function(id) { };

MappingTable.prototype.renderBadgeColumn = function(row, type, val, meta) {
	var result = "";	
	if (type=="display") {
		if (row.entity.draft) {
			result += '<span class="label label-warning">Draft</span> ';
		} else {
			result += '<span class="label label-info">Public</span> ';
		}
	}
	return result;
};

MappingTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		result += '<a href="' + __util.getBaseUrl() + 'mapping/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
			__translator.translate("~eu.dariah.de.minfba.common.link.edit") +
		'</a> ';
		/*if (row.entity.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" onclick="mappingTable.triggerPublish(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span> ' +
				__translator.translate("~eu.dariah.de.minfba.common.link.publish") +
			'</button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" onclick="mappingTable.triggerDelete(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> ' + 
				__translator.translate("~eu.dariah.de.minfba.common.link.delete") +
			'</button> ';
		}*/
	}
	return result;
};

MappingTable.prototype.triggerAdd = function () {
	this.triggerEdit();
};

MappingTable.prototype.triggerEdit = function(mappingId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-mapping-" + mappingId;
	var url = __util.getBaseUrl() + "mapping/" + (mappingId!=undefined ? ("forms/edit/" + mappingId) : "forms/add");
	
	modalFormHandler = new ModalFormHandler({
		formFullUrl: url,
		identifier: form_identifier,
		//additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};