var mappingTable;
$(document).ready(function() {
	mappingTable = new MappingTable();
	
	$("#btn-add-mapping").click(function() { 
		mappingTable.triggerAdd(); 
	});
});

var MappingTable = function() {
	this.prepareTranslations(["~de.unibamberg.minf.common.link.delete",
	                          "~de.unibamberg.minf.common.link.view",
	                          "~de.unibamberg.minf.common.link.edit",
	                          "~de.unibamberg.minf.common.link.publish",
	                          "~de.unibamberg.minf.common.model.id",
	                          "~de.unibamberg.minf.common.view.forms.servererror.head",
	                          "~de.unibamberg.minf.common.view.forms.servererror.body",
	                          
	                          "~de.unibamberg.minf.common.model.stub",
	                          "~de.unibamberg.minf.common.model.draft",
	                          "~de.unibamberg.minf.common.model.public",
	                          "~de.unibamberg.minf.common.model.readonly"
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
	           "searchable": false,
	           "sortable" : false,
	           "class" : "td-no-wrap",
	           "data": function (row, type, val, meta) { return mappingTable.renderActionColumn(row, type, val, meta); }
	       }, {	
	    	   "targets": [4],
	    	   "data": "entity.pojo.sourceId",
	    	   "visible" : false
	       }, {	
	    	   "targets": [5],
	    	   "data": "entity.pojo.targetId",
	    	   "visible" : false
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
			result += '<span class="label label-warning">' + __translator.translate("~de.unibamberg.minf.common.model.draft") + '</span> ';
		} else {
			result += '<span class="label label-info">' + __translator.translate("~de.unibamberg.minf.common.model.public") + '</span> ';
		}
		
		if (row.entity.readOnly) {
			result += '<span class="label label-info">' + __translator.translate("~de.unibamberg.minf.common.model.readonly") + '</span> ';
		} 
	} else {
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

MappingTable.prototype.renderActionColumn = function(row, type, val, meta) {
	var result = "";	
	
	if (type==="display") {
		if (row.entity.own || row.entity.write || row.entity.share) {
			result += '<a href="' + __util.getBaseUrl() + 'mapping/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~de.unibamberg.minf.common.link.edit") +
			'</a> ';
		} else {
			result += '<a href="' + __util.getBaseUrl() + 'mapping/editor/' + row.entity.id + '/" class="btn btn-xs btn-default" type="button"><span class="glyphicon glyphicon-pencil"></span> ' + 
				__translator.translate("~de.unibamberg.minf.common.link.view") +
			'</a> ';
		}
		
		/*if (row.entity.draft) {
			result += '<button class="btn btn-xs btn-default hint-tooltip" onclick="mappingTable.triggerPublish(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Publish draft..."><span class="glyphicon glyphicon-export" aria-hidden="true"></span> ' +
				__translator.translate("~de.unibamberg.minf.common.link.publish") +
			'</button> ';
			result += '<button class="btn btn-xs btn-danger hint-tooltip" onclick="mappingTable.triggerDelete(\'' + row.entity.id + '\'); return false;" data-toggle="tooltip" data-placement="top" title="Delete draft..."><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> ' + 
				__translator.translate("~de.unibamberg.minf.common.link.delete") +
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
		setupCallback: function(form) {
			$(form).find("select.form-control").on("change", function(e) {
				if ($("#sourceId").val()==$("#targetId").val()) {
					var changeId = e.target.id=="sourceId" ? "#targetId" : "#sourceId";
					$(changeId).find("option").removeAttr("selected");
					$(changeId).find("option").each(function() {
						if ($(this).val()!=$(changeId).val()) {
							$(this).attr("selected", true);
						}
					});
				}
			})
		},
		//additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};