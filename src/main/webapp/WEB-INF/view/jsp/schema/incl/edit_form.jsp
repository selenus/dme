<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="schema" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="schemaRegistry.new" /></h3>
		<sf:hidden path="id" />
		<sf:hidden path="prevSource" />
	</div>
	<div class="form-content">
		<div class="control-group">
			<label class="control-label" for="schema_name"><s:message code="schemaRegistry.name" /></label>
			<div class="controls">
				<sf:input path="name" size="20" id="schema_name" />
				<sf:errors path="name" cssClass="error" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="schema_type"><s:message code="schemaRegistry.type" /></label>
			<div class="controls">
				<sf:select path="type" items="${schemaTypes}" id="schema_type" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="schema_source"><s:message code="schemaRegistry.source" /></label>
			<div class="controls">
				<sf:hidden path="file.id" />
				<input id="schema_source" type="file" name="file" />
				<sf:errors path="source" cssClass="error" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="schema_description"><s:message code="schemaRegistry.description" /></label>
			<div class="controls">
				<sf:textarea path="description" id="schema_description" />
			</div>
		</div>
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn cancel form-btn-cancel" type="reset"><i class="icon-ban-circle icon-black"></i><span> <s:message code="schemaRegistry.new.cancel" /></span></button>
			<button class="btn btn-primary start form-btn-submit" type="submit"><i class="icon-upload icon-white"></i><span> <s:message code="schemaRegistry.new.submit" /></span></button>
		</div>
	</div>
</sf:form>
