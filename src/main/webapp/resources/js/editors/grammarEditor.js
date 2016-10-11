var grammarEditor;

var GrammarEditor = function(modal, options) {
	this.options = $.extend({ 	
		pathPrefix: "",
		entityId : "",
		grammarId : ""
	}, options)
	
	this.modal = modal;
	this.combinedGrammar = true;
	this.originalMode = "";
	this.originalModeModified = false;
	this.grammarModified = false;
	this.validated = false;
	this.error = $(this.modal).find(".error").val()=="true";
	this.schemaId = this.options.entityId;
	this.grammarId = this.options.grammarId;
	this.pathname = this.options.pathPrefix + "/grammar/" + this.grammarId;
	
	this.processGrammarModal = null;
	
	this.svg = null;
	
	__translator.addTranslations(["~eu.dariah.de.minfba.common.link.ok",
	                              "~eu.dariah.de.minfba.common.link.error",
	                              "~eu.dariah.de.minfba.common.link.validated",
	                              "~eu.dariah.de.minfba.common.link.modified",
	                              "~eu.dariah.de.minfba.common.link.validation_required",
	                              
	                              "~eu.dariah.de.minfba.schereg.model.grammar.passthrough"]);
	__translator.getTranslations();
	
	this.init();
}

GrammarEditor.prototype.updateGrammarState = function() {
	var state = "";
	if ($(this.modal).find(".passthrough").val()=="true") {
		state = "<span class=\"glyphicon glyphicon-forward\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.schereg.model.grammar.passthrough");
		this.setSampleParseFunctionality(false);
	} else if (this.originalModeModified || this.grammarModified) {
		state = "<span class=\"glyphicon glyphicon-info-sign glyphicon-color-info\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.modified") + "; " + __translator.translate("~eu.dariah.de.minfba.common.link.validation_required");
		this.setSampleParseFunctionality(false);
	} else if (this.error) {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-danger\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.error");
		this.setSampleParseFunctionality(false);
	} else if (this.validated) {
		state = "<span class=\"glyphicon glyphicon-ok-sign glyphicon-color-success\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.validated");
		this.setSampleParseFunctionality(true);
	} else {
		state = "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.ok");
		this.setSampleParseFunctionality(true);
	}
	$(this.modal).find(".grammar_state").html(state);
};

GrammarEditor.prototype.setSampleParseFunctionality = function(enabled) {
	if (enabled) {
		$(this.modal).find(".btn-parse-sample").removeClass("disabled");
		$(this.modal).find(".btn-parse-sample").removeClass("btn-warning");
		$(this.modal).find(".btn-parse-sample").addClass("btn-info");
	} else {
		$(this.modal).find(".btn-parse-sample").addClass("disabled");
		$(this.modal).find(".btn-parse-sample").addClass("btn-warning");
		$(this.modal).find(".btn-parse-sample").removeClass("btn-info");
	}
};

GrammarEditor.prototype.init = function() {
	var _this = this;
	
	if ($(this.modal).find(".passthrough").val()=="true") {
		this.originalMode = "passthrough";
		this.setLexerParserPassthrough();
		$(this.modal).find(".lexer-parser-option-passthrough").prop("checked", "checked");
	} else if ($(this.modal).find(".grammarContainer_lexerGrammar").val()!==null && $(this.modal).find(".grammarContainer_lexerGrammar").val()!=="") {
		this.originalMode = "separate";
		this.setLexerParserSeparate();
		$(this.modal).find(".lexer-parser-option-separate").prop("checked", "checked");
	} else {
		this.originalMode = "combined";
		this.setLexerParserCombined();
		$(this.modal).find(".lexer-parser-option-combined").prop("checked", "checked");
	}
	
	this.updateGrammarState();
	
	$(this.modal).find(".grammarContainer_lexerGrammar").on('change keyup paste', function() {
		_this.grammarModified = true;
		_this.updateGrammarState();
	});
	$(this.modal).find(".grammarContainer_parserGrammar").on('change keyup paste', function() {
		_this.grammarModified = true;
		_this.updateGrammarState();
	});
	
	$(this.modal).find(".lexer-parser-option").change(function() {
		  if($(this).val()=="combined") {
			  _this.setLexerParserCombined();
		  } else if($(this).val()=="passthrough") {
			  _this.setLexerParserPassthrough();
		  } else{
			  _this.setLexerParserSeparate();
		  }
		  $(_this.modal).modal("layout");
		  _this.updateGrammarState();
	});
};

