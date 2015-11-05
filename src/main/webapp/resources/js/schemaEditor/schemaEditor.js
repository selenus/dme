var schemaEditor;
$(document).ready(function() {
	schemaEditor = new SchemaEditor();
	schemaEditor.initLayout();
	schemaEditor.initGraph();
});
$(window).resize(function() {
	schemaEditor.resizeLayout();
	schemaEditor.resizeContent();
});

var SchemaEditor = function() {
	var _this = this;
	
	this.schemaId = $("#schema-id").val();
	this.schemaOwn = $("#schema-own").val()==="true";
	this.schemaWrite = $("#schema-write").val()==="true";
	
	this.selectedElementId = -1;
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId;
	this.context = document.getElementById("schema-editor-canvas").getContext("2d");
	this.footerOffset = 70;	
	
	this.outerLayout = null;
	this.innerLayout = null;
	this.layoutContainer = $("#schema-editor-layout-container");
	this.editorContainer = $("#schema-editor-container");
		
	this.schemaContextContainer = $("#schema-context-container");
	this.elementContextContainer = $("#schema-element-context-container");
	
	this.elementContextDetail = $("#schema-element-context-info");	
	this.elementContextDetail = $("#schema-element-context-info");
	this.elementContextButtons = $("#schema-element-context-buttons");
	
	this.warningIcon = __util.getBaseUrl() + "resources/img/warning.png";
	this.errorIcon = __util.getBaseUrl() + "resources/img/error.png";
		
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	document.addEventListener("newMappingCellEvent", this.newMappingCellHandler, false);
	
	__translator.addTranslations(["~eu.dariah.de.minfba.common.model.id",
	                              "~eu.dariah.de.minfba.common.model.label",
	                              "~eu.dariah.de.minfba.common.model.type",
	                              "~eu.dariah.de.minfba.common.link.edit",
	                              "~eu.dariah.de.minfba.common.link.delete",
	                              "~eu.dariah.de.minfba.schereg.button.add_nonterminal",
	                              "~eu.dariah.de.minfba.schereg.button.add_label", 
	                              "~eu.dariah.de.minfba.schereg.button.add_desc_function",
	                              "~eu.dariah.de.minfba.schereg.button.add_trans_function",
	                              "~eu.dariah.de.minfba.schereg.dialog.confirm_delete",
	                              "~eu.dariah.de.minfba.schereg.dialog.element_label",
	                              "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                              "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                              "~eu.dariah.de.minfba.schereg.model.element.element",
	                              "~eu.dariah.de.minfba.schereg.model.element.name",
	                              "~eu.dariah.de.minfba.schereg.model.element.namespace",
	                              "~eu.dariah.de.minfba.schereg.model.element.attribute",
	                              "~eu.dariah.de.minfba.schereg.model.element.transient",
	                              "~eu.dariah.de.minfba.schereg.model.function.function",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.grammar",
	                              "~eu.dariah.de.minfba.schereg.notification.no_terminal_configured"]);
	__translator.getTranslations();
	
	this.logArea = new LogArea({
		pathPrefix :  __util.getBaseUrl() + "sessions/",
		entityId : _this.schemaId
	});
	
	this.sample_init();
	this.activities_init();
}

