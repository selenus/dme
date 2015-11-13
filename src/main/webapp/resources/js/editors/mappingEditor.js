var editor;
$(document).ready(function() {
	editor = new MappingEditor({
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

var MappingEditor = function(options) {
	var _this = this;
	this.options = options;
	
	this.mappingId = $("#mapping-id").val();
	this.sourceId = $("#source-id").val();
	this.targetId = $("#target-id").val();
	this.mappingOwn = $("#mapping-own").val()==="true";
	this.mappingWrite = $("#mapping-write").val()==="true";
	
	this.mappingPath = __util.getBaseUrl() + "mapping/editor/" + this.mappingId + "/";
	this.sourcePath = __util.getBaseUrl() + "schema/editor/" + this.sourceId + "/";
	this.targetPath = __util.getBaseUrl() + "schema/editor/" + this.targetId + "/";
	
	this.layoutContainer = $(".editor-layout-container");
	this.editorContainer = $(".editor-container");
	
	this.mappingContextContainer = $("#mapping-context-container");
	this.mappingContextButtons = $("#mapping-context-buttons");
	this.mappingActivitiesContainer = $("#mapping-context-activities");
	
	this.conceptContextContainer = $("#mapped-concept-context-container");
	this.conceptContextDetail = $("#mapped-concept-context-info");
	this.conceptContextButtons = $("#mapped-concept-context-buttons");
	this.conceptActivitiesContainer = $("#mapped-concept-context-activities");
		
	this.elementContextContainer = $("#schema-element-context-container");
	this.elementContextDetail = $("#schema-element-context-info");
	this.elementContextButtons = $("#schema-element-context-buttons");
	this.elementActivitiesContainer = $("#schema-element-context-activities");
	
	this.context = document.getElementById("mapping-editor-canvas").getContext("2d");
	this.layout = null;
	
	this.footerOffset = 70;
	
	this.oneDone = false;
	
	this.contextMenuClickEventHandler = this.handleContextMenuClicked.bind(this);
	
	__translator.addTranslations(["~eu.dariah.de.minfba.schereg.model.schema.schema",
	                              "~eu.dariah.de.minfba.schereg.editor.element_model",
	                              "~eu.dariah.de.minfba.schereg.model.mapping.mapping",
	                              "~eu.dariah.de.minfba.schereg.model.element.element",
	                              "~eu.dariah.de.minfba.schereg.dialog.confirm_delete",
	                              
	                              "~eu.dariah.de.minfba.schereg.button.expand_all",
	                              "~eu.dariah.de.minfba.schereg.button.collapse_all",
	                              "~eu.dariah.de.minfba.schereg.button.expand_from_here",
	                              "~eu.dariah.de.minfba.schereg.button.collapse_from_here",
	                              "~eu.dariah.de.minfba.common.link.reload_data",
	                              "~eu.dariah.de.minfba.common.link.reset_view",
	                              "~eu.dariah.de.minfba.common.link.delete",
	                              
	                              "~eu.dariah.de.minfba.schereg.editor.actions.ensure_connected_visible",
	                              "~eu.dariah.de.minfba.schereg.editor.actions.reset_position",
	                              "~eu.dariah.de.minfba.schereg.editor.actions.edit_grammar",
	                              "~eu.dariah.de.minfba.schereg.editor.actions.edit_function"]);
	__translator.getTranslations();
	
	this.initLayout();
	this.initGraphs();
};

MappingEditor.prototype = new BaseEditor();

MappingEditor.prototype.registerEvents = function() {
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	document.addEventListener("newConceptMappingEvent", this.saveConceptMappingHandler, false);
	document.addEventListener("changeConceptMappingEvent", this.saveConceptMappingHandler, false);
	document.addEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler, false);
};

MappingEditor.prototype.deregisterEvents = function() {
	document.removeEventListener("selectionEvent", this.selectionHandler);
	document.removeEventListener("deselectionEvent", this.deselectionHandler);
	document.removeEventListener("newConceptMappingEvent", this.saveConceptMappingHandler);
	document.removeEventListener("changeConceptMappingEvent", this.saveConceptMappingHandler);
	document.removeEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler, false);
};

MappingEditor.prototype.initLayout = function() {
	var _this = this;
	this.layoutContainer.addClass("fade");
	this.layoutContainer.removeClass("hide");
	
	var initEastClosed = true;
	var initWestClosed = true;
	if (this.layoutContainer.width()>800) {
		initEastClosed = false;
	}
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
		east : {
			size : "25%",
			minWidth : 200,
			minHeight : 400,
			initClosed : initEastClosed,
			paneSelector : ".layout-east"
		},
		center : {
			size : "50%",
			paneSelector : ".layout-center",
			minHeight : 200
		}, 
		south : {
			size : 100,
			paneSelector : ".layout-south",
			initClosed : initSouthClosed 
		},
		west : {
			size : "25%",
			minWidth : 200,
			minHeight : 400,
			initClosed : initWestClosed,
			paneSelector : ".layout-west"
		},
		onresize: function () {
			_this.resizeContent();
	        return false;
	    }
	});
	this.layoutContainer.removeClass("fade");
};