GrammarEditor.prototype.showHelp = function() {
	var _this = this;
	
	var form_identifier = "edit-grammar-help";
	modalFormHandler = new ModalFormHandler({
		formUrl: "/grammar/" + this.grammarId + "/async/help/editGrammar",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		additionalModalClasses: "wider-modal"
	});
	modalFormHandler.show(form_identifier);
};

GrammarEditor.prototype.setLexerParserCombined = function() {	
	$(this.modal).find(".non-passthrough-only").removeClass("hide");
	$(this.modal).find(".passthrough-only").addClass("hide");
	$(this.modal).find(".passthrough").val("false");
	
	$(this.modal).find(".form-group-lexer-grammar").addClass("hide");	
		
	this.combinedGrammar = true;
	
	if (this.originalMode!=="combined") {
		this.originalModeModified = true;
	} else {
		this.originalModeModified = false;
	}
}

GrammarEditor.prototype.setLexerParserSeparate = function() {	
	$(this.modal).find(".non-passthrough-only").removeClass("hide");
	$(this.modal).find(".passthrough-only").addClass("hide");	
	$(this.modal).find(".passthrough").val("false");
	
	$(this.modal).find(".form-group-lexer-grammar").removeClass("hide");
	
	this.combinedGrammar = false;
	
	if (this.originalMode!=="separate") {
		this.originalModeModified = true;
	} else {
		this.originalModeModified = false;
	}
}

GrammarEditor.prototype.setLexerParserPassthrough = function() {
	$(this.modal).find(".non-passthrough-only").addClass("hide");
	$(this.modal).find(".passthrough-only").removeClass("hide");
	$(this.modal).find(".passthrough").val("true");
	
	this.combinedGrammar = false;
	
	if (this.originalMode!=="passthrough") {
		this.originalModeModified = true;
	} else {
		this.originalModeModified = false;
	}
}

GrammarEditor.prototype.validateGrammar = function() {
	var _this = this;	
	var form_identifier = "process-grammar";
	this.processGrammarModal = new ModalFormHandler({
		formUrl: "/grammar/" + this.grammarId + "/async/processGrammarDialog",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		displayCallback: function() { 
			_this.validated = false;
			_this.grammarModified = true;
			_this.updateGrammarState();
			_this.uploadGrammar(); 
		},
		additionalModalClasses: "wider-modal",
		completeCallback: function() {_this.reload();}
	});
	this.processGrammarModal.show(form_identifier);
};

GrammarEditor.prototype.uploadGrammar = function() {
	var _this = this;
	
	this.setGrammarProcessingPanelStatus("grammar-uploading", "loading");
	
	$.ajax({
	    url: _this.pathname + "/async/upload",
	    type: "POST",
	    data: {
	    	combined : _this.combinedGrammar,
	    	lexerGrammar : _this.combinedGrammar ? null : $(_this.modal).find(".grammarContainer_lexerGrammar").val(),
	    	parserGrammar : $(_this.modal).find(".grammarContainer_parserGrammar").val()
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		_this.setGrammarProcessingPanelStatus("grammar-uploading", "success");
	    		_this.setGrammarProcessingPanelSuccessFiles("grammar-uploading", data.pojo);
	    	   	_this.parseGrammar();
	    	} else {
	    		_this.setGrammarProcessingPanelStatus("grammar-uploading", "error");
	    		_this.setGrammarProcessingPanelErrors("grammar-uploading", data.objectErrors, data.fieldErrors)
	    	}
	    	$(window).trigger('resize');
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	_this.setGrammarProcessingPanelStatus("grammar-uploading", "error");
	    }
	});
};

