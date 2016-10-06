var MappedConceptEditor = function(container, options) {
	this.options = {
			conceptId: "",
			sourcePath: "mappedConcept/{0}/async/getSource",
			targetPath: "mappedConcept/{0}/async/getTarget",
			layoutContainer: "#layout-helper-container",
			editorContainer: "#mapped-concept-editor-container",
			canvasId: "mapped-concept-editor",
			readOnly: false
	};
	$.extend(true, this.options, options);
	this.options.sourcePath = String.format(this.options.sourcePath, this.options.conceptId); 
	this.options.targetPath = String.format(this.options.targetPath, this.options.conceptId);
	
	this.context = document.getElementById(this.options.canvasId).getContext("2d");;
	this.layoutContainer = $(container).find(this.options.layoutContainer);
	this.editorContainer = $(container).find(this.options.editorContainer);

	this.sourceGrammars = [];
	this.targetElements = [];
	this.functionId = "";
	
	this.init();
};

MappedConceptEditor.prototype.getElementContextMenu = function(element) { 
	var _this = editor.conceptEditor;
		var items = [
		    _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
		    _this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
		    _this.graph.createContextMenuSeparator()
		];
		if (editor.mappingOwn || editor.mappingWrite) {
			items.push(_this.graph.createContextMenuItem("addNonterminal", "~eu.dariah.de.minfba.schereg.button.add_nonterminal", "asterisk", element.id, element.template.options.key));
			items.push(_this.graph.createContextMenuItem("addDescription", "~eu.dariah.de.minfba.schereg.button.add_desc_function", "asterisk", element.id, element.template.options.key));
			items.push(_this.graph.createContextMenuItem("editGrammar", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key));
			items.push(_this.graph.createContextMenuSeparator());
			items.push(_this.graph.createContextMenuItem("removeElement", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key));
		} else {
			items.push(_this.graph.createContextMenuItem("editElement", "~eu.dariah.de.minfba.common.link.view", "edit", element.id, element.template.options.key));
		}
		return items; 
};

MappedConceptEditor.prototype.init = function() {
	var _this = this;
	
	var commonElementOptions = {
			visible: true,
			collapsible: false,
			isMappable: false,
			isInteractive: false,
			getContextMenuItems: _this.getElementContextMenu,
			hierarchyOutConnector: { positionfunction: function(element) {
				return { x: element.rectangle.width, y: Math.floor(element.rectangle.height / 2) };
			} }	
	}
	
	this.graph = new Model(this.context.canvas, {
		readOnly: _this.readOnly,
		elementTemplateOptions: [
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "LogicalRoot", 
		                        	 visible: false,
		                        	 getContextMenuItems: undefined
		                         }),
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "Nonterminal", 
		                        	 primaryColor: "#e6f1ff", 
		                        	 secondaryColor: "#0049a6"	 
		                         }),
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "Label", 
		                        	 primaryColor: "#f3e6ff", 
		                        	 secondaryColor: "#5700a6"	 
		                         }),
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "DescriptionGrammarImpl", 
		                        	 primaryColor: "#FFE173", 
		                        	 secondaryColor: "#6d5603",
		                        	 offsetFunction: function(x, y, element, parentElement, isTarget, elementPositioningDelta) {
		                        		 var delta = {};
		                        		 delta.y = y;
		                        		 delta.x = x + 30 + parentElement.getRectangle().width;
		                        		 return delta;
		                        	 },
		                        	 radius: 5
		                         }),
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "TransformationFunctionImpl", 
		                        	 primaryColor: "#FFE173", 
		                        	 offsetFunction: function(x, y, element, parentElement, isTarget, elementPositioningDelta) {
		                        		 var delta = {};
		                        		 delta.y = y;
		                        		 delta.x = x + 30 + parentElement.getRectangle().width;
		                        		 return delta;
		                        	 },
		                        	 secondaryColor: "#6d5603",
		                        	 radius: 5
		                         })],
		mappingTemplateOption : {
			relativeControlPointX : 4, 
			connectionHoverTolerance : 5,
			//getContextMenuItems: _this.getConnectionContextMenu,
			functionTemplateOptions : {
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 3
			}
		}
	});
	this.source = this.graph.addArea({ getContextMenuItems: _this.getAreaContextMenu });
	this.target = this.graph.addArea({ getContextMenuItems: _this.getAreaContextMenu });
	
	this.graph.init();
	this.resize();
	
	this.getElementHierarchy(this.options.sourcePath, this.source, true);
 	this.getElementHierarchy(this.options.targetPath, this.target, false);
 	
 	this.addMapping();
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