MappingEditor.prototype.initGraphs = function() {
	var _this = this;
	this.graph = new Model(this.context.canvas, {
		elementTemplateOptions: [
		    { 
		    	key: "Nonterminal", 
		    	primaryColor: "#e6f1ff", 
		    	secondaryColor: "#0049a6",
				getContextMenuItems: _this.getElementContextMenu
			}, {
				key: "Label",
				primaryColor: "#f3e6ff", 
				secondaryColor: "#5700a6",
				getContextMenuItems: _this.getElementContextMenu
			}],
		mappingTemplateOption : {
			relativeControlPointX : 4, 
			connectionHoverTolerance : 5,
			getContextMenuItems: _this.getConnectionContextMenu,
			functionTemplateOptions : {
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 3
			}
		}
		
	});
	this.source = this.graph.addArea({ getContextMenuItems: _this.getAreaContextMenu });
	this.target = this.graph.addArea({ getContextMenuItems: _this.getAreaContextMenu });
	
	this.graph.init();
	this.createActionButtons(this.mappingContextButtons, this.source.getContextMenuItems());
	this.loadActivitiesForEntity(this.mappingId, this.mappingActivitiesContainer);
	
	this.resize();
	
 	this.getElementHierarchy(this.sourcePath, this.source);
 	this.getElementHierarchy(this.targetPath, this.target);
};

MappingEditor.prototype.getAreaContextMenu = function(area) {
	return [
	    area.model.createContextMenuHeading("~eu.dariah.de.minfba.schereg.model.schema.schema"),
	    area.model.createContextMenuItem("expandAll", "~eu.dariah.de.minfba.schereg.button.expand_all", "resize-full", area.index, "area"),
	    area.model.createContextMenuItem("collapseAll", "~eu.dariah.de.minfba.schereg.button.collapse_all", "resize-small", area.index, "area"),
	    area.model.createContextMenuHeading("~eu.dariah.de.minfba.schereg.editor.element_model"),
	    area.model.createContextMenuItem("reset", "~eu.dariah.de.minfba.common.link.reset_view", "repeat"),
	    area.model.createContextMenuItem("reload", "~eu.dariah.de.minfba.common.link.reload_data", "refresh"),
	];
};

MappingEditor.prototype.getElementContextMenu = function(element) {
	var _this = editor;
	return [
	        _this.graph.createContextMenuHeading("~eu.dariah.de.minfba.schereg.model.element.element"),
	        _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
			_this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key)	
	]; 
};

MappingEditor.prototype.getConnectionContextMenu = function(connection) {
	var _this = editor;
	return [
	        _this.graph.createContextMenuHeading("~eu.dariah.de.minfba.schereg.model.mapping.mapping"),
		    _this.graph.createContextMenuItem("ensureConnectedVisible", "~eu.dariah.de.minfba.schereg.editor.actions.ensure_connected_visible", "resize-full", connection.id),
		    _this.graph.createContextMenuItem("resetPosition", "~eu.dariah.de.minfba.schereg.editor.actions.reset_position", "repeat", connection.id),
		    _this.graph.createContextMenuSeparator(),
		    _this.graph.createContextMenuItem("editGrammar", "~eu.dariah.de.minfba.schereg.editor.actions.edit_grammar", "edit", connection.id),
		    _this.graph.createContextMenuItem("editFunction", "~eu.dariah.de.minfba.schereg.editor.actions.edit_function", "edit", connection.id),
		    _this.graph.createContextMenuItem("removeMapping", "~eu.dariah.de.minfba.common.link.delete", "trash", connection.id, undefined, "danger"),
	];
};

