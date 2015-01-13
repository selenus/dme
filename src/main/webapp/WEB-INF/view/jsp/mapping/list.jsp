<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<s:url value="/mapping/new" var="new_url" />
<c:set value="/mapping?" var="view_url_prefix" />
<c:if test="${selectedMapping != null}">
	<c:set value="/mapping?mapping=${selectedMapping.id}&" var="view_url_prefix" />
</c:if>

<s:url value="/schema" var="home_url" />

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 main-content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
		    	<li><a href="${home_url}"><s:message code="schemaRegistry.title" /></a> <span class="divider">›</span></li>
		    	<li class="active"><s:message code="crosswalkRegistry.title" /></li>
		    </ul>
			<a id="btn-new-mapping" class="btn btn-inverse pull-right" href="${new_url}"><i class="icon-plus icon-white"></i> <s:message code="crosswalkRegistry.new" /></a>
		    <h1><s:message code="schemaRegistry.title" /></h1>
			<div id="contentInner">
				<div>	
					<table class="table table-bordered table-striped table-hover model-list model-list-50">
					<thead>
						<tr>
							<th></th>
							<th width="100%"><s:message code="crosswalkRegistry.name" /></th>
							<th><s:message code="crosswalkRegistry.mappingCount" /></th>
							<th><s:message code="crosswalkRegistry.modified" /></th>
							<th></th>
						</tr>
						</thead>
						<tbody>
						<c:forEach items="${mappings}" var="mapping">
							<%@ include file="incl/list_row.jsp" %>
						</c:forEach>
						</tbody>
					</table>
				</div>
				
				<div class="model-details">
					<ul class="nav nav-tabs">
						<c:choose>
							<c:when test="${view == 0}"><li class="active"><a href="<s:url value="${view_url_prefix}v=0" />"><s:message code="~schemaRegistry.attributes" /></a></li></c:when>
							<c:otherwise><li><a href="<s:url value="${view_url_prefix}v=0" />"><s:message code="~schemaRegistry.attributes" /></a></li></c:otherwise>
						</c:choose>
						<c:choose>
							<c:when test="${view == 1}"><li class="active"><a href="<s:url value="${view_url_prefix}v=1" />"><s:message code="~schemaRegistry.schemas" /></a></li></c:when>
							<c:otherwise><li><a href="<s:url value="${view_url_prefix}v=1" />"><s:message code="~schemaRegistry.schemas" /></a></li></c:otherwise>
						</c:choose>
						<c:choose>
							<c:when test="${view >= 2}"><li class="active"><a href="<s:url value="${view_url_prefix}v=2" />"><s:message code="~schemaRegistry.collections" /></a></li></c:when>
							<c:otherwise><li><a href="<s:url value="${view_url_prefix}v=2" />"><s:message code="~schemaRegistry.collections" /></a></li></c:otherwise>
						</c:choose>
					</ul>
					<div class="model-details-container">
						<c:choose>
							<c:when test="${selectedMapping != null}">
								<c:choose>
									<c:when test="${view == 0}"><%@ include file="incl/list_detail_view_0.jsp" %></c:when>
									<c:when test="${view == 1}"><%@ include file="incl/list_detail_view_1.jsp" %></c:when>
									<c:otherwise><%@ include file="incl/list_detail_view_2.jsp" %></c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>Please select a crosswalk in the list on the left!</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>