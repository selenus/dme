<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
		   		<li><a href="<s:url value='/' />">Start</a> <span class="divider">›</span></li>
		    	<li class="active">User Profile</li>
		    </ul>
			<h1>Your Profile</h1>	
			<sf:form class="form-horizontal" style="clear: both;" modelAttribute="user" method="post" commandName="user" action="profile">
				<fieldset>
					<sf:hidden path="id" />
					<legend>Your assigned privileges</legend>
					<div class="control-group">
						<label class="control-label" for="user_roles">Assigned roles: </label>
						<div class="controls">
							<select class="input-xlarge uneditable-input" multiple id="user_roles">
								<c:forEach items="${authorityList}" var="authority">
									<option>${authority.authority}</option>	
								</c:forEach>
							</select>
						</div>
					</div>
					<legend>Information from your identity providers</legend>
					<div class="control-group">
						<label class="control-label" for="user_endpoint">Original identity Provider: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xxlarge uneditable-input" path="endpointId" id="user_endpoint" />
							<sf:errors path="endpointName" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_nameid">Name ID: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xxlarge uneditable-input" path="nameId" id="user_nameid" />
							<sf:errors path="nameId" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_lastname">Common Name (CN): </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xlarge uneditable-input" path="commonName" id="user_lastname" />
							<sf:errors path="commonName" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_lastname">Last Name: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-large uneditable-input" path="lastName" id="user_lastname" />
							<sf:errors path="lastName" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_firstName">First Name: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-large uneditable-input" path="firstName" id="user_firstName" />
							<sf:errors path="firstName" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_email">E-Mail Address: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xxlarge uneditable-input" path="email" id="user_email" />
							<span class="help-inline"><sf:errors path="email" cssClass="error" /></span>
						</div>
					</div>
		
					<div class="control-group">
						<label class="control-label" for="user_eduPersonPrincipalName">eduPersonPrincipalName: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xxlarge uneditable-input" path="eduPersonPrincipalName" id="user_eduPersonPrincipalName" />
							<sf:errors path="eduPersonPrincipalName" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_eduPersonEntitlement">eduPersonEntitlement: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xxlarge uneditable-input" path="eduPersonEntitlement" id="user_eduPersonEntitlement" />
							<sf:errors path="eduPersonEntitlement" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_eduPersonAffiliation">eduPersonAffiliation: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xlarge uneditable-input" path="eduPersonAffiliation" id="user_eduPersonAffiliation" />
							<sf:errors path="eduPersonAffiliation" cssClass="error" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label" for="user_eduPersonScopedAffiliation">eduPersonScopedAffiliation: </label>
						<div class="controls">
							<sf:input disabled="true" class="input-xlarge uneditable-input" path="eduPersonScopedAffiliation" id="user_eduPersonScopedAffiliation" />
							<sf:errors path="eduPersonScopedAffiliation" cssClass="error" />
						</div>
					</div>
				</fieldset>
			</sf:form>
		</div>
	</div>
</div>