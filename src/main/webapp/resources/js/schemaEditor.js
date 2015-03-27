var schemaEditor;
$(document).ready(function() {
	schemaEditor = new SchemaEditor();
	schemaEditor.loadElementHierarchy();
	schemaEditor.resize();
});
$(window).resize(function() {
	schemaEditor.resize();
});

var SchemaEditor = function() {
	this.menuContainer = $("#schema-editor-dynamic-buttons");
	this.schemaId = $("#schema-id").val();
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId;
	this.context = document.getElementById("schema-editor-canvas").getContext("2d");
	this.graph = new Graph(this.context.canvas);
	this.editorContainer = $("#schema-editor-container");
	this.contextContainer = $("#schema-editor-context");
	this.footerOffset = 70;	
	
	this.editorForm = $("#schema-editor-context-form");
	this.editorResponse = $("#schema-editor-context-response");
	
	this.schema = new Area(this.graph, new Rectangle(0, 0, 0, 0), true);
 	this.graph.addArea(this.schema);
	
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	document.addEventListener("newMappingCellEvent", this.newMappingCellHandler, false);
	
	__translator.addTranslations(["~eu.dariah.de.minfba.common.view.common.delete",
	                              "~eu.dariah.de.minfba.schereg.schemas.button.add_nonterminal",
	                              "~eu.dariah.de.minfba.schereg.schemas.button.add_label", 
	                              "~eu.dariah.de.minfba.schereg.schemas.button.add_desc_function",
	                              "~eu.dariah.de.minfba.schereg.schemas.button.add_trans_function"]);
	__translator.getTranslations();
}

SchemaEditor.prototype.resize = function() {
	var height = Math.floor($(window).height() - this.editorContainer.offset().top - this.footerOffset);
	this.editorContainer.css("height", height + "px");
	this.contextContainer.css("height", height + "px");
	
	if (this.context.canvas) {
		this.context.canvas.width = this.editorContainer.width();
		this.context.canvas.height = height;
		window.scroll(0, 0);			
		if (this.graph !== null) {
			if (this.schema != null) {
				this.schema.setSize(new Rectangle(0, 0, this.context.canvas.width, this.context.canvas.height));
			}
			this.graph.update();
		}
	}
};

SchemaEditor.prototype.loadElementHierarchy = function() {
	var _this = this;
	$.ajax({
	    url: this.pathname + "/async/getHierarchy",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	_this.processElementHierarchy(data);
	    }
	});
};

SchemaEditor.prototype.processElementHierarchy = function(data) {
	var parent = this.schema.addRoot(editorRootTemplate, { x: 50, y: 25 }, data.id, data.name, "Nonterminal");
	this.generateTree(this.schema, parent, data.childNonterminals, null, data.functions, true);
	
	this.schema.root.setExpanded(true);
	this.graph.update();	
};