SchemaEditor.prototype.initLayout = function() {
	var _this = this;
	this.layoutContainer.addClass("fade");
	this.layoutContainer.removeClass("hide");
	
	var initEastClosed = true;
	if (this.layoutContainer.width()>1100) {
		initEastClosed = false;
	}
	
	this.outerLayout = this.layoutContainer.layout({
		defaults : {
			fxName : "slide",
			fxSpeed : "slow",
			spacing_closed : 14,
			minWidth : 200,
		}, 
		east : {
			size : initEastClosed ? "40%" : "60%",
			minWidth : 200,
			minHeight : 400,
			paneSelector : ".outer-east"
		},
		center : {
			size : initEastClosed ? "60%" : "40%",
			paneSelector : ".outer-center",
			minHeight : 200
		}, 
		south : {
			size : 100,
			paneSelector : ".outer-south"
		},
		onresize: function () {
			_this.resizeContent();
	        return false;
	    }
	});
	
	this.innerLayout = this.layoutContainer.find(".outer-east").layout({
		defaults : {
			fxName : "slide",
			fxSpeed : "slow",
			spacing_closed : 14,
			minWidth : 200,
		}, 
		east : {
			size : "50%",
			paneSelector : ".inner-east",
			initClosed : initEastClosed,
			onopen_start: function () { _this.sample_onPaneOpenStart(); }
		},
		center : {
			size : "50%",
			paneSelector : ".inner-center"
		}
	});
	
	this.layoutContainer.removeClass("fade");
	

};


/*
 	var actions = [];
	if (_this.schemaOwn || _this.schemaWrite) {
		if (e.element.getType() === "Nonterminal" || e.element.getType() === "Label") {
			if (e.element.getType()==="nonterminal") {
				actions[0] = ["addNonterminal", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_nonterminal")];
			} else {
				actions[0] = ["addLabel", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_nonterminal")];
			}
			actions[1] = ["addDescription", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_desc_function")];
			actions[2] = ["editElement", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
			actions[3] = ["removeElement", "trash", "danger", ""];
			_this.getElement(e.element.id);	
		} else if (e.element.getType() === "Grammar") {
			actions[0] = ["addTransformation", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_trans_function")];
			actions[1] = ["editGrammar", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
			actions[2] = ["removeElement", "trash", "danger", ""];
			_this.getGrammar(e.element.id);
		} else if (e.element.getType() === "Function") {
			actions[0] = ["addLabel", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_label")];
			actions[1] = ["editFunction", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
			actions[2] = ["removeElement", "trash", "danger", ""];
			_this.getFunction(e.element.id);
		}
	}
	
	var button;
	for (var i=0; i<actions.length; i++) {
		button = "<button " +
					"class='btn btn-" + actions[i][2] + " btn-sm' " +
					"onclick='schemaEditor." + actions[i][0] + "(); return false;' type='button'>" +
						"<span class='glyphicon glyphicon-" + actions[i][1] + "'></span> " + actions[i][3] + 
				 "</button> ";
		_this.elementContextButtons.append(button);
	}
 
 */

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
					var items = [{ 
						key: "addNonterminal", 
						label: __translator.translate("~eu.dariah.de.minfba.schereg.button.add_nonterminal"), 
						glyphicon: "plus", 
						e: { id: element.id, type: element.simpleType }, 
						callback: _this.addNonterminal
					
					}, { 
						key: "addDescription", 
						label: __translator.translate("~eu.dariah.de.minfba.schereg.button.add_desc_function"), 
						glyphicon: "plus", 
						e: { id: element.id, type: element.simpleType }, 
						callback: _this.addDescription
					
					}, { 
						key: "editElement", 
						label: __translator.translate("~eu.dariah.de.minfba.common.link.edit"), 
						glyphicon: "edit", 
						e: { id: element.id, type: element.simpleType }, 
						callback: _this.editElement
					
					}, { 
						key: "-"
					}, { 
						key: "removeElement", 
						label: __translator.translate("~eu.dariah.de.minfba.common.link.delete"), 
						glyphicon: "trash", 
						e: { id: element.id, type: element.simpleType }, 
						callback: _this.removeElement
					
					}];
					
					
					return items; 
				}
			}, {
				key: "Label",
				primaryColor: "#f3e6ff", secondaryColor: "#5700a6"
			}, {
				key: "Function",
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5
			}, {
				key: "Grammar",
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5
			}]);
	this.schema = this.graph.addArea();
		
	this.graph.init();
 	
 	this.loadElementHierarchy();
 	this.resizeLayout();
 	this.resizeContent();
	this.activities_loadForSchema();
};

