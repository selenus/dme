function BaseEditor() {
	__translator.addTranslations(["~eu.dariah.de.minfba.common.model.id",
	                              "~eu.dariah.de.minfba.common.model.label",
	                              "~eu.dariah.de.minfba.schereg.model.element.name",
	                              "~eu.dariah.de.minfba.schereg.model.element.transient",
	                              "~eu.dariah.de.minfba.schereg.model.element.attribute",
	                              "~eu.dariah.de.minfba.schereg.notification.no_terminal_configured",
	                              
	                              "~eu.dariah.de.minfba.schereg.model.mapped_concept.source",
	                              "~eu.dariah.de.minfba.schereg.model.mapped_concept.targets",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.grammar",
	                              "~eu.dariah.de.minfba.schereg.model.function.function",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.state",
	                              "~eu.dariah.de.minfba.schereg.model.function.state",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.base_rule",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.grammar_layout",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.separate",
	                              "~eu.dariah.de.minfba.schereg.model.grammar.combined"]);
};

BaseEditor.prototype.createActionButtons = function(container, contextMenuItems) {
	if (contextMenuItems==undefined) {
		return;
	}
	for (var i=0; i<contextMenuItems.length; i++) {
		if (contextMenuItems[i].key==="-" || contextMenuItems[i].key==undefined) {
			continue;
		}
		button = "<button " +
					"class='btn btn-default btn-sm' " +
					"onclick='editor.performTreeAction(\"" + contextMenuItems[i].key + "\", \"" + contextMenuItems[i].id + "\", \"" + contextMenuItems[i].type + "\"); return false;' type='button'>" +
						"<span class='glyphicon glyphicon-" + contextMenuItems[i].glyphicon + "'></span> " + contextMenuItems[i].label + 
				 "</button> ";
		container.append(button);
	}
};

BaseEditor.prototype.getElementDetails = function(pathPrefix, type, id, container, callback) {
	var _this = this;
	$.ajax({
		url: pathPrefix + "/" + this.getElementType(type) + "/" + id + "/async/get",
        type: "GET",
        dataType: "json",
        success: function(data) {
        	switch (_this.getElementType(type)) {
				case "element": return _this.processElementDetails(data, callback, container, pathPrefix);
				case "grammar": return _this.processGrammarDetails(data, callback, container, pathPrefix);
				case "function": return _this.processFunctionDetails(data, callback, container, pathPrefix);
				case "mappedConcept": return _this.processMappedConceptDetails(data, callback, container, pathPrefix);
				default: throw Error("Unknown element type: " + type);
			}
        },
        error: __util.processServerError
 	});
};

BaseEditor.prototype.getElementType = function(originalType) {
	var type = originalType;
	if (type==="Nonterminal" || type==="Label") {
		type="element";
	} else if (type==="DescriptionGrammarImpl") {
		type="grammar";
	} else if (type==="TransformationFunctionImpl") {
		type="function";
	}
	return type;
};

BaseEditor.prototype.processMappedConceptDetails = function(data, callback, container, pathPrefix) {
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.mapped_concept.source"), data.sourceElementId));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.mapped_concept.targets"), data.targetElementIds));
	
	details.append("<h5>" + __translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.grammar") + "</h5>");
	container.append(details);
	
	var _this = this;
	
	for (var elementId in data.elementGrammarIdsMap) {
		if (data.elementGrammarIdsMap.hasOwnProperty(elementId)) {
			var grammar = data.elementGrammarIdsMap[elementId];
			if (grammar!=null) {
				this.getElementDetails(pathPrefix, "DescriptionGrammarImpl", grammar.id, details, function() {
					details.append("<h5>" + __translator.translate("~eu.dariah.de.minfba.schereg.model.function.function") + "</h5>");
					_this.getElementDetails(pathPrefix, "TransformationFunctionImpl", grammar.id.transformationFunctions[0].id, details);
				});
			}
		}
	}
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processGrammarDetails = function(data, callback, container, pathPrefix) { 
	var details = $("<div class=\"clearfix\">");
	
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.state"), 
			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "") +
			(data.passthrough==true ? "<span class='glyphicon glyphicon-forward' aria-hidden='true'></span>&nbsp;" : "")
	));
	
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.label"), data.grammarName));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.base_rule"), data.baseMethod));
	
	if (data.passthrough!=true && data.grammarContainer!=null) {
		if (data.grammarContainer.lexerGrammar!==null && data.grammarContainer.lexerGrammar !=="") {
			details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.grammar_layout"), __translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.separate")));
		} else {
			details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.grammar_layout"), __translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.combined")));
		}
	}
	
	container.append(details);  
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processFunctionDetails = function(data, callback, container, pathPrefix) { 
	var details = $("<div class=\"clearfix\">");
	
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.function.state"), 
			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "")
	));
	
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.label"), data.name));
	
	container.append(details);
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processElementDetails = function(data, callback, container, pathPrefix) { 
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.name"), data.name));
	details.append(this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.transient"), data.transient));
		
	container.append(details); 
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processTerminalElement = function(data, container, pathPrefix) { 
	var _this = editor;
	if (data.terminalId!=null && data.terminalId!="") {
		$.ajax({
			url: pathPrefix + "/element/" + data.id + "/async/getTerminal",
	        type: "GET",
	        dataType: "json",
	        success: function(data) {
	        	var details = $("<div class=\"clearfix tab-details-block\">");
	        	details.append(_this.renderContextTabDetail("", "<h4>" + data.simpleType + "</h4>"));
	        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.common.model.id"), data.id));
	        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.name"), data.name));
	        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.transient"), data.namespace));
	        	details.append(_this.renderContextTabDetail(__translator.translate("~eu.dariah.de.minfba.schereg.model.element.attribute"), data.attribute));
	        		
	        	container.append(details);
	        },
	        error: function(textStatus) {}
	 	});    
	} else if (data.simpleType==="Nonterminal") {
		var details = $("<div class=\"clearfix tab-details-block\">");
		details.append("<div class='alert alert-sm alert-warning' role='alert'>" + 
				"<span aria-hidden='true' class='glyphicon glyphicon-info-sign'></span> " +
				__translator.translate("~eu.dariah.de.minfba.schereg.notification.no_terminal_configured") +
				"</div>");
		container.append(details);
	}
};

BaseEditor.prototype.renderContextTabDetail = function(label, data, pre) {
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

BaseEditor.prototype.loadActivitiesForEntity = function(entityId, container) {
	var _this = this;
	$.ajax({
        url: window.location.pathname + "/async/getChangesForEntity/" + entityId,
        type: "GET",
        dataType: "json",
        success: function(data) { __util.renderActivities(container, null, data); },
        error: function(textStatus) {
        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.head"), 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.body"));
        }
	});
};

BaseEditor.prototype.loadActivitiesForElement = function(elementId, container) {
	var _this = this;
	$.ajax({
        url: window.location.pathname + "/async/getChangesForElement/" + elementId,
        type: "GET",
        dataType: "json",
        success: function(data) { __util.renderActivities(container, elementId, data); },
        error: function(textStatus) {
        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.head"), 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.body"));
        }
	});
};