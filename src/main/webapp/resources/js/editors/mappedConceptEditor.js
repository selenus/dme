var MappedConceptEditor = function(owner, container, modal, options) {
	this.options = {
			conceptId: "",
			path: "mappedConcept/{0}",
			layoutContainer: ".layout-helper-container",
			editorContainer: ".mapped-concept-editor-container",
			canvasId: "mapped-concept-editor",
			readOnly: false
	};
	$.extend(true, this.options, options);
	this.options.path = String.format(this.options.path, this.options.conceptId);
	
	this.context = document.getElementById(this.options.canvasId).getContext("2d");;
	this.layoutContainer = $(container).find(this.options.layoutContainer);
	this.editorContainer = $(container).find(this.options.editorContainer);
	this.container = container;
	
	this.sourceGrammars = [];
	this.targetElements = [];
	this.modal = modal;
	
	this.owningEditor = owner;
	
	__translator.addTranslations([
		"~eu.dariah.de.minfba.common.link.edit",
		"~eu.dariah.de.minfba.common.link.delete",
		"~eu.dariah.de.minfba.common.link.view"]);
	__translator.getTranslations();
	
	this.init();
};

MappedConceptEditor.prototype.init = function() {
	var _this = this;
	
	var commonElementOptions = {
			visible: true,
			collapsible: false,
			isMappable: false,
			isInteractive: false,
			hierarchyOutConnector: { positionfunction: function(element) {
				return { x: element.rectangle.width, y: Math.floor(element.rectangle.height / 2) };
			} }	
	}
	
	this.graph = new Model(this.context.canvas, {
		readOnly: true,
		eventId: "_mc",
		elementTemplateOptions: [
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "LogicalRoot", 
		                        	 visible: false,
		                        	 getContextMenuItems: undefined
		                         }),
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "Nonterminal", 
		                        	 primaryColor: "#e6f1ff", 
		                        	 secondaryColor: "#0049a6",
		                        	 getContextMenuItems: _this.getElementContextMenu
		                         }),
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "Label", 
		                        	 primaryColor: "#f3e6ff", 
		                        	 secondaryColor: "#5700a6",
		                        	 getContextMenuItems: _this.getElementContextMenu
		                         }),
		                         $.extend(true, {}, commonElementOptions, {
		                        	 key: "DescriptionGrammarImpl", 
		                        	 primaryColor: "#FFE173", 
		                        	 secondaryColor: "#6d5603",
		                        	 getContextMenuItems: _this.getGrammarContextMenu,
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
			highlightSelectedConnection : false,
			getContextMenuItems: _this.getFunctionContextMenu,
			functionTemplateOptions : {
				primaryColor: "#FFE173", 
				secondaryColor: "#6d5603", 
				radius: 3,
				text: "f:",
				font: "11px Georgia, serif",
				vPadding: 5,
				hPadding: 20,
			}
		}
	});
	this.source = this.graph.addArea({ getContextMenuItems: _this.getAreaContextMenu });
	this.target = this.graph.addArea({ getContextMenuItems: _this.getAreaContextMenu });
	
	this.contextMenuClickEventHandler = this.handleContextMenuClicked.bind(this);
	this.resizeHandler = this.resize.bind(this);
	
	this.graph.init();
	this.resize();
	
 	this.reloadAll();
};

MappedConceptEditor.prototype.dispose = function() {
	this.deregisterEvents();
};

MappedConceptEditor.prototype.registerEvents = function() {
	/** TODO: Possibly show sample input when selecting some source */ 
	//document.addEventListener("selectionEvent_mc", this.selectionHandler, false);
	//document.addEventListener("deselectionEvent_mc", this.deselectionHandler, false);
	document.addEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler, false);
	window.addEventListener("resize", this.resizeHandler, false);
};

MappedConceptEditor.prototype.deregisterEvents = function() {
	//document.removeEventListener("selectionEvent_mc", this.selectionHandler);
	//document.removeEventListener("deselectionEvent_mc", this.deselectionHandler);
	document.removeEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler);
	window.removeEventListener("resize", this.resizeHandler);
};

MappedConceptEditor.prototype.handleContextMenuClicked = function(e) {
	this.performTreeAction(e.key, e.id, e.nodeType);
};

MappedConceptEditor.prototype.performTreeAction = function(action, elementId, elementKey) {	
	switch(action) {	 
	    case "removeSourceGrammar" : return this.removeSourceByGrammar(elementId);
	    case "editFunction" : return this.editFunction(elementId);
	    case "editGrammar" : return this.editGrammar(elementId);
	    case "removeSource" : return this.removeElement(elementId, true);
	    case "removeTarget" : return this.removeElement(elementId, false);
	}  
};

