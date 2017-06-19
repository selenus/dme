/**   - getContextMenuItems() must return an array of item objects
*   	- {key: ..., label: ..., glyphicon: ..., e: ..., callback: function(itemKey, e)} for items or
*   	- {key: "-"} for a separator
*/
SchemaEditor.prototype.initGraph = function() {
	var _this = this;
	this.graph = new Model(this.context.canvas, {
		readOnly: !(_this.schema.owned || _this.schema.write),
		elementTemplateOptions : [{
			key: "Nonterminal",
			primaryColor: "#e6f1ff", secondaryColor: "#0049a6",
			getContextMenuItems: function(element) { 
				var items = [
				    _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
				    _this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
				    _this.graph.createContextMenuSeparator()
				];
				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("editElement", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("addNonterminal", "~eu.dariah.de.minfba.schereg.button.add_nonterminal", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("addDescription", "~eu.dariah.de.minfba.schereg.button.add_desc_function", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("assignChild", "~eu.dariah.de.minfba.schereg.button.assign_child", "link", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("setProcessingRoot", "~eu.dariah.de.minfba.schereg.button.set_processing_root", "grain", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("removeElement", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editElement", "~eu.dariah.de.minfba.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}, 
		}, {
			key: "Label",
			primaryColor: "#f3e6ff", secondaryColor: "#5700a6",
			getContextMenuItems: function(element) { 
				var items = [
				    _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
				    _this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
				    _this.graph.createContextMenuSeparator()
				];
				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("editElement", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("addLabel", "~eu.dariah.de.minfba.schereg.button.add_label", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("addDescription", "~eu.dariah.de.minfba.schereg.button.add_desc_function", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("assignChild", "~eu.dariah.de.minfba.schereg.button.assign_child", "link", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("removeElement", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editElement", "~eu.dariah.de.minfba.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}
		}, {
			key: "Function",
			primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5,
			getContextMenuItems: function(element) { 
				var items = [
				    _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
				    _this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
				    _this.graph.createContextMenuSeparator()
				];
				
				if (_this.schema.owned || _this.schema.write) {
					items.push(
							_this.graph.createContextMenuItem("addLabel", "~eu.dariah.de.minfba.schereg.button.add_label", "asterisk", element.id, element.template.options.key),
							_this.graph.createContextMenuItem("editFunction", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key),
							_this.graph.createContextMenuSeparator(),
							_this.graph.createContextMenuItem("removeElement", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key)
					);
				} else {
					items.push(_this.graph.createContextMenuItem("editFunction", "~eu.dariah.de.minfba.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}
		}, {
			key: "Grammar",
			primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5,
			getContextMenuItems: function(element) { 
				var items = [
				    _this.graph.createContextMenuItem("expandFromHere", "~eu.dariah.de.minfba.schereg.button.expand_from_here", "resize-full", element.id, element.template.options.key),
				    _this.graph.createContextMenuItem("collapseFromHere", "~eu.dariah.de.minfba.schereg.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
				    _this.graph.createContextMenuSeparator()
				];
				if (_this.schema.owned || _this.schema.write) {
					items.push(
							_this.graph.createContextMenuItem("addFunction", "~eu.dariah.de.minfba.schereg.button.add_trans_function", "asterisk", element.id, element.template.options.key),
							_this.graph.createContextMenuItem("editGrammar", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key),
							_this.graph.createContextMenuItem("moveUpGrammar", "~eu.dariah.de.minfba.common.link.move_up", "arrow-up", element.id, element.template.options.key),
							_this.graph.createContextMenuItem("moveDownGrammar", "~eu.dariah.de.minfba.common.link.move_down", "arrow-down", element.id, element.template.options.key),
							_this.graph.createContextMenuSeparator(),
							_this.graph.createContextMenuItem("removeElement", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key)
					);
				} else {
					items.push(_this.graph.createContextMenuItem("editGrammar", "~eu.dariah.de.minfba.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}
		}]
	});
	
	this.area = this.graph.addArea({
		getContextMenuItems: function(area) { 
			var items = [
				_this.graph.createContextMenuItem("expandAll", "~eu.dariah.de.minfba.schereg.button.expand_all", "resize-full", area, "schema"),
				_this.graph.createContextMenuItem("collapseAll", "~eu.dariah.de.minfba.schereg.button.collapse_all", "resize-small", area, "schema"),
				_this.graph.createContextMenuItem("reset", "~eu.dariah.de.minfba.common.link.reset_view", "repeat", area, "schema"),
				_this.graph.createContextMenuSeparator(),
				_this.graph.createContextMenuItem("reload", "~eu.dariah.de.minfba.common.link.reload_data", "refresh", area, "schema"),
				_this.graph.createContextMenuItem("exportSchema", "~eu.dariah.de.minfba.schereg.button.export", "cloud-download", area, "schema"),
			]; 
			if (_this.schema.owned || _this.schema.write) {
				items.push(_this.graph.createContextMenuSeparator(),
						_this.graph.createContextMenuItem("importSchema", "~eu.dariah.de.minfba.schereg.button.import", "cloud-upload", area, "schema"),
						_this.graph.createContextMenuItem("createRoot", "~eu.dariah.de.minfba.schereg.button.create_root", "plus", area, "schema")); 
				
			}
			return items;
		}
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
	
		case "addNonterminal": return this.addNonterminal(elementId);
	    case "addDescription": return this.addDescription(elementId);
	    case "addFunction": return this.addTransformation(elementId);
	    case "addLabel": return this.addLabel(elementId);
	    
	    case "editElement" : return this.editElement(elementId);
	    case "editGrammar" : return this.editGrammar(elementId);
	    case "editFunction" : return this.editFunction(elementId);
	    case "assignChild" : return this.assignChild(elementId);
	    case "setProcessingRoot" : return this.setProcessingRoot(elementId);
	    
	    case "moveUpGrammar" : return this.moveGrammar(1);
	    case "moveDownGrammar" : return this.moveGrammar(-1);
	    
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
	
	if (this.checkSchemaState()===true) {
		
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
	} else {
		setTimeout(function() { _this.loadElementHierarchy() }, 2000);
	}
};

SchemaEditor.prototype.checkSchemaState = function() {
	var _this = this;
	var result = false;
	
	$.ajax({
	    url: this.pathname + "/state",
	    type: "GET",
	    async: false,
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	if (data.pojo.processing) {
	    		_this.stateNotificationId = __notifications.showTranslatedMessage(NOTIFICATION_TYPES.INFO, 
	    				"~eu.dariah.de.minfba.schereg.notification.import_processing.head", 
	    				"~eu.dariah.de.minfba.schereg.notification.import_processing.body", 
	    				_this.stateNotificationId, false);
	    		$("#schema-editor-canvas").addClass("hide");
	    	} else {
	    		if (data.pojo.error) {
		    		_this.stateNotificationId = __notifications.showTranslatedMessage(NOTIFICATION_TYPES.ERROR, 
		    				"~eu.dariah.de.minfba.schereg.notification.import_error.head", 
		    				"~eu.dariah.de.minfba.schereg.notification.import_error.body", 
		    				_this.stateNotificationId, false);
		    	} else if (data.pojo.ready) {
		    		__notifications.quitMessage(_this.stateNotificationId);
		    		_this.stateNotificationId = undefined;
		    	}
	    		result = true;
	    		$("#schema-editor-canvas").removeClass("hide");
	    	}	
	    },
	    error: function(jqXHR, textStatus, errorThrown) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	result = true;
	    }
	});
	return result;
};


SchemaEditor.prototype.reloadElementHierarchy = function(callback) {
	if (editor.area.root==null) {
		this.loadElementHierarchy();
		return;
	}
	
	var _this = this;
	var rootX = this.area.root.rectangle.x;
	var rootY = this.area.root.rectangle.y;
	var expandedItemIds = this.area.getExpandedElementIds(this.area.root);

	
	var selectedItemIds = [];
	for (var i=0; i<this.graph.selectedItems.length; i++) {
		selectedItemIds.push(this.graph.selectedItems[i].id);
	}
	
	if (this.checkSchemaState()===true) {
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
	} else {
		setTimeout(function() { _this.reloadElementHierarchy(callback) }, 2000);
	}
};

SchemaEditor.prototype.processElementHierarchy = function(data) {
	var root = this.area.addElement(data.type, null, data.id, this.formatLabel(data.label), null, data.pRoot);
	this.generateTree(this.area, root, data.childElements, true, data.pRoot);
	this.area.elements[0].setExpanded(true);
	this.graph.update();
};

SchemaEditor.prototype.generateTree = function(area, parentNode, elements, isSource, processed) {
	if (elements!=null && elements instanceof Array) {
		for (var i=0; i<elements.length; i++) {
			var icon = null;
			if (elements[i].state==="ERROR") {
				icon = this.options.icons.error;
			} else if (elements[i].state==="WARNING") {
				icon = this.options.icons.warning;
			}
			var childProcessed = processed || elements[i].pRoot;
			var e = this.area.addElement(elements[i].type, parentNode, elements[i].id, this.formatLabel(elements[i].label), icon, childProcessed);
			
			this.generateTree(area, e, elements[i].childElements, isSource, childProcessed);
		}
	}
}

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