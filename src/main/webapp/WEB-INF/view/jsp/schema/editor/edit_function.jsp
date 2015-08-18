<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="function">
	<div class="form-header">
		<h3 id="form-header-title">~Function editor <small><span class="glyphicon glyphicon-info-sign help-sign" onclick="functionEditor.showHelp(); return false;" aria-hidden="true"></span></small></h3>	
		<sf:hidden path="id" />
		<sf:hidden path="error" />
		<input type="hidden" id="grammar_id" name="grammar_id" value="${grammar.id}" />
		<input type="hidden" id="grammar_name" name="grammar_name" value="${grammar.grammarName}" />
		<input type="hidden" id="grammar_error" name="grammar_error" value="${grammar.error}" />
	</div>
	<div class="form-content row" style="padding-bottom: 0px;">
		<div class="col-md-7" style="border-right: 1px solid #E5E5E5;">
			<div class="legend"><strong>1</strong> ~Edit function</div>
			<div class="form-group row">
				<div class="col-sm-8">
					<label class="control-label" for="function_name">~Function name:</label>
					<div>
						<sf:input path="name" class="form-control" id="function_name" />
						<sf:errors path="name" cssClass="error" />
					</div>
				</div>
				<div class="col-sm-4">
					<label class="control-label" for="base_method">~Language version:</label>
					<div>
						<select class="form-control"><option>v1.2</option></select>
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="control-label">~Function state:</label>
				<span id="function_state"><span class="glyphicon glyphicon-ok-sign" aria-hidden="true"></span> ~ok</span>
				<button class="btn btn-info btn-sm pull-right" onclick="functionEditor.processFunction(); return false;"><span class="glyphicon glyphicon-cog" aria-hidden="true"></span> ~Validate</button>
			</div>
			<div class="form-group">
				<label class="control-label">~Evaluated grammar:</label>
				<span>${grammar.grammarName}</span> (<span id="grammar_state">
					<c:choose>
						<c:when test="${grammar.passthrough}">
							<span class="glyphicon glyphicon-forward" aria-hidden="true"></span> ~passthrough
						</c:when>
						<c:when test="${grammar.error}">
							<span class="glyphicon glyphicon-exclamation-sign glyphicon-color-danger" aria-hidden="true"></span> ~error
						</c:when>
						<c:otherwise>
							<span class="glyphicon glyphicon-ok-sign" aria-hidden="true"></span> ~ok
						</c:otherwise>
					</c:choose>
				</span>)
			</div>
			<div class="form-group">
				<label class="control-label" for="function_function">~Transformation function:</label>
				<div>
					<sf:textarea path="function" rows="12" class="form-control codearea" id="function_function" />
					<sf:errors path="function" cssClass="error" />
				</div>
			</div>		
			<div class="form-footer">
				<div class="controls">
					<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.cancel" /></button>
					<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~eu.dariah.de.minfba.common.link.save" /></button>
				</div>
			</div>
		</div>
		<div class="col-md-5" style="border-left: 1px solid #E5E5E5; margin-left: -1px;">
			<div class="legend"><strong>2</strong> ~Perform sample transformation</div>
			<div>
				<div class="form-group">
					<label class="control-label" for="function-sample-input">~Sample input:</label>
					<div>
						<textarea id="function-sample-input" rows="6" class="form-control codearea">LATITUDE: -70.650000 * LONGITUDE: -8.250000 * DATE/TIME START: 1993-12-01 * DATE/TIME END: 1993-12-31 * MINIMUM ELEVATION: 42.0 m * MAXIMUM ELEVATION: 42.0 m</textarea>
					</div>
				</div>
				<div class="clearfix">
					<c:choose>
						<c:when test="${grammar.error}">
							<button class="btn btn-warning disabled btn-sm pull-right"><span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> ~Process input</button>
						</c:when>
						<c:otherwise>
							<button class="btn btn-info btn-sm pull-right" onclick="functionEditor.performTransformation(); return false;"><span class="glyphicon glyphicon-play" aria-hidden="true"></span> ~Process input</button>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="legend"><strong>3</strong> Transformation result</div>
			<div id="transformation-result-container">
				<c:choose>
					<c:when test="${grammar.error}">
						<div class="alert alert-warning">~Sample transformation not possible due to errors in base grammar</div>
					</c:when>
					<c:otherwise>
						<div class="alert alert-info">~Provide a sample above and select 'Process input' to test the transformation function</div>
					</c:otherwise>
				</c:choose>
			</div>
		</div>	
	</div>
</sf:form>