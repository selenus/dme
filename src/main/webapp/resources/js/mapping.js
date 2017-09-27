var editor;
$(document).ready(function() {
	editor = new MappingEditor();
	$("#btn-add-mapping").click(function() { editor.triggerAdd(); });
});

var MappingEditor = function() {
	this.prepareTranslations(["~de.unibamberg.minf.common.link.delete",
	                          "~de.unibamberg.minf.common.link.edit",
	                          "~de.unibamberg.minf.common.model.id",
	                          "~de.unibamberg.minf.common.view.forms.servererror.head",
	                          "~de.unibamberg.minf.common.view.forms.servererror.body",
	                          /*"~de.unibamberg.minf.dme.button.editor",
	                          "~de.unibamberg.minf.dme.dialog.confirm_detete",
	                          "~de.unibamberg.minf.dme.model.schema.description",
	                          "~de.unibamberg.minf.dme.model.schema.label",
	                          "~de.unibamberg.minf.dme.model.schema.draft",
	                          "~de.unibamberg.minf.dme.notification.deleted.head",
	                          "~de.unibamberg.minf.dme.notification.deleted.body"*/]);
	//this.loadChanges();
	this.createTable();
	this.assignTableEvents();
};


MappingEditor.prototype = new BaseTable();

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
						 },
						 {	"aTargets": [2], 
							"mData": "entity.id", 
		                 	"sClass": "td-no-wrap",
		                 	"bSortable": false,
							"bSearchable": true,
							"mRender": function (data, type, full) { return editor.renderActionColumn(data, type, full.entity); }
						 }]
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
						_this.handleSelection(selected[i]);
						hasSelected = true;
					});
				}
			} 
			if (!hasSelected) {
				_this.handleSelection(null);
			}
		});
	}
};

MappingEditor.prototype.assignTableEvents = function() {
	var _this = this;
	
    $('.data-table-filter input').on('keyup click', function () {
    	$('#schema-table').DataTable().search($('.data-table-filter input').val(), false, false).draw();
    });
    $(".data-table-filter input").trigger("keyup");
    
    $('.data-table-count select').on('change', function () {
    	var len = parseInt($('.data-table-count select').val());
    	if (isNaN(len)) {
    		len = -1; // Show all
    	}    	
    	_this._base.table.page.len(len).draw();
    });
    $(".data-table-count select").trigger("change");
    		
	$("#mapping-table tbody").on("click", "tr", function () {
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


MappingEditor.prototype.loadChanges = function(id) {
	var url;
	if (id==null || id==undefined) {
		url = "async/getChanges";
	} else {
		url = "async/getChangesForEntity/" + id;
	}
	
	
	$.ajax({
        url: window.location.pathname + url,
        type: "GET",
        dataType: "json",
        success: function(data) { __util.renderActivities("#mapping-activity", id, data); },
        error: function(textStatus) {
        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.head"), 
        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.body"));
        }
	});
}

MappingEditor.prototype.handleSelection = function(id) {
	var _this = this;
	
	if (id==null || id=="") {
		$('#tab-mapping-activity a').tab('show');
		$("#tab-mapping-metadata").addClass("hide");
		$("#tab-mapping-schemas").addClass("hide");
		$("#tab-mapping-elements").addClass("hide");
		$("#mapping-metadata").html("");
		$("#mapping-schemas").html("");
		$("#mapping-elements").html("");
		$("#mapping-activity").html("");
		_this.loadChanges();
	} else {
		$("#tab-mapping-metadata").removeClass("hide");
		$("#tab-mapping-schemas").removeClass("hide");
		$("#tab-mapping-elements").removeClass("hide");
		
		if ($("#tab-mapping-activity").hasClass("active")) {
			$('#tab-mapping-metadata a').tab('show');
		}
		
		$("#mapping-metadata").html("<div class=\"loader\">");
		//$("#schema-elements").html("<div class=\"loader\">");
		
		$.ajax({
	        url: window.location.pathname + "async/getData/" + id,
	        type: "GET",
	        dataType: "json",
	        success: function(data) { _this.renderMappingTab(id, data); },
	        error: function(textStatus) {
	        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
	        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.head"), 
	        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.body"));
	        }
		});
			
		_this.loadChanges(id);
	}
};

MappingEditor.prototype.renderMappingTab = function(id, data) {
	
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
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};