<%@ page isErrorPage="true" language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="/schereg" var="home_url" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 content-wrapper no-margin">
		<div id="contentInner" class="primary-area">
			<ul class="breadcrumb">
		    	<li><a href="${home_url}"><s:message code="schemaRegistry.title" /></a> <span class="divider">›</span></li>
		    	
		    	<li class="active">Error</li>
		    </ul>
			<h1>Sorry...</h1>
			<h3>An error occurred while processing your request.</h3>
		
		    <c:if test="${exception!=null && exception.message!=''}">
			    <div class="alert alert-block alert-error">
			    	<h4>Error Message:</h4>
			    	<p>${exception.message}</p>
			    </div>
			
				<c:if test="${exception.cause!=null && exception.cause.message!=''}">
				    <div class="alert alert-block">
				    	<h4>Inner Message:</h4>
				    	<p>${exception.cause.message}</p>
				    </div>
			    </c:if>
		    </c:if>
		</div>    
	</div>
</div>