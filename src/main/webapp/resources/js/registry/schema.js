var schemaTable;
$(document).ready(function() {
	schemaTable = new SchemaTable();
	
	$("#btn-add-schema").click(function() { 
		schemaTable.triggerAdd(); 
	});
});

var SchemaTable = function() {
	this.prepareTranslations(["~de.unibamberg.minf.common.link.delete",
	                          "~de.unibamberg.minf.common.link.view",
	                          "~de.unibamberg.minf.common.link.edit",
	                          "~de.unibamberg.minf.common.link.publish",
	                          "~de.unibamberg.minf.common.model.id",
	                          
	                          "~de.unibamberg.minf.common.model.stub",
	                          "~de.unibamberg.minf.common.model.draft",
	                          "~de.unibamberg.minf.common.model.public",
	                          "~de.unibamberg.minf.common.model.readonly",
	                          
	                          "~de.unibamberg.minf.common.view.forms.servererror.head",
	                          "~de.unibamberg.minf.common.view.forms.servererror.body",
	                          
	                          "~de.unibamberg.minf.dme.dialog.confirm_delete",
	                          "~de.unibamberg.minf.dme.notification.deleted.head",
	                          "~de.unibamberg.minf.dme.notification.deleted.body",
	                          
	                          "~de.unibamberg.minf.dme.dialog.confirm_publish",
	                          "~de.unibamberg.minf.dme.notification.published.head",
	                          "~de.unibamberg.minf.dme.notification.published.body",
	                          "~de.unibamberg.minf.dme.notification.publish_error.head",
	                          "~de.unibamberg.minf.dme.notification.publish_error.body"
	                          ]);
	this.createTable();
};

SchemaTable.prototype = new BaseTable(__util.getBaseUrl() + "model/async/getData", "#schema-table-container");

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
	    	   "data" : "entity.pojo.name",
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
SchemaTable.prototype.handleSelection = function(id) { };

SchemaTable.prototype.renderBadgeColumn = function(row, type, val, meta) {
	var result = "";	
	if (type==="display") {
		if (row.entity.pojo.type=="BaseSchema") {
			result += '<span class="label label-warning">' + __translator.translate("~de.unibamberg.minf.common.model.stub") + '</span> ';
		}		
		if (row.entity.draft) {
			result += '<span class="label label-warning">' + __translator.translate("~de.unibamberg.minf.common.model.draft") + '</span> ';
		} else {
			result += '<span class="label label-info">' + __translator.translate("~de.unibamberg.minf.common.model.public") + '</span> ';
		}
		
		if (row.entity.readOnly) {
			result += '<span class="label label-info">' + __translator.translate("~de.unibamberg.minf.common.model.readonly") + '</span> ';
		} 
	} else if (type==="filter" || type==="sort") {
		if (row.entity.draft) {
			result += __translator.translate("~de.unibamberg.minf.common.model.draft") + " ";
		} else {
			result += __translator.translate("~de.unibamberg.minf.common.model.public" + " ");
		}
		if (row.entity.readOnly) {
			result += __translator.translate("~de.unibamberg.minf.common.model.draft" + " ");
		}
	}
	return result;
};

SchemaTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		if (row.entity.own || row.entity.write || row.entity.share) {
			result += '<a href="' + __util.getBaseUrl() + 'model/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~de.unibamberg.minf.common.link.edit") +
			'</a> ';
		} else {
			result += '<a href="' + __util.getBaseUrl() + 'model/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~de.unibamberg.minf.common.link.view") +
			'</a> ';
		}
		/*if (row.entity.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" onclick="schemaTable.triggerPublish(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span> ' + 
				__translator.translate("~de.unibamberg.minf.common.link.publish") +
			'</button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" onclick="schemaTable.triggerDelete(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> ' + 
				__translator.translate("~de.unibamberg.minf.common.link.delete") +
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
	var url = __util.getBaseUrl() + "model/" + (schemaId!=undefined ? ("forms/edit/" + schemaId) : "forms/add");
	
	modalFormHandler = new ModalFormHandler({
		formFullUrl: url,
		identifier: form_identifier,
		additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
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
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_publish"), schemaId), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "model/async/publish/" + schemaId,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	if (data.success) {
		        		__notifications.showMessage(NOTIFICATION_TYPES.INFO, 
			        			__translator.translate("~de.unibamberg.minf.dme.notification.published.head"), 
			        			String.format(__translator.translate("~de.unibamberg.minf.dme.notification.published.body"), schemaId));
		        	} else {
		        		__notifications.showMessage(NOTIFICATION_TYPES.WARNING, 
			        			__translator.translate("~de.unibamberg.minf.dme.notification.publish_error.head"), 
			        			String.format(__translator.translate("~de.unibamberg.minf.dme.notification.publish_error.body"), schemaId));
		        	}		        	
		        	_this.refresh();
		        },
		        error: function(textStatus) {
		        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
		        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.head"), 
		        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.body"));
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
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_delete"), schemaId), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "model/async/delete/" + schemaId,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	__notifications.showMessage(NOTIFICATION_TYPES.INFO, 
		        			__translator.translate("~de.unibamberg.minf.dme.notification.deleted.head"), 
		        			String.format(__translator.translate("~de.unibamberg.minf.dme.notification.deleted.body"), schemaId));
		        	_this.refresh();
		        },
		        error: function(textStatus) {
		        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
		        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.head"), 
		        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.body"));
		        }
			});
		}
	});
};