SchemaEditor.prototype.resizeLayout = function() {
	var height = Math.floor($(window).height() - this.layoutContainer.offset().top - this.footerOffset);
	
	this.layoutContainer.height(height);
	this.outerLayout.resizeAll();
	this.innerLayout.resizeAll();
}

SchemaEditor.prototype.resizeContent = function() {
	var editorHeight = Math.floor(this.editorContainer.offsetParent().innerHeight() - (this.editorContainer.offset().top - this.editorContainer.offsetParent().offset().top));
	this.editorContainer.css("height", editorHeight + "px");
	
	if (this.context.canvas) {
		this.context.canvas.width = this.editorContainer.width() - 1; // border-bottom
		this.context.canvas.height = editorHeight - 4; // border-top
		window.scroll(0, 0);			
		if (this.graph !== null) {
			if (this.schema != null) {
				this.schema.setSize(new Rectangle(0, 0, this.context.canvas.width, this.context.canvas.height));
			}
			this.graph.update();
		}
	}
	
	this.sample_resize();
};

SchemaEditor.prototype.loadElementHierarchy = function() {
	var _this = this;
	$.ajax({
	    url: this.pathname + "/async/getHierarchy",
	    type: "GET",
	    //dataType: "json",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	
	    	_this.processElementHierarchy(data);
	    	_this.graph.update();
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.reload = function() {
	if (schemaEditor.schema.root==null) {
		this.loadElementHierarchy();
		return;
	}
	
	var rootX = schemaEditor.schema.root.rectangle.x;
	var rootY = schemaEditor.schema.root.rectangle.y;
	
	var expandedItemIds = this.getExpanded(this.schema.root);
	
	var selectedItemIds = [];
	for (var i=0; i<this.graph.selectedItems.length; i++) {
		selectedItemIds.push(this.graph.selectedItems[i].id);
		this.schema.deselectAll();
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
	    	
	    	_this.processElementHierarchy(data);	    	
	    	_this.schema.root.rectangle = new Rectangle(
	    			rootX, rootY,
	    			_this.schema.root.rectangle.width,
	    			_this.schema.root.rectangle.height
	    	);
	    	
	    	if (selectedItemIds.contains(schemaEditor.schema.root.id)) {
	    		_this.graph.select(schemaEditor.schema.root);
	    	}	    		
	    	_this.selectChildren(schemaEditor.schema.root.children, selectedItemIds);
	    	
	    	if (expandedItemIds.contains(schemaEditor.schema.root.id)) {
	    		schemaEditor.schema.root.setExpanded(true);
	    	}	    		
	    	_this.expandChildren(schemaEditor.schema.root.children, expandedItemIds);
	    		    	
	    	_this.schema.invalidate();
	    	_this.graph.update();
	    },
	    error: function(jqXHR, textStatus, errorThrown) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	_this.initGraph();
	    }
	});	
};

SchemaEditor.prototype.getExpanded = function(parent) {
	var expandedIds = [];
	if (parent.getExpanded()) {
		expandedIds.push(parent.id);
		if (parent.children!=null) {
			for (var i=0; i<parent.children.length; i++) {
				var expandedSubIds = this.getExpanded(parent.children[i]); 
				if (expandedSubIds.length > 0) {
					for (var j=0; j<expandedSubIds.length; j++) {
						expandedIds.push(expandedSubIds[j]);
					}
				}
			}
		}
	}
	return expandedIds;
};

SchemaEditor.prototype.selectChildren = function(children, ids) {
	var _this = schemaEditor;
	if (children!=null) {
		for (var i=0; i<children.length; i++) {
			if (ids.contains(children[i].id)) {
	    		_this.graph.select(children[i]);
	    	}	    		
	    	_this.selectChildren(children[i].children, ids);
		}
	}
};