MappingEditor.prototype.handleContextMenuClicked = function(e) {
	this.performTreeAction(e.key, e.id, e.nodeType);
};


MappingEditor.prototype.getAreaForElementId = function(elementId) {
	for (var i=0; i<this.graph.areas.length; i++) {
		var e = this.graph.areas[i].findElementById(this.graph.areas[i].root, elementId);
		if (e!=null) {
			return this.graph.areas[i];
		}
	}
};

MappingEditor.prototype.performTreeAction = function(action, elementId, elementKey) {	
	switch(action) {
		case "expandFromHere" : 
			var area = this.getAreaForElementId(elementId);
			return area.expandFromElement(elementId, true);
		case "collapseFromHere" : 
			var area = this.getAreaForElementId(elementId);
			return area.expandFromElement(elementId, false);
	
	    case "expandAll" :  return this.graph.areas[elementId].expandAll(true);
	    case "collapseAll" : return this.graph.areas[elementId].expandAll(false);
	    case "reload" : return this.reloadAll();
	    case "reset" : return this.graph.resetView();
	 
	    case "ensureConnectedVisible" : return this.ensureConnectedVisible(elementId);
	    case "resetPosition" : return this.resetMappingPosition(elementId);
	    case "editGrammar" : return this.editGrammar(elementId);
	    case "editFunction" : return this.editFunction(elementId);
	    case "removeMapping" : return this.removeConceptMapping(elementId);
	    
	    
	    default:
	        throw new Error("Unknown tree action requested: " + action);
	}  
};

MappingEditor.prototype.editGrammar = function(connectionId) {
	var _this = this;
	var form_identifier = "edit-grammar-" + connectionId;
	
	
	$.ajax({
		url: this.mappingPath + "mappedConcept/" + connectionId + "/async/get",
	    type: "GET",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	
	    	var mapping = _this.graph.getMappingById(connectionId);
	    	
	    	modalFormHandler = new ModalFormHandler({
	    		formUrl: "/grammar/" + data.grammars[0].id + "/form/edit",
	    		identifier: form_identifier,
	    		additionalModalClasses: "max-modal",
	    		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
	    		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
	    		                ],
	    		setupCallback: function(modal) { grammarEditor = new GrammarEditor(modal, {
	    			pathPrefix: __util.getBaseUrl() + "mapping/editor/" + connectionId,
	    			entityId : _this.mappingId,
	    			grammarId : data.grammars[0].id
	    		}); },     
	    		completeCallback: function() { _this.graph.reselect(); }
	    	});
	    		
	    	modalFormHandler.show(form_identifier);
	    }
	});
};

MappingEditor.prototype.editFunction = function(connectionId) {
	var _this = this;	
	var mapping = _this.graph.getMappingById(connectionId);
	
	
	$.ajax({
		url: this.mappingPath + "mappedConcept/" + connectionId + "/async/get",
	    type: "GET",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	
	    	var mapping = _this.graph.getMappingById(connectionId);
	    	
	    	var form_identifier = "edit-function-" + connectionId;
	    	
	    	modalFormHandler = new ModalFormHandler({
	    		formUrl: "/function/" + data.grammars[0].transformationFunctions[0].id + "/form/edit",
	    		identifier: form_identifier,
	    		additionalModalClasses: "max-modal",
	    		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
	    		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
	    		                ],
	            setupCallback: function(modal) { functionEditor = new FunctionEditor(modal, {
	    			pathPrefix: __util.getBaseUrl() + "mapping/editor/" + _this.mappingId,
	    			entityId : _this.mappingId,
	    			functionId : data.grammars[0].transformationFunctions[0].id
	    		}); },       
	    		completeCallback: function() { _this.graph.reselect(); }
	    	});
	    		
	    	modalFormHandler.show(form_identifier);
	    }
	});
};

MappingEditor.prototype.resetMappingPosition = function(connectionId) {
	var mapping = this.graph.getMappingById(connectionId);
	mapping.clearMovedForkPoint();
	this.graph.paint();
};

