<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="element" class="form-horizontal">
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.nonterminal.edit" /></h3>	
		<sf:hidden path="id" />
		<sf:hidden path="entityId" />
	</div>
	<div class="form-content">
		<fieldset<c:if test="${readonly}"> disabled</c:if>>
			<div class="form-group">
				<label class="col-sm-3 control-label" for="element_name"><s:message code="~eu.dariah.de.minfba.schereg.model.element.name" />:</label>
				<div class="col-sm-8">
					<sf:input path="name" class="form-control" id="nonterminal_name" />
					<sf:errors path="name" cssClass="error" />
				</div>
			</div>		
			<div class="form-group">
				<div class="col-sm-8 col-sm-offset-3">
					<div class="checkbox">
						<label><sf:checkbox path="transient" /> <s:message code="~eu.dariah.de.minfba.schereg.model.element.transient" /></label>
					</div>
				</div>
			</div>
			<div class="form-group">
				<label class="col-sm-3 control-label" for="element_terminalId"><s:message code="~eu.dariah.de.minfba.schereg.model.element.terminal" />:</label>
				<div class="col-sm-8">
					<s:message code="~eu.dariah.de.minfba.schereg.notification.assign_terminal" var="assign_terminal" />
					<sf:select path="terminalId" class="form-control">
						<sf:option value="" label="${assign_terminal}"  />
						<sf:option value="-1" label="_________________" disabled="true"  />
	   					<sf:options cssClass="schema-terminal" items="${availableTerminals}" />
					</sf:select>
					<sf:errors path="terminalId" cssClass="error" />
					<c:if test="${!readonly}">
						<div class="clearfix tab-buttons">
							<button type="button" onclick="editor.editTerminal(); return false;" class="btn btn-default btn-sm">
								<span class="glyphicon glyphicon-edit"></span> <s:message code="~eu.dariah.de.minfba.common.link.edit" />
							</button>
							<button type="button" onclick="editor.addTerminal(); return false;" class="btn btn-default btn-sm">
								<span class="glyphicon glyphicon-plus"></span> <s:message code="~eu.dariah.de.minfba.common.link.add" />
							</button> 
							<button type="button" onclick="editor.removeTerminal(); return false;" class="btn btn-danger btn-sm">
								<span class="glyphicon glyphicon-trash"></span> <s:message code="~eu.dariah.de.minfba.common.link.delete" />
							</button>
						</div>
					</c:if>
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