<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<tiles:importAttribute name="fluidLayout" />
<div class="jumbotron">
	 <div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
		<div class="row">
			<div class="xs-hidden sm-visible col-sm-3 col-lg-2 col-sm-offset-1">
      			<div class="pull-right dariah-flower-white-83">DARIAHSP Test App</div>
			</div>
		</div>
	</div>
</div>

<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li class="active">Logout</li>
			</ul>
			<div id="main-content">
				<h2>Logout</h2>
				<p>
					You have been logged out from the system. Close browser to complete
				</p>
			</div>
		</div>
	</div>
</div>