SchemaEditor.prototype.expandChildren = function(children, ids) {
	var _this = schemaEditor;
	if (children!=null) {
		for (var i=0; i<children.length; i++) {
			if (ids.contains(children[i].id)) {
	    		children[i].setExpanded(true);
	    	}	    		
	    	_this.expandChildren(children[i].children, ids);
		}
	}
};


/* v2 */
SchemaEditor.prototype.processElementHierarchy = function(data) {
	

	//var parent = this.schema.addRoot(editorRootTemplate, { x: 50, y: 25 }, data.id, this.formatLabel(data.name), "element", data.simpleType, null);
	
	var parent = this.schema.addElement("Nonterminal", null, data.id, this.formatLabel(data.name), null);
	
	this.generateTree(this.schema, parent, data.childNonterminals, null, data.functions, true);
	
	this.schema.elements[0].setExpanded(true);
	
	this.graph.update();
};

/* v1
SchemaEditor.prototype.processElementHierarchy = function(data) {
	var parent = this.schema.addRoot(editorRootTemplate, { x: 50, y: 25 }, data.id, this.formatLabel(data.name), "element", data.simpleType, null);
	this.generateTree(this.schema, parent, data.childNonterminals, null, data.functions, true);
	
	this.schema.root.setExpanded(true);
}; */

