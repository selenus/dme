<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="grammar" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.edit" /></h3>	
		<sf:hidden path="id" />
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-3 control-label" for="grammar_name"><s:message code="~eu.dariah.de.minfba.common.model.label" />:</label>
			<div class="col-sm-9">
				<sf:input path="grammarName" class="form-control" id="grammar_name" />
				<sf:errors path="grammarName" cssClass="error" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-3 control-label" for="grammarContainer_lexerGrammar">~Grammar layout:</label>
			<div class="col-sm-9">
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
		<div class="form-group" id="form-group-lexer-grammar">
			<label class="col-sm-3 control-label" for="grammarContainer_lexerGrammar">~Lexer Grammar:</label>
			<div class="col-sm-9">
				<sf:textarea path="grammarContainer.lexerGrammar" rows="10" class="form-control codearea" id="grammarContainer_lexerGrammar" />
				<sf:errors path="grammarContainer.lexerGrammar" cssClass="error" />
			</div>
		</div>		
		<div class="form-group">
			<label id="label-parser-grammar" class="col-sm-3 control-label" for="grammarContainer_parserGrammar">~Parser Grammar:</label>
			<div class="col-sm-9">
				<sf:textarea path="grammarContainer.parserGrammar" rows="10" class="form-control codearea" id="grammarContainer_parserGrammar" />
				<sf:errors path="grammarContainer.parserGrammar" cssClass="error" />
			</div>
		</div>		
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.cancel" /></button>
			<button class="btn btn-success btn-sm" onclick="grammarEditor.validateGrammar(); return false;">~Validate</button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~eu.dariah.de.minfba.common.link.save" /></button>
		</div>
	</div>
</sf:form>