var grammarEditor;

var GrammarEditor = function(modal) {
	this.modal = modal;
	this.combinedGrammar = true;
	this.originalMode = "";
	this.originalModeModified = false;
	this.grammarModified = false;
	this.validated = false;
	this.schemaId = schemaEditor.schemaId;
	this.grammarId = schemaEditor.selectedElementId;
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId + "/grammar/" + this.grammarId;
	this.init();
}

GrammarEditor.prototype.updateGrammarState = function() {
	var state = "";
	if ($(this.modal).find("#passthrough").val()=="true") {
		state = "<span class=\"glyphicon glyphicon-forward\" aria-hidden=\"true\"></span> ~passthrough</span>";
	} else if (this.originalModeModified || this.grammarModified) {
		state = "<span class=\"glyphicon glyphicon-info-sign glyphicon-color-info\" aria-hidden=\"true\"></span> modified, validation needed</span>";
	} else if (this.validated) {
		state = "<span class=\"glyphicon glyphicon-ok-sign glyphicon-color-success\" aria-hidden=\"true\"></span> validated</span>";
	} else {
		state = "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> ok</span>";
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
		displayCallback: function() { _this.uploadGrammar(); },
		additionalModalClasses: "wide-modal",
		completeCallback: function() {_this.reload();}
	});
	modalFormHandler.show(form_identifier);
};

GrammarEditor.prototype.uploadGrammar = function() {
	var _this = this;
	
	$("#grammar-uploading .grammar-waiting").addClass("hide");
	$("#grammar-uploading .grammar-loading").removeClass("hide");
	
	$.ajax({
	    url: _this.pathname + "/async/upload",
	    type: "POST",
	    data: { 
	    	lexerGrammar : this.combinedGrammar ? null : $("#grammarContainer_lexerGrammar").val(),
	    	parserGrammar : $("#grammarContainer_parserGrammar").val()
	    },
	    dataType: "json",
	    success: function(data) {
	    	$("#grammar-uploading").removeClass("panel-default");
	    	$("#grammar-uploading").addClass("panel-success");
	    	$("#grammar-uploading .grammar-loading").addClass("hide");
	    	$("#grammar-uploading .grammar-ok").removeClass("hide");
	    	
	    	_this.parseGrammar();
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	$("#grammar-uploading").removeClass("panel-default");
	    	$("#grammar-uploading").addClass("panel-alert");
	    	$("#grammar-uploading .grammar-loading").addClass("hide");
	    	$("#grammar-uploading .grammar-error").removeClass("hide");
	    }
	});
};

GrammarEditor.prototype.parseGrammar = function() {
	var _this = this;
	
	$("#grammar-parsing .grammar-waiting").addClass("hide");
	$("#grammar-parsing .grammar-loading").removeClass("hide");
	
	$.ajax({
	    url: _this.pathname + "/async/parse",
	    type: "GET",
	    success: function(data) {
	    	$("#grammar-parsing").removeClass("panel-default");
	    	$("#grammar-parsing").addClass("panel-success");
	    	$("#grammar-parsing .grammar-loading").addClass("hide");
	    	$("#grammar-parsing .grammar-ok").removeClass("hide");
	    	
	    	_this.compileGrammar();
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	$("#grammar-parsing").removeClass("panel-default");
	    	$("#grammar-parsing").addClass("panel-alert");
	    	$("#grammar-parsing .grammar-error").removeClass("hide");
	    }
	});
};

GrammarEditor.prototype.compileGrammar = function() {
	var _this = this;
	
	$("#grammar-compiling .grammar-waiting").addClass("hide");
	$("#grammar-compiling .grammar-loading").removeClass("hide");
	
	$.ajax({
	    url: _this.pathname + "/async/compile",
	    type: "GET",
	    success: function(data) {
	    	$("#grammar-compiling").removeClass("panel-default");
	    	$("#grammar-compiling").addClass("panel-success");
	    	$("#grammar-compiling .grammar-loading").addClass("hide");
	    	$("#grammar-compiling .grammar-ok").removeClass("hide");
	    	
	    	_this.sandboxGrammar();
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	$("#grammar-compiling").removeClass("panel-default");
	    	$("#grammar-compiling").addClass("panel-alert");
	    	$("#grammar-compiling .grammar-loading").addClass("hide");
	    	$("#grammar-compiling .grammar-error").removeClass("hide");
	    }
	});
};

GrammarEditor.prototype.sandboxGrammar = function() {
	var _this = this;
	
	$("#grammar-sandboxing .grammar-waiting").addClass("hide");
	$("#grammar-sandboxing .grammar-loading").removeClass("hide");
	
	$.ajax({
	    url: _this.pathname + "/async/sandbox",
	    type: "GET",
	    success: function(data) {
	    	$("#grammar-sandboxing").removeClass("panel-default");
	    	$("#grammar-sandboxing").addClass("panel-success");
	    	$("#grammar-sandboxing .grammar-loading").addClass("hide");
	    	$("#grammar-sandboxing .grammar-ok").removeClass("hide");
	    	
	    	_this.originalModeModified = false;
	    	_this.grammarModified = false;
	    	_this.validated = true;
	    	_this.updateGrammarState();
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	$("#grammar-sandboxing").removeClass("panel-default");
	    	$("#grammar-sandboxing").addClass("panel-alert");
	    	$("#grammar-sandboxing .grammar-loading").addClass("hide");
	    	$("#grammar-sandboxing .grammar-error").removeClass("hide");
	    }
	});
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