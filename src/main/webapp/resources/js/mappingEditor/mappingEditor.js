var mappingEditor;
$(document).ready(function() {
	mappingEditor = new MappingEditor();
	mappingEditor.initLayout();
	mappingEditor.initGraphs();
});
$(window).resize(function() {
	mappingEditor.resizeLayout();
	mappingEditor.resizeContent();
});

var MappingEditor = function() {
	var _this = this;
	
	this.mappingId = $("#mapping-id").val();
	this.sourceId = $("#source-id").val();
	this.targetId = $("#target-id").val();
	this.mappingOwn = $("#mapping-own").val()==="true";
	this.mappingWrite = $("#mapping-write").val()==="true";
	
	this.mappingPath = __util.getBaseUrl() + "mapping/editor/" + this.mappingId + "/";
	this.sourcePath = __util.getBaseUrl() + "schema/editor/" + this.sourceId + "/";
	this.targetPath = __util.getBaseUrl() + "schema/editor/" + this.targetId + "/";
	
	this.layoutContainer = $("#mapping-editor-layout-container");
	this.editorContainer = $("#mapping-editor-container");
	this.context = document.getElementById("mapping-editor-canvas").getContext("2d");
	this.layout = null;
	
	this.footerOffset = 70;
	
	this.oneDone = false;
	
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	document.addEventListener("newMappingCellEvent", this.newMappingCellHandler, false);
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
			paneSelector : ".outer-east"
		},
		center : {
			size : "50%",
			paneSelector : ".outer-center",
			minHeight : 200
		}, 
		south : {
			size : 100,
			paneSelector : ".outer-south"
		},
		west : {
			size : "25%",
			minWidth : 200,
			minHeight : 400,
			initClosed : initWestClosed,
			paneSelector : ".outer-west"
		},
		onresize: function () {
			_this.resizeContent();
	        return false;
	    }
	});
	this.layoutContainer.removeClass("fade");
};

MappingEditor.prototype.initGraphs = function() {
	this.graph = new Model(this.context.canvas);
	this.source = this.graph.addArea();
	this.target = this.graph.addArea();
	this.graph.init();
	
 	this.getElementHierarchy(this.sourcePath, this.source);
 	this.getElementHierarchy(this.targetPath, this.target);

 	this.resizeLayout();
 	this.resizeContent();
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
	    	_this.graph.update();
	    	
	    	if (_this.oneDone) {
	    		//_this.getMappings();
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
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	
	    	for (var i=0; i<data.length; i++) {
	    		var lhs = _this.source.getElement(data[i].sourceElementId).getConnector("mappings");
	    		var rhs = [];
	    		
	    		for (var j=0; j<data.length; j++) {
	    			rhs.push(_this.target.getElement(data[i].targetElementIds[j]).getConnector("mappings"));	
	    		}
	    		if (lhs != null && rhs != null) {			
	    			_this.graph.addMapping(lhs, rhs, data[i].id, 1);
	    		}
	    	}
	    	
	    	_this.graph.update();
	    }/*,
	    error: __util.processServerError*/
	});
};


MappingEditor.prototype.processElementHierarchy = function(schema, data) {
	var parent = schema.addElement(data.type, null, data.id, this.formatLabel(data.label), null);
	
	this.generateTree(schema, parent, data.children);
	
	schema.elements[0].setExpanded(true);
	
	this.graph.update();

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

MappingEditor.prototype.resizeLayout = function() {
	var height = Math.floor($(window).height() - this.layoutContainer.offset().top - this.footerOffset);
	
	this.layoutContainer.height(height);
	this.layout.resizeAll();
}

MappingEditor.prototype.resizeContent = function() {
	var editorHeight = Math.floor(this.editorContainer.offsetParent().innerHeight() - (this.editorContainer.offset().top - this.editorContainer.offsetParent().offset().top));
	this.editorContainer.css("height", editorHeight + "px");
	
	if (this.context.canvas) {
		this.context.canvas.width = this.editorContainer.width(); // border-bottom
		this.context.canvas.height = editorHeight; // border-top
		window.scroll(0, 0);			
		if (this.graph !== null) {
			if (this.source != null) {
				this.source.setSize(new Rectangle(0, 0, Math.floor(this.context.canvas.width / 2), this.context.canvas.height));
			}
			if (this.target != null) {
				this.target.setSize(new Rectangle(Math.floor(this.context.canvas.width / 2), 0, this.context.canvas.width - Math.floor(this.context.canvas.width / 2), this.context.canvas.height));
			}
			this.graph.update();
		}
	} 
};

MappingEditor.prototype.deselectionHandler = function() {};

MappingEditor.prototype.selectionHandler = function(e) {};

MappingEditor.prototype.newMappingCellHandler = function(e) {
	
	var _this = mappingEditor;
	$.ajax({
		url: _this.mappingPath + 'async/saveConcept',
        type: "POST",
        data: { conceptId: e.mappingId, sourceElementId: e.input, targetElementId: e.output},
        dataType: "json",
        //success: function(data) { $("#save-notice-area").text(data);},
        //error: function(textStatus) { modalMessage.showMessage("warning", "Error updating mapping!", "Please refresh."); }
 	});
	
};













