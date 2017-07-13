<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>



<div>
	<div class="form-header">
		<h3 class="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.concept.edit" /> <small><span class="glyphicon glyphicon-info-sign help-sign" onclick="grammarEditor.showHelp(); return false;" aria-hidden="true"></span></small></h3>	
		
	</div>
	<div class="form-content row" style="padding-bottom: 0px;">
		<div class="col-md-7" style="border-right: 1px solid #E5E5E5;">
			<div class="legend"><strong>1</strong> <s:message code="~eu.dariah.de.minfba.schereg.form.grammar.legend.edit_concept" /></div>
			<div class="form-group">
				<div class="row">
					<div class="mapped-concept-editor-container col-xs-12">
						<canvas id="mapped-concept-editor"></canvas>
					</div>
				</div>
			</div>	
			<div class="form-footer">
				<div class="controls">
					<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.close" /></button>
				</div>
			</div>
		</div>
		<div class="layout-helper-container col-md-5" style="border-left: 1px solid #E5E5E5; margin-left: -1px;">
			<div class="legend"><strong>2</strong> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.execute" /></div>
			<div class="non-passthrough-only">
			
				<div class="sample-input-container">
					<c:forEach var="sampleInput" items="${sampleInputMap}">
					
						<div class="sample-input form-group">
							<label class="control-label" for="grammar-sample-input"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.input" /> <em>(${sampleInput.key.name}):</em></label>
							<div>
								<input type="hidden" name="elementId" value="${sampleInput.key.id}" />
								<textarea rows="3" class="form-control codearea">${sampleInput.value}</textarea>
							</div>
						</div>
					
					</c:forEach>
				</div>
				
				<div class="clearfix">
					<button class="btn-parse-sample btn btn-warning btn-sm pull-right" onclick="editor.conceptEditor.performTransformation(); return false;"><span class="glyphicon glyphicon-play" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.process_input" /></button>
				</div>
			</div>
			<div class="passthrough-only">
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
</div>