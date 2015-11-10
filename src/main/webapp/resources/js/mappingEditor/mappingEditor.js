var mappingEditor;
$(document).ready(function() {
	mappingEditor = new MappingEditor({
		footerOffset: 70,
		icons: {
			warning: __util.getBaseUrl() + "resources/img/warning.png",
			error: __util.getBaseUrl() + "resources/img/error.png"
		}
	});
});
$(window).resize(function() {
	mappingEditor.resize();
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
	this.context = document.getElementById("mapping-editor-canvas").getContext("2d");
	this.layout = null;
	
	this.footerOffset = 70;
	
	this.oneDone = false;
	
	__translator.addTranslations(["~eu.dariah.de.minfba.schereg.button.expand_all",
	                              "~eu.dariah.de.minfba.schereg.button.collapse_all",
	                              "~eu.dariah.de.minfba.schereg.button.expand_from_here",
	                              "~eu.dariah.de.minfba.schereg.button.collapse_from_here",
	                              "~eu.dariah.de.minfba.common.link.reload_data",
	                              "~eu.dariah.de.minfba.common.link.reset_view"]);
	__translator.getTranslations();
	
	this.initLayout();
	this.initGraphs();
};

MappingEditor.prototype.registerEvents = function() {
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	document.addEventListener("newConceptMappingEvent", this.newConceptMappingHandler, false);
	document.addEventListener("changeConceptMappingEvent", this.changeConceptMappingHandler, false);
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
	var getContextMenuItems = function(element) { 
		var items = [
					    _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
					    _this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
					];
		return items; 
	};
	
	this.graph = new Model(this.context.canvas, 
			[{
				key: "Nonterminal",
				primaryColor: "#e6f1ff", secondaryColor: "#0049a6",
				getContextMenuItems: getContextMenuItems
			}, {
				key: "Label",
				primaryColor: "#f3e6ff", secondaryColor: "#5700a6",
				getContextMenuItems: getContextMenuItems
			}]);
	this.source = this.graph.addArea({
		contextMenuItems: [
							_this.graph.createContextMenuItem("expandAll", "~eu.dariah.de.minfba.schereg.button.expand_all", "resize-full", 0, "schema"),
							_this.graph.createContextMenuItem("collapseAll", "~eu.dariah.de.minfba.schereg.button.collapse_all", "resize-small", 0, "schema"),
							_this.graph.createContextMenuItem("reset", "~eu.dariah.de.minfba.common.link.reset_view", "repeat", 0, "schema"),
							_this.graph.createContextMenuSeparator(),
							_this.graph.createContextMenuItem("reload", "~eu.dariah.de.minfba.common.link.reload_data", "refresh", 0, "schema"),
						]
	});
	this.target = this.graph.addArea({
		contextMenuItems: [
							_this.graph.createContextMenuItem("expandAll", "~eu.dariah.de.minfba.schereg.button.expand_all", "resize-full", 1, "schema"),
							_this.graph.createContextMenuItem("collapseAll", "~eu.dariah.de.minfba.schereg.button.collapse_all", "resize-small", 1, "schema"),
							_this.graph.createContextMenuItem("reset", "~eu.dariah.de.minfba.common.link.reset_view", "repeat", 1, "schema"),
							_this.graph.createContextMenuSeparator(),
							_this.graph.createContextMenuItem("reload", "~eu.dariah.de.minfba.common.link.reload_data", "refresh", 1, "schema"),
						]
	});
	this.graph.init();
	
	this.contextMenuClickEventHandler = this.handleContextMenuClicked.bind(this);
	document.addEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler, false);
	
	
	this.resize();
	
 	this.getElementHierarchy(this.sourcePath, this.source);
 	this.getElementHierarchy(this.targetPath, this.target);
};

MappingEditor.prototype.handleContextMenuClicked = function(e) {
	this.performTreeAction(e.key, e.id, e.nodeType);
};

MappingEditor.prototype.performTreeAction = function(action, elementId, elementType) {	
	switch(action) {
		case "expandFromHere" : return this.area.expandFromElement(elementId, true);
		case "collapseFromHere" : return this.area.expandFromElement(elementId, false);
	
	    case "expandAll" :  return this.area.expandAll(true);
	    case "collapseAll" : return this.area.expandAll(false);
	    case "reload" : return this.reloadElementHierarchy();
	    case "reset" : return this.area.resetView();
	 
	    default:
	        throw new Error("Unknown tree action requested: " + action);
	}  
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
	    	} else {
	    		_this.oneDone = true;
	    	}
	    }/*,
	    error: __util.processServerError*/
	});
};

MappingEditor.prototype.getMappings = function() {
	var _this = this;
	$.ajax({
	    url: _this.mappingPath + "async/getConcepts",
	    type: "GET",
	    success: function(data) {
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

MappingEditor.prototype.deselectionHandler = function() {};

MappingEditor.prototype.selectionHandler = function(e) {};


MappingEditor.prototype.changeConceptMappingHandler = function(e) {
	if (e.connection.id===undefined) {
		throw Error("update failed, connection not saved yet");
	}
	var _this = mappingEditor;
	
	_this.newConceptMappingHandler(e);
}

MappingEditor.prototype.newConceptMappingHandler = function(e) {
	var targetIds = [];
	for (var i=0; i<e.connection.to.length; i++) {
		targetIds.push(e.connection.to[i].element.id);
	}
	
	var _this = mappingEditor;
	$.ajax({
		url: _this.mappingPath + 'async/saveConcept',
        type: "POST",
        data: { conceptId: e.connection.id, sourceElementId: e.connection.from.element.id, targetElementId: targetIds},
        dataType: "json",
        success: function(data) { 
        	e.connection.id = data.pojo.id;        	
        	//$("#save-notice-area").text(data);
        },
        error: function(textStatus) { modalMessage.showMessage("warning", "Error updating mapping!", "Please refresh."); }
 	});
	
};