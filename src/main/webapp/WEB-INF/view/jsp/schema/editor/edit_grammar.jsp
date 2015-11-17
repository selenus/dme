<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="grammar">
	<div class="form-header">
		<h3 id="form-header-title">~Grammar editor <small><span class="glyphicon glyphicon-info-sign help-sign" onclick="grammarEditor.showHelp(); return false;" aria-hidden="true"></span></small></h3>	
		<sf:hidden path="id" />
		<sf:hidden path="passthrough" />
		<sf:hidden path="error" />
		<sf:hidden path="entityId" />
	</div>
	<div class="form-content row" style="padding-bottom: 0px;">
		<div class="col-md-7" style="border-right: 1px solid #E5E5E5;">
			<div class="legend"><strong>1</strong> Edit grammar</div>
			<div class="form-group row">
				<div class="col-sm-6">
					<fieldset<c:if test="${readonly}"> disabled</c:if>>
						<label class="control-label" for="grammar_name">~Grammar name:</label>
						<div>
							<sf:input path="grammarName" class="form-control" id="grammar_name" />
							<sf:errors path="grammarName" cssClass="error" />
						</div>
					</fieldset>
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
					<div class="radio">
	  					<label>
	    					<input type="radio" name="lexer-parser-option" class="lexer-parser-option" id="lexer-parser-option-passthrough" value="passthrough">
	    					~Passthrough: No grammatical analysis; input is forwarded only
	  					</label>
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label">~Grammar state:</label>
				<span id="grammar_state"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> ~ok</span>
				<button class="btn btn-info btn-sm pull-right non-passthrough-only" onclick="grammarEditor.validateGrammar(); return false;"><span class="glyphicon glyphicon-cog" aria-hidden="true"></span> ~Validate</button>
			</div>
			<div class="form-group non-passthrough-only" id="form-group-lexer-grammar">
				<label class="control-label" for="grammarContainer_lexerGrammar">~Lexer Grammar:</label>
				<div>
					<sf:textarea path="grammarContainer.lexerGrammar" rows="8" class="form-control codearea" id="grammarContainer_lexerGrammar" />
					<sf:errors path="grammarContainer.lexerGrammar" cssClass="error" />
				</div>
			</div>		
			<div class="form-group non-passthrough-only">
				<label id="label-parser-grammar" class="control-label" for="grammarContainer_parserGrammar">~Parser Grammar:</label>
				<div>
					<sf:textarea path="grammarContainer.parserGrammar" rows="8" class="form-control codearea" id="grammarContainer_parserGrammar" />
					<sf:errors path="grammarContainer.parserGrammar" cssClass="error" />
				</div>
			</div>	
			<div class="form-footer">
				<div class="controls">
					<c:choose>
						<c:when test="${readonly}">
							<button class="btn btn-primary btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.close" /></button>
						</c:when>
						<c:otherwise>
							<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.cancel" /></button>
							<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~eu.dariah.de.minfba.common.link.save" /></button>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</div>
		<div class="col-md-5" style="border-left: 1px solid #E5E5E5; margin-left: -1px;">
			<div class="legend"><strong>2</strong> Perform sample parse</div>
			<div class="non-passthrough-only">
				<div class="form-group">
					<label class="control-label" for="grammar-sample-input">~Sample input:</label>
					<div>
						<textarea id="grammar-sample-input" rows="6" class="form-control codearea">${elementSample}</textarea>
					</div>
				</div>
				<div class="clearfix">
					<button id="btn-parse-sample" class="btn btn-warning btn-sm pull-right disabled" onclick="grammarEditor.parseSample(); return false;"><span class="glyphicon glyphicon-play" aria-hidden="true"></span> ~Parse input</button>
				</div>
			</div>
			<div class="passthrough-only">
			</div>
			<div class="legend"><strong>3</strong> Analyze sample results</div>
			<div class="non-passthrough-only">
				<div id="grammar-parse-alerts">
					<div class="alert alert-sm alert-info">~Provide a sample above and select 'Parse input' to verify the input against the grammar</div>
				</div>
				<div id="grammar-sample-svg-embedded" class="outer-svg-container hide">
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
				~Passthrough mode: All input is forwarded as provided
			</div>	
		</div>
	</div>
</sf:form>