MappingEditor.prototype.ensureConnectedVisible = function(connectionId) {
	var mapping = this.graph.getMappingById(connectionId);
	mapping.from.element.ensureVisible();
	for (var i=0; i<mapping.to.length; i++) {
		mapping.to[i].element.ensureVisible();
	}
	this.graph.update();
};

MappingEditor.prototype.reloadAll = function() {
	this.deregisterEvents();
	
	var selectedItemIds = [];
	for (var i=0; i<this.graph.selectedItems.length; i++) {
		selectedItemIds.push(this.graph.selectedItems[i].getId());
	}

	this.reloadElementHierarchy(this.sourcePath, this.source, selectedItemIds);
 	this.reloadElementHierarchy(this.targetPath, this.target, selectedItemIds);
};

MappingEditor.prototype.reloadElementHierarchy = function(path, area, selectedItemIds) {
	if (area.root==null) {
		this.getElementHierarchy(path, source);
		return;
	}
	
	var rootX = area.root.rectangle.x;
	var rootY = area.root.rectangle.y;
	var expandedItemIds = area.getExpandedElementIds(area.root);

	var _this = this;
	$.ajax({
		url: path + "async/getRendered",
	    type: "GET",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	area.clear();
	    	
	    	_this.processElementHierarchy(area, data);	    	
	    	
	    	area.root.rectangle = new Rectangle(rootX, rootY, area.root.rectangle.width, area.root.rectangle.height);
	    			
	    	area.selectElementsByIds(area.root, selectedItemIds);    		
	    	area.expandElementsByIds(area.root, expandedItemIds);
	    	
	    	area.invalidate();
	    	
	    	if (_this.oneDone) {
	    		_this.getMappings(selectedItemIds);
	    		_this.oneDone = false;
	    	} else {
	    		_this.oneDone = true;
	    	}
	    }/*,
	    error: function(jqXHR, textStatus, errorThrown) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	_this.initGraph();
	    }*/
	});	
};

MappingEditor.prototype.getElementHierarchy = function(path, area) {
	var _this = this;
	$.ajax({
	    url: path + "async/getRendered",
	    type: "GET",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	_this.processElementHierarchy(area, data);
	    	//_this.graph.paint();
	    	
	    	if (_this.oneDone) {
	    		_this.getMappings();
	    		_this.oneDone = false;
	    	} else {
	    		_this.oneDone = true;
	    	}
	    }/*,
	    error: __util.processServerError*/
	});
};

MappingEditor.prototype.getMappings = function(selectedItemIds) {
	var _this = this;
	$.ajax({
	    url: _this.mappingPath + "async/getConcepts",
	    type: "GET",
	    success: function(data) {
	    	_this.graph.clearMappings();
	    	
	    	if (data!==undefined && data!=null && data.length>0) {
		    	for (var i=0; i<data.length; i++) {
		    		var lhs = _this.source.getElementById(data[i].sourceElementId).getConnector("mappings");
		    		var rhs = [];
		    		
		    		for (var j=0; j<data[i].targetElementIds.length; j++) {
		    			rhs.push(_this.target.getElementById(data[i].targetElementIds[j]).getConnector("mappings"));	
		    		}
		    		if (lhs != null && rhs != null) {			
		    			_this.graph.addMappingConnection(lhs, rhs, data[i].id);   			
		    		}
		    	}
	    	}
	    	if (selectedItemIds!==undefined && selectedItemIds.length>0) {
	    		_this.graph.selectMappingsByIds(selectedItemIds);   
	    	}
	    	_this.graph.update();
	    	
	    	_this.registerEvents();
	    }/*,
	    error: __util.processServerError*/
	});
};


MappingEditor.prototype.processElementHierarchy = function(schema, data) {
	var parent = schema.addElement(data.type, null, data.id, this.formatLabel(data.label), null);
	this.generateTree(schema, parent, data.children);
	schema.elements[0].setExpanded(true);
};

MappingEditor.prototype.generateTree = function(area, parent, children) {

	if (children!=null && children instanceof Array) {
		for (var i=0; i<children.length; i++) {
			var icon = null;
			
			var e = area.addElement(children[i].type, parent, children[i].id, this.formatLabel(children[i].label), icon);
			this.generateTree(area, e, children[i].children);
		}
	}
};

