var grammarEditor;

var GrammarEditor = function() {
	this.combinedGrammar = true;
	this.schemaId = schemaEditor.schemaId;
	this.grammarId = schemaEditor.selectedElementId;
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId + "/grammar/" + this.grammarId;
	this.init();
}

GrammarEditor.prototype.init = function() {
	var _this = this;
	
	if ($("#grammarContainer_lexerGrammar").val()!==null && $("#grammarContainer_lexerGrammar").val()!=="") {
		this.setLexerParserSeparate();
		$("#lexer-parser-option-separate").prop("checked", "checked");
	} else {
		this.setLexerParserCombined();
		$("#lexer-parser-option-combined").prop("checked", "checked");
	}
	
	$(".lexer-parser-option").change(function() {
		  if($(this).val()=="combined") {
			  _this.setLexerParserCombined();
		  } else {
			  _this.setLexerParserSeparate();
		  }
	});
};

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
		//additionalModalClasses: "narrow-modal",
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
	    	$("#grammar-uploading").addClass("alert-success");
	    	$("#grammar-uploading .grammar-loading").addClass("hide");
	    	$("#grammar-uploading .grammar-ok").removeClass("hide");
	    	
	    	_this.parseGrammar();
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	$("#grammar-uploading").addClass("alert-danger");
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
	    	$("#grammar-parsing").addClass("alert-success");
	    	$("#grammar-parsing .grammar-loading").addClass("hide");
	    	$("#grammar-parsing .grammar-ok").removeClass("hide");
	    	
	    	_this.compileGrammar();
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	$("#grammar-parsing").addClass("alert-danger");
	    	$("#grammar-parsing .grammar-loading").addClass("hide");
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
	    	$("#grammar-compiling").addClass("alert-success");
	    	$("#grammar-compiling .grammar-loading").addClass("hide");
	    	$("#grammar-compiling .grammar-ok").removeClass("hide");
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	$("#grammar-compiling").addClass("alert-danger");
	    	$("#grammar-compiling .grammar-loading").addClass("hide");
	    	$("#grammar-compiling .grammar-error").removeClass("hide");
	    }
	});
};

GrammarEditor.prototype.parseSample = function() {
	var _this = this;
		
	$.ajax({
	    url: _this.pathname + "/async/parseSample",
	    type: "POST",
	    data: { 
	    	sample : $("#grammar-sample-input").val()
	    },
	    dataType: "json",
	    success: function(data) {
	    	alert("Something done")
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	
	    }
	});
};


GrammarEditor.prototype.setLexerParserCombined = function() {
	$("#form-group-lexer-grammar").addClass("hide");
	this.combinedGrammar = true;
}

GrammarEditor.prototype.setLexerParserSeparate = function() {
	$("#form-group-lexer-grammar").removeClass("hide");
	this.combinedGrammar = false;
}