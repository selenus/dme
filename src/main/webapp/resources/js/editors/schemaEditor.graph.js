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
				var items = [];
				items.push(_this.graph.createContextMenuItem("expandFromHere", "~de.unibamberg.minf.dme.button.expand_from_here", "resize-full", element.id, element.template.options.key));
				items.push(_this.graph.createContextMenuItem("collapseFromHere", "~de.unibamberg.minf.dme.button.collapse_from_here", "resize-small", element.id, element.template.options.key));
				
				if (element.reusing || element.reused) {
					items.push(_this.graph.createContextMenuItem("showReused", "~de.unibamberg.minf.dme.button.show_reused", "resize-full", element.id, element.template.options.key));
					if (element.reusing) {
						items.push(_this.graph.createContextMenuItem("modelIndividually", "~de.unibamberg.minf.dme.button.model_individually", "asterisk", element, element.template.options.key));
					}
				}
				
				items.push(_this.graph.createContextMenuSeparator());

				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("editElement", "~de.unibamberg.minf.common.link.edit", "edit", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("addNonterminal", "~de.unibamberg.minf.dme.button.add_nonterminal", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("addDescription", "~de.unibamberg.minf.dme.button.add_desc_function", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("assignChild", "~de.unibamberg.minf.dme.button.assign_child", "link", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());					
					items.push(_this.graph.createContextMenuItem("setProcessingRoot", "~de.unibamberg.minf.dme.button.set_processing_root", "grain", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("exportSubtree", "~de.unibamberg.minf.dme.button.export_from_here", "cloud-download", element.id));
					items.push(_this.graph.createContextMenuItem("importSubtree", "~de.unibamberg.minf.dme.button.import_here", "leaf", element.id));
					items.push(_this.graph.createContextMenuSeparator());
					if (element.disabled) {
						items.push(_this.graph.createContextMenuItem("enableElement", "~de.unibamberg.minf.common.link.enable", "plus", element.id, element.template.options.key));
					} else {
						items.push(_this.graph.createContextMenuItem("disableElement", "~de.unibamberg.minf.common.link.disable", "minus", element.id, element.template.options.key));
					}
					
					items.push(_this.graph.createContextMenuItem("removeElement", "~de.unibamberg.minf.common.link.delete", "trash", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editElement", "~de.unibamberg.minf.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}
		}, {
			key: "Label",
			primaryColor: "#f3e6ff", secondaryColor: "#5700a6",
			getContextMenuItems: function(element) { 
				var items = [
				    _this.graph.createContextMenuItem("expandFromHere", "~de.unibamberg.minf.dme.button.expand_from_here", "resize-full", element.id, element.template.options.key),
				    _this.graph.createContextMenuItem("collapseFromHere", "~de.unibamberg.minf.dme.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
				    _this.graph.createContextMenuSeparator()
				];
				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("editElement", "~de.unibamberg.minf.common.link.edit", "edit", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("addLabel", "~de.unibamberg.minf.dme.button.add_label", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("addDescription", "~de.unibamberg.minf.dme.button.add_desc_function", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("assignChild", "~de.unibamberg.minf.dme.button.assign_child", "link", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("exportSubtree", "~de.unibamberg.minf.dme.button.export_from_here", "cloud-download", element.id));
					items.push(_this.graph.createContextMenuItem("importSubtree", "~de.unibamberg.minf.dme.button.import_here", "leaf", element.id));
					items.push(_this.graph.createContextMenuSeparator());
					if (element.disabled) {
						items.push(_this.graph.createContextMenuItem("enableElement", "~de.unibamberg.minf.common.link.enable", "plus", element.id, element.template.options.key));
					} else {
						items.push(_this.graph.createContextMenuItem("disableElement", "~de.unibamberg.minf.common.link.disable", "minus", element.id, element.template.options.key));
					}
					items.push(_this.graph.createContextMenuItem("removeElement", "~de.unibamberg.minf.common.link.delete", "trash", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editElement", "~de.unibamberg.minf.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}
		}, {
			key: "Function",
			primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5,
			getContextMenuItems: function(element) { 
				var items = [
				    _this.graph.createContextMenuItem("expandFromHere", "~de.unibamberg.minf.dme.button.expand_from_here", "resize-full", element.id, element.template.options.key),
				    _this.graph.createContextMenuItem("collapseFromHere", "~de.unibamberg.minf.dme.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
				    _this.graph.createContextMenuSeparator()
				];
				
				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("addLabel", "~de.unibamberg.minf.dme.button.add_label", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("editFunction", "~de.unibamberg.minf.common.link.edit", "edit", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("exportSubtree", "~de.unibamberg.minf.dme.button.export_from_here", "cloud-download", element.id));
					items.push(_this.graph.createContextMenuItem("importSubtree", "~de.unibamberg.minf.dme.button.import_here", "leaf", element.id));
					items.push(_this.graph.createContextMenuSeparator());
					if (element.disabled) {
						items.push(_this.graph.createContextMenuItem("enableElement", "~de.unibamberg.minf.common.link.enable", "plus", element.id, element.template.options.key));
					} else {
						items.push(_this.graph.createContextMenuItem("disableElement", "~de.unibamberg.minf.common.link.disable", "minus", element.id, element.template.options.key));
					}
					items.push(_this.graph.createContextMenuItem("removeElement", "~de.unibamberg.minf.common.link.delete", "trash", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editFunction", "~de.unibamberg.minf.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}
		}, {
			key: "Grammar",
			primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5,
			getContextMenuItems: function(element) { 
				var items = [
				    _this.graph.createContextMenuItem("expandFromHere", "~de.unibamberg.minf.dme.button.expand_from_here", "resize-full", element.id, element.template.options.key),
				    _this.graph.createContextMenuItem("collapseFromHere", "~de.unibamberg.minf.dme.button.collapse_from_here", "resize-small", element.id, element.template.options.key),
				    _this.graph.createContextMenuSeparator()
				];
				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("addFunction", "~de.unibamberg.minf.dme.button.add_trans_function", "asterisk", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("editGrammar", "~de.unibamberg.minf.common.link.edit", "edit", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("moveUpGrammar", "~de.unibamberg.minf.common.link.move_up", "arrow-up", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuItem("moveDownGrammar", "~de.unibamberg.minf.common.link.move_down", "arrow-down", element.id, element.template.options.key));
					items.push(_this.graph.createContextMenuSeparator());
					items.push(_this.graph.createContextMenuItem("exportSubtree", "~de.unibamberg.minf.dme.button.export_from_here", "cloud-download", element.id));
					items.push(_this.graph.createContextMenuItem("importSubtree", "~de.unibamberg.minf.dme.button.import_here", "leaf", element.id));
					items.push(_this.graph.createContextMenuSeparator());
					if (element.disabled) {
						items.push(_this.graph.createContextMenuItem("enableElement", "~de.unibamberg.minf.common.link.enable", "plus", element.id, element.template.options.key));
					} else {
						items.push(_this.graph.createContextMenuItem("disableElement", "~de.unibamberg.minf.common.link.disable", "minus", element.id, element.template.options.key));
					}
					items.push(_this.graph.createContextMenuItem("removeElement", "~de.unibamberg.minf.common.link.delete", "trash", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editGrammar", "~de.unibamberg.minf.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}
		}, {
			key: "Terminal",
			primaryColor: "#E4FFEF", secondaryColor: "#00772C",
			getContextMenuItems: function(element) { 
				var items = [];
				items.push(_this.graph.createContextMenuItem("expandFromHere", "~de.unibamberg.minf.dme.button.expand_from_here", "resize-full", element.id, element.template.options.key));
				items.push(_this.graph.createContextMenuItem("collapseFromHere", "~de.unibamberg.minf.dme.button.collapse_from_here", "resize-small", element.id, element.template.options.key));
				
				if (element.reusing || element.reused) {
					items.push(_this.graph.createContextMenuItem("showReused", "~de.unibamberg.minf.dme.button.show_reused", "resize-full", element.id, element.template.options.key));
				}
				
				items.push(_this.graph.createContextMenuSeparator());

				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("editTerminal", "~de.unibamberg.minf.common.link.edit", "edit", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editTerminal", "~de.unibamberg.minf.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}, 
		}, {
			key: "Terminal/Missing",
			primaryColor: "#E4FFEF", secondaryColor: "#A40000",
			getContextMenuItems: function(element) { 
				var items = [];
				items.push(_this.graph.createContextMenuItem("expandFromHere", "~de.unibamberg.minf.dme.button.expand_from_here", "resize-full", element.id, element.template.options.key));
				items.push(_this.graph.createContextMenuItem("collapseFromHere", "~de.unibamberg.minf.dme.button.collapse_from_here", "resize-small", element.id, element.template.options.key));
				
				if (element.reusing || element.reused) {
					items.push(_this.graph.createContextMenuItem("showReused", "~de.unibamberg.minf.dme.button.show_reused", "resize-full", element.id, element.template.options.key));
				}
				
				items.push(_this.graph.createContextMenuSeparator());

				if (_this.schema.owned || _this.schema.write) {
					items.push(_this.graph.createContextMenuItem("editTerminal", "~de.unibamberg.minf.common.link.edit", "edit", element.id, element.template.options.key));
				} else {
					items.push(_this.graph.createContextMenuItem("editTerminal", "~de.unibamberg.minf.common.link.view", "edit", element.id, element.template.options.key));
				}
				return items; 
			}, 
		}
		]
	});
	
	this.area = this.graph.addArea({
		getContextMenuItems: function(area) { 
			var items = [
				_this.graph.createContextMenuItem("expandAll", "~de.unibamberg.minf.dme.button.expand_all", "resize-full", area, "schema"),
				_this.graph.createContextMenuItem("collapseAll", "~de.unibamberg.minf.dme.button.collapse_all", "resize-small", area, "schema"),
				_this.graph.createContextMenuItem("reset", "~de.unibamberg.minf.common.link.reset_view", "repeat", area, "schema"),
				_this.graph.createContextMenuSeparator(),
				_this.graph.createContextMenuItem("reload", "~de.unibamberg.minf.common.link.reload_data", "refresh", area, "schema"),
				_this.graph.createContextMenuItem("exportSchema", "~de.unibamberg.minf.dme.button.export", "cloud-download", area, "schema"),
			]; 
			if (_this.schema.owned || _this.schema.write) {
				items.push(_this.graph.createContextMenuSeparator(),
						_this.graph.createContextMenuItem("importSchema", "~de.unibamberg.minf.dme.button.import", "cloud-upload", area, "schema"),
						_this.graph.createContextMenuItem("createRoot", "~de.unibamberg.minf.dme.button.create_root", "plus", area, "schema")); 
				
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
	    case "editTerminal": return this.editTerminal(elementType, elementId);
	    
	    case "assignChild" : return this.assignChild(elementId);
	    case "setProcessingRoot" : return this.setProcessingRoot(elementId);
	    
	    case "moveUpGrammar" : return this.moveGrammar(1);
	    case "moveDownGrammar" : return this.moveGrammar(-1);
	    
	    case "exportSubtree" :  return this.exportSubtree(elementId);
	    case "importSubtree" :  return this.importSchema(elementId);
	    
	    case "removeElement" :  return this.removeElement(elementType, elementId);
	    case "disableElement" :  return this.toggleElementDisabled(elementType, elementId, true);
	    case "enableElement" :  return this.toggleElementDisabled(elementType, elementId, false);
	    
	    case "expandAll" :  return this.area.expandAll(true);
	    case "collapseAll" : return this.area.expandAll(false);
	    case "reload" : return this.reloadElementHierarchy();
	    case "reset" : return this.area.resetView();
	    
	    case "showReused" : return this.showReuseOccurrences(elementId);
	    case "modelIndividually" : return this.modelIndividually(elementId);
	    
	    case "exportSchema" : return this.exportSchema();
	    case "importSchema" : return this.importSchema();
	    case "createRoot" : return this.createRoot();
	    
	    default:
	        throw new Error("Unknown tree action requested: " + action);
	}  
};

SchemaEditor.prototype.showReuseOccurrences = function(elementId) {
	this.area.ensureExpandedTo(elementId);
	//this.area.selectElementsByIds(this.area.root, [elementId]);
};

SchemaEditor.prototype.modelIndividually = function(elementId) {
	var _this = this;
	bootbox.confirm({
		title: __translator.translate("~de.unibamberg.minf.dme.dialog.confirm_clone_tree.head"),
		message: __translator.translate("~de.unibamberg.minf.dme.dialog.confirm_clone_tree.body"),
		callback: function(result) {
			if(result) {
				var path = _this.area.getElementPath(_this.graph.selectedItems[0]);
				$.ajax({
				    url: _this.pathname + "/element/" + _this.graph.selectedItems[0].id + "/async/clone",
				    type: "POST",
				    data: {path: path.reverse()},
				    dataType: "json",
				    //contentType: "application/json; charset=UTF-8",
				    success: function(data) {
				    	_this.reloadElementHierarchy();
				    },
				    error: __util.processServerError
				});
			}
		}
	});
};

SchemaEditor.prototype.loadElementHierarchy = function() {
	var _this = this;
	
	if (this.checkSchemaState()===true) {
		
		$.ajax({
		    url: this.pathname + "/async/getHierarchy",
		    data: { model: $("#current-model-nature").val(), collectNatureClasses: true },
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
	    				"~de.unibamberg.minf.dme.notification.import_processing.head", 
	    				"~de.unibamberg.minf.dme.notification.import_processing.body", 
	    				_this.stateNotificationId, false);
	    		$("#schema-editor-canvas").addClass("hide");
	    	} else {
	    		if (data.pojo.error) {
		    		_this.stateNotificationId = __notifications.showTranslatedMessage(NOTIFICATION_TYPES.ERROR, 
		    				"~de.unibamberg.minf.dme.notification.import_error.head", 
		    				"~de.unibamberg.minf.dme.notification.import_error.body", 
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

SchemaEditor.prototype.reloadPage = function() {
	var _this = this;
	if (this.checkSchemaState()===true) {
		window.location.reload();
	} else {
		setTimeout(function() { _this.reloadPage() }, 2000);
	}
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
		    data: { model: $("#current-model-nature").val(), collectNatureClasses: true },
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
	this.generateTree(this.area, data, null, true, (data.pRoot && !data.disabled));
	this.area.elements[0].setExpanded(true);
	this.graph.update();
};

SchemaEditor.prototype.generateTree = function(area, node, parentNode, isSource, processed) {
	var icon = null;
	var terminalMissing = false;
	if (node.state==="ERROR") {
		icon = this.options.icons.error;
	} else if (node.state==="WARNING") {
		icon = this.options.icons.warning;
	} else if (node.state==="REUSING") {
		icon = this.options.icons.reusing;
	} else if (node.state==="REUSED") {
		icon = this.options.icons.reused;
	}
	if (node.type==="Nonterminal" && this.availableNatures!==undefined && this.availableNatures!==null && this.availableNatures.length > 0) {
		if (node.info===undefined || node.info===null || node.info["mappedNatureClasses"]===undefined || node.info["mappedNatureClasses"].length<this.availableNatures.length) {
			icon = this.options.icons.warning;
			terminalMissing = true;
		}
	}
	var childProcessed = (processed || node.pRoot) && !node.disabled;
	var e = this.area.addElement(node.type, parentNode, node.id, this.formatLabel(node.label), icon, childProcessed, node.disabled);
	e.reusing = node.state==="REUSING";
	e.reused = node.state==="REUSED";
	e.terminalMissing = terminalMissing;
		
	if (node.childElements!=null && node.childElements instanceof Array) {
		for (var i=0; i<node.childElements.length; i++) {
			this.generateTree(area, node.childElements[i], e, isSource, childProcessed);
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

SchemaEditor.prototype.processElementDetails = function(data, callback, container, pathPrefix) { 
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.element.name"), data.label));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.element.transient"), data.disabled));
		
	container.append(details); 
	
	if (data.type==="Nonterminal" && this.availableNatures!==undefined && this.availableNatures!==null && this.availableNatures.length > 0) {
		var missing = "";
		for (var i=0; i<this.availableNatures.length; i++) {
			if (data.info===undefined || data.info===null || 
					data.info["mappedNatureClasses"]===undefined || 
					data.info["mappedNatureClasses"].indexOf(this.availableNatures[i])<0) {
				
				var label = "~" + this.availableNatures[i] + ".display_label";
		    	__translator.addTranslation(label);
		    	__translator.getTranslations();
				
				missing = missing + __translator.translate(label) + ", ";
			}
		}
		if (missing.length>0) {
			details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.form.nature.terminals.missing"), missing.substring(0, missing.length-2), undefined, "color-warning"));
		}
	}
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};