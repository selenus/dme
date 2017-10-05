var editor;
$(document).ready(function() {
	editor = new MappingEditor({
		footerOffset: 70,
		icons: {
			warning: __util.getBaseUrl() + "resources/img/warning.png",
			error: __util.getBaseUrl() + "resources/img/error.png"
		}
	});
	$('[data-toggle="tooltip"]').tooltip( { container: 'body' });
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
	this.sourcePath = __util.getBaseUrl() + "model/editor/" + this.sourceId + "/";
	this.targetPath = __util.getBaseUrl() + "model/editor/" + this.targetId + "/";
	
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
	
	this.pathname = __util.getBaseUrl() + "mapping/editor/" + this.mappingId + "/";
	this.context = document.getElementById("mapping-editor-canvas").getContext("2d");
	this.layout = null;
	
	this.footerOffset = 70;
	
	this.oneDone = false;
	
	this.conceptEditor = null;
	this.logArea = null;
	
	this.contextMenuClickEventHandler = this.handleContextMenuClicked.bind(this);
	
	__translator.addTranslations(["~de.unibamberg.minf.dme.model.schema.schema",
	                              "~de.unibamberg.minf.dme.editor.element_model",
	                              "~de.unibamberg.minf.dme.model.mapping.mapping",
	                              "~de.unibamberg.minf.dme.model.element.element",
	                              "~de.unibamberg.minf.dme.dialog.confirm_delete",
	                              
	                              "~de.unibamberg.minf.dme.button.expand_all",
	                              "~de.unibamberg.minf.dme.button.collapse_all",
	                              "~de.unibamberg.minf.dme.button.expand_from_here",
	                              "~de.unibamberg.minf.dme.button.collapse_from_here",
	                              "~de.unibamberg.minf.common.link.reload_data",
	                              "~de.unibamberg.minf.common.link.reset_view",
	                              "~de.unibamberg.minf.common.link.delete",
	                              
	                              "~de.unibamberg.minf.dme.editor.actions.ensure_connected_visible",
	                              "~de.unibamberg.minf.dme.editor.actions.reset_position",
	                              "~de.unibamberg.minf.dme.editor.actions.edit_connection",
	                              "~de.unibamberg.minf.dme.editor.actions.show_connection",
	                              
	                              "~de.unibamberg.minf.dme.button.export",
	                              "~de.unibamberg.minf.dme.button.import",
	                              "~de.unibamberg.minf.dme.editor.mapping",
	                              
	                              "~de.unibamberg.minf.dme.dialog.confirm_publish",
	                              "~de.unibamberg.minf.dme.model.mapping.validation.no_pub_schema_drafts"]);
	__translator.getTranslations();
	
	this.initLayout();
	this.initGraphs();
	this.initSample(this.mappingPath, this.mappingId);
	
	this.logArea = new LogArea({
		pathPrefix :  __util.getBaseUrl() + "sessions/",
		entityId : _this.mappingId,
		logList: $("ul#mapping-editor-log"),
	});
};

MappingEditor.prototype = new BaseEditor();

MappingEditor.prototype.getEntityId = function() {
	return this.mappingId;
};

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
	document.removeEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler);
};

MappingEditor.prototype.initLayout = function() {
	var _this = this;
	this.layoutContainer.addClass("fade");
	this.layoutContainer.removeClass("hide");
	
	var initEastClosed = true;
	var initWestClosed = true;
	/*if (this.layoutContainer.width()>800) {
		initEastClosed = false;
	}*/
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
			size : "33%",
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
		readOnly: !(_this.mappingOwn || _this.mappingWrite),
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
	var _this = editor;
	var items = [
	    area.model.createContextMenuHeading("~de.unibamberg.minf.dme.model.schema.schema"),
	    area.model.createContextMenuItem("expandAll", "~de.unibamberg.minf.dme.button.expand_all", "resize-full", area.index, "area"),
	    area.model.createContextMenuItem("collapseAll", "~de.unibamberg.minf.dme.button.collapse_all", "resize-small", area.index, "area"),
	    area.model.createContextMenuHeading("~de.unibamberg.minf.dme.editor.element_model"),
	    area.model.createContextMenuItem("reset", "~de.unibamberg.minf.common.link.reset_view", "repeat"),
	    area.model.createContextMenuItem("reload", "~de.unibamberg.minf.common.link.reload_data", "refresh"),
	    
	    area.model.createContextMenuHeading("~de.unibamberg.minf.dme.editor.mapping"),
	    area.model.createContextMenuItem("exportMapping", "~de.unibamberg.minf.dme.button.export", "cloud-download"),
	    area.model.createContextMenuItem("importMapping", "~de.unibamberg.minf.dme.button.import", "cloud-upload"),
	];
	return items;
};

