/**   - getContextMenuItems() must return an array of item objects
*   	- {key: ..., label: ..., glyphicon: ..., e: ..., callback: function(itemKey, e)} for items or
*   	- {key: "-"} for a separator
*/
SchemaEditor.prototype.initGraph = function() {
	var _this = this;
	this.graph = new Model(this.context.canvas, 
			[{
				key: "Nonterminal",
				primaryColor: "#e6f1ff", secondaryColor: "#0049a6",
				getContextMenuItems: function(element) { 
					var items = [
					    _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
					    _this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
					    _this.graph.createContextMenuSeparator(),
						_this.graph.createContextMenuItem("addNonterminal", "~eu.dariah.de.minfba.schereg.button.add_nonterminal", "plus", element.id, element.template.options.key),
						_this.graph.createContextMenuItem("addDescription", "~eu.dariah.de.minfba.schereg.button.add_desc_function", "plus", element.id, element.template.options.key),
						_this.graph.createContextMenuItem("editElement", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key),
						_this.graph.createContextMenuSeparator(),
						_this.graph.createContextMenuItem("removeElement", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key),
					];
					return items; 
				}
			}, {
				key: "Label",
				primaryColor: "#f3e6ff", secondaryColor: "#5700a6"
			}, {
				key: "TransformationFunctionImpl",
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5
			}, {
				key: "DescriptionGrammarImpl",
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5
			}]);
	
	this.area = this.graph.addArea({
		contextMenuItems: [
							_this.graph.createContextMenuItem("expandAll", "~eu.dariah.de.minfba.schereg.button.expand_all", "resize-full", this.schema.id, "schema"),
							_this.graph.createContextMenuItem("collapseAll", "~eu.dariah.de.minfba.schereg.button.collapse_all", "resize-small", this.schema.id, "schema"),
							_this.graph.createContextMenuItem("reset", "~eu.dariah.de.minfba.common.link.reset_view", "repeat", this.schema.id, "schema"),
							_this.graph.createContextMenuSeparator(),
							_this.graph.createContextMenuItem("reload", "~eu.dariah.de.minfba.common.link.reload_data", "refresh", this.schema.id, "schema"),
							_this.graph.createContextMenuItem("exportSchema", "~eu.dariah.de.minfba.schereg.button.export", "cloud-download", this.schema.id, "schema"),
							_this.graph.createContextMenuSeparator(),
							// Only when allowed
							_this.graph.createContextMenuItem("importSchema", "~eu.dariah.de.minfba.schereg.button.import", "cloud-upload", this.schema.id, "schema"),
							_this.graph.createContextMenuItem("createRoot", "~eu.dariah.de.minfba.schereg.button.create_root", "plus", this.schema.id, "schema"),
						]
	});
	this.graph.init();
	this.createActionButtons(this.schemaContextButtons, this.area.getContextMenuItems());
	
	this.contextMenuClickEventHandler = this.handleContextMenuClicked.bind(this);
	document.addEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler, false);
};

SchemaEditor.prototype.handleContextMenuClicked = function(e) {
	this.performTreeAction(e.key, e.id, e.nodeType);
};

SchemaEditor.prototype.performTreeAction = function(action, elementId, elementType) {	
	switch(action) {
		case "expandFromHere" : return this.area.expandFromElement(elementId, true);
		case "collapseFromHere" : return this.area.expandFromElement(elementId, false);
	
		case "addNonterminal": return this.addNode(elementType, elementId, "nonterminal");
	    case "addDescription": return this.addNode(elementType, elementId, "grammar");
	    case "addTransformation": return this.addNode(elementType, elementId, "transformation");
	    case "addLabel": return this.addNode(elementType, elementId, "label");
	    
	    case "editElement" : return this.editElement(elementId);
	    case "editGrammar" : return this.editGrammar(elementId);
	    case "editFunction" : return this.editFunction(elementId);
	    
	    case "removeElement" :  return this.removeElement(elementType, elementId);
	    
	    case "expandAll" :  return this.area.expandAll(true);
	    case "collapseAll" : return this.area.expandAll(false);
	    case "reload" : return this.reloadElementHierarchy();
	    case "reset" : return this.area.resetView();
	    
	    case "exportSchema" : return this.exportSchema();
	    case "importSchema" : return this.triggerUploadFile();
	    case "createRoot" : return this.createRoot();
	    
	    default:
	        throw new Error("Unknown tree action requested: " + action);
	}  
};

