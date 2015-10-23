var editor;
$(document).ready(function() {
	editor = new MappingEditor();
	$("#btn-add-mapping").click(function() { editor.triggerAdd(); });
});

var MappingEditor = function() {
	this.prepareTranslations(["~eu.dariah.de.minfba.common.link.delete",
	                          "~eu.dariah.de.minfba.common.link.edit",
	                          "~eu.dariah.de.minfba.common.model.id",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                          "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                          /*"~eu.dariah.de.minfba.schereg.button.editor",
	                          "~eu.dariah.de.minfba.schereg.dialog.confirm_detete",
	                          "~eu.dariah.de.minfba.schereg.model.schema.description",
	                          "~eu.dariah.de.minfba.schereg.model.schema.label",
	                          "~eu.dariah.de.minfba.schereg.model.schema.draft",
	                          "~eu.dariah.de.minfba.schereg.notification.deleted.head",
	                          "~eu.dariah.de.minfba.schereg.notification.deleted.body"*/]);
	//this.loadChanges();
	this.createTable();
	//this.assignTableEvents();
};


MappingEditor.prototype = new BaseEditor();

MappingEditor.prototype.createTable = function() {
	this._base.table = $('#mapping-table').DataTable({
		"aaSorting": [[ 1, "asc" ]],
		"aoColumnDefs": [{	"aTargets": [0], 
							"mData": "entity.id",
							"bSortable": false,
							"bSearchable": true,
							"sClass": "td-no-wrap",
							"mRender": function (data, type, full) { return editor.renderBadgeColumn(data, type, full.entity); }
						 },
		                 {	"aTargets": [1],
		                 	"mData": "entity.pojo.label",
		                 	"sWidth" : "100%"
						 }/*,
						 {	"aTargets": [2], 
							"mData": "entity.type", 
		                 	"sClass": "td-no-wrap"
						 }*/]
	});
};

MappingEditor.prototype.refresh = function() {
	var _this = this;
	
	if (!this.error && this.table!=null) {
		var selected = [];
		this._base.table.$("tr.selected").each(function() {
			selected.push($(this).prop("id"));
		});
				
		this.table.ajax.reload(function() {
			var hasSelected = false;
			if (selected.length>0) {
				for (var i=0; i<selected.length; i++) {
					$("#"+selected[i]).each(function() {
						$(this).addClass("selected");
						// Only executed if the row (id) still exists
						//_this.handleSelection(selected[i]);
						hasSelected = true;
					});
				}
			} 
			if (!hasSelected) {
				//_this.handleSelection(null);
			}
		});
	}
};

MappingEditor.prototype.renderBadgeColumn = function(data, type, full) {
	var result = "";	
	if (type=="display") {
		
	}
	
	return result;
};

MappingEditor.prototype.triggerAdd = function () {
	this.triggerEdit();
};

MappingEditor.prototype.triggerEdit = function(mappingId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-mapping-" + mappingId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: (mappingId!=undefined ? ("forms/edit/" + mappingId) : "forms/add"),
		identifier: form_identifier,
		//additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};