MappingEditor.prototype.getElementContextMenu = function(element) {
	var _this = editor;
	var items = [
	        _this.graph.createContextMenuHeading("~de.unibamberg.minf.dme.model.element.element"),
	        _this.graph.createContextMenuItem("expandFromHere", "~de.unibamberg.minf.dme.button.expand_from_here", "resize-full", element.id, element.template.options.key),
			_this.graph.createContextMenuItem("collapseFromHere", "~de.unibamberg.minf.dme.button.collapse_from_here", "resize-small", element.id, element.template.options.key)	
	]; 
	return items;
};

MappingEditor.prototype.getConnectionContextMenu = function(connection) {
	var _this = editor;
	var items = [
	        _this.graph.createContextMenuHeading("~de.unibamberg.minf.dme.model.mapping.mapping"),
		    _this.graph.createContextMenuItem("ensureConnectedVisible", "~de.unibamberg.minf.dme.editor.actions.ensure_connected_visible", "resize-full", connection.id),
		    _this.graph.createContextMenuItem("resetPosition", "~de.unibamberg.minf.dme.editor.actions.reset_position", "repeat", connection.id),
	];
	if (_this.mappingOwn || _this.mappingWrite) {
		items.push(_this.graph.createContextMenuSeparator(),
			    _this.graph.createContextMenuItem("editConnection", "~de.unibamberg.minf.dme.editor.actions.edit_connection", "edit", connection.id),
			    _this.graph.createContextMenuItem("removeMapping", "~de.unibamberg.minf.common.link.delete", "trash", connection.id, undefined, "danger"))
	} else {
		items.push(_this.graph.createContextMenuSeparator(),
				_this.graph.createContextMenuItem("editConnection", "~de.unibamberg.minf.dme.editor.actions.show_connection", "edit", connection.id));
	}
	return items;
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
	    case "editConnection" : return this.editConnection(elementId);
	    case "removeMapping" : return this.removeConceptMapping(elementId);
	    
	    case "importMapping" : return this.importMapping();
	    case "exportMapping" : return this.exportMapping();
	    
	    /*default:
	        throw new Error("Unknown tree action requested: " + action);*/
	}  
};

MappingEditor.prototype.editConnection = function(connectionId) {
	var form_identifier = "edit-connection-" + connectionId;
	var _this = this;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "mappedConcept/" + connectionId + "/form/edit",
		identifier: form_identifier,
		additionalModalClasses: "max-modal",
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],     
		displayCallback: function(container, modal) { 
			if (_this.conceptEditor!==undefined && _this.conceptEditor!==null) {
				_this.conceptEditor.dispose();
				_this.conceptEditor = null;
			}
			_this.conceptEditor = new MappedConceptEditor(_this, container, modal, { conceptId: connectionId }); }
	});
		
	modalFormHandler.show(form_identifier);
};

MappingEditor.prototype.resetMappingPosition = function(connectionId) {
	var mapping = this.graph.getMappingById(connectionId);
	mapping.clearMovedForkPoint();
	this.graph.paint();
};

