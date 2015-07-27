<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="grammar">
	<div class="form-header">
		<h3 id="form-header-title">~Grammar editor <small><span class="glyphicon glyphicon-info-sign help-sign" onclick="grammarEditor.showHelp(); return false;" aria-hidden="true"></span></small></h3>	
		<sf:hidden path="id" />
	</div>
	<div class="form-content row">
		<div class="col-md-7" style="border-right: 1px solid #E5E5E5;">
			<legend><strong>1</strong> Edit grammar</legend>
			<div class="form-group row">
				<div class="col-sm-6">
					<label class="control-label" for="grammar_name">~Grammar name:</label>
					<div>
						<sf:input path="grammarName" class="form-control" id="grammar_name" />
						<sf:errors path="grammarName" cssClass="error" />
					</div>
				</div>
				<div class="col-sm-6">
					<label class="control-label" for="base_method">~Base rule:</label>
					<div>
						<sf:input path="baseMethod" class="form-control" id="base_method" />
						<sf:errors path="baseMethod" cssClass="error" />
					</div>
				</div>
			</div>
			
			<div class="form-group">
				<label class="control-label" for="grammarContainer_lexerGrammar">~Grammar layout:</label>
				<div>
					<div class="radio">
	  					<label>
	    					<input type="radio" name="lexer-parser-option" class="lexer-parser-option" id="lexer-parser-option-combined" value="combined" checked>
	    					~Combined: Include lexer/parser rules in one grammar
	  					</label>
					</div>
					<div class="radio">
	  					<label>
	    					<input type="radio" name="lexer-parser-option" class="lexer-parser-option" id="lexer-parser-option-separate" value="separate">
	    					~Separate: Specify lexer and parser rules in separate grammars
	  					</label>
					</div>
				</div>
			</div>
			<div class="clearfix">
				<div class="alert alert-sm alert-info" role="alert">~This is a passthrough grammar. Input will not be decomposed.</div>
			</div>
			<div class="form-group" id="form-group-lexer-grammar">
				<label class="control-label" for="grammarContainer_lexerGrammar">~Lexer Grammar:</label>
				<div>
					<sf:textarea path="grammarContainer.lexerGrammar" rows="10" class="form-control codearea" id="grammarContainer_lexerGrammar" />
					<sf:errors path="grammarContainer.lexerGrammar" cssClass="error" />
				</div>
			</div>		
			<div class="form-group">
				<label id="label-parser-grammar" class="control-label" for="grammarContainer_parserGrammar">~Parser Grammar:</label>
				<div>
					<sf:textarea path="grammarContainer.parserGrammar" rows="10" class="form-control codearea" id="grammarContainer_parserGrammar" />
					<sf:errors path="grammarContainer.parserGrammar" cssClass="error" />
				</div>
			</div>	
			<div class="form-footer">
				<div class="controls">
					<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.cancel" /></button>
					<button class="btn btn-info btn-sm" onclick="grammarEditor.validateGrammar(); return false;">~Validate</button>
					<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~eu.dariah.de.minfba.common.link.save" /></button>
				</div>
			</div>
		</div>
		<div class="col-md-5" style="border-left: 1px solid #E5E5E5; margin-left: -1px;">
			<legend><strong>2</strong> Perform sample parse</legend>
			<div class="form-group">
				<label class="control-label" for="grammar-sample-input">~Sample input:</label>
				<div>
					<textarea id="grammar-sample-input" rows="6" class="form-control codearea">{1, {3, 5}, {1, {3, 5}, {1, {3, 5}, {1, {3, 5}, {1, {3, 5}, {1, {3, 5}, {1, {3, {1, {3, 5}, 5, 9}}, 5, 9}, 9}, 9}, 9}, 9}, 9}, 9}</textarea>
				</div>
			</div>	
			<div class="clearfix">
				<button class="btn btn-info btn-sm pull-right" onclick="grammarEditor.parseSample(); return false;"><span class="glyphicon glyphicon-play" aria-hidden="true"></span> ~Parse input</button>
			</div>
			<legend><strong>3</strong> Analyze sample results</legend>
			<div id="grammar-sample-svg-embedded" class="grammar-sample-svg">
				<div class="grammar-sample-svg-container"></div>
				<div class="grammar-svg-controls">
					<button class="btn btn-link btn-sm btn-svg-zoomin"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
					<button class="btn btn-link btn-sm btn-svg-zoomout"><span class="glyphicon glyphicon-minus" aria-hidden="true"></span></button>
					<button class="btn btn-link btn-sm btn-svg-reset"><span class="glyphicon glyphicon-repeat" aria-hidden="true"></span></button>
					<button class="btn btn-link btn-sm btn-svg-newwindow"><span class="glyphicon glyphicon-new-window" aria-hidden="true"></span></button>
				</div>
			</div>
			
			
				
		</div>
	
			
	</div>
	
</sf:form>