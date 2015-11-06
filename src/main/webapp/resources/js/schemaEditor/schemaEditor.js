var schemaEditor;
$(document).ready(function() {
	schemaEditor = new SchemaEditor({
		footerOffset: 70,
		icons: {
			warning: __util.getBaseUrl() + "resources/img/warning.png",
			error: __util.getBaseUrl() + "resources/img/error.png"
		}
	});
});
$(window).resize(function() {
	schemaEditor.resize();
});

var SchemaEditor = function(options) {	
	this.options = options;
	this.schema = { 
			id: $("#schema-id").val(),
			owned: $("#schema-own").val()==="true",
			write: $("#schema-write").val()==="true"
	}
	
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schema.id;
	this.context = document.getElementById("schema-editor-canvas").getContext("2d");

	this.layout = null;
	this.layoutContainer = $(".editor-layout-container");
	this.editorContainer = $(".editor-container");
		
	this.schemaContextContainer = $("#schema-context-container");
	this.elementContextContainer = $("#schema-element-context-container");
	
	this.elementContextDetail = $("#schema-element-context-info");
	this.elementContextButtons = $("#schema-element-context-buttons");
	
	this.logArea = null;
	
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	
	
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
	
	this.init();
}

SchemaEditor.prototype.init = function() {
	this.initLayout();
	this.initGraph();
	
	this.loadElementHierarchy();
	this.sample_init();
	//this.activities_init();
	//this.activities_loadForSchema();
	
	var _this = this;
	this.logArea = new LogArea({
		pathPrefix :  __util.getBaseUrl() + "sessions/",
		entityId : _this.schema.id
	});
	
	this.resize();
};

SchemaEditor.prototype.initLayout = function() {
	var _this = this;
	this.layoutContainer.removeClass("hide").addClass("fade");
	
	var initEastClosed = true;
	if (this.layoutContainer.width()>1100) {
		initEastClosed = false;
	}
	
	var initSouthClosed = true;
	if ($(window).height()>700) {
		initSouthClosed = false;
	}
	this.layout = this.layoutContainer.layout({
		defaults : {
			fxName : "slide",
			fxSpeed : "slow",
			spacing_closed : 14,
			minWidth : 200,
		},
		center : {
			size : initEastClosed ? "60%" : "40%",
			paneSelector : ".layout-center",
			minHeight : 200
		},
		east : {
			size : initEastClosed ? "0%" : "30%",
			paneSelector : ".layout-east",
			initClosed : initEastClosed,
		},
		west : {
			size : initEastClosed ? "40%" : "30%",
			paneSelector : ".layout-west",
			initClosed : initEastClosed,
			onopen_start: function () { _this.sample_onPaneOpenStart(); }
		},
		south : { 
			size : 100, 
			initClosed : initSouthClosed,
			paneSelector : ".layout-south" 
		},
		onresize: function () {
			_this.resizeContent();
	        return false;
	    }
	});
	this.layoutContainer.removeClass("fade");
};

SchemaEditor.prototype.resize = function() {
	this.resizeLayout();
	this.resizeContent();
};

SchemaEditor.prototype.resizeLayout = function() {
	var height = Math.floor($(window).height() - this.layoutContainer.offset().top - this.options.footerOffset);
	
	this.layoutContainer.height(height);
	this.layout.resizeAll();
}

