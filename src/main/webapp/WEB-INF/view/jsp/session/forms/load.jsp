<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<form method="GET" action="${saveUrl}" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~Load session</h3>	
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-4 control-label" for="sessionId">Previous sessions</label>
			<div class="col-sm-8">
				<select id="sessionId" name="sessionId" class="form-control">
					<c:forEach items="${prevSessions}" var="prevSession">					
						<option value="${prevSession.id}">
							<c:if test="${prevSession.label!=null && prevSession.label!=''}">${prevSession.label} - </c:if>
							<joda:format value="${prevSession.created}" locale="${locale}" style="LM" />
						</option>
					</c:forEach>
				</select>
			</div>
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset">~Cancel</button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit">~Load</button>
		</div>
	</div>
</form>