SchemaEditor.prototype.generateTree = function(area, parent, nonterminals, subelements, functions,  isSource) {
	
	if (nonterminals!=null && nonterminals instanceof Array) {
		for (var i=0; i<nonterminals.length; i++) {
			var e = this.schema.addElement(editorTemplate, nonterminals[i].id, nonterminals[i].name, parent, "Nonterminal");
			if (parent != null) {
				parent.addChild(e);
			}
			this.generateTree(area, e, nonterminals[i].childNonterminals, null, nonterminals[i].functions, isSource);
		}
	}
	if (functions != null && functions instanceof Array) {
		for (var i=0; i<functions.length; i++) {
			var fDesc = this.schema.addElement(functionTemplate, functions[i].id, "fDesc", parent, "fDesc");
			parent.addChild(fDesc);
			if (functions[i].dataTransformationFunctions != null && functions[i].dataTransformationFunctions instanceof Array) {
				for (var j=0; j<functions[i].dataTransformationFunctions.length; j++) {
					var fOut = this.schema.addElement(functionTemplate, functions[i].dataTransformationFunctions[j].id, "fOut", fDesc, "fOut");
					fDesc.addChild(fOut);
					if (functions[i].dataTransformationFunctions[j].outputElements != null && functions[i].dataTransformationFunctions[j].outputElements instanceof Array) {
						for (var k=0; k<functions[i].dataTransformationFunctions[j].outputElements.length; k++) {
							var e = this.schema.addElement(editorRootTemplate, 
									functions[i].dataTransformationFunctions[j].outputElements[k].id, 
									functions[i].dataTransformationFunctions[j].outputElements[k].name, fOut, "Label");
							fOut.addChild(e);
							this.generateTree(area, e, 
									functions[i].dataTransformationFunctions[j].outputElements[k].childNonterminals,
									functions[i].dataTransformationFunctions[j].outputElements[k].subLabels,
									functions[i].dataTransformationFunctions[j].outputElements[k].functions, isSource);
						}
					}
				}
			}
		}
	}
	if (subelements!=null && subelements instanceof Array) {
		for (var i=0; i<subelements.length; i++) {
			var e = this.schema.addElement(editorRootTemplate, subelements[i].id, subelements[i].name, parent, "Label");
			if (parent != null) {
				parent.addChild(e);
			}
			this.generateTree(area, e, null, subelements[i].subLabels, subelements[i].functions, isSource);
		}
	}
};

SchemaEditor.prototype.deselectionHandler = function() {
	var _this = schemaEditor;
	
	_this.menuContainer.html("");
	_this.editorForm.html("");
	_this.editorResponse.html("");
};

SchemaEditor.prototype.selectionHandler = function(e) {
	var _this = schemaEditor;
	
	_this.menuContainer.html("");
	_this.editorForm.html("");
	_this.editorResponse.html("");
	
	// TODO Show details in context response
	var actions = [];
	if (e.elementSubtype === "Nonterminal") {
		actions[0] = ["addNonterminal", "plus", "primary", __translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.add_nonterminal")];
		actions[1] = ["addDescription", "plus", "primary", __translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.add_desc_function")];
		actions[2] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.view.common.delete")];	
	} else if (e.elementSubtype === "DescriptiveFunction") {
		actions[0] = ["addTransformation", "plus", "primary", __translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.add_trans_function")];
		actions[1] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.view.common.delete")];
	} else if (e.elementSubtype === "OutputFunction") {
		actions[0] = ["addLabel", "plus", "primary", __translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.add_label")];
		actions[1] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.view.common.delete")];	
		_this.getOutputFunctionInfo(e.elementId);
	} else if (e.elementSubtype === "Label") {
		actions[0] = ["addLabel", "plus", "primary", __translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.add_label")];
		actions[1] = ["addDescription", "plus", "primary", __translator.translate("~eu.dariah.de.minfba.schereg.schemas.button.add_desc_function")];
		actions[2] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.view.common.delete")];
	}
	
	var button;
	for (var i=0; i<actions.length; i++) {
		button = "<button " +
					"class='btn btn-" + actions[i][2] + " btn-sm' " +
					"onclick='schemaEditor." + actions[i][0] + "(); return false;' type='button'>" +
						"<span class='glyphicon glyphicon-" + actions[i][1] + "'></span> " + actions[i][3] + 
				 "</button>";
		_this.menuContainer.append(button);
	}
};

SchemaEditor.prototype.addNonterminal = function() {
	$.ajax({
	    url: this.pathname + "/async/" + this.graph.selectedItems[0].id + "/createSubelement",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	this.addElement(this.schema.addElement(editorTemplate, -1, "new Nonterminal*", this.graph.selectedItems[0], "Nonterminal"));
	    }
	});
};

SchemaEditor.prototype.removeElement = function() {
	this.schema.removeElement(this.graph.selectedItems[0]);
	this.graph.update();
};

SchemaEditor.prototype.addElement = function(e) {
	if (this.graph.selectedItems[0].isExpanded) {
		e.isVisible = true;
	}
	this.graph.selectedItems[0].addChild(e);
	this.graph.update();
};