GrammarEditor.prototype.parseGrammar = function() {
	var _this = this;
	
	this.setGrammarProcessingPanelStatus("grammar-parsing", "loading");
		
	$.ajax({
	    url: _this.pathname + "/async/parse",
	    type: "GET",
	    success: function(data) {
	    	if (data.success) {
	    		_this.setGrammarProcessingPanelStatus("grammar-parsing", "success");
	    		_this.setGrammarProcessingPanelSuccessFiles("grammar-parsing", data.pojo);
	    		_this.compileGrammar();
	    	} else {
	    		_this.setGrammarProcessingPanelStatus("grammar-parsing", "error");
	    		_this.setGrammarProcessingPanelErrors("grammar-parsing", data.objectErrors, data.fieldErrors)
	    	}
	    	$(window).trigger('resize');
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	_this.setGrammarProcessingPanelStatus("grammar-parsing", "error");
	    }
	});
};

GrammarEditor.prototype.compileGrammar = function() {
	var _this = this;
	
	this.setGrammarProcessingPanelStatus("grammar-compiling", "loading");
	
	$.ajax({
	    url: _this.pathname + "/async/compile",
	    type: "GET",
	    success: function(data) {
	    	if (data.success) {
	    		_this.setGrammarProcessingPanelStatus("grammar-compiling", "success");
	    		_this.setGrammarProcessingPanelSuccessFiles("grammar-compiling", data.pojo);
	    		_this.sandboxGrammar();
	    	} else {
	    		_this.setGrammarProcessingPanelStatus("grammar-compiling", "error");
	    		_this.setGrammarProcessingPanelErrors("grammar-compiling", data.objectErrors, data.fieldErrors)
	    	}	    
	    	$(window).trigger('resize');
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	_this.setGrammarProcessingPanelStatus("grammar-compiling", "error");
	    }
	});
};

GrammarEditor.prototype.sandboxGrammar = function() {
	var _this = this;
	
	this.setGrammarProcessingPanelStatus("grammar-sandboxing", "loading");
	
	$.ajax({
	    url: _this.pathname + "/async/sandbox",
	    type: "GET",
	    data: { baseMethod: $(_this.modal).find("#baseMethod").val() },
	    success: function(data) {
	    	if (data.success) {
	    		_this.setGrammarProcessingPanelStatus("grammar-sandboxing", "success");
	    		
	    		_this.originalModeModified = false;
		    	_this.grammarModified = false;
		    	_this.validated = true;
		    	_this.error = false;
		    	_this.updateGrammarState();
	    	} else {
	    		_this.setGrammarProcessingPanelStatus("grammar-sandboxing", "error");
	    		_this.setGrammarProcessingPanelErrors("grammar-sandboxing", data.objectErrors, data.fieldErrors)
	    	}
	    	$(window).trigger('resize');
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	_this.setGrammarProcessingPanelStatus("grammar-sandboxing", "error");
	    }
	});
};

GrammarEditor.prototype.setGrammarProcessingPanelErrors = function(id, objectErrors, fieldErrors) {
	var panel = $(this.processGrammarModal.container).find("." + id);
	
	panel.find(".panel-body").html();
	panel.find(".panel-body").removeClass("hide");
	
	$(this.processGrammarModal.container).find(".accordion-validate-grammar .panel-collapse").removeClass("in");
	panel.find(".panel-collapse").addClass("in");
	
	if (objectErrors!=null && objectErrors.length > 0) {
		var errorList = $("<ul>");
		for (var i=0; i<objectErrors.length; i++) {
			errorList.append("<li>" + objectErrors[i] + "</li>");
		}
		panel.find(".panel-body").append(errorList);
	}
	
	if (fieldErrors!=null && fieldErrors.length > 0) {
		for (var i=0; i<fieldErrors.length; i++) {
			var errorList = $("<ul>");
			for (var j=0; j<fieldErrors[i].errors.length; j++) {
				errorList.append("<li>" + fieldErrors[i].errors[j] + "</li>");
			}
			panel.find(".panel-body").append("<h4>" + fieldErrors[i].field + "</h4>");
			panel.find(".panel-body").append(errorList);
		}
	}
}

