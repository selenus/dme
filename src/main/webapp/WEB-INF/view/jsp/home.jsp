<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 main-content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
		    	<li class="active">DARIAH - Schema Registry</li>
		    </ul>
		    <h1>DARIAH - Schema Registry</h1>
			<div class="span5">
			<h3>Content</h3>	
				<ul>
					<li>
						<p>
							<a href="<s:url value='/schema' />"><s:message code="schemaRegistry.title" /></a><br />
							<s:message code="home.schema.count" arguments="${schema_count}" />, 
							<s:message code="home.schema.lastmodified" arguments="${schema_modified}" />
						</p>
					</li>
					<li>
						<p>
							<a href="<s:url value='/mapping' />"><s:message code="crosswalkRegistry.title" /></a><br />
							<s:message code="home.crosswalk.count" arguments="${crosswalk_count}" />, 
							<s:message code="home.crosswalk.lastmodified" arguments="${crosswalk_modified}" />
						</p>
					</li>
				</ul>
				<div style="height: 30px;"></div>
			</div>
			<!-- <div class="span5">
				<%@ include file="_common/incl/user_annotations.jsp" %>
			</div>  -->
		</div>
	</div>
</div>