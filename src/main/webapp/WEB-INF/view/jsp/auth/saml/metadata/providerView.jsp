<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<c:set value="/auth/saml/admin" var="pathPrefix" />
<c:set value="/auth/saml/metadata" var="ssoMetadataPrefix" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
				<li><a href="<c:url value="/"/>">Start</a> <span class="divider">/</span></li>
			   	<li><a href="<c:url value="${pathPrefix}"/>">Metadata</a> <span class="divider">/</span></li>
			   	<li class="active">Metadata provider detail</li>
			</ul>
			<h1>Metadata provider detail</h1>
			<sf:form commandName="provider" action="removeProvider">
				<fieldset>
					<input type="hidden" name="providerIndex" value="<c:out value="${providerIndex}"/>"/>
				    <ul>
				    	<li><c:out value="${provider}"/></li>
				    </ul>
					<div>
						<button type="submit" class="btn btn-primary">Remove provider</button>
						<a class="btn" href="<c:url value="${pathPrefix}"/>">Cancel</a>
					</div>
				</fieldset>
			</sf:form>
		</div>
	</div>
</div>