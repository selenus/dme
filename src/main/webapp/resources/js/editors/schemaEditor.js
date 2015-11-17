var editor;

$(document).ready(function() {
	editor = new SchemaEditor({
		footerOffset: 70,
		icons: {
			warning: __util.getBaseUrl() + "resources/img/warning.png",
			error: __util.getBaseUrl() + "resources/img/error.png"
		}
	});
	$('[data-toggle="tooltip"]').tooltip();
});
$(window).resize(function() {
	editor.resize();
});

var SchemaEditor = function(options) {	
	this.options = options;
	this.schema = { 
			id: $("#schema-id").val(),
			owned: $("#schema-own").val()==="true",
			write: $("#schema-write").val()==="true"
	}
	
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schema.id + "/";
	this.context = document.getElementById("schema-editor-canvas").getContext("2d");

	this.layout = null;
	this.layoutContainer = $(".editor-layout-container");
	this.Container = $(".editor-container");
		
	this.schemaContextContainer = $("#schema-context-container");
	this.schemaContextButtons = $("#schema-context-buttons");
	
	this.elementContextContainer = $("#schema-element-context-container");
	this.elementContextDetail = $("#schema-element-context-info");
	this.elementContextButtons = $("#schema-element-context-buttons");
	
	this.schemaActivitiesContainer = $("#schema-context-activities");
	this.elementActivitiesContainer = $("#schema-element-context-activities");
	
	this.logArea = null;
	
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	
	
	
	__translator.addTranslations(["~eu.dariah.de.minfba.common.model.id",
	                              "~eu.dariah.de.minfba.common.model.label",
	                              "~eu.dariah.de.minfba.common.model.type",
	                              "~eu.dariah.de.minfba.common.link.edit",
	                              "~eu.dariah.de.minfba.common.link.view",
	                              "~eu.dariah.de.minfba.common.link.delete",
	                              "~eu.dariah.de.minfba.schereg.button.add_nonterminal",
	                              "~eu.dariah.de.minfba.schereg.button.add_label", 
	                              "~eu.dariah.de.minfba.schereg.button.add_desc_function",
	                              "~eu.dariah.de.minfba.schereg.button.add_trans_function",
	                              "~eu.dariah.de.minfba.schereg.dialog.confirm_delete",
	                              "~eu.dariah.de.minfba.schereg.dialog.element_label",
	                              "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                              "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                              "~eu.dariah.de.minfba.schereg.model.element.element",
	                              "~eu.dariah.de.minfba.schereg.model.element.name",
	                              "~eu.dariah.de.minfba.schereg.model.element.namespace",
	                              "~eu.dariah.de.minfba.schereg.model.element.attribute",
	                              "~eu.dariah.de.minfba.schereg.model.element.transient",
	                              "~eu.dariah.de.minfba.schereg.model.function.function",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.grammar",
	                              "~eu.dariah.de.minfba.schereg.notification.no_terminal_configured",
	                              
	                              "~eu.dariah.de.minfba.schereg.button.expand_all",
	                              "~eu.dariah.de.minfba.schereg.button.collapse_all",
	                              "~eu.dariah.de.minfba.schereg.button.expand_from_here",
	                              "~eu.dariah.de.minfba.schereg.button.collapse_from_here",
	                              "~eu.dariah.de.minfba.common.link.reload_data",
	                              "~eu.dariah.de.minfba.common.link.reset_view",
	                              "~eu.dariah.de.minfba.schereg.button.export",
	                              "~eu.dariah.de.minfba.schereg.button.import",
	                              "~eu.dariah.de.minfba.schereg.button.create_root"]);
	__translator.getTranslations();
	
	this.init();
}

SchemaEditor.prototype = new BaseEditor();

SchemaEditor.prototype.init = function() {
	this.initLayout();
	this.initGraph();
	
	this.loadElementHierarchy();
	this.initSample(this.pathname, this.schema.id);
	this.loadActivitiesForEntity(this.schema.id, this.schemaActivitiesContainer);
	
	var _this = this;
	this.logArea = new LogArea({
		pathPrefix :  __util.getBaseUrl() + "sessions/",
		entityId : _this.schema.id
	});
	
	this.resize();
};

