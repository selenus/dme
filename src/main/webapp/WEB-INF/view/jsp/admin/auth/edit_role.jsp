<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<s:url value="/admin/auth" var="back_url" />
<s:url value="/admin/auth/save" var="save_url" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
			   	<li><a href="<s:url value="/" />">Start</a> <span class="divider">/</span></li>
			   	<li><a href="${back_url}">Authorization Management</a> <span class="divider">/</span></li>
			   	<li class="active">Edit Role Assignment</li>
			</ul>
			<h1>Edit group assignment</h1>
			<sf:form method="POST" action="${save_url}" modelAttribute="roleMapping" class="form-horizontal" >
				<p>
					<a class="btn" href="${back_url}">Cancel</a>
					<button class="btn btn-primary" type="submit">Save</button>
				</p>
				<div id="contentInner">
					<div class="form-header">
						<h3 id="myModalLabel">Modal header</h3>
					</div>
					<div class="form-content">
						<sf:hidden path="id"/>
						<div class="control-group">
							<label class="control-label" for="mapping_endpoint">Endpoint</label>
							<div class="controls">
								<sf:select class="input-xxlarge" path="endpoint" items="${knownEndpoints}" multiple="false" id="mapping_endpoint" />
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="mapping_name">Name</label>
							<div class="controls">
								<sf:input class="input-xxlarge" id="mapping_name" path="name" />
							</div>
						</div>
						<legend>Assignment options</legend>
						<div class="control-group">
							<label class="control-label" for="mapping_role">Assigned role: </label>
							<div class="controls">
								<sf:select class="input-xxlarge" path="roleId" itemValue="id" items="${roles}" multiple="false" id="mapping_role" /><br/>
								<label for="mapping_isActive" class="checkbox"><sf:checkbox id="mapping_isActive" path="active" /> Active</label>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="mapping_expires">Expiration date: </label>
							<div class="controls">
								<sf:input class="input-large" id="mapping_expires" path="expires" />
								<span class="help-inline">Format: ${formattedDate}</span>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="mapping_description">Description: </label>
							<div class="controls">
								<sf:textarea rows="3" class="input-xxlarge" id="mapping_description" path="description" />
							</div>
						</div>
					</div>
				</div>
			</sf:form>
		</div>
	</div>
</div>