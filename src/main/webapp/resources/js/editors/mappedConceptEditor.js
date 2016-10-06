var MappedConceptEditor = function(container, options) {
	this.options = {
			conceptId: "",
			elementPath: "mappedConcept/{0}/",
			layoutContainer: "#layout-helper-container",
			editorContainer: "#mapped-concept-editor-container",
			canvasId: "mapped-concept-editor",
			readOnly: false
	};
	$.extend(true, this.options, options);
	this.options.elementPath = String.format(this.options.elementPath, this.options.conceptId); 
	
	this.context = document.getElementById(this.options.canvasId).getContext("2d");;
	this.layoutContainer = $(container).find(this.options.layoutContainer);
	this.editorContainer = $(container).find(this.options.editorContainer);

	this.init();
};

MappedConceptEditor.prototype.init = function() {
	var _this = this;
	this.graph = new Model(this.context.canvas, {
		readOnly: _this.readOnly,
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
			//getContextMenuItems: _this.getConnectionContextMenu,
			functionTemplateOptions : {
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 3
			}
		}
	});
	this.model = this.graph.addArea({ getContextMenuItems: _this.getAreaContextMenu });
	
	this.graph.init();
	this.resize();
	
	this.getElementHierarchy();
 	//this.getElementHierarchy(this.targetPath, this.target);
};

MappedConceptEditor.prototype.resize = function() {
	if (this.context.canvas) {
		this.context.canvas.width = this.editorContainer.innerWidth() - 40;
		this.context.canvas.height = this.layoutContainer.innerHeight();		
		if (this.graph !== null) {
			this.graph.update();
		}
	}	
};

MappedConceptEditor.prototype.getElementHierarchy = function() {
	var _this = this;
	$.ajax({
	    url: _this.options.elementPath + "async/getRendered",
	    type: "GET",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	_this.processElementHierarchy(data);
	    }
	});
};

MappedConceptEditor.prototype.processElementHierarchy = function(data) {
	var parent = this.model.addElement(data.type, null, data.id, this.formatLabel(data.label), null);
	this.generateTree(this.model, parent, data.children);
	this.model.elements[0].setExpanded(true);
};