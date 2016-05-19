var vocabularyTable;
$(document).ready(function() {
	vocabularyTable = new VocabularyTable();
	
	$("#btn-add-vocabulary").click(function() { 
		vocabularyTable.triggerAdd(); 
	});
});

var VocabularyTable = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.link.delete",
	                          "~eu.dariah.de.minfba.common.link.view",
	                          "~eu.dariah.de.minfba.common.link.edit",
	                          "~eu.dariah.de.minfba.common.link.publish",
	                          "~eu.dariah.de.minfba.common.link.ok",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          
	                          "~eu.dariah.de.minfba.common.model.stub",
	                          "~eu.dariah.de.minfba.common.model.draft",
	                          "~eu.dariah.de.minfba.common.model.public",
	                          "~eu.dariah.de.minfba.common.model.readonly",
	                          
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
	this.itemTable = null;
	this.createTable();
	var _this = this;
	
	$("#vocabulary-table-container").find("tbody").on("click", "tr", function () {
        if ($(this).hasClass("selected")) {
            $(this).removeClass("selected");
            _this.handleSelection(null);
        } else {
        	_this._base.table.$("tr.selected").removeClass("selected");
            $(this).addClass("selected");
            _this.handleSelection($(this).prop("id"));
        }
    });
};
VocabularyTable.prototype = new BaseTable(__util.getBaseUrl() + "vocabulary/async/getData", "#vocabulary-table-container");

/* Overrides the base abstract method */
VocabularyTable.prototype.handleSelection = function(id) {
	if (id==null) {
		$("#vocabulary-item-table-hide").removeClass("hide");
		$("#vocabulary-item-table-display").addClass("hide");
	} else {
		$("#vocabulary-item-table-hide").addClass("hide");
		$("#vocabulary-item-table-display").removeClass("hide");
		this.itemTable = new VocabularyItemTable(id);
	}
};

VocabularyTable.prototype.createTable = function() {
	this._base.table = $('#vocabulary-table').DataTable($.extend(true, {
		"order": [[1, "asc"]],
		"columnDefs": [
	       {
	           "targets": [0],
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return vocabularyTable.renderBadgeColumn(row, type, val, meta); }
	       }, {
	    	   "targets": [1],
	    	   "data" : "entity.label",
	    	   "width" : "100%"
	       }, {
	    	   "targets": [2],
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return vocabularyTable.renderActionColumn(row, type, val, meta); }
	       }
	   ]
	}, this.baseSettings));
};

VocabularyTable.prototype.renderBadgeColumn = function(row, type, val, meta) {
	var result = "";	
	if (type==="display") {
		result += '<span class="label label-info">' + __translator.translate("~eu.dariah.de.minfba.common.link.ok") + '</span> ';
	} else if (type==="filter" || type==="sort") {
		result += __translator.translate("~eu.dariah.de.minfba.common.link.ok" + " ");
	}
	return result;
};

VocabularyTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		//if (row.entity.own || row.entity.write || row.entity.share) {
			result += '<button onclick="vocabularyTable.triggerEdit(\'' + row.entity.id + '\');" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~eu.dariah.de.minfba.common.link.edit") +
			'</button> ';
		/*} else {
			result += '<a href="' + __util.getBaseUrl() + 'schema/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~eu.dariah.de.minfba.common.link.view") +
			'</a> ';
		}*/
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

VocabularyTable.prototype.triggerAdd = function () {
	this.triggerEdit();
};

VocabularyTable.prototype.triggerEdit = function(vocabularyId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-vocabulary-" + vocabularyId;
	var url = __util.getBaseUrl() + "vocabulary/" + (vocabularyId!=undefined ? ("forms/edit/" + vocabularyId) : "forms/add");
	
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


/**  
 *  Vocabulary item table
 */
var VocabularyItemTable = function(id) {
	this.createTable(id);
};
VocabularyItemTable.prototype = new BaseTable(null, "#vocabulary-table-container");
VocabularyItemTable.prototype.createTable = function(id) {
	var _this = this;
	
	this.id = id;
	this._base.table = $('#vocabulary-item-table').DataTable($.extend(true, {}, this.baseSettings, {
		"destroy": true,
		"ajax": {
			"url" : window.location.pathname + _this.id + "/async/getData/"
		},
		"order": [[1, "asc"]],
		"columnDefs": [
	       {
	           "targets": [0],
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return _this.renderBadgeColumn(row, type, val, meta); }
	       }, {
	    	   "targets": [1],
	    	   "data" : "entity.label",
	    	   "width" : "100%"
	       }, {
	    	   "targets": [2],
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return _this.renderActionColumn(row, type, val, meta); }
	       }
	   ]
	}));
};

VocabularyItemTable.prototype.renderBadgeColumn = function(row, type, val, meta) {
	var result = "";	
	if (type==="display") {
		result += '<span class="label label-info">' + __translator.translate("~eu.dariah.de.minfba.common.link.ok") + '</span> ';
	} else if (type==="filter" || type==="sort") {
		result += __translator.translate("~eu.dariah.de.minfba.common.link.ok" + " ");
	}
	return result;
};

VocabularyItemTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		//if (row.entity.own || row.entity.write || row.entity.share) {
			result += '<button onclick="vocabularyTable.itemTable.triggerEdit(\'' + row.entity.id + '\');" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~eu.dariah.de.minfba.common.link.edit") +
			'</button> ';
		/*} else {
			result += '<a href="' + __util.getBaseUrl() + 'schema/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~eu.dariah.de.minfba.common.link.view") +
			'</a> ';
		}*/
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

VocabularyItemTable.prototype.triggerAdd = function () {
	this.triggerEdit();
};

VocabularyItemTable.prototype.triggerEdit = function(itemId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-vocabulary-item-" + itemId;
	var url = __util.getBaseUrl() + "vocabulary/" + _this.id + "/" + (itemId!=undefined ? ("forms/edit/" + itemId) : "forms/add");
	
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

