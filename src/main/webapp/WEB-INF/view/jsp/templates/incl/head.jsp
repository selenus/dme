<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<head>
	<meta charset="utf-8">
    <title><tiles:insertAttribute name="title"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="author" content="Tobias Gradl, University of Bamberg">
    <meta name="description" content="<tiles:insertAttribute name="title" />">
    <meta name="_csrf" content="${_csrf.token}"/>
    <!-- default header name is X-CSRF-TOKEN -->
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
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