SchemaEditor.prototype.resizeContent = function() {
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


SchemaEditor.prototype.performTreeAction = function(action, elementId, elementType) {	
	switch(action) {
		case "addNonterminal":
			this.addNode(elementType, elementId, "nonterminal");
	        break;
	    case "addDescription":
	    	this.addNode(elementType, elementId, "grammar");
	        break;
	    case "addTransformation":
	    	this.addNode(elementType, elementId, "transformation");
	        break;
	    case "addLabel":
	    	this.addNode(elementType, elementId, "label");
	        break;
	    case "editElement" :
	    	this.editElement(elementId);
	    	break;
	    case "editGrammar" :
	    	this.editGrammar(elementId);
	    	break;
	    case "editFunction" :
	    	this.editFunction(elementId);
	    	break;
	    case "removeElement" :
	    	this.removeElement(elementType, elementId);
	    	break;
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

SchemaEditor.prototype.reload = function() {
	if (schemaEditor.schema.root==null) {
		this.loadElementHierarchy();
		return;
	}
	
	var rootX = schemaEditor.schema.root.rectangle.x;
	var rootY = schemaEditor.schema.root.rectangle.y;
	
	var expandedItemIds = this.getExpanded(this.area.root);
	
	var selectedItemIds = [];
	for (var i=0; i<this.graph.selectedItems.length; i++) {
		selectedItemIds.push(this.graph.selectedItems[i].id);
		this.area.deselectAll();
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
	    	_this.area.root.rectangle = new Rectangle(
	    			rootX, rootY,
	    			_this.area.root.rectangle.width,
	    			_this.area.root.rectangle.height
	    	);
	    	
	    	if (selectedItemIds.contains(schemaEditor.schema.root.id)) {
	    		_this.graph.select(schemaEditor.schema.root);
	    	}	    		
	    	_this.selectChildren(schemaEditor.schema.root.children, selectedItemIds);
	    	
	    	if (expandedItemIds.contains(schemaEditor.schema.root.id)) {
	    		schemaEditor.schema.root.setExpanded(true);
	    	}	    		
	    	_this.expandChildren(schemaEditor.schema.root.children, expandedItemIds);
	    		    	
	    	_this.area.invalidate();
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


SchemaEditor.prototype.processElementHierarchy = function(data) {
	var parent = this.area.addElement(data.simpleType, null, data.id, this.formatLabel(data.name), null);
	this.generateTree(this.area, parent, data.childNonterminals, null, data.functions, true);
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

SchemaEditor.prototype.deselectionHandler = function() {
	var _this = schemaEditor;
	
	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	_this.elementContextContainer.addClass("hide");
	_this.schemaContextContainer.removeClass("hide");
	_this.activities_loadForSchema();
};

SchemaEditor.prototype.selectionHandler = function(e) {
	var _this = schemaEditor;

	_this.elementContextDetail.text("");
	_this.elementContextButtons.text("");
	
	if (_this.schema.owned || _this.schema.write) {
		var items = e.element.getContextMenuItems();
		for (var i=0; i<items.length; i++) {
			if (items[i].key==="-") {
				continue;
			}
			button = "<button " +
						"class='btn btn-default btn-sm' " +
						"onclick='schemaEditor.performTreeAction(\"" + items[i].key + "\", \"" + items[i].id + "\", \"" + items[i].type + "\"); return false;' type='button'>" +
							"<span class='glyphicon glyphicon-" + items[i].glyphicon + "'></span> " + items[i].label + 
					 "</button> ";
			_this.elementContextButtons.append(button);
		}
	}
	
	_this.getElementDetails(e.element.getType(), e.element.id);
	
	/*var actions = [];
	if (_this.schema.owned || _this.schema.write) {
		if (e.element.getType() === "Nonterminal" || e.element.getType() === "Label") {
			if (e.element.getType()==="Nonterminal") {
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
	}*/
	_this.activities_loadForElement(e.element.id);
	
	_this.elementContextContainer.removeClass("hide");
	_this.schemaContextContainer.addClass("hide");
};

SchemaEditor.prototype.getElementDetails = function(type, id) {
	if (type==="Nonterminal" || type==="Label") {
		type="element";
	} else {
		type=type.toLowerCase();
	}
	var _this = this;
	$.ajax({
		url: this.pathname + "/" + type + "/" + id + "/async/get",
        type: "GET",
        dataType: "json",
        success: function(data) {
        	switch (type) {
				case "element": return _this.processElementDetails(data);
				case "grammar": return _this.processGrammarDetails(data);
				case "function": return _this.processFunctionDetails(data);
				default: throw Error("Unknown element type: " + type);
			}
        },
        error: __util.processServerError
 	});
};

SchemaEditor.prototype.processGrammarDetails = function(data) { 
	var details = $("<div class=\"clearfix\">");
	
	details.append(this.renderContextTabDetail("State", 
			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "") +
			(data.passthrough==true ? "<span class='glyphicon glyphicon-forward' aria-hidden='true'></span>&nbsp;" : "")
	));
	
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.label"), data.grammarName));
	details.append(this.renderContextTabDetail("~Base rule", data.baseMethod));
	
	if (data.passthrough!=true && data.grammarContainer!=null) {
		if (data.grammarContainer.lexerGrammar!==null && data.grammarContainer.lexerGrammar !=="") {
			details.append(this.renderContextTabDetail("~Grammar layout", "separate lexer/parser grammars"));
		} else {
			details.append(this.renderContextTabDetail("~Grammar layout", "combined grammar"));
		}
	}
	
	this.elementContextDetail.append(details);  	   
};

SchemaEditor.prototype.processFunctionDetails = function(data) { 
	var details = $("<div class=\"clearfix\">");
	
	details.append(this.renderContextTabDetail("State", 
			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "")
	));
	
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.label"), data.name));
	
	this.elementContextDetail.append(details);
};

SchemaEditor.prototype.processElementDetails = function(data) { 
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.name"), data.name));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.transient"), data.transient));
		
	this.elementContextDetail.append(details);  	
	var _this = this;
	if (data.terminalId!=null && data.terminalId!="") {
		$.ajax({
			url: _this.pathname + "/element/" + data.id + "/async/getTerminal",
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
		this.elementContextDetail.append(details);
	}
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

SchemaEditor.prototype.editElement = function(id) {
	var _this = this;
	var form_identifier = "edit-element-" + id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/element/" + id + "/form/element",
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


SchemaEditor.prototype.addNode = function(parentType, parentId, childType) {
	var _this = this;
	var form_identifier = "edit-element-" + parentId;
	
	var parentSupertype = (parentType=="Nonterminal" || parentType=="Label") ? "element" : parentType.toLowerCase();
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/" + parentSupertype + "/" + parentId + "/form/new_" + childType.toLowerCase(),
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

SchemaEditor.prototype.removeElement = function(type, id) { 
	var _this = this;
	
	type = (type=="Nonterminal" || type=="Label") ? "element" : type;
	
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), id), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/" + type.toLowerCase() + "/" + id + "/async/remove",
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