SchemaEditor.prototype.initLayout = function() {
	var _this = this;
	this.layoutContainer.removeClass("hide").addClass("fade");
	
	var initEastClosed = true;
	if (this.layoutContainer.width()>1100) {
		initEastClosed = false;
	}
	
	var initSouthClosed = true;
	if ($(window).height()>800) {
		initSouthClosed = false;
	}
	this.layout = this.layoutContainer.layout({
		defaults : {
			fxName : "slide",
			fxSpeed : "slow",
			spacing_closed : 14,
			minWidth : 200,
		},
		center : {
			size : initEastClosed ? "60%" : "40%",
			paneSelector : ".layout-center",
			minHeight : 200
		},
		east : {
			size : initEastClosed ? "0%" : "30%",
			paneSelector : ".layout-east",
			initClosed : initEastClosed,
		},
		west : {
			size : initEastClosed ? "40%" : "30%",
			paneSelector : ".layout-west",
			initClosed : initEastClosed,
			onopen_start: function () { _this.sample_onPaneOpenStart(); }
		},
		south : { 
			size : 100, 
			initClosed : initSouthClosed,
			paneSelector : ".layout-south" 
		},
		onresize: function () {
			_this.resizeContent();
	        return false;
	    }
	});
	this.layoutContainer.removeClass("fade");
};

SchemaEditor.prototype.resize = function() {
	this.resizeLayout();
	this.resizeContent();
};

SchemaEditor.prototype.resizeLayout = function() {
	var height = Math.floor($(window).height() - this.layoutContainer.offset().top - this.options.footerOffset);
	
	this.layoutContainer.height(height);
	this.layout.resizeAll();
}

SchemaEditor.prototype.resizeContent = function() {
	var layoutBottom = this.layout.center.state.layoutHeight + this.layout.center.state.offsetTop;
	var paddingBottom = this.layout.center.state.css.paddingBottom+2; // +2 just to be sure
	
	$(".height-sized-element").each(function() {
		var pane = $(this).closest(".layout-pane");
		var innerHeight = $(pane).innerHeight() + $(pane).offset().top - $(this).offset().top - parseInt($(pane).css("padding-bottom"), 10);
		var minHeight = parseInt($(this).css("min-height"));
		
		if (innerHeight < minHeight) {
			innerHeight = minHeight;
		} else if (innerHeight < 0) {
			innerHeight = 0;
		}
		$(this).css("height", Math.floor(innerHeight) + "px");
	});

	if (this.context.canvas) {
		this.context.canvas.width = this.Container.innerWidth();
		this.context.canvas.height = this.Container.innerHeight();		
		if (this.graph !== null) {
			this.graph.update();
		}
	}
};

SchemaEditor.prototype.deselectionHandler = function() {
	var _this = editor;
	
	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	_this.elementContextContainer.addClass("hide");
	_this.schemaContextContainer.removeClass("hide");
	_this.loadActivitiesForEntity(_this.schema.id, _this.schemaActivitiesContainer);
};

SchemaEditor.prototype.selectionHandler = function(e) {
	var _this = editor;

	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	_this.createActionButtons(_this.elementContextButtons, e.element.getContextMenuItems(), "editor");
	
	if (e.element.getType()==="Nonterminal") {
		_this.getElementDetails(_this.pathname, e.element.getType(), e.element.id, _this.elementContextDetail, _this.processTerminalElement);
	} else {
		_this.getElementDetails(_this.pathname, e.element.getType(), e.element.id, _this.elementContextDetail);
	}
	
	
	_this.loadActivitiesForElement(e.element.id, _this.elementActivitiesContainer);
	
	_this.elementContextContainer.removeClass("hide");
	_this.schemaContextContainer.addClass("hide");
};



SchemaEditor.prototype.addDescription = function(id) {
	this.addNode("element", "grammar", id);
};

SchemaEditor.prototype.addTransformation = function(id) {
	this.addNode("grammar", "function", id);
};

SchemaEditor.prototype.addNonterminal = function(id) {
	this.addNode("element", "nonterminal", id);
};

SchemaEditor.prototype.addLabel = function(id) {
	this.addNode("element", "label", id);
};


SchemaEditor.prototype.addNode = function(type, childType, id) {
	var _this = this;
	var form_identifier = "edit-element-" + id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/" + type + "/" + id + "/form/new_" + childType,
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],               
        completeCallback: function() {
			_this.reloadElementHierarchy(function() {
				_this.area.expandFromElement(id, true);
			});
		}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.createRoot = function() {
	var _this = this;
	var form_identifier = "edit-root";
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/form/createRoot",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],               
		completeCallback: function() {
	    	_this.reloadElementHierarchy();
		}
	});
		
	modalFormHandler.show(form_identifier)
};

SchemaEditor.prototype.editElement = function(id) {
	var _this = this;
	var form_identifier = "edit-element-" + id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/element/" + id + "/form/element",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],          
		completeCallback: function() {_this.reloadElementHierarchy();}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.editGrammar = function() {
	var _this = this;
	var form_identifier = "edit-grammar-" + this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/grammar/" + this.graph.selectedItems[0].id + "/form/edit",
		identifier: form_identifier,
		additionalModalClasses: "max-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		setupCallback: function(modal) { grammarEditor = new GrammarEditor(modal, {
			pathPrefix: __util.getBaseUrl() + "schema/editor/" + _this.schema.id,
			entityId : _this.schema.id,
			grammarId : _this.graph.selectedItems[0].id
		}); },       
		completeCallback: function() { _this.reloadElementHierarchy(); }
	});
		
	modalFormHandler.show(form_identifier);
};


