function BaseEditor() {
	__translator.addTranslations(["~de.unibamberg.minf.common.model.id",
	                              "~de.unibamberg.minf.common.model.label",
	                              "~de.unibamberg.minf.dme.model.element.name",
	                              "~de.unibamberg.minf.dme.model.element.transient",
	                              "~de.unibamberg.minf.dme.model.element.attribute",
	                              "~de.unibamberg.minf.dme.model.element.namespace",
	                              "~de.unibamberg.minf.dme.notification.no_terminal_configured",
	                              
	                              "~de.unibamberg.minf.dme.model.mapped_concept.source",
	                              "~de.unibamberg.minf.dme.model.mapped_concept.targets",
	                              "~de.unibamberg.minf.dme.model.grammar.grammar",
	                              "~de.unibamberg.minf.dme.model.function.function",
	                              "~de.unibamberg.minf.dme.model.grammar.state",
	                              "~de.unibamberg.minf.dme.model.function.state",
	                              "~de.unibamberg.minf.dme.model.grammar.base_rule",
	                              "~de.unibamberg.minf.dme.model.grammar.grammar_layout",
	                              "~de.unibamberg.minf.dme.model.grammar.separate",
	                              "~de.unibamberg.minf.dme.model.grammar.combined",
	                              
	                              "~de.unibamberg.minf.dme.editor.sample.download.file_count",
	                              "~de.unibamberg.minf.dme.editor.sample.download.files_count",
	                              "~de.unibamberg.minf.common.link.download",
	                              "~de.unibamberg.minf.common.labels.no_match_found",
	                              
	                              "~de.unibamberg.minf.dme.editor.sample.notice.empty_sample"]);
	this.vocabularySources = new Array();
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

BaseEditor.prototype.addVocabularySource = function(name, urlSuffix, params) {
	this.vocabularySources[name] = new Bloodhound({
		  datumTokenizer: Bloodhound.tokenizers.whitespace,
		  queryTokenizer: Bloodhound.tokenizers.whitespace,
		  remote: {
			  url: urlSuffix + "%QUERY" + (params!==undefined ? "?" + params : ""),
			  wildcard: '%QUERY'
		  }
	});
};

BaseEditor.prototype.registerTypeahead = function(element, name, datasource, displayAttr, limit, suggestionCallback, selectionCallback, changeCallback) {
	
	var _this = this;
	element.typeahead(null, {
		name: name,
		hint: false,
		display: displayAttr,
		source: datasource,
		limit: limit,
		templates: {
			empty: ['<div class="tt-empty-message">',
			        	__translator.translate("~de.unibamberg.minf.common.labels.no_match_found"),
			        '</div>'].join('\n'),
			suggestion: function(data) { return suggestionCallback(data); }
		}
	});

	// Executed when a suggestion has been accepted by the user
	if (selectionCallback!==undefined && selectionCallback!==null && typeof selectionCallback==='function') {
		element.bind('typeahead:select typeahead:autocomplete', function(ev, suggestion) {
			selectionCallback(this, suggestion);
		});
	}
	
	// Executed on custom input -> typically needs some validation
	if (changeCallback!==undefined && changeCallback!==null && typeof changeCallback==='function') {
		element.bind('change', function() {
			changeCallback(this, $(this).val());
		});
	}
};

BaseEditor.prototype.validateInput = function(element, urlPrefix, value) {
	var _this = this;
	$.ajax({
        url: urlPrefix + value,
        type: "GET",
        dataType: "json",
        success: function(data) {
        	$(element).closest(".form-group").removeClass("has-error");
        },
        error: function(textStatus) { 
        	$(element).closest(".form-group").addClass("has-error");
        }
	});
};

BaseEditor.prototype.deregisterTypeahead = function(element) {
	typeahead.typeahead('destroy');
};

BaseEditor.prototype.getElementDetails = function(pathPrefix, type, id, container, callback) {
	var _this = this;
	var elementType = this.getElementType(type);
	$.ajax({
		url: pathPrefix + "/" + elementType + "/" + id + "/async/get",
        type: "GET",
        dataType: "json",
        data: elementType.startsWith("terminal") ? { n: this.currentNature } : undefined,
        success: function(data) {
        	switch (_this.getElementType(type)) {
				case "element": return _this.processElementDetails(data, callback, container, pathPrefix);
				case "grammar": return _this.processGrammarDetails(data, callback, container, pathPrefix);
				case "function": return _this.processFunctionDetails(data, callback, container, pathPrefix);
				case "mappedconcept": return _this.processMappedConceptDetails(data, callback, container, pathPrefix);
				case "terminal": return _this.processTerminalElement(data, callback, container, pathPrefix);
				case "terminal/missing": return _this.processTerminalElement(data, callback, container, pathPrefix);
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
	}
	return type.toLowerCase();
};

BaseEditor.prototype.processMappedConceptDetails = function(data, callback, container, pathPrefix) {
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.id"), data.id));
	
	var _this = this;
	var inputIds = [];
	
	for (var elementId in data.elementGrammarIdsMap) {
		if (data.elementGrammarIdsMap.hasOwnProperty(elementId)) {
			inputIds.push(elementId);
		}
	}
	
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.mapped_concept.source"), inputIds));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.mapped_concept.targets"), data.targetElementIds));
	
	container.append(details);
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processGrammarDetails = function(data, callback, container, pathPrefix) { 
	var details = $("<div class=\"clearfix\">");
	
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.grammar.state"), 
			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "") +
			(data.passthrough==true ? "<span class='glyphicon glyphicon-forward' aria-hidden='true'></span>&nbsp;" : "")
	));
	
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.label"), data.name));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.grammar.base_rule"), data.baseMethod));
	
	if (data.passthrough!=true && data.grammarContainer!=null) {
		if (data.grammarContainer.lexerGrammar!==null && data.grammarContainer.lexerGrammar !=="") {
			details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.grammar.grammar_layout"), __translator.translate("~de.unibamberg.minf.dme.model.grammar.separate")));
		} else {
			details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.grammar.grammar_layout"), __translator.translate("~de.unibamberg.minf.dme.model.grammar.combined")));
		}
	}
	
	container.append(details);  
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processFunctionDetails = function(data, callback, container, pathPrefix) { 
	var details = $("<div class=\"clearfix\">");
	
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.function.state"), 
			(data.locked!=true && data.error!=true ? "<span class='glyphicon glyphicon-ok' aria-hidden='true'></span>&nbsp;" : "") +
			(data.locked==true ? "<span class='glyphicon glyphicon-wrench' aria-hidden='true'></span>&nbsp;" : "") +
			(data.error==true ? "<span class='glyphicon glyphicon-exclamation-sign glyphicon-color-danger' aria-hidden='true'></span>&nbsp;" : "")
	));
	
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.label"), data.name));
	
	container.append(details);
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processElementDetails = function(data, callback, container, pathPrefix) { 
	var details = $("<div class=\"clearfix\">");
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.element.name"), data.label));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.element.transient"), data.disabled));
		
	container.append(details); 
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.processTerminalElement = function(data, callback, container, pathPrefix) {
	var details = $("<div class=\"clearfix tab-details-block\">");
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.common.model.id"), data.id));
	details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.element.name"), data.name));
	if (data.namespace!==undefined) {
		details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.element.namespace"), data.namespace));
	}
	if (data.attribute!==undefined) {
		details.append(this.renderContextTabDetail(__translator.translate("~de.unibamberg.minf.dme.model.element.attribute"), data.attribute));
	}
		
	container.append(details);
	
	if (callback!==undefined) {
		callback(data, container, pathPrefix);
	}
};

