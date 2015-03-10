<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="date" class="java.util.Date" />

<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
	<head>
		<meta charset="utf-8">
	    <title>DARIAH-DE - Schema Registry</title>
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <meta name="author" content="Tobias Gradl, University of Bamberg">
	    <meta name="description" content="<tiles:insertAttribute name="title" />">
	    <tiles:importAttribute name="styles" />  	
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
        <tiles:importAttribute name="navbarInverse" />
        <tiles:importAttribute name="fluidLayout" />
        
        <!-- Top Navigation -->
        <%@ include file="incl/topNav.jsp" %>
        
        <!-- Notifications -->
		<div id="notifications-area"></div>
		
		<!-- Content -->
		<tiles:insertAttribute name="content"/>
		
		<!-- TODO Replace with actual footer -->
		<div style="height: 50px;">&nbsp;</div>
		
		<noscript>
	        <div>No script</div>
	    </noscript>
	  	<!-- JavaScript files at the end for faster loading of documents -->
	  	<tiles:importAttribute name="scripts" />  	
	  	<c:forEach items="${scripts}" var="s">
	  		<script type="text/javascript" src="<s:url value="/resources/js/${s}" />"></script>
	  	</c:forEach>
	</body>
</html>