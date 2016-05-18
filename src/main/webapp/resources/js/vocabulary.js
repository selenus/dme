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
	this.createTable();
};

VocabularyTable.prototype = new BaseTable(__util.getBaseUrl() + "vocabulary/async/getData", "#vocabulary-table-container");

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
		result += '<span class="label label-info">' + __translator.translate("~eu.dariah.de.minfba.common.model.public") + '</span> ';
	} else if (type==="filter" || type==="sort") {
		result += __translator.translate("~eu.dariah.de.minfba.common.model.public" + " ");
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