MappedConceptEditor.prototype.editGrammar = function(grammarId) {
	var _this = this;
	var form_identifier = "edit-grammar-" + grammarId;

	var elementId = this.source.findElementById(this.source.root, grammarId).parent.id;
	var sampleData = "";
	
	$(this.container).find(".sample-input").each(function() {
		if ($(this).find("input[name='elementId']").val()==elementId) {
			sampleData = $(this).find(".form-control").val();
		}
	});
	
	modalFormHandler = new ModalFormHandler({
		method: "POST",
		formUrl: "/grammar/" + grammarId + "/form/editWdata",
		data: { sample: sampleData },
		identifier: form_identifier,
		additionalModalClasses: "max-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		setupCallback: function(modal) { 
			grammarEditor = new GrammarEditor(modal, {
				pathPrefix: __util.getBaseUrl() + "mapping/editor/" + grammarId,
				entityId : _this.mappingId,
				grammarId : grammarId
				}
			); 
		},     
		completeCallback: function() { _this.graph.reselect(); }
	});
		
	modalFormHandler.show(form_identifier);
};

MappedConceptEditor.prototype.editFunction = function(connectionId) {
	var _this = this;	
	
	$.ajax({
	    url: _this.options.path + "/function",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	
	    	var functionId = data.id;
	    	var form_identifier = "edit-function-" + functionId;
		   	
	    	var elementIds = [];
	    	var samples = [];
	    	
	    	$(_this.container).find(".sample-input").each(function() {
	    		elementIds.push($(this).find("input[name='elementId']").val());
	    		samples.push($(this).find(".form-control").val());
	    	});
	    	
	    	modalFormHandler = new ModalFormHandler({
	    		method: "POST",
	    		data: { elementIds : elementIds, samples: samples },
	    		formUrl: "/function/" + functionId + "/form/editWdata",
	    		identifier: form_identifier,
	    		additionalModalClasses: "max-modal",
	    		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
	    		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
	    		                ],
	            setupCallback: function(modal) { functionEditor = new FunctionEditor(modal, {
	    			pathPrefix: __util.getBaseUrl() + "mapping/editor/" + _this.owningEditor.mappingId,
	    			entityId : _this.mappingId,
	    			functionId : functionId
	    		}); },       
	    		completeCallback: function() { _this.graph.reselect(); }
	    	});
	    		
	    	modalFormHandler.show(form_identifier);
	    },
	    error: __util.processServerError
	});
};

MappedConceptEditor.prototype.removeSourceByGrammar = function(grammarId) {
	this.removeElement(this.source.findElementById(this.source.root, grammarId).parent.id, true);
};

MappedConceptEditor.prototype.removeElement = function(elementId, isSource) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	bootbox.confirm(String.format(__translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete"), elementId), function(result) {
		if(result) {
			$.ajax({
			    url: _this.options.path + (isSource===true ? "/source/" : "/target/") + elementId + "/remove",
			    type: "POST",
			    dataType: "json",
			    success: function(data) {
			    	if (isSource) {
			    		$(_this.container).find(".sample-input").each(function() {
			    			if ($(this).find("input[name='elementId']").val()==elementId) {
			    				$(this).remove();
			    			}
				    	});
			    	}
			    	_this.reloadAll();
			    	_this.owningEditor.reloadAll();
			    },
			    error: __util.processServerError
			});
		}
	});
};

MappedConceptEditor.prototype.getGrammarContextMenu = function(element) { 
	var _this = editor.conceptEditor;
		var items = [
		];
		if (editor.mappingOwn || editor.mappingWrite) {
			items.push(_this.graph.createContextMenuItem("editGrammar", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key));
			items.push(_this.graph.createContextMenuSeparator());
			items.push(_this.graph.createContextMenuItem("removeSourceGrammar", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key));
		} else {
			items.push(_this.graph.createContextMenuItem("editGrammar", "~eu.dariah.de.minfba.common.link.view", "edit", element.id, element.template.options.key));
		}
		return items; 
};

MappedConceptEditor.prototype.getFunctionContextMenu = function(element) { 
	var _this = editor.conceptEditor;
		var items = [
		];
		if (editor.mappingOwn || editor.mappingWrite) {
			items.push(_this.graph.createContextMenuItem("editFunction", "~eu.dariah.de.minfba.common.link.edit", "edit", element.id, element.template.options.key));
		} else {
			items.push(_this.graph.createContextMenuItem("editFunction", "~eu.dariah.de.minfba.common.link.view", "edit", element.id, element.template.options.key));
		}
		return items; 
};

