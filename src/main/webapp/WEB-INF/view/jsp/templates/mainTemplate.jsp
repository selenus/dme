<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />

<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
	<head>
		<meta charset="utf-8">
	    <title>DARIAH-DE - <tiles:insertAttribute name="title"/></title>
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <meta name="author" content="Tobias Gradl, University of Bamberg">
	    <meta name="description" content="DARIAH-DE Generic Search">
	    <tiles:useAttribute id="styles" name="styles" classname="java.util.List" />  	
	  	<c:forEach items="${styles}" var="css">
	  		<link rel="stylesheet" href="<s:url value="/resources/css/${css}" />" type="text/css" media="screen, projection" />
	  	</c:forEach>
  		
  		<!-- JavaScript files at the end for faster loading of documents -->
	  	<script type="text/javascript" src="<s:url value="http://maps.google.com/maps/api/js?sensor=false" />"></script>
	  	<tiles:useAttribute id="scripts" name="scripts" classname="java.util.List" />  	
	  	<c:forEach items="${scripts}" var="s">
	  		<script type="text/javascript" src="<s:url value="/resources/js/${s}" />"></script>
	  	</c:forEach>
  		
  		<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
	    <!--[if lt IE 9]>
	      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
	    <![endif]-->
  		<link rel="shortcut icon" type="image/png" href="<s:url value="/resources/img/page_icon.png" />" />
  		
	</head>
	<body>
		<tiles:useAttribute scope="request" id="section" name="section" classname="java.lang.String" />
		<tiles:useAttribute id="showFooter" name="showFooter" classname="java.lang.String" />
		
		<tiles:insertAttribute name="top"/>
		<div id="content_layout" class="container<c:if test="${fluidLayout}">-fluid</c:if>">
			<tiles:insertAttribute name="searchbar"/>
			<tiles:insertAttribute name="content"/>
			<c:if test="${showFooter==true}">
				<tiles:insertAttribute name="footer"/>
			</c:if>
		</div>
		<div id="notes">
			<tiles:insertAttribute name="notes"/>
		</div>	
		
		<noscript>
	        <div><s:message code="~de.dariah.common.view.noscript" /></div>
	    </noscript>

	  	
	</body>
</html>