MappingEditor.prototype.formatLabel = function(label) {
	if (label.length > 25) {
		return label.substring(0,25) + "...";
	} else {
		return label;
	}	
};

MappingEditor.prototype.resize = function() {
	this.resizeLayout();
	this.resizeContent();	
};

MappingEditor.prototype.resizeLayout = function() {
	var height = Math.floor($(window).height() - this.layoutContainer.offset().top - this.options.footerOffset);
	
	this.layoutContainer.height(height);
	this.layout.resizeAll();
}

MappingEditor.prototype.resizeContent = function() {
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
		this.context.canvas.width = this.editorContainer.innerWidth();
		this.context.canvas.height = this.editorContainer.innerHeight();		
		if (this.graph !== null) {
			this.graph.update();
		}
	}
};

/*this.mappingContextContainer
this.mappingContextButtons

this.conceptContextContainer
this.conceptContextDetail
this.conceptContextButtons*/

MappingEditor.prototype.deselectionHandler = function() {
	var _this = editor;
	
	_this.conceptContextDetail.text("");
	_this.conceptContextButtons.text("");
	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	_this.conceptContextContainer.addClass("hide");
	_this.elementContextContainer.addClass("hide");
	_this.mappingContextContainer.removeClass("hide");
	
	
	_this.loadActivitiesForEntity(_this.mappingId, _this.mappingActivitiesContainer);
};

MappingEditor.prototype.selectionHandler = function(e) {
	var _this = editor;
	
	_this.conceptContextDetail.text("");
	_this.conceptContextButtons.text("");
	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	_this.mappingContextContainer.addClass("hide");
		
	if (e.element instanceof Element) {
		_this.createActionButtons(_this.elementContextButtons, e.element.getContextMenuItems(), "editor");
		if (e.element.template.area===_this.source) {
			var path = _this.sourcePath;
		} else {
			var path = _this.targetPath;
		}
		_this.getElementDetails(path, e.element.getType(), e.element.getId(), _this.elementContextDetail);
		_this.loadActivitiesForElement(e.element.getId(), _this.elementActivitiesContainer);
		
		_this.elementContextContainer.removeClass("hide");
		_this.conceptContextContainer.addClass("hide");
	} else if (e.element instanceof Connection || e.element instanceof Function) {
		_this.createActionButtons(_this.conceptContextButtons, e.element.getContextMenuItems(), "editor");
		
		_this.getElementDetails(_this.mappingPath, "mappedConcept", e.element.getId(), _this.conceptContextDetail);
		_this.loadActivitiesForElement(e.element.getId(), _this.conceptActivitiesContainer);
		
		_this.elementContextContainer.addClass("hide");
		_this.conceptContextContainer.removeClass("hide");
	} else {
		throw new Error("Element selection not supported");
	}
};


MappingEditor.prototype.changeConceptMappingHandler = function(e) {
	if (e.connection.id===undefined) {
		throw Error("update failed, connection not saved yet");
	}
	var _this = editor;
	
	_this.newConceptMappingHandler(e);
}

MappingEditor.prototype.removeConceptMapping = function(conceptMappingId) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), conceptMappingId), function(result) {
		if(result) {
			$.ajax({
			    url: _this.mappingPath + "mappedConcept/" + conceptMappingId + "/async/remove",
			    type: "POST",
			    dataType: "json",
			    success: function(data) {
			    	_this.graph.deselectAll();
			    	_this.reloadAll();
			    },
			    error: __util.processServerError
			});
		}
	});
};

MappingEditor.prototype.saveConceptMappingHandler = function(e) {
	var targetIds = [];
	for (var i=0; i<e.connection.to.length; i++) {
		targetIds.push(e.connection.to[i].element.id);
	}
	
	var _this = editor;
	$.ajax({
		url: _this.mappingPath + "mappedConcept/" + e.connection.id + '/async/save',
        type: "POST",
        data: { sourceElementId: e.connection.from.element.id, targetElementId: targetIds},
        dataType: "json",
        success: function(data) { 
        	e.connection.id = data.pojo.id;        	
        	//$("#save-notice-area").text(data);
        },
        error: function(textStatus) { modalMessage.showMessage("warning", "Error updating mapping!", "Please refresh."); }
 	});
	
};