var editor;

$(document).ready(function() {
	editor = new SchemaEditor({
		footerOffset: 70,
		icons: {
			warning: __util.getBaseUrl() + "resources/img/warning.png",
			error: __util.getBaseUrl() + "resources/img/error.png",
			reusing: __util.getBaseUrl() + "resources/img/reuse.png",
			reused: __util.getBaseUrl() + "resources/img/reuse.png"
		}
	});
	$('[data-toggle="tooltip"]').tooltip( { container: 'body' });
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
	
	this.pathname = __util.getBaseUrl() + "model/editor/" + this.schema.id + "/";
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
	this.stateNotificationId = undefined;
	
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	
	
	
	__translator.addTranslations(["~de.unibamberg.minf.common.model.id",
	                              "~de.unibamberg.minf.common.model.label",
	                              "~de.unibamberg.minf.common.model.type",
	                              "~de.unibamberg.minf.common.link.edit",
	                              "~de.unibamberg.minf.common.link.view",
	                              "~de.unibamberg.minf.common.link.delete",
	                              "~de.unibamberg.minf.common.link.disable",
	                              "~de.unibamberg.minf.common.link.enable",
	                              "~de.unibamberg.minf.common.link.move_down",
	                              "~de.unibamberg.minf.common.link.move_up",
	                              "~de.unibamberg.minf.dme.button.add_nonterminal",
	                              "~de.unibamberg.minf.dme.button.add_label", 
	                              "~de.unibamberg.minf.dme.button.add_desc_function",
	                              "~de.unibamberg.minf.dme.button.add_trans_function",
	                              "~de.unibamberg.minf.dme.button.export_from_here",
	                              "~de.unibamberg.minf.dme.button.import_here",
	                              "~de.unibamberg.minf.dme.dialog.confirm_delete",
	                              "~de.unibamberg.minf.dme.dialog.confirm_delete_element",
	                              "~de.unibamberg.minf.dme.dialog.confirm_disable",
	                              "~de.unibamberg.minf.dme.dialog.confirm_enable",
	                              "~de.unibamberg.minf.dme.dialog.element_label",
	                              "~de.unibamberg.minf.dme.dialog.confirm_processing_root.head",
	                              "~de.unibamberg.minf.dme.dialog.confirm_processing_root.body",
	                              "~de.unibamberg.minf.common.view.forms.servererror.head",
	                              "~de.unibamberg.minf.common.view.forms.servererror.body",
	                              "~de.unibamberg.minf.dme.model.element.element",
	                              "~de.unibamberg.minf.dme.model.element.name",
	                              "~de.unibamberg.minf.dme.model.element.namespace",
	                              "~de.unibamberg.minf.dme.model.element.attribute",
	                              "~de.unibamberg.minf.dme.model.element.transient",
	                              "~de.unibamberg.minf.dme.model.function.function",
	                              "~de.unibamberg.minf.dme.model.grammar.grammar",
	                              "~de.unibamberg.minf.dme.notification.no_terminal_configured",
	                              
	                              "~de.unibamberg.minf.dme.button.show_reused",
	                              "~de.unibamberg.minf.dme.button.model_individually",
	                              "~de.unibamberg.minf.dme.dialog.confirm_clone_tree.head",
	                              "~de.unibamberg.minf.dme.dialog.confirm_clone_tree.body",
	                              
	                              "~de.unibamberg.minf.dme.button.expand_all",
	                              "~de.unibamberg.minf.dme.button.collapse_all",
	                              "~de.unibamberg.minf.dme.button.expand_from_here",
	                              "~de.unibamberg.minf.dme.button.collapse_from_here",
	                              "~de.unibamberg.minf.common.link.reload_data",
	                              "~de.unibamberg.minf.common.link.reset_view",
	                              "~de.unibamberg.minf.dme.button.export",
	                              "~de.unibamberg.minf.dme.button.import",
	                              "~de.unibamberg.minf.dme.button.create_root",
	                              "~de.unibamberg.minf.dme.button.set_processing_root",
	                              
	                              "~de.unibamberg.minf.dme.notification.import_error.head",
	                              "~de.unibamberg.minf.dme.notification.import_error.body",
	                              "~de.unibamberg.minf.dme.notification.import_processing.head",
	                              "~de.unibamberg.minf.dme.notification.import_processing.body",
	                              "~de.unibamberg.minf.dme.button.assign_child",
	                              
	                              "~de.unibamberg.minf.common.model.types.descriptiongrammarimpl",
	                              "~de.unibamberg.minf.common.model.types.labelimpl",
	                              "~de.unibamberg.minf.common.model.types.nonterminalimpl",
	                              "~de.unibamberg.minf.common.model.types.transformationfunctionimpl",
	                              
	                              "~de.unibamberg.minf.dme.dialog.confirm_publish",
	                              
	                              "~de.unibamberg.minf.dme.form.nature.terminals.missing",
	                              
	                              "~de.unibamberg.minf.dme.dialog.confirm_delete_nature"]);
	__translator.getTranslations();
	
	this.init();
}

SchemaEditor.prototype = new BaseEditor();

SchemaEditor.prototype.getEntityId = function() {
	return this.schema.id;
};

SchemaEditor.prototype.init = function() {
	this.initLayout();
	this.initGraph();
	
	this.initNatures();
	
	this.loadElementHierarchy();
	this.initSample(this.pathname, this.schema.id);
	this.loadActivitiesForEntity(this.schema.id, this.schemaActivitiesContainer);
	
	this.addVocabularySource("elements", "query/");
	
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
	if (this.layoutContainer.width()>800) {
		initEastClosed = false;
	}
	
	var initWestClosed = true;
	if (this.layoutContainer.width()>1100) {
		initWestClosed = false;
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
			size : "25%",
			paneSelector : ".layout-east",
			initClosed : initEastClosed,
		},
		west : {
			size : initWestClosed ? "40%" : "30%",
			paneSelector : ".layout-west",
			initClosed : initWestClosed
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
	_this.getElementDetails(_this.pathname, e.element.getType(), e.element.id, _this.elementContextDetail);
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
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
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
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
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
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],          
		completeCallback: function() {_this.reloadElementHierarchy();}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.moveGrammar = function(delta) {
	var _this = this;
	
	$.ajax({
	    url: _this.pathname + "/grammar/" + this.graph.selectedItems[0].id + "/move?delta=" + delta,
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	_this.reloadElementHierarchy();
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.editGrammar = function() {
	var _this = this;
	var form_identifier = "edit-grammar-" + this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/grammar/" + this.graph.selectedItems[0].id + "/form/edit",
		identifier: form_identifier,
		additionalModalClasses: "max-modal",
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		setupCallback: function(modal) { 
			grammarEditor = new GrammarEditor(modal, {
				pathPrefix: __util.getBaseUrl() + "model/editor/" + _this.schema.id,
				entityId : _this.schema.id,
				grammarId : _this.graph.selectedItems[0].id
			}); 
		},       
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
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
        setupCallback: function(modal) { 
        	functionEditor = new FunctionEditor(modal, {
				pathPrefix: __util.getBaseUrl() + "model/editor/" + _this.schema.id,
				entityId : _this.schema.id,
				functionId : _this.graph.selectedItems[0].id
			}); 
        },       
		completeCallback: function() { _this.reloadElementHierarchy(); }
	});
		
	modalFormHandler.show(form_identifier);
};


SchemaEditor.prototype.assignChild = function(elementId) {
	var _this = this;
	var form_identifier = "edit-function-" + elementId;//this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/element/" + elementId + "/form/assignChild",
		identifier: form_identifier,
		additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],      
		displayCallback: function(modal) { 
			_this.registerElementTypeahead($(modal).find("#child-element"));
		},
		completeCallback: function(data, modal) { 
			_this.reloadElementHierarchy(); 
		}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.setProcessingRoot = function(elementId) {
	var _this = this;
	
	bootbox.confirm({
				title: __translator.translate("~de.unibamberg.minf.dme.dialog.confirm_processing_root.head"),
				message: __translator.translate("~de.unibamberg.minf.dme.dialog.confirm_processing_root.body"),
				callback: function(result) {
					if(result) {
						$.ajax({
						    url: _this.pathname + "/element/" + elementId + "/async/setProcessingRoot",
						    type: "GET",
						    dataType: "json",
						    success: function(data) {
						    	_this.reloadElementHierarchy();
						    },
						    error: __util.processServerError
						});
					}
				}
	});
};

SchemaEditor.prototype.registerElementTypeahead = function(typeahead) {
	var _this = this;
	this.registerTypeahead(typeahead, "elements", _this.vocabularySources["elements"], "code", 8, 
			function(data) { return _this.showTypeaheadFoundResult(data); },
			function(t, suggestion) { 
				$(t).closest(".form-group").removeClass("has-error"); 
				$(t).closest(".form-content").find("#element-id").val(suggestion.id);
				$(t).closest(".form-content").find("#element-id-display").val(suggestion.id);
				$(t).closest(".form-content").find("#element-name").val(suggestion.name!==undefined ? suggestion.name : suggestion.grammarName);
			},
			null
	);
};

SchemaEditor.prototype.showTypeaheadFoundResult = function(data) {
	var result = '<p>' +
					'<strong>' + (data.name!==undefined ? data.name : data.grammarName) + '</strong>' +
					'<br/ >';
	
	if (data.simpleType==='Nonterminal') {
		result += 	 	'Nonterminal: ';
	} else if (data.simpleType==='Label') {
		result += 	 	'Label: ';
	} else if (data.simpleType==='Grammar') {
		result += 	 	'Grammar: ';
	} else if (data.simpleType==='Function') {
		result += 	 	'Function: ';
	}
	result += 	 data.id + '<p>';
	return result;
};

SchemaEditor.prototype.removeElement = function(type, id) { 
	var _this = this;
	
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_delete_element"), id), function(result) {
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

SchemaEditor.prototype.toggleElementDisabled = function(type, id, disable) { 
	var _this = this;
	
	bootbox.confirm(String.format((disable ? __translator.translate("~de.unibamberg.minf.dme.dialog.confirm_disable") : __translator.translate("~de.unibamberg.minf.dme.dialog.confirm_enable")), id), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/" + _this.getElementType(type) + "/" + id + "/async/disable",
			    data: { disabled: disable },
			    type: "GET",
			    dataType: "json",
			    success: function(data) {
			    	_this.reloadElementHierarchy();
			    },
			    error: __util.processServerError
			});
		}
	});
};

SchemaEditor.prototype.editTerminal = function(elementType, id) {
	var _this = this;
	var form_identifier = "edit-terminal" + id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/" + elementType.toLowerCase() + "/" + id + "/form/edit",
		data: { n: this.currentNature },
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],       
		completeCallback: function() { 
			_this.reloadElementHierarchy();
		}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.removeTerminal = function() {
	var terminalId = $("#terminalId").val();
	
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_delete"), terminalId), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/terminal/" + terminalId + "/async/remove",
			    data: { n: this.currentNature },
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
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		completeCallback: function(d) {
			if (d.statusInfo!==undefined && d.statusInfo!==null && d.statusInfo.length==24 && d.statusInfo!==_this.schema.id) {
				window.location.replace(__util.composeUrl("model/editor/" + d.statusInfo + "/"));
			} else {
				window.location.reload();
			}
		}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.triggerDeleteSchema = function() {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_delete"), _this.schema.id), function(result) {
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
		        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.head"), 
		        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.body"));
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
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_publish"), _this.schema.id), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "model/async/publish/" + _this.schema.id,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	window.location.reload();
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