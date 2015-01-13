<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="mapping" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="crosswalkRegistry.new" /></h3>
		<sf:hidden path="id" />
		<sf:hidden path="project.id" />
	</div>
	<div class="form-content">
		<!-- <div class="control-group">
			<label class="control-label" for="source_schema"><s:message code="crosswalkRegistry.edit.options" /></label>
			<div class="controls">
				<label class="checkbox"> <sf:checkbox path="performMatching" id="performMatching" />
					<s:message code="crosswalkRegistry.performMatching" />
				</label>
				<p class="error">Experimental: Only edit distance matcher executed; inaccurate results generated</p>
			</div>
		</div> -->
		<div class="control-group">
			<label class="control-label" for="mapping_source"><s:message code="crosswalkRegistry.source" /></label>
			<div class="controls">
				<sf:select path="source.id" items="${schemaList}" itemValue="id" itemLabel="name" id="mapping_source" />
				<sf:errors path="source" cssClass="error" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="mapping_target"><s:message code="crosswalkRegistry.target" /></label>
			<div class="controls">
				<sf:select path="target.id" items="${schemaList}" itemValue="id" itemLabel="name" id="mapping_target" />
				<sf:errors path="target" cssClass="error" />
			</div>
		</div>
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn cancel form-btn-cancel" type="reset"><i class="icon-ban-circle icon-black"></i><span> <s:message code="crosswalkRegistry.new.cancel" /></span></button>
			<button class="btn btn-primary start form-btn-submit" type="submit"><i class="icon-upload icon-white"></i><span> <s:message code="crosswalkRegistry.new.submit" /></span></button>
		</div>
	</div>
</sf:form>