SchemaEditor.prototype.generateTree = function(area, parent, nonterminals, subelements, functions, isSource) {

	if (nonterminals!=null && nonterminals instanceof Array) {
		for (var i=0; i<nonterminals.length; i++) {
			var icon = null;
			if (nonterminals[i].terminalId==null || nonterminals[i].terminalId=="") {
				icon = this.warningIcon;
			}
			var e = this.schema.addElement("Nonterminal", parent, nonterminals[i].id, this.formatLabel(nonterminals[i].name), icon);
			
			this.generateTree(area, e, nonterminals[i].childNonterminals, null, nonterminals[i].functions, isSource);
		}
	}
	if (functions != null && functions instanceof Array) {
		for (var i=0; i<functions.length; i++) {
			var icon = null;
			if (functions[i].error==true) {
				icon = this.errorIcon;
			}
			var fDesc = this.schema.addElement("Grammar", parent, functions[i].id, this.formatLabel("g: " + functions[i].grammarName), icon);
			
			if (functions[i].transformationFunctions != null && functions[i].transformationFunctions instanceof Array) {
				for (var j=0; j<functions[i].transformationFunctions.length; j++) {
					if (functions[i].transformationFunctions[j].error==true) {
						icon = this.errorIcon;
					}
					var fOut = this.schema.addElement("Function", fDesc, functions[i].transformationFunctions[j].id, 
							this.formatLabel("f: " + functions[i].transformationFunctions[j].name), icon);
					
					if (functions[i].transformationFunctions[j].outputElements != null && functions[i].transformationFunctions[j].outputElements instanceof Array) {
						for (var k=0; k<functions[i].transformationFunctions[j].outputElements.length; k++) {
							
							var e = this.schema.addElement("Label", fOut, functions[i].transformationFunctions[j].outputElements[k].id, 
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
			var e = this.schema.addElement("nonterminal", parent, subelements[i].id, this.formatLabel(subelements[i].name), null);
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

SchemaEditor.prototype.deselectionHandler = function() {
	var _this = schemaEditor;
	
	_this.selectedElementId = -1;
	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	_this.elementContextContainer.addClass("hide");
	_this.schemaContextContainer.removeClass("hide");
	_this.activities_loadForSchema();
};

SchemaEditor.prototype.selectionHandler = function(e) {
	var _this = schemaEditor;
	_this.selectedElementId = e.element.id;
	
	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	var actions = [];
	if (_this.schemaOwn || _this.schemaWrite) {
		if (e.element.getType() === "Nonterminal" || e.element.getType() === "Label") {
			if (e.element.getType()==="nonterminal") {
				actions[0] = ["addNonterminal", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_nonterminal")];
			} else {
				actions[0] = ["addLabel", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_nonterminal")];
			}
			actions[1] = ["addDescription", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_desc_function")];
			actions[2] = ["editElement", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
			actions[3] = ["removeElement", "trash", "danger", ""];
			_this.getElement(e.element.id);	
		} else if (e.element.getType() === "Grammar") {
			actions[0] = ["addTransformation", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_trans_function")];
			actions[1] = ["editGrammar", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
			actions[2] = ["removeElement", "trash", "danger", ""];
			_this.getGrammar(e.element.id);
		} else if (e.element.getType() === "Function") {
			actions[0] = ["addLabel", "plus", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_label")];
			actions[1] = ["editFunction", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
			actions[2] = ["removeElement", "trash", "danger", ""];
			_this.getFunction(e.element.id);
		}
	}
	
	var button;
	for (var i=0; i<actions.length; i++) {
		button = "<button " +
					"class='btn btn-" + actions[i][2] + " btn-sm' " +
					"onclick='schemaEditor." + actions[i][0] + "(); return false;' type='button'>" +
						"<span class='glyphicon glyphicon-" + actions[i][1] + "'></span> " + actions[i][3] + 
				 "</button> ";
		_this.elementContextButtons.append(button);
	}
	_this.activities_loadForElement(_this.selectedElementId);
	
	_this.elementContextContainer.removeClass("hide");
	_this.schemaContextContainer.addClass("hide");
};

SchemaEditor.prototype.getGrammar = function(id) {
	var _this = this;
	$.ajax({
		url: this.pathname + "/grammar/" + id + "/async/get",
        type: "GET",
        dataType: "json",
        success: function(data) { 
        	/*
        	 * locked, baseMethod, passthrough, error
        	 */
        	var details = $("<div class=\"clearfix\">");
        	
        	details.append(_this.renderContextTabDetail("State", 
        			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
        			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
        			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "") +
        			(data.passthrough==true ? "<span class='glyphicon glyphicon-forward' aria-hidden='true'></span>&nbsp;" : "")
        	));
        	
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.label"), data.grammarName));
        	details.append(_this.renderContextTabDetail("~Base rule", data.baseMethod));
        	
        	if (data.passthrough!=true && data.grammarContainer!=null) {
        		if (data.grammarContainer.lexerGrammar!==null && data.grammarContainer.lexerGrammar !=="") {
        			details.append(_this.renderContextTabDetail("~Grammar layout", "separate lexer/parser grammars"));
        		} else {
        			details.append(_this.renderContextTabDetail("~Grammar layout", "combined grammar"));
        		}
        	}
        	
        	_this.elementContextDetail.append(details);  	
        },
        error: __util.processServerError
 	});
};

SchemaEditor.prototype.getFunction = function(id) {
	var _this = this;
	$.ajax({
		url: this.pathname + "/function/" + id + "/async/get",
        type: "GET",
        dataType: "json",
        success: function(data) { 
        	var details = $("<div class=\"clearfix\">");
        	
        	details.append(_this.renderContextTabDetail("State", 
        			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
        			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
        			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "")
        	));
        	
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.label"), data.name));
        	
        	_this.elementContextDetail.append(details);  	
        },
        error: __util.processServerError
 	});
};

SchemaEditor.prototype.getElement = function(id) {
	var _this = this;
	$.ajax({
		url: this.pathname + "/element/" + id + "/async/get",
        type: "GET",
        dataType: "json",
        success: function(data) { 
        	var details = $("<div class=\"clearfix\">");
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.name"), data.name));
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.transient"), data.transient));
        		
        	_this.elementContextDetail.append(details);  	
        	
        	if (data.terminalId!=null && data.terminalId!="") {
	        	$.ajax({
	        		url: _this.pathname + "/element/" + id + "/async/getTerminal",
	                type: "GET",
	                dataType: "json",
	                success: function(data) {
	                	var details = $("<div class=\"clearfix tab-details-block\">");
	                	details.append(_this.renderContextTabDetail("", "<h4>" + data.simpleType + "</h4>"));
	                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.name"), data.name));
	                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.transient"), data.namespace));
	                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.attribute"), data.attribute));
	                		
	                	_this.elementContextDetail.append(details);
	                },
	                error: function(textStatus) {}
	         	});    
        	} else if (data.simpleType==="Nonterminal") {
            	var details = $("<div class=\"clearfix tab-details-block\">");
            	details.append("<div class='alert alert-sm alert-warning' role='alert'>" + 
            			"<span aria-hidden='true' class='glyphicon glyphicon-info-sign'></span> " +
            			__translator.translate("~eu.dariah.de.minfba.schereg.notification.no_terminal_configured") +
            			"</div>");
            	_this.elementContextDetail.append(details);
        	}
        },
        error: __util.processServerError
 	});
};

SchemaEditor.prototype.renderContextTabDetail = function(label, data, pre) {
	var detail = $("<div class=\"row\">");
	
	if (pre) {
		if (label!=null && label!="") {
			detail.append("<div class=\"schema-metadata-label\">" + label + ":</div>");
		} else {
			detail.append("<div>&nbsp;</div>");
		}
		
		var dataE = "<div class=\"schema-metadata-data\"><pre>";
		if (data===true) {
			dataE += "<span class=\"glyphicon glyphicon-check\" aria-hidden=\"true\"></span>";
		} else if (data===false) {
			dataE += "<span class=\"glyphicon glyphicon-unchecked\" aria-hidden=\"true\"></span>";
		} else {
			dataE += data;
		}
		
		detail.append(dataE + "</pre></div>");
		
		
	} else {
		if (label!=null && label!="") {
			detail.append("<div class=\"schema-metadata-label col-xs-3 col-md-4\">" + label + ":</div>");
		} else {
			detail.append("<div class=\"col-xs-3 col-md-4\">&nbsp;</div>");
		}
		
		var dataE = "<div class=\"schema-metadata-data col-xs-9 col-md-8\">";
		if (data===true) {
			dataE += "<span class=\"glyphicon glyphicon-check\" aria-hidden=\"true\"></span>";
		} else if (data===false) {
			dataE += "<span class=\"glyphicon glyphicon-unchecked\" aria-hidden=\"true\"></span>";
		} else {
			dataE += data;
		}
		
		detail.append(dataE + "</div>");
	}
	
	
	
	
	return detail;
};

SchemaEditor.prototype.editElement = function() {
	var _this = this;
	var form_identifier = "edit-element-" + this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/element/" + this.graph.selectedItems[0].id + "/form/element",
		identifier: form_identifier,
		//additionalModalClasses: "wider-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		//additionalModalClasses: "wide-modal",               
		completeCallback: function() {_this.reload();}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.editGrammar = function() {
	var _this = this;
	var form_identifier = "edit-grammar-" + this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/grammar/" + this.graph.selectedItems[0].id + "/form/edit",
		identifier: form_identifier,
		additionalModalClasses: "max-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		setupCallback: function(modal) { grammarEditor = new GrammarEditor(modal); },       
		completeCallback: function() { _this.reload(); }
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
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
        setupCallback: function(modal) { functionEditor = new FunctionEditor(modal); },       
		completeCallback: function() { _this.reload(); }
	});
		
	modalFormHandler.show(form_identifier);
};



SchemaEditor.prototype.addDescription = function() {
	this.addNode("element", "grammar");
};

SchemaEditor.prototype.addTransformation = function() {
	this.addNode("grammar", "function");
};

SchemaEditor.prototype.addNonterminal = function() {
	this.addNode("element", "nonterminal");
};

SchemaEditor.prototype.addLabel = function() {
	this.addNode("element", "label");
};

SchemaEditor.prototype.addNode = function(type, childType) {
	var _this = this;
	var form_identifier = "edit-element-" + this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/" + type + "/" + this.graph.selectedItems[0].id + "/form/new_" + childType,
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],               
		completeCallback: function() {
			_this.graph.selectedItems[0].setExpanded(true);
	    	_this.reload();
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
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],               
		completeCallback: function() {
	    	_this.reload();
		}
	});
		
	modalFormHandler.show(form_identifier)
};

SchemaEditor.prototype.removeElement = function(e) { 
	var _this = this;
	
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), e.id), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/" + e.type + "/" + e.id + "/async/remove",
			    type: "GET",
			    dataType: "json",
			    success: function(data) {
			    	//_this.graph.selectedItems[0].deselect();
			    	_this.reload();
			    },
			    error: __util.processServerError
			});
		}
	});
};

SchemaEditor.prototype.addTerminal = function() {
	this.innerEditTerminal("-1");
};

SchemaEditor.prototype.editTerminal = function() {
	var terminalId = $("#terminalId").val();
	this.innerEditTerminal(terminalId);
};

SchemaEditor.prototype.innerEditTerminal = function(id) {
	var _this = this;
	var form_identifier = "edit-terminal" + id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/terminal/" + id + "/form/edit",
		identifier: form_identifier,
		//additionalModalClasses: "wider-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		//additionalModalClasses: "wide-modal",               
		completeCallback: function() { _this.updateTerminalList(); }
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.removeTerminal = function() {
	var terminalId = $("#terminalId").val();
	
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), terminalId), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/terminal/" + terminalId + "/async/remove",
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

SchemaEditor.prototype.updateTerminalList = function() {
	var _this = this;
	$(".form-btn-submit").prop("disabled", "disabled");
		
	$.ajax({
	    url: _this.pathname + "/async/getTerminals",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	$("#terminalId option.schema-terminal").remove();
	    	for (var i=0; i<data.length; i++) {
	    		$("#terminalId").append("<option class='schema-terminal' value='" + data[i].id + "'>" + 
	    				data[i].name + " (" + data[i].namespace + ")" + 
	    			"</option>");
	    	}
	    	
	    	$(".form-btn-submit").removeProp("disabled");
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.exportSchema = function() {
	var _this = this;

	$.ajax({
	    url: _this.pathname + "/async/export",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	blob = new Blob([JSON.stringify(data.pojo)], {type: "application/json; charset=utf-8"});
	    	saveAs(blob, "schema_" + _this.schemaId + ".json");
	    },
	    error: __util.processServerError
	});
};

SchemaEditor.prototype.triggerUploadFile = function(schemaId) {
	var _this = this;
	var form_identifier = "upload-file-" + schemaId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/forms/import/",
		identifier: form_identifier,
		//additionalModalClasses: "wider-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.reload();}
	});
	
	modalFormHandler.fileUploadElements.push({
		selector: "#schema_source",				// selector for identifying where to put widget
		formSource: "/forms/fileupload",		// where is the form
		uploadTarget: "/async/upload", 			// where to we upload the file(s) to
		multiFiles: false, 						// one or multiple files
		elementChangeCallback: _this.handleFileValidatedOrFailed
	});
		
	modalFormHandler.show(form_identifier);
};


SchemaEditor.prototype.handleFileValidatedOrFailed = function(data) {
	var select = $("#schema_root");
	select.html("");
	if (data==null || data.pojo==null || data.pojo.length==0) {
		select.prop("disabled", "disabled");
		$("#btn-submit-schema-elements").prop("disabled", "disabled");
		return;
	}
	
	var option;
	for (var i=0; i<data.pojo.length; i++) {
		option = "<option value='" + i + "'>" + data.pojo[i].name + " <small>(" + data.pojo[i].namespace + ")</small>" + "</option>";
		select.append(option);
	}
	select.removeProp("disabled");
	$("#btn-submit-schema-elements").removeProp("disabled");
};