MappedConceptEditor.prototype.getElementContextMenu = function(element) { 
	var _this = editor.conceptEditor;
		var items = [
		];
		if (editor.mappingOwn || editor.mappingWrite) {
			if (element.template.area.isSource) {
				items.push(_this.graph.createContextMenuItem("removeSource", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key));
			} else {
				items.push(_this.graph.createContextMenuItem("removeTarget", "~eu.dariah.de.minfba.common.link.delete", "trash", element.id, element.template.options.key));
			}
		}
		return items; 
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

MappedConceptEditor.prototype.reloadAll = function() {
	this.deregisterEvents();
	this.graph.clearMappings();
	
	this.targetElements = [];
	this.sourceGrammars = [];
	
	this.getElementHierarchy(this.options.path + "/source", this.source, true);
	this.getElementHierarchy(this.options.path + "/target", this.target, false);
	
	if (this.source.root===undefined || this.source.root===null || 
			this.target.root===undefined || this.target.root===null) {
		this.modal.close();
		return;
	}
 	
 	this.addMapping();
 	
 	this.graph.update();
 	this.registerEvents();
};

MappedConceptEditor.prototype.getElementHierarchy = function(path, area, isSource) {	
	var _this = this;
	$.ajax({
	    url: path,
	    async: false, // This is required in our case
	    type: "GET",
	    success: function(data) {
	    	/* Reloading data while preserving layout */
	    	var rootX = 0;
	    	var rootY = 0;
	    	var reposition = false;
	    	if (area.root!=null) {
	    		rootX = area.root.rectangle.x;
	    		rootY = area.root.rectangle.y;
	    		reposition = true;
	    		area.clear();
	    	}
	    	
	    	if (data===null || data===undefined || data==="null" || data.length==0) {
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

	    	if (reposition) {
	    		area.root.rectangle = new Rectangle(rootX, rootY, area.root.rectangle.width, area.root.rectangle.height);
	    		area.invalidate();
	    	}
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
			/*if (nonterminals[i].terminalId==null || nonterminals[i].terminalId=="") {
				icon = this.options.icons.warning;
			}*/
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
			/*if (grammars[i].error==true) {
				icon = this.options.icons.error;
			}*/
			var fDesc = area.addElement(grammars[i].simpleType, parent, grammars[i].id, grammars[i].grammarName, icon);
			
			if (isSource) {
				this.sourceGrammars.push(grammars[i].id);
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

MappedConceptEditor.prototype.performTransformation = function() {
	var _this = this;	
	
	$.ajax({
	    url: _this.options.path + "/function",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	var f = data.id;
	    	
	    	var elementIds = [];
	    	var samples = [];
	    	
	    	$(_this.container).find(".sample-input").each(function() {
	    		elementIds.push($(this).find("input[name='elementId']").val());
	    		samples.push($(this).find(".form-control").val());
	    	});
	    	
	    	$.ajax({
	    	    url: "function/" + f + "/async/parseSample",
	    	    type: "POST",
	    	    data: { 
	    	    	elementIds : elementIds, 
	    	    	samples: samples
	    	    },
	    	    dataType: "json",
	    	    success: function(data) {
	    	    	if (data.success) {
	    	    		//$(_this.container).find("#transformation-result-container").text(JSON.stringify(data.pojo));
	    	    		_this.showTransformationResults(data);
	    	    	} else {
	    	    		alert("error1");
	    	    	}
	    	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	    }
	    	});
	    },
	    error: __util.processServerError
	});
};

MappedConceptEditor.prototype.showTransformationResults = function(data) {
	if (data.pojo==null || !Array.isArray(data.pojo)) {
		$(this.container).find(".transformation-result").addClass("hide");
		$(this.container).find(".transformation-result").text("");
		$(this.container).find(".no-results-alert").removeClass("hide");
		return;
	}
	$(this.container).find(".no-results-alert").addClass("hide");
	
	var list = $("<ul>");
	this.appendTransformationResults(data.pojo, list);
	$(this.container).find(".transformation-result").removeClass("hide");
	$(this.container).find(".transformation-result").html(list);
	$(this.container).find(".transformation-alerts").html("");
	if (data.objectWarnings!=null && Array.isArray(data.objectWarnings)) {
		for (var i=0; i<data.objectWarnings.length; i++) {
			$(this.container).find(".transformation-alerts").append(
					"<div class=\"alert alert-sm alert-warning\">" +
						"<span class=\"glyphicon glyphicon-exclamation-sign\" aria-hidden=\"true\"></span> " 
						+ data.objectWarnings[i] + 
					"</div>");			
		}
	}
	
};

MappedConceptEditor.prototype.appendTransformationResults = function(elements, container) {
	for (var i=0; i<elements.length; i++) {
		var elem = $("<li>");
		elem.append("<span class=\"transformation-result-label\">" + elements[i].label + "</span>");
		if (elements[i].children!=null && Array.isArray(elements[i].children) && elements[i].children.length > 0) {
			var subelem = $("<ul>");
			this.appendTransformationResults(elements[i].children, subelem);
			elem.append(subelem);
		} else {
			elem.append(": ");
			elem.append("<span class=\"transformation-result-value\">" + elements[i].value + "</span>");
		}
		container.append(elem);
	}
};
