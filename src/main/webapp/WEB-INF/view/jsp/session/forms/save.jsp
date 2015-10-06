<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="GET" action="${saveUrl}" model-attribute="session" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~Save session</h3>
		<sf:hidden path="id"/>	
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-4 control-label" for="sessionId">~Label</label>
			<div class="col-sm-8">
				<sf:input path="label"/>
			</div>
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset">~Cancel</button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit">~Save</button>
		</div>
	</div>
</sf:form>