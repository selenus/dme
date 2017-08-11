<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="function">
	<div class="form-header">
		<h3 class="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.function.edit" /> <small><span class="glyphicon glyphicon-info-sign help-sign" onclick="functionEditor.showHelp(); return false;" aria-hidden="true"></span></small></h3>	
		<sf:hidden path="id" />
		<sf:hidden path="error" />
		<sf:hidden path="entityId" />
		<input type="hidden" class="grammar_id" name="grammar_id" value="${grammar.id}" />
		<input type="hidden" class="grammar_name" name="grammar_name" value="${grammar.name}" />
		<input type="hidden" class="grammar_error" name="grammar_error" value="${grammar.error}" />
	</div>
	<div class="form-content row" style="padding-bottom: 0px;">
		<div class="col-md-7" style="border-right: 1px solid #E5E5E5;">
			<div class="legend"><strong>1</strong> <s:message code="~eu.dariah.de.minfba.schereg.form.function.legend.edit_function" /></div>
			<div class="form-group row">
				<div class="col-sm-8">
					<fieldset<c:if test="${readonly}"> disabled</c:if>>
						<label class="control-label" for="function_name"><s:message code="~eu.dariah.de.minfba.schereg.model.function.name" />:</label>
						<div>
							<sf:input path="name" class="form-control function_name" />
							<sf:errors path="name" cssClass="error" />
						</div>
					</fieldset>
				</div>
				<div class="col-sm-4">
					<label class="control-label" for="base_method"><s:message code="~eu.dariah.de.minfba.schereg.model.function.language_version" />:</label>
					<div>
						<select class="form-control"><option>v1.3</option><option>v1.2</option></select>
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label"><s:message code="~eu.dariah.de.minfba.schereg.model.function.state" />:</label>
				<span class="function_state"><span class="glyphicon glyphicon-ok-sign" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.common.link.ok" /> </span>
				<button class="btn btn-info btn-sm pull-right" onclick="functionEditor.processFunction(); return false;"><span class="glyphicon glyphicon-cog" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.common.link.validate" /></button>
			</div>
			<div class="form-group">
				<label class="control-label"><s:message code="~eu.dariah.de.minfba.schereg.model.function.grammar" />:</label>
				<span>${grammar.name}</span> (<span class="grammar_state">
					<c:choose>
						<c:when test="${grammar.passthrough}">
							<span class="glyphicon glyphicon-forward" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.schereg.model.grammar.passthrough" />
						</c:when>
						<c:when test="${grammar.error}">
							<span class="glyphicon glyphicon-exclamation-sign glyphicon-color-danger" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.common.link.error" />
						</c:when>
						<c:otherwise>
							<span class="glyphicon glyphicon-ok-sign" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.common.link.ok" />
						</c:otherwise>
					</c:choose>
				</span>)
			</div>
			<div class="form-group">
				<div class="row">
					<div class="col-sm-8">
						<label class="control-label" for="function_function"><s:message code="~eu.dariah.de.minfba.schereg.model.function.transformation_function" />:</label>
						<div>
							<sf:textarea path="function" rows="12" class="form-control codearea function_function" />
							<sf:errors path="function" cssClass="error" />
						</div>
					</div>
					<div class="col-sm-4">
						<label class="control-label"><s:message code="~eu.dariah.de.minfba.schereg.notification.transformation.available_inputs" />:</label>
						<div class='available-input-container'>
							<c:if test="${availablePassthroughGrammars!=null && fn:length(availablePassthroughGrammars)>0}">
								<em><s:message code="~eu.dariah.de.minfba.schereg.notification.transformation.available_passthrough_grammars" /></em>
								<ul>
									<c:forEach items="${availablePassthroughGrammars}" var="availablePassthroughGrammar">
										<li>${availablePassthroughGrammar}</li>
									</c:forEach>
								</ul>
							</c:if>
							<c:if test="${availableRules!=null && fn:length(availableRules)>0}">
								<em><s:message code="~eu.dariah.de.minfba.schereg.notification.transformation.available_parser_rules" /></em>
								<ul>
									<c:forEach items="${availableRules}" var="availableRule">
										<li>${availableRule}</li>
									</c:forEach>
								</ul>
							</c:if>
						</div>
					</div>
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
			<div class="legend"><strong>2</strong> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.execute" /></div>
			<div>
				<div class="sample-input-container">
					<c:forEach var="sampleInput" items="${sampleInputMap}">
					
						<div class="sample-input form-group">
							<label class="control-label"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.input" /> <em>(${sampleInput.key.name}):</em></label>
							<div>
								<input type="hidden" name="elementId" value="${sampleInput.key.id}" />
								<textarea rows="3" class="form-control codearea">${sampleInput.value}</textarea>
							</div>
						</div>
					
					</c:forEach>
				</div>
				<div class="clearfix">
					<c:choose>
						<c:when test="${grammar.error}">
							<button class="btn btn-warning disabled btn-sm pull-right"><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.process_input" /></button>
						</c:when>
						<c:otherwise>
							<button class="btn btn-info btn-sm pull-right" onclick="functionEditor.performTransformation(); return false;"><span class="glyphicon glyphicon-play" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.process_input" /></button>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="legend"><strong>3</strong> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.transformation_result" /></div>
			<div class="transformation-result-container">
				<div class="transformation-alerts">
					<c:choose>
						<c:when test="${grammar.error}">
							<div class="alert alert-sm alert-warning"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.error.error_in_grammar" /></div>
						</c:when>
						<c:otherwise>
							<div class="alert alert-sm alert-info"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.notice.hint_sample" /></div>
						</c:otherwise>
					</c:choose>
				</div>
				<div class="no-results-alert alert alert-sm alert-warning hide"><s:message code="~eu.dariah.de.minfba.schereg.notification.transformation.no_results" /></div>
				<pre class="transformation-result hide">
				</pre>
			</div>
		</div>	
	</div>
</sf:form>