SchemaEditor.prototype.loadElementHierarchy = function() {
	var _this = this;
	$.ajax({
	    url: this.pathname + "/async/getHierarchy",
	    type: "GET",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	_this.processElementHierarchy(data);
	    	_this.updateGraph();
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.reloadElementHierarchy = function(callback) {
	if (schemaEditor.area.root==null) {
		this.loadElementHierarchy();
		return;
	}
	
	var rootX = this.area.root.rectangle.x;
	var rootY = this.area.root.rectangle.y;
	var expandedItemIds = this.area.getExpandedElementIds(this.area.root);

	
	var selectedItemIds = [];
	for (var i=0; i<this.graph.selectedItems.length; i++) {
		selectedItemIds.push(this.graph.selectedItems[i].id);
	}
	
	var _this = this;
	$.ajax({
	    url: this.pathname + "/async/getHierarchy",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	_this.area.clear();
	    	_this.processElementHierarchy(data);	    	
	    	_this.area.root.rectangle = new Rectangle(rootX, rootY, _this.area.root.rectangle.width, _this.area.root.rectangle.height);
	    			
	    	_this.area.selectElementsByIds(_this.area.root, selectedItemIds);    		
	    	_this.area.expandElementsByIds(_this.area.root, expandedItemIds);
	    	if (callback!==undefined) {
	    		callback();
	    	}
	    	_this.area.invalidate();
	    	_this.graph.paint();
	    },
	    error: function(jqXHR, textStatus, errorThrown) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	_this.initGraph();
	    }
	});	
};

SchemaEditor.prototype.processElementHierarchy = function(data) {
	var root = this.area.addElement(data.simpleType, null, data.id, this.formatLabel(data.name), null);
	this.generateTree(this.area, root, data.childNonterminals, null, data.functions, true);
	this.area.elements[0].setExpanded(true);
	this.graph.update();
};


SchemaEditor.prototype.generateTree = function(area, parent, nonterminals, subelements, functions, isSource) {

	if (nonterminals!=null && nonterminals instanceof Array) {
		for (var i=0; i<nonterminals.length; i++) {
			var icon = null;
			if (nonterminals[i].terminalId==null || nonterminals[i].terminalId=="") {
				icon = this.options.icons.warning;
			}
			var e = this.area.addElement(nonterminals[i].simpleType, parent, nonterminals[i].id, this.formatLabel(nonterminals[i].name), icon);
			
			this.generateTree(area, e, nonterminals[i].childNonterminals, null, nonterminals[i].functions, isSource);
		}
	}
	if (functions != null && functions instanceof Array) {
		for (var i=0; i<functions.length; i++) {
			var icon = null;
			if (functions[i].error==true) {
				icon = this.options.icons.error;
			}
			var fDesc = this.area.addElement(functions[i].simpleType, parent, functions[i].id, this.formatLabel("g: " + functions[i].grammarName), icon);
			
			if (functions[i].transformationFunctions != null && functions[i].transformationFunctions instanceof Array) {
				for (var j=0; j<functions[i].transformationFunctions.length; j++) {
					if (functions[i].transformationFunctions[j].error==true) {
						icon = this.options.icons.error;
					}
					var fOut = this.area.addElement(functions[i].transformationFunctions[j].simpleType, fDesc, functions[i].transformationFunctions[j].id, 
							this.formatLabel("f: " + functions[i].transformationFunctions[j].name), icon);
					
					if (functions[i].transformationFunctions[j].outputElements != null && functions[i].transformationFunctions[j].outputElements instanceof Array) {
						for (var k=0; k<functions[i].transformationFunctions[j].outputElements.length; k++) {
							
							var e = this.area.addElement(functions[i].transformationFunctions[j].outputElements[k].simpleType, fOut, functions[i].transformationFunctions[j].outputElements[k].id, 
									this.formatLabel(functions[i].transformationFunctions[j].outputElements[k].name), null);
							
							
							this.generateTree(area, e, 
									functions[i].transformationFunctions[j].outputElements[k].childNonterminals,
									functions[i].transformationFunctions[j].outputElements[k].subLabels,
									functions[i].transformationFunctions[j].outputElements[k].functions, isSource);
						}
					}
				}
			}
		}
	}
	if (subelements!=null && subelements instanceof Array) {
		for (var i=0; i<subelements.length; i++) {
			var e = this.area.addElement(subelements[i].simpleType, parent, subelements[i].id, this.formatLabel(subelements[i].name), null);
			if (parent != null) {
				parent.addChild(e);
			}
			this.generateTree(area, e, null, subelements[i].subLabels, subelements[i].functions, isSource);
		}
	}
};

SchemaEditor.prototype.formatLabel = function(label) {
	if (label.length > 25) {
		return label.substring(0,25) + "...";
	} else {
		return label;
	}	
};

SchemaEditor.prototype.updateGraph = function() {
	this.graph.update();
};