<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="terminal" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.terminal.edit" /></h3>	
		<sf:hidden path="id" />
	</div>
	<div class="form-content">
		<fieldset<c:if test="${readonly}"> disabled</c:if>>
			<div class="form-group">
				<label class="col-sm-3 control-label" for="xmlTerminal_namespace"><s:message code="~eu.dariah.de.minfba.schereg.model.element.namespace" />:</label>
				<div class="col-sm-8">
					<sf:select path="namespace" class="form-control" id="xmlTerminal_namespace">
	   					<sf:options items="${availableNamespaces}" />
					</sf:select>
					<sf:errors path="namespace" cssClass="error" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-sm-3 control-label" for="xmlTerminal_name"><s:message code="~eu.dariah.de.minfba.schereg.model.element.name" />:</label>
				<div class="col-sm-8">
					<sf:input path="name" class="form-control" id="xmlTerminal_name" />
					<sf:errors path="name" cssClass="error" />
				</div>
			</div>		
			<div class="form-group">
				<div class="col-sm-8 col-sm-offset-3">
					<div class="checkbox">
						<label><sf:checkbox path="attribute" /> <s:message code="~eu.dariah.de.minfba.schereg.model.element.attribute" /></label>
					</div>
				</div>
			</div>
		</fieldset>
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
</sf:form>