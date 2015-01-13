<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="/mapping/analyze/ajax/delete" var="saveUrl" />
<sf:form method="GET" action="${saveUrl}" modelAttribute="mappingCell" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">Really delete this mapping cell?</h3>
		<sf:hidden path="id" />
		<sf:hidden path="mapping.id" />
	</div>
	<div class="form-content">
		Leave a comment:<br />
		<div>
			<textarea class="input-xlarge" name="comment" rows="4"></textarea>
		</div>
	</div>
	<div class="form-footer">
		<button class="btn cancel form-btn-cancel" type="reset"><i class="icon-ban-circle icon-black"></i><span> <s:message code="crosswalkRegistry.new.cancel" /></span></button>
		<button class="btn btn-primary start form-btn-submit" type="submit"><i class="icon-upload icon-white"></i><span> <s:message code="crosswalkRegistry.new.submit" /></span></button>
	</div>
</sf:form>