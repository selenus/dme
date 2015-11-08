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
	                          "~eu.dariah.de.minfba.common.link.publish",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                          
	                          "~eu.dariah.de.minfba.schereg.dialog.confirm_delete",
	                          "~eu.dariah.de.minfba.schereg.notification.deleted.head",
	                          "~eu.dariah.de.minfba.schereg.notification.deleted.body",
	                          
	                          "~eu.dariah.de.minfba.schereg.dialog.confirm_publish",
	                          "~eu.dariah.de.minfba.schereg.notification.published.head",
	                          "~eu.dariah.de.minfba.schereg.notification.published.body",
	                          "~eu.dariah.de.minfba.schereg.notification.publish_error.head",
	                          "~eu.dariah.de.minfba.schereg.notification.publish_error.body"
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
			result += '<span class="label label-warning">Draft</span> ';
		} else {
			result += '<span class="label label-info">Public</span> ';
		}
	} else if (type==="filter" || type==="sort") {
		if (row.entity.draft) {
			result += 'draft ';
		} else {
			result += 'public ';
		}
	}
	return result;
};

SchemaTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		result += '<a href="' + __util.getBaseUrl() + 'schema/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
			__translator.translate("~eu.dariah.de.minfba.common.link.edit") +
		'</a> ';
		/*if (row.entity.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" onclick="schemaTable.triggerPublish(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span> ' + 
				__translator.translate("~eu.dariah.de.minfba.common.link.publish") +
			'</button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" onclick="schemaTable.triggerDelete(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> ' + 
				__translator.translate("~eu.dariah.de.minfba.common.link.delete") +
			'</button> ';
		}*/
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

SchemaTable.prototype.triggerPublish = function(schemaId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_publish"), schemaId), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "schema/async/publish/" + schemaId,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	if (data.success) {
		        		__notifications.showMessage(NOTIFICATION_TYPES.INFO, 
			        			__translator.translate("~eu.dariah.de.minfba.schereg.notification.published.head"), 
			        			String.format(__translator.translate("~eu.dariah.de.minfba.schereg.notification.published.body"), schemaId));
		        	} else {
		        		__notifications.showMessage(NOTIFICATION_TYPES.WARNING, 
			        			__translator.translate("~eu.dariah.de.minfba.schereg.notification.publish_error.head"), 
			        			String.format(__translator.translate("~eu.dariah.de.minfba.schereg.notification.publish_error.body"), schemaId));
		        	}		        	
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

SchemaTable.prototype.triggerDelete = function(schemaId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), schemaId), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "schema/async/delete/" + schemaId,
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