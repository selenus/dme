<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<meta http-equiv="refresh" content="5; URL=https://ldap-dariah.esc.rzg.mpg.de/Shibboleth.sso/Login?target=/secure/UserAttributesCompletion.php%3ForiginalURL%3D${reqestedURL}&entityID=${homeOrganisationEntityID}"/>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 content-wrapper no-margin">
		<div id="content" class="primary-area">
			<h1>Insufficient Information</h1>
			<div class="alert">
		    Your home organisation did not provide sufficient attributes to this service. Therefore, you are now being redirected to the DARIAH central user registry.
		    </div>
			<h3>Details:</h3>
			<p>
				Home Organisation entityID: <b>${homeOrganisationEntityID}</b><br/>
				You were trying to access the following URL: <b>${reqestedURL}</b>
			</p>
			<p style="font-style: italic;">After Registration, you will be able to access your resources provided you are authorized.</p>
		</div>
	</div>
</div>