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
	$.ajax({
	    url: _this.pathname + "/async/validate",
	    type: "POST",
	    data: { 
	    	lexerGrammar : this.combinedGrammar ? null : $("#grammarContainer_lexerGrammar").val(),
	    	parserGrammar : $("#grammarContainer_parserGrammar").val()
	    },
	    dataType: "json",
	    success: function(data) {
	    	alert(data);
	    }
	});
}

GrammarEditor.prototype.setLexerParserCombined = function() {
	$("#form-group-lexer-grammar").addClass("hide");
	this.combinedGrammar = true;
}

GrammarEditor.prototype.setLexerParserSeparate = function() {
	$("#form-group-lexer-grammar").removeClass("hide");
	this.combinedGrammar = false;
}