BaseEditor.prototype.renderContextTabDetail = function(label, data, pre, classes) {
	var detail = $("<div class=\"row " + (classes===undefined ? "" : classes) + "\">");
	
	if (pre) {
		if (label!=null && label!="") {
			detail.append("<div class=\"schema-metadata-label\">" + label + ":</div>");
		} else {
			detail.append("<div>&nbsp;</div>");
		}
		detail.append("<div class=\"schema-metadata-data\"><pre>" + this.renderData(data) + "</pre></div>");
	} else {
		if (label!=null && label!="") {
			detail.append("<div class=\"schema-metadata-label col-xs-3 col-md-4\">" + label + ":</div>");
		} else {
			detail.append("<div class=\"col-xs-3 col-md-4\">&nbsp;</div>");
		}
		detail.append("<div class=\"schema-metadata-data col-xs-9 col-md-8\">" + this.renderData(data) + "</div>");
	}
	return detail;
};

BaseEditor.prototype.renderData = function(data) {
	if (data===true) {
		return "<span class=\"glyphicon glyphicon-check\" aria-hidden=\"true\"></span>";
	} else if (data===false) {
		return "<span class=\"glyphicon glyphicon-unchecked\" aria-hidden=\"true\"></span>";
	} else if (data!==undefined && data instanceof Array) {
		var strArray = "";
		for (var i=0; i<data.length; i++) {
			strArray += data[i];
			if (i<data.length-1) {
				strArray += ", ";
			}
		}
		return strArray;
	} else {
		return data;
	}
}

BaseEditor.prototype.loadActivitiesForEntity = function(entityId, container) {
	var _this = this;
	$.ajax({
        url: window.location.pathname + "/async/getChangesForEntity/" + entityId,
        type: "GET",
        dataType: "json",
        success: function(data) { __util.renderActivities(container, null, data); },
        error: function(textStatus) {
        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.head"), 
        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.body"));
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
        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.head"), 
        			__translator.translate("~de.unibamberg.minf.common.view.forms.servererror.body"));
        }
	});
};