SchemaEditor.prototype.editFunction = function() {
	var _this = this;
	var form_identifier = "edit-function-" + this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/function/" + this.graph.selectedItems[0].id + "/form/edit",
		identifier: form_identifier,
		additionalModalClasses: "max-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
        setupCallback: function(modal) { functionEditor = new FunctionEditor(modal, {
			pathPrefix: __util.getBaseUrl() + "schema/editor/" + _this.schema.id,
			entityId : _this.schema.id,
			functionId : _this.graph.selectedItems[0].id
		}); },       
		completeCallback: function() { _this.reloadElementHierarchy(); }
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.removeElement = function(type, id) { 
	var _this = this;
	
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), id), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/" + _this.getElementType(type) + "/" + id + "/async/remove",
			    type: "GET",
			    dataType: "json",
			    success: function(data) {
			    	_this.graph.deselectAll();
			    	_this.reloadElementHierarchy();
			    },
			    error: __util.processServerError
			});
		}
	});
};

SchemaEditor.prototype.addTerminal = function() {
	this.innerEditTerminal("-1");
};

SchemaEditor.prototype.editTerminal = function() {
	var terminalId = $("#terminalId").val();
	this.innerEditTerminal(terminalId);
};

SchemaEditor.prototype.innerEditTerminal = function(id) {
	var _this = this;
	var form_identifier = "edit-terminal" + id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/terminal/" + id + "/form/edit",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],       
		completeCallback: function() { _this.updateTerminalList(); }
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.removeTerminal = function() {
	var terminalId = $("#terminalId").val();
	
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), terminalId), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/terminal/" + terminalId + "/async/remove",
			    type: "GET",
			    dataType: "json",
			    success: function(data) {
			    	_this.graph.update();
			    	_this.updateTerminalList();
			    }
			});
		}
	});
};

SchemaEditor.prototype.updateTerminalList = function() {
	var _this = this;
	$(".form-btn-submit").prop("disabled", "disabled");
		
	$.ajax({
	    url: _this.pathname + "/async/getTerminals",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	$("#terminalId option.schema-terminal").remove();
	    	for (var i=0; i<data.length; i++) {
	    		$("#terminalId").append("<option class='schema-terminal' value='" + data[i].id + "'>" + 
	    				data[i].name + " (" + data[i].namespace + ")" + 
	    			"</option>");
	    	}
	    	
	    	$(".form-btn-submit").removeProp("disabled");
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.exportSchema = function() {
	var _this = this;

	$.ajax({
	    url: _this.pathname + "/async/export",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	blob = new Blob([JSON.stringify(data.pojo)], {type: "application/json; charset=utf-8"});
	    	saveAs(blob, "schema_" + _this.schemaId + ".json");
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.triggerUploadFile = function() {
	var _this = this;
	var form_identifier = "upload-file-" + this.schema.id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/forms/import/",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.reloadElementHierarchy();}
	});
	
	modalFormHandler.fileUploadElements.push({
		selector: "#schema_source",				// selector for identifying where to put widget
		formSource: "/forms/fileupload",		// where is the form
		uploadTarget: "/async/upload", 			// where to we upload the file(s) to
		multiFiles: false, 						// one or multiple files
		elementChangeCallback: _this.handleFileValidatedOrFailed
	});
		
	modalFormHandler.show(form_identifier);
};


SchemaEditor.prototype.handleFileValidatedOrFailed = function(data) {
	var select = $("#schema_root");
	select.html("");
	if (data==null || data.pojo==null || data.pojo.length==0) {
		select.prop("disabled", "disabled");
		$("#btn-submit-schema-elements").prop("disabled", "disabled");
		return;
	}
	
	var option;
	for (var i=0; i<data.pojo.length; i++) {
		option = "<option value='" + i + "'>" + data.pojo[i].name + " <small>(" + data.pojo[i].namespace + ")</small>" + "</option>";
		select.append(option);
	}
	select.removeProp("disabled");
	$("#btn-submit-schema-elements").removeProp("disabled");
};

SchemaEditor.prototype.triggerEditSchema = function() {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-schema-" + _this.schema.id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/forms/edit/",
		identifier: form_identifier,
		additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() { window.location.reload(); }
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.triggerDeleteSchema = function() {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), _this.schema.id), function(result) {
		if(result) {
			$.ajax({
		        url: _this.pathname + "/async/delete/",
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	window.location.reload();
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

SchemaEditor.prototype.triggerPublish = function() {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_publish"), _this.schema.id), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "schema/async/publish/" + _this.schema.id,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	window.location.reload();
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