MappingEditor.prototype.ensureConnectedVisible = function(connectionId) {
	var mapping = this.graph.getMappingById(connectionId);
	for (var i=0; i<mapping.from.length; i++) {
		mapping.from[i].element.ensureVisible();
	}
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
		url: path + "async/getHierarchy",
		data: { staticElementsOnly: true },
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
		url: path + "async/getHierarchy",
		data: { staticElementsOnly: true },
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
		    		var lhs = [];
		    		var rhs = [];
		    		for (var key in data[i].elementGrammarIdsMap) {
                        try {
                                if(data[i].elementGrammarIdsMap.hasOwnProperty(key)) {
                                        lhs.push(_this.source.getElementById(key).getConnector("mappings"));    
                                }
                        }
                        catch (e) {
                                __notifications.showMessage(NOTIFICATION_TYPES.ERROR, "Unknown source element", "" + key + " mapping cell: " + data[i].id, "err-" + key, false);
                        }
	                }
	                for (var j=0; j<data[i].targetElementIds.length; j++) {
	                        try {
	                                rhs.push(_this.target.getElementById(data[i].targetElementIds[j]).getConnector("mappings")); 
	                        }
	                        catch (e) {
	                                __notifications.showMessage(NOTIFICATION_TYPES.ERROR, "Unknown target element", "" + data[i].targetElementIds[j] + " mapping cell: " + data[i].id, "err-" + key, false);
	                        }
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
	this.generateTree(schema, parent, data.childElements);
	schema.elements[0].setExpanded(true);
};

MappingEditor.prototype.generateTree = function(area, parent, children) {

	if (children!=null && children instanceof Array) {
		for (var i=0; i<children.length; i++) {
			var icon = null;
			
			var e = area.addElement(children[i].type, parent, children[i].id, this.formatLabel(children[i].label), icon);
			this.generateTree(area, e, children[i].childElements);
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
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_delete"), conceptMappingId), function(result) {
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
	
	var sourceIds = [];
	for (var i=0; i<e.connection.from.length; i++) {
		sourceIds.push(e.connection.from[i].element.id);
	}
	
	
	var _this = editor;
	$.ajax({
		url: _this.mappingPath + "mappedConcept/" + e.connection.id + '/async/save',
        type: "POST",
        data: { sourceElementId: sourceIds, targetElementId: targetIds},
        dataType: "json",
        success: function(data) { 
        	e.connection.id = data.pojo.id;        	
        	//$("#save-notice-area").text(data);
        },
        error: function(textStatus) { modalMessage.showMessage("warning", "Error updating mapping!", "Please refresh."); }
 	});
	
};

MappingEditor.prototype.triggerEdit = function() {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "edit-mapping-" + _this.mappingId;
	var url = __util.getBaseUrl() + "mapping/forms/edit/" + _this.mappingId;
	
	modalFormHandler = new ModalFormHandler({
		formFullUrl: url,
		identifier: form_identifier,
		//additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		completeCallback: function(data) { 
			if (data.success) {
				window.location.reload();
			} else {
        		__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
	        			__translator.translate(data.message.messageHead), 
	        			__translator.translate(data.message.messageBody));
        	}
		}
	});
		
	modalFormHandler.show(form_identifier);
};

MappingEditor.prototype.triggerPublish = function() {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_publish"), _this.mappingId), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "mapping/async/publish/" + _this.mappingId,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	if (data.success) {
		        		window.location.reload();
		        	} else {
		        		__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
			        			__translator.translate(data.message.messageHead), 
			        			__translator.translate(data.message.messageBody));
		        	}
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

MappingEditor.prototype.triggerDelete = function() {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_delete"), _this.mappingId), function(result) {
		if(result) {
			$.ajax({
		        url: __util.getBaseUrl() + "mapping/async/delete/" + _this.mappingId,
		        type: "GET",
		        dataType: "json",
		        success: function(data) { 
		        	if (data.success) {
		        		window.location.reload();
		        	} else {
		        		__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
			        			__translator.translate(data.message.messageHead), 
			        			__translator.translate(data.message.messageBody));
		        	}
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