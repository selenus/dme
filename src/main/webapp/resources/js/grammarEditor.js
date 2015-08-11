var grammarEditor;

var GrammarEditor = function(modal) {
	this.modal = modal;
	this.combinedGrammar = true;
	this.originalMode = "";
	this.originalModeModified = false;
	this.grammarModified = false;
	this.validated = false;
	this.error = false;
	this.schemaId = schemaEditor.schemaId;
	this.grammarId = schemaEditor.selectedElementId;
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId + "/grammar/" + this.grammarId;
	this.init();
}

GrammarEditor.prototype.updateGrammarState = function() {
	var state = "";
	if ($(this.modal).find("#passthrough").val()=="true") {
		state = "<span class=\"glyphicon glyphicon-forward\" aria-hidden=\"true\"></span> ~passthrough";
	} else if (this.error || $(this.modal).find("#error").val()=="true") {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-danger\" aria-hidden=\"true\"></span> error";
	} else if (this.originalModeModified || this.grammarModified) {
		state = "<span class=\"glyphicon glyphicon-info-sign glyphicon-color-info\" aria-hidden=\"true\"></span> modified, validation needed";
	} else if (this.validated) {
		state = "<span class=\"glyphicon glyphicon-ok-sign glyphicon-color-success\" aria-hidden=\"true\"></span> validated";
	} else {
		state = "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> ok";
	}
	$(this.modal).find("#grammar_state").html(state);
};

