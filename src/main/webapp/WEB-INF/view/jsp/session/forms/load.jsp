<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:url value="${actionPath}" var="saveUrl" />

<form method="GET" action="${saveUrl}" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.button.load_session" /></h3>	
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-5 control-label" for="sessionId"><s:message code="~eu.dariah.de.minfba.schereg.model.session.previous_sessions" />: </label>
			<div class="col-sm-7">
				<select id="sessionId" name="sessionId" class="form-control">
					<c:if test="${fn:length(savedSessions)>0}">
						<optgroup label="<s:message code="~eu.dariah.de.minfba.schereg.model.session.persisted_sessions" />">
							<c:forEach items="${savedSessions}" var="s">					
								<option value="${s.id}"<c:if test="${s.id==currentSessionId}"> selected="selected"</c:if>>
									<c:if test="${s.label!=null && s.label!=''}">${s.label} - </c:if>
									<joda:format value="${s.created}" locale="${locale}" style="LM" />
								</option>
							</c:forEach>
						</optgroup>
					</c:if>
					<c:if test="${fn:length(transientSessions)>0}">
						<optgroup label="<s:message code="~eu.dariah.de.minfba.schereg.model.session.temporary_sessions" />">
							<c:forEach items="${transientSessions}" var="s">					
								<option value="${s.id}"<c:if test="${s.id==currentSessionId}"> selected="selected"</c:if>>
									<joda:format value="${s.created}" locale="${locale}" style="LM" />
								</option>
							</c:forEach>
						</optgroup>
					</c:if>
				</select>
			</div>
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.cancel" /></button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~eu.dariah.de.minfba.common.link.load" /></button>
		</div>
	</div>
</form>