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
	this.schemaId = $("#schema-id").val();
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId;
	this.context = document.getElementById("schema-editor-canvas").getContext("2d");
	this.graph = new Graph(this.context.canvas);
	this.footerOffset = 70;	
	
	this.editorContainer = $("#schema-editor-container");
	this.contextContainer = $(".schema-editor-context");
	this.contextContainerInfo = $("#schema-editor-context-info");
	
	this.menuContainer = $("#schema-editor-dynamic-buttons");
	this.elementTab = $("#tab-element-metadata");
	this.activityTab = $("#tab-element-activity");
	
	this.schema = new Area(this.graph, new Rectangle(0, 0, 0, 0), true);
 	this.graph.addArea(this.schema);
	
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	document.addEventListener("newMappingCellEvent", this.newMappingCellHandler, false);
	
	__translator.addTranslations(["~eu.dariah.de.minfba.common.model.id",
	                              "~eu.dariah.de.minfba.common.model.type",
	                              "~eu.dariah.de.minfba.common.link.edit",
	                              "~eu.dariah.de.minfba.common.link.delete",
	                              "~eu.dariah.de.minfba.schereg.button.add_nonterminal",
	                              "~eu.dariah.de.minfba.schereg.button.add_label", 
	                              "~eu.dariah.de.minfba.schereg.button.add_desc_function",
	                              "~eu.dariah.de.minfba.schereg.button.add_trans_function",
	                              "~eu.dariah.de.minfba.common.view.forms.servererror.head",
	                              "~eu.dariah.de.minfba.common.view.forms.servererror.body",
	                              "~eu.dariah.de.minfba.schereg.model.element.name",
	                              "~eu.dariah.de.minfba.schereg.model.element.namespace",
	                              "~eu.dariah.de.minfba.schereg.model.element.attribute",
	                              "~eu.dariah.de.minfba.schereg.model.element.transient"]);
	__translator.getTranslations();
}

SchemaEditor.prototype.resize = function() {
	var height = Math.floor($(window).height() - this.editorContainer.offset().top - this.footerOffset);
	this.editorContainer.css("height", height + "px");
	this.contextContainer.find(".tab-content").css("height", height + "px");
	
	this.contextContainer.css("margin-top", Math.floor(this.editorContainer.offset().top - this.contextContainer.parent().offset().top - 42) + "px");
	
	if (this.context.canvas) {
		this.context.canvas.width = this.editorContainer.width() - 1; // border-bottom
		this.context.canvas.height = height - 2; // border-left and -right
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
	_this.contextContainerInfo.html("");
	_this.elementTab.addClass("hide");
	_this.activityTab.find("a").tab('show');
};

SchemaEditor.prototype.selectionHandler = function(e) {
	var _this = schemaEditor;
	
	_this.menuContainer.html("");
	_this.contextContainerInfo.html("");
	_this.elementTab.removeClass("hide");
	_this.elementTab.find("a").tab('show');
	
	// TODO Show details in context response
	var actions = [];
	if (e.elementSubtype === "Nonterminal") {
		actions[0] = ["addElement", "cog", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_nonterminal")];
		actions[1] = ["addDescription", "cog", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_desc_function")];
		actions[2] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.link.delete")];
		actions[3] = ["editElement", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
		_this.getElement(e.elementId);	
	} else if (e.elementSubtype === "DescriptiveFunction") {
		actions[0] = ["addTransformation", "cog", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_trans_function")];
		actions[1] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.link.delete")];
	} else if (e.elementSubtype === "OutputFunction") {
		actions[0] = ["addElement", "cog", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_label")];
		actions[1] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.link.delete")];	
		_this.getOutputFunctionInfo(e.elementId);
	} else if (e.elementSubtype === "Label") {
		actions[0] = ["editElement", "edit", "default", __translator.translate("~eu.dariah.de.minfba.common.link.edit")];
		actions[1] = ["addElement", "cog", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_label")];
		actions[2] = ["addDescription", "cog", "default", __translator.translate("~eu.dariah.de.minfba.schereg.button.add_desc_function")];
		actions[3] = ["removeElement", "trash", "danger", __translator.translate("~eu.dariah.de.minfba.common.link.delete")];
	}
	
	var button;
	for (var i=0; i<actions.length; i++) {
		button = "<button " +
					"class='btn btn-" + actions[i][2] + " btn-sm pull-right' " +
					"onclick='schemaEditor." + actions[i][0] + "(); return false;' type='button'>" +
						"<span class='glyphicon glyphicon-" + actions[i][1] + "'></span> " + actions[i][3] + 
				 "</button> ";
		_this.menuContainer.append(button);
	}
};

SchemaEditor.prototype.getElement = function(id) {
	var _this = this;
	$.ajax({
		url: this.pathname + "/element/" + id + "/async/get",
        type: "GET",
        dataType: "json",
        success: function(data) { 
        	var details = $("<div class=\"clearfix\">");
        	details.append(_this.renderContextTabDetail("", "<h4>~Element details</h4>"));
        	//details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.name"), data.name));
        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.transient"), data.transient));
        		
        	_this.contextContainerInfo.append(details);  	
        	
        	$.ajax({
        		url: _this.pathname + "/element/" + id + "/async/getTerminal",
                type: "GET",
                dataType: "json",
                success: function(data) {
                	var details = $("<div class=\"clearfix\">");
                	details.append(_this.renderContextTabDetail("", "<h4>~Source parser details</h4>"));
                	//details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.type"), data.simpleType));
                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.name"), data.name));
                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.transient"), data.namespace));
                	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.attribute"), data.attribute));
                		
                	_this.contextContainerInfo.append(details);  	
                },
                error: function(textStatus) { /*alert("error");*/ }
         	});    
        },
        error: function(textStatus) { /*alert("error");*/ }
 	});
};

SchemaEditor.prototype.renderContextTabDetail = function(label, data) {
	var detail = $("<div class=\"row\">");
	
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
	
	return detail;
};

SchemaEditor.prototype.editElement = function() {
	var _this = this;
	var form_identifier = "edit-element-" + this.graph.selectedItems[0].id;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/element/" + this.graph.selectedItems[0].id + "/form/nonterminal",
		identifier: form_identifier,
		//additionalModalClasses: "wider-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.addElement = function() {
	var _this = this;
	$.ajax({
	    url: this.pathname + "/element/" + this.graph.selectedItems[0].id + "/async/createSubelement",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	var e = _this.schema.addElement(editorTemplate, data.id, data.name, _this.graph.selectedItems[0], data.simpleType);
	    	
	    	// TODO Expand if not expanded
	    	if (_this.graph.selectedItems[0].isExpanded) {
	    		e.isVisible = true;
	    	}
	    	_this.graph.selectedItems[0].addChild(e);
	    	_this.graph.update();
	    }
	});
};

SchemaEditor.prototype.removeElement = function() { 
	var _this = this;
	$.ajax({
	    url: this.pathname + "/element/" + this.graph.selectedItems[0].id + "/async/remove",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	_this.schema.removeElement(_this.graph.selectedItems[0]);
	    	_this.graph.update();
	    }
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
		completeCallback: function() {_this.refresh();}
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