<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="util" uri="/WEB-INF/taglib/util.tld" %>

<s:url value="/mapping/analyze/ajax/save" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="mappingCell" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">Transformation Rule Editor</h3>
		<sf:hidden path="id" />
		<sf:hidden path="mappingId" />
		<sf:hidden path="output" />
	</div>
	<div class="form-content">
		<legend>Confidence: <span id="mappingCell_score_readable"></span></legend>
		<sf:hidden path="score" id="mappingCell_score" />
		<div style="padding: 10px 10px 20px 10px;">
			<div id="score-slider"></div>
		</div>


		<ul class="nav nav-tabs">
			<li class="active"><a data-toggle="tab" href="#rule">Transformation rule</a></li>
			<li class=""><a data-toggle="tab" href="#general">General attributes</a></li>
		</ul>
		<div class="tab-content">
			<div id="rule" class="tab-pane fade active in">
				<div style="overflow: hidden;">
					<div class="pull-left">
						<select id="proxyMcis" name="proxyMcis" class="input-xlarge" multiple>
							<c:forEach items="${possibleInputs}" var="possibleInput">
								<option <c:if test="${util:contains(currentInputs, possibleInput.id)==true}">selected="selected" </c:if>value="${possibleInput.id}">${possibleInput.name}</option>
							</c:forEach>
						</select>
					</div>
					<div style="margin-top: 20px;" class="pull-right">
						<div>
							<span>&rArr;</span>
							<input class="input-xlarge uneditable-input" disabled value="${output.name}" size="20" id="schema_name" />
						</div>
					</div>
				</div>
			
				<div style="margin-top: 20px;">
					<label for="mapping-cell-formula">Transformation expression:</label>
					<sf:textarea rows="5" style="width: 95%;" class="input-xxlarge" path="function" id="mapping-cell-formula" />
					<sf:errors path="function" cssClass="error" />
				</div>
				
				
			</div>
			<div id="general" class="tab-pane fade">
				<div class="control-group">
					<label class="control-label" for="mapping_cell_name">Name:</label>
					<div class="controls">
						<sf:input path="name" size="20" id="mapping_cell_name" />
						<sf:errors path="name" cssClass="error" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="mapping_cell_name">Created:</label>
					<div class="controls">
						<sf:input class="input-xlarge" path="created" disabled="true" size="20"
							id="mapping_cell_created" />
						<sf:errors path="created" cssClass="error" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="mapping_cell_name">Last change:</label>
					<div class="controls">
						<sf:input class="input-xlarge uneditable-input" path="modified" disabled="true" size="20" id="mapping_cell_modified" />
						<sf:errors path="modified" cssClass="error" />
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn cancel form-btn-cancel" type="reset"><i class="icon-ban-circle icon-black"></i><span> Cancel</span></button>
			<button class="btn btn-primary start form-btn-submit" type="submit"><i class="icon-upload icon-white"></i><span> Submit</span></button>
		</div>
	</div>
</sf:form>




