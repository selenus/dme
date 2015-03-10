<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="date" class="java.util.Date" />

<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
	<head>
		<meta charset="utf-8">
	    <title>DARIAH-DE - Generic Search</title>
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <meta name="author" content="Tobias Gradl, University of Bamberg">
	    <meta name="description" content="DARIAH-DE Generic Search">
	    <tiles:useAttribute id="styles" name="styles" classname="java.util.List" />  	
	  	<c:forEach items="${styles}" var="css">
	  		<link rel="stylesheet" href="<s:url value="/resources/css/${css}" />" type="text/css" media="screen, projection" />
	  	</c:forEach>  		
  		<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
	    <!--[if lt IE 9]>
	      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
	    <![endif]-->
  		<link rel="shortcut icon" type="image/png" href="<s:url value="/resources/img/page_icon.png" />" />
	</head>
	<body>
		<tiles:useAttribute name="showSideNav" classname="java.lang.String" />
		<tiles:useAttribute name="inclOptions" ignore="true" classname="java.lang.String" />
		<tiles:useAttribute name="fluidLayout" classname="java.lang.String" />
		
		<!-- Top Navigation -->
		<c:set var="navbarInverse" target="java.lang.Boolean" value="true"></c:set>
		<%@ include file="incl/topNav.jsp" %>
		<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
			<!-- This is a vertical spacer used for notifications etc. -->
			<div id="notifications-area"></div>
			<div id="primary-content-area" class="row">
				<div class="col-md-3">
					<div class="nav-primary-dariah">
						<a class="logo-dariah" href="http://portal-de.dariah.eu" title="DARIAH-DE Portal">
							<img width="220" height="103" src="<s:url value="/resources/img/dariah-logo.png" />" alt="DARIAH-DE Digital Research Infrastructure for the Arts and Humanities">
						</a>
						<c:if test="${inclOptions!=null}">
							<tiles:insertAttribute name="inclOptions"/>
						</c:if>
		                <c:if test="${showSideNav==true}">
							<%@ include file="incl/sideNav.jsp" %>
						</c:if>
	              	</div>
	      		</div>  
				<div class="col-md-9 main-content-wrapper">
					<tiles:insertAttribute name="content"/>
				</div>
				<%@ include file="incl/footer.jsp" %>
			</div>
		</div>
		<noscript>
	        <div><s:message code="~eu.dariah.de.minfba.geosearch.common.noscript" /></div>
	    </noscript>
	  	<!-- JavaScript files at the end for faster loading of documents -->
	  	<tiles:useAttribute id="scripts" name="scripts" classname="java.util.List" />  	
	  	<c:forEach items="${scripts}" var="s">
	  		<script type="text/javascript" src="<s:url value="/resources/js/${s}" />"></script>
	  	</c:forEach>
	</body>
</html>