GrammarEditor.prototype.setGrammarProcessingPanelStatus = function(id, state) {
	var panel = $(this.processGrammarModal.container).find("." + id);
	
	if (state==="loading") {
		panel.find(".grammar-waiting").addClass("hide");
		panel.find(".grammar-loading").removeClass("hide");
	} else if (state==="success") {
		panel.removeClass("panel-default");
		panel.addClass("panel-success");
		panel.find(".grammar-loading").addClass("hide");
		panel.find(".grammar-ok").removeClass("hide");
	} else if (state==="error") {
		panel.removeClass("panel-default");
		panel.addClass("panel-danger");
		panel.find(".grammar-loading").addClass("hide");
		panel.find(".grammar-error").removeClass("hide");
		this.error = true;
	} 
};

GrammarEditor.prototype.setGrammarProcessingPanelSuccessFiles = function(id, files) {
	var panel = $(this.processGrammarModal.container).find("." + id);
	
	panel.find(".panel-body").html();
	panel.find(".panel-body").removeClass("hide");
	
	//$(this.modal).find(".accordion-validate-grammar .panel-collapse").removeClass("in");
	panel.find(".panel-collapse").addClass("in");
	
	var fileList = $("<ul>");
	for (var i=0; i<files.length; i++) {
		fileList.append("<li>" + files[i] + "</li>");
	}
	panel.find(".panel-body").append(fileList);
};

GrammarEditor.prototype.parseSample = function() {
	var _this = this;
	
	$(this.modal).find(".grammar-parse-alerts").html("");
	if (this.svg!=null) {
		this.svg.destroy();
	}

	$.ajax({
	    url: _this.pathname + "/async/parseSample",
	    type: "POST",
	    data: { 
	    	sample : $(this.modal).find(".grammar-sample-input").val(),
	    	initRule : $(this.modal).find("#baseMethod").val(),
	    	temporary : _this.originalModeModified || _this.grammarModified || _this.validated
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success===true) {
	    		_this.showParseSampleResult(data.pojo.svg, null, data.pojo.errors);
	    	} else {
	    		_this.showParseSampleResult(null, data.objectErrors, data.objectWarnings);
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

GrammarEditor.prototype.showParseSampleResult = function(svg, errors, warnings) {
	if (errors!=null && Array.isArray(errors) && errors.length > 0) {
		var list = $("<ul>");
		for (var i=0; i<errors.length; i++) {
			$(list).append("<li>" + errors[i] + "</li>");
		}

		var alerts = $("<div class=\"alert alert-sm alert-danger\"> " +
				"<span class=\"glyphicon glyphicon-exclamation-sign\" aria-hidden=\"true\"></span>" +
				"~Errors" +
			"</div>");
		
		$(alerts).append(list)
		$(this.modal).find(".grammar-parse-alerts").append(alerts);
	}
	if (warnings!=null && Array.isArray(warnings) && warnings.length > 0) {
		var list = $("<ul>");
		for (var i=0; i<warnings.length; i++) {
			$(list).append("<li>" + warnings[i] + "</li>");
		}

		var alerts = $("<div class=\"alert alert-sm alert-warning\"> " +
				"<span class=\"glyphicon glyphicon-exclamation-sign\" aria-hidden=\"true\"></span>" +
				"~Warnings" +
			"</div>");
		
		$(alerts).append(list)
		$(this.modal).find(".grammar-parse-alerts").append(alerts);
	}
	if (svg != null) {
		var svgContainer = ".grammar-sample-svg-embedded";	
		$(this.modal).find(".grammar-sample-svg-embedded").removeClass("hide");
		this.svg = new SvgViewer(svgContainer, svg)
	}
};