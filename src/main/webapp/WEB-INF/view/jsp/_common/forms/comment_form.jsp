<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="/leaveComment" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="annotation" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">Leave comment for latest action?</h3>
		<sf:hidden path="id" />
		<sf:hidden path="annotatedObjectType" />
		<sf:hidden path="annotatedObjectId" />
		<sf:hidden path="aggregatorObjectType" />
		<sf:hidden path="aggregatorObjectId" />
	</div>
	<div class="form-content">
		Your comment:<br />
		<div>
			<sf:textarea class="input-xlarge" path="comment" rows="4"></sf:textarea>
		</div>
	</div>
	<div class="form-footer">
		<button class="btn cancel form-btn-cancel" type="reset"><i class="icon-ban-circle icon-black"></i><span> <s:message code="crosswalkRegistry.new.cancel" /></span></button>
		<button class="btn btn-primary start form-btn-submit" type="submit"><i class="icon-upload icon-white"></i><span> <s:message code="crosswalkRegistry.new.submit" /></span></button>
	</div>
</sf:form>