GrammarEditor.prototype.init = function() {
	var _this = this;
	
	if ($(_this.modal).find("#passthrough").val()=="true") {
		this.originalMode = "passthrough";
		this.setLexerParserPassthrough();
		$(_this.modal).find("#lexer-parser-option-passthrough").prop("checked", "checked");
	} else if ($(_this.modal).find("#grammarContainer_lexerGrammar").val()!==null && $(_this.modal).find("#grammarContainer_lexerGrammar").val()!=="") {
		this.originalMode = "separate";
		this.setLexerParserSeparate();
		$(_this.modal).find("#lexer-parser-option-separate").prop("checked", "checked");
	} else {
		this.originalMode = "combined";
		this.setLexerParserCombined();
		$(_this.modal).find("#lexer-parser-option-combined").prop("checked", "checked");
	}
	
	this.updateGrammarState();
	
	$(_this.modal).find("#grammarContainer_lexerGrammar").on('change keyup paste', function() {
		_this.grammarModified = true;
		_this.updateGrammarState();
	});
	$(_this.modal).find("#grammarContainer_parserGrammar").on('change keyup paste', function() {
		_this.grammarModified = true;
		_this.updateGrammarState();
	});
	
	$(_this.modal).find(".lexer-parser-option").change(function() {
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
	$(this.modal).find("#passthrough").val("false");
	
	$(this.modal).find("#form-group-lexer-grammar").addClass("hide");	
		
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
	$(this.modal).find("#passthrough").val("false");
	
	$(this.modal).find("#form-group-lexer-grammar").removeClass("hide");
	
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
	$(this.modal).find("#passthrough").val("true");
	
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
	modalFormHandler = new ModalFormHandler({
		formUrl: "/grammar/" + this.grammarId + "/async/processGrammarDialog",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		displayCallback: function() { 
			_this.validated = false;
			_this.error = true;
			_this.updateGrammarState();
			_this.uploadGrammar(); 
		},
		additionalModalClasses: "wider-modal",
		completeCallback: function() {_this.reload();}
	});
	modalFormHandler.show(form_identifier);
};

GrammarEditor.prototype.uploadGrammar = function() {
	var _this = this;
	
	this.setGrammarProcessingPanelStatus("grammar-uploading", "loading");
	
	$.ajax({
	    url: _this.pathname + "/async/upload",
	    type: "POST",
	    data: {
	    	combined : this.combinedGrammar,
	    	lexerGrammar : this.combinedGrammar ? null : $("#grammarContainer_lexerGrammar").val(),
	    	parserGrammar : $("#grammarContainer_parserGrammar").val()
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		_this.setGrammarProcessingPanelStatus("grammar-uploading", "success");
	    	   	_this.parseGrammar();
	    	} else {
	    		_this.setGrammarProcessingPanelStatus("grammar-uploading", "error");
	    		_this.setGrammarProcessingPanelErrors("grammar-uploading", data.objectErrors, data.fieldErrors)
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
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
	    		_this.compileGrammar();
	    	} else {
	    		_this.setGrammarProcessingPanelStatus("grammar-parsing", "error");
	    		_this.setGrammarProcessingPanelErrors("grammar-parsing", data.objectErrors, data.fieldErrors)
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
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
	    		_this.sandboxGrammar();
	    	} else {
	    		_this.setGrammarProcessingPanelStatus("grammar-compiling", "error");
	    		_this.setGrammarProcessingPanelErrors("grammar-compiling", data.objectErrors, data.fieldErrors)
	    	}	    	
	    }, error: function(jqXHR, textStatus, errorThrown ) {
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
	    data: { baseMethod: $("#base_method").val() },
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
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	_this.setGrammarProcessingPanelStatus("grammar-sandboxing", "error");
	    }
	});
};

GrammarEditor.prototype.setGrammarProcessingPanelErrors = function(id, objectErrors, fieldErrors) {	
	$("#" + id + " .panel-body").html();
	$("#" + id + " .panel-body").removeClass("hide");
	
	$("#accordion-validate-grammar .panel-collapse").removeClass("in");
	$("#" + id + " .panel-collapse").addClass("in");
	
	if (objectErrors!=null && objectErrors.length > 0) {
		var errorList = $("<ul>");
		for (var i=0; i<objectErrors.length; i++) {
			errorList.append("<li>" + objectErrors[i] + "</li>");
		}
		$("#" + id + " .panel-body").append(errorList);
	}
	
	if (fieldErrors!=null && fieldErrors.length > 0) {
		for (var i=0; i<fieldErrors.length; i++) {
			var errorList = $("<ul>");
			for (var j=0; j<fieldErrors[i].errors.length; j++) {
				errorList.append("<li>" + fieldErrors[i].errors[j] + "</li>");
			}
			$("#" + id + " .panel-body").append("<h4>" + fieldErrors[i].field + "</h4>");
			$("#" + id + " .panel-body").append(errorList);
		}
	}
}

GrammarEditor.prototype.setGrammarProcessingPanelStatus = function(id, state) {
	if (state==="loading") {
		$("#" + id + " .grammar-waiting").addClass("hide");
		$("#" + id + " .grammar-loading").removeClass("hide");
	} else if (state==="success") {
		$("#" + id).removeClass("panel-default");
		$("#" + id).addClass("panel-success");
		$("#" + id + " .grammar-loading").addClass("hide");
		$("#" + id + " .grammar-ok").removeClass("hide");
	} else if (state==="error") {
		$("#" + id).removeClass("panel-default");
		$("#" + id).addClass("panel-danger");
		$("#" + id + " .grammar-loading").addClass("hide");
		$("#" + id + " .grammar-error").removeClass("hide");
	} 
};


GrammarEditor.prototype.parseSample = function() {
	var _this = this;
	var svgContainer = "#grammar-sample-svg-embedded";
	var svgId = "grammar-sample-svg-image";
	
	this.destroySVG(svgContainer, svgId);
	
	$.ajax({
	    url: _this.pathname + "/async/parseSample",
	    type: "POST",
	    data: { 
	    	sample : $("#grammar-sample-input").val(),
	    	initRule : $("#base_method").val()
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success===true) {
	    		_this.showSVG(svgContainer, svgId, data.pojo);
	    	} else {
	    		alert("Some error");
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	alert("Some error");
	    }
	});
};

GrammarEditor.prototype.maximizeTree = function() {
	var _this = this;
	var height = $(window).height() - 300;
	var svgContainer = "grammar-sample-svg-max";
	var svgId = "grammar-sample-svg-image-max";
	var form_identifier = "max-parsed-input";
	var content = $($("#grammar-sample-svg-embedded .grammar-sample-svg-container").html());
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/grammar/" + this.grammarId + "/async/parsedInputContainer",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		setupCallback: function(modal) {
			$(modal).find("#" + svgContainer).height(height + "px");
		},
		displayCallback: function() { 
			_this.showSVG("#" + svgContainer, svgId, content);
		},
		additionalModalClasses: "max-modal",
	});
	modalFormHandler.show(form_identifier);
}

GrammarEditor.prototype.showSVG = function(selector, svgid, content) {
	var _this = this;
	var svg = $(content);
	svg.prop("id", svgid);
	
	$(selector + " .grammar-sample-svg-container").html(svg);
	
	var panZoom = svgPanZoom("#" + svgid, {
		fit: false,
		center: false
	});
	
	$(selector + " .btn-svg-zoomin").click(function() {
		panZoom.zoomIn(); return false;
	});
	$(selector + " .btn-svg-zoomout").click(function() {
		panZoom.zoomOut(); return false;
	});
	$(selector + " .btn-svg-reset").click(function() {
		panZoom.reset(); return false;
	});
	$(selector + " .btn-svg-newwindow").click(function() {
		_this.maximizeTree(); return false;
	});
};

GrammarEditor.prototype.destroySVG = function(selector, svgid) {
	if ($("#" + svgid).length) {
		svgPanZoom("#" + svgid).destroy();
		
		$(selector + " .grammar-sample-svg-container").text("");
		$(selector + " button").off();
	}
};