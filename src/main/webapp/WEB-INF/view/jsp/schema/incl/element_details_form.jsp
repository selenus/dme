<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="schemaElement" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~Configure schema element</h3>
		<sf:hidden path="id" />
	</div>
	<div class="form-content">
		<div class="control-group">
			<label class="control-label" for="analyzers">~Analyzers</label>
			<div class="controls">
				<sf:select path="analyzers" items="${availableAnalyzers}" id="analyzers" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="processSourceLinks">~Processing options</label>
			<div class="controls">
				<label class="checkbox"> <sf:checkbox path="processSourceLinks" id="processSourceLinks" />
					~process source links
				</label>
				<label class="checkbox"> <sf:checkbox path="processGeoData" id="processGeoData" />
					~process geodata
				</label>
				<label class="checkbox"> <sf:checkbox path="useForTitle" id="useForTitle" />
					~use as title
				</label>
				<label class="checkbox"> <sf:checkbox path="useForTopicModelling" id="useForTopicModelling" />
					~use for topics
				</label>
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