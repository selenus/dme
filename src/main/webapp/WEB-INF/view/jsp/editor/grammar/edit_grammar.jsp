<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="grammar">
	<div class="form-header">
		<h3 class="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.grammar.edit" /> <small><span class="glyphicon glyphicon-info-sign help-sign" onclick="grammarEditor.showHelp(); return false;" aria-hidden="true"></span></small></h3>	
		<sf:hidden path="id" />
		<sf:hidden path="passthrough" />
		<sf:hidden path="error" />
		<sf:hidden path="entityId" />
	</div>
	<div class="form-content row" style="padding-bottom: 0px;">
		<div class="col-md-7" style="border-right: 1px solid #E5E5E5;">
			<div class="legend"><strong>1</strong> <s:message code="~de.unibamberg.minf.dme.form.grammar.legend.edit_function" /></div>
			<div class="form-group row">
				<div class="col-sm-6">
					<fieldset<c:if test="${readonly}"> disabled</c:if>>
						<label class="control-label" for="name"><s:message code="~de.unibamberg.minf.dme.model.grammar.name" />:</label>
						<div>
							<sf:input path="name" class="form-control" />
							<sf:errors path="name" cssClass="error" />
						</div>
					</fieldset>
				</div>
				<div class="col-sm-6">
					<label class="control-label" for="base_method"><s:message code="~de.unibamberg.minf.dme.model.grammar.base_rule" />:</label>
					<div>
						<sf:input path="baseMethod" class="form-control" />
						<sf:errors path="baseMethod" cssClass="error" />
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label" for="grammarContainer_lexerGrammar"><s:message code="~de.unibamberg.minf.dme.model.grammar.grammar_layout" />:</label>
				<div>
					<div class="radio">
	  					<label>
	    					<input type="radio" name="lexer-parser-option" class="lexer-parser-option lexer-parser-option-combined" value="combined" checked>
	    					<s:message code="~de.unibamberg.minf.dme.form.grammar.hint.combined_layout" />
	  					</label>
					</div>
					<div class="radio">
	  					<label>
	    					<input type="radio" name="lexer-parser-option" class="lexer-parser-option lexer-parser-option-separate" value="separate">
	    					<s:message code="~de.unibamberg.minf.dme.form.grammar.hint.separate_layout" />
	  					</label>
					</div>
					<div class="radio">
	  					<label>
	    					<input type="radio" name="lexer-parser-option" class="lexer-parser-option lexer-parser-option-passthrough" value="passthrough">
	    					<s:message code="~de.unibamberg.minf.dme.form.grammar.hint.passthrough" />
	  					</label>
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label"><s:message code="~de.unibamberg.minf.dme.model.grammar.state" />:</label>
				<span class="grammar_state"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> <s:message code="~de.unibamberg.minf.common.link.ok" /></span>
				<button class="btn btn-info btn-sm pull-right non-passthrough-only" onclick="grammarEditor.validateGrammar(); return false;"><span class="glyphicon glyphicon-cog" aria-hidden="true"></span> <s:message code="~de.unibamberg.minf.common.link.validate" /></button>
			</div>
			<div class="form-group non-passthrough-only form-group-lexer-grammar">
				<label class="control-label" for="grammarContainer_lexerGrammar"><s:message code="~de.unibamberg.minf.dme.model.grammar.lexer_grammar" />:</label>
				<div>
					<sf:textarea path="grammarContainer.lexerGrammar" rows="8" class="form-control codearea grammarContainer_lexerGrammar" />
					<sf:errors path="grammarContainer.lexerGrammar" cssClass="error" />
				</div>
			</div>		
			<div class="form-group non-passthrough-only">
				<label class="control-label" for="grammarContainer_parserGrammar"><s:message code="~de.unibamberg.minf.dme.model.grammar.parser_grammar" />:</label>
				<div>
					<sf:textarea path="grammarContainer.parserGrammar" rows="8" class="form-control codearea grammarContainer_parserGrammar" />
					<sf:errors path="grammarContainer.parserGrammar" cssClass="error" />
				</div>
			</div>	
			<div class="form-footer">
				<div class="controls">
					<c:choose>
						<c:when test="${readonly}">
							<button class="btn btn-primary btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.close" /></button>
						</c:when>
						<c:otherwise>
							<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
							<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~de.unibamberg.minf.common.link.save" /></button>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</div>
		<div class="col-md-5" style="border-left: 1px solid #E5E5E5; margin-left: -1px;">
			<div class="legend"><strong>2</strong> <s:message code="~de.unibamberg.minf.dme.editor.sample.execute" /></div>
			<div class="non-passthrough-only">
				<div class="form-group">
					<label class="control-label" for="grammar-sample-input"><s:message code="~de.unibamberg.minf.dme.editor.sample.input" />:</label>
					<div>
						<textarea rows="6" class="grammar-sample-input form-control codearea">${elementSample}</textarea>
					</div>
				</div>
				<div class="clearfix">
					<button class="btn-parse-sample btn btn-warning btn-sm pull-right disabled" onclick="grammarEditor.parseSample(); return false;"><span class="glyphicon glyphicon-play" aria-hidden="true"></span> <s:message code="~de.unibamberg.minf.dme.editor.sample.process_input" /></button>
				</div>
			</div>
			<div class="passthrough-only">
			</div>
			<div class="legend"><strong>3</strong> <s:message code="~de.unibamberg.minf.dme.editor.sample.transformation_result" /></div>
			<div class="non-passthrough-only">
				<div class="grammar-parse-alerts">
					<div class="alert alert-sm alert-info"><s:message code="~de.unibamberg.minf.dme.editor.sample.notice.hint_sample" /></div>
				</div>
				<div class="grammar-sample-svg-embedded outer-svg-container hide">
					<div class="inner-svg-container"></div>
					<div class="svg-button-container">
						<button class="btn btn-link btn-sm btn-svg-zoomin"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
						<button class="btn btn-link btn-sm btn-svg-zoomout"><span class="glyphicon glyphicon-minus" aria-hidden="true"></span></button>
						<button class="btn btn-link btn-sm btn-svg-reset"><span class="glyphicon glyphicon-repeat" aria-hidden="true"></span></button>
						<button class="btn btn-link btn-sm btn-svg-newwindow"><span class="glyphicon glyphicon-new-window" aria-hidden="true"></span></button>
					</div>
				</div>
			</div>
			<div class="passthrough-only">
				<s:message code="~de.unibamberg.minf.dme.form.grammar.hint.passthrough" />
			</div>	
		</div>
	</div>
</sf:form>