MappedConceptEditor.prototype.getElementHierarchy = function(path, area, isSource) {
	var _this = this;
	$.ajax({
	    url: path,
	    async: false,
	    type: "GET",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	
	    	var wrapper = {};
	    	wrapper.childNonterminals = data;
	    	wrapper.id = "-1";
	    	wrapper.name = "~Input";
	    	wrapper.transient = true;
	    	wrapper.type = "null";
	    	wrapper.simpleType = "LogicalRoot";
	    	
	    	_this.processElementHierarchy(wrapper, area, isSource);
	    }
	});
};

MappedConceptEditor.prototype.processElementHierarchy = function(data, area, isSource) {
	var root = area.addElement(data.simpleType, null, data.id, this.formatLabel(data.name), null);
	this.generateTree(area, root, data.childNonterminals, null, data.grammars, isSource);
	area.elements[0].setExpanded(true);
	this.graph.update();
};


MappedConceptEditor.prototype.generateTree = function(area, parent, nonterminals, subelements, grammars, isSource) {

	if (nonterminals!=null && nonterminals instanceof Array) {
		for (var i=0; i<nonterminals.length; i++) {
			var icon = null;
			if (nonterminals[i].terminalId==null || nonterminals[i].terminalId=="") {
				icon = this.options.icons.warning;
			}
			var e = area.addElement(nonterminals[i].simpleType, parent, nonterminals[i].id, this.formatLabel(nonterminals[i].name), icon);
			if (!isSource) {
				this.targetElements.push(nonterminals[i].id);
			}
			this.generateTree(area, e, nonterminals[i].childNonterminals, null, nonterminals[i].grammars, isSource);
		}
	}
	if (grammars != null && grammars instanceof Array) {
		for (var i=0; i<grammars.length; i++) {
			var icon = null;
			if (grammars[i].error==true) {
				icon = this.options.icons.error;
			}
			var fDesc = area.addElement(grammars[i].simpleType, parent, grammars[i].id, this.formatLabel("g:"), icon);
			
			if (isSource) {
				this.sourceGrammars.push(grammars[i].id);
			}
			
			if (grammars[i].transformationFunctions != null && grammars[i].transformationFunctions instanceof Array) {
				this.functionId = grammars[i].transformationFunctions[0].id;
			}
			
		}
	}
	if (subelements!=null && subelements instanceof Array) {
		for (var i=0; i<subelements.length; i++) {
			var e = area.addElement(subelements[i].simpleType, parent, subelements[i].id, this.formatLabel(subelements[i].name), null);
			if (!isSource) {
				this.targetElements.push(subelements[i].id);
			}
			this.generateTree(area, e, null, subelements[i].subLabels, subelements[i].grammars, isSource);
		}
	}
};

MappedConceptEditor.prototype.addMapping = function() {
	var lhs = [];
	var rhs = [];
	
	for (var i=0; i<this.sourceGrammars.length; i++) {
		lhs.push(this.source.getElementById(this.sourceGrammars[i]).getConnector("mappings"));
	}

	for (var j=0; j<this.targetElements.length; j++) {
		rhs.push(this.target.getElementById(this.targetElements[j]).getConnector("mappings"));	
	}
	if (lhs != null && rhs != null) {			
		this.graph.addMappingConnection(lhs, rhs, this.functionId, true);   			
	}
};

MappedConceptEditor.prototype.formatLabel = function(label) {
	if (label.length > 25) {
		return label.substring(0,25) + "...";
	} else {
		return label;
	}	
};
