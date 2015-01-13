<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="util" uri="/WEB-INF/taglib/util.tld" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<s:url value="/schema" var="home_url" />
<s:url value="/mapping" var="mapping_url" />

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 main-content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
		    	<li><a href="${home_url}">Schema Registry</a> <span class="divider">›</span></li>
		    	<li><a href="${mapping_url}"><s:message code="crosswalkRegistry.title" /></a> <span class="divider">›</span></li>
		    	<li class="active">${util:limitStringSize(mapping.source.name, 25)} ›› ${util:limitStringSize(mapping.target.name, 25)}</li>
		    </ul>
			<div>
				<h1 class="pull-left"><s:message code="~crosswalkRegistry.vis.mapping.title" /></h1>
				<div style="line-height: 40px; margin: 0 10px;" class="pull-left">
					<a href="#" onclick="annotationViewer.getByAggregatorObject('${util:getType(mapping)}', '${mapping.id}'); return false;"><i class="icon-comment icon-black"></i></a>
				</div>
				<div style="line-height: 40px; margin: 20px 0px 0px 0px; font-style: italic; font-size: 85%;" class="pull-right" id="save-notice-area"></div>
			</div>
			<div id="contentInner" style="clear: both;" >
				<div class="navbar">
				    <div class="navbar-inner">
					    <div class="pull-left">
						    <ul class="nav">
						    	<li class="dropdown">
			                        <a data-toggle="dropdown" style="min-width: 150px; text-align: right;" class="brand dropdown-toggle" href="#">${util:limitStringSize(mapping.source.name, 25)} <b class="caret"></b></a>
			                        <ul class="dropdown-menu">
			                          <li><a href="#" onclick="mappingUI.sourceSchema.performAction('expandAll'); return false;"><i class="icon-resize-full icon-black"></i> Expand all</a></li>
			                          <li><a href="#" onclick="mappingUI.sourceSchema.performAction('collapseAll'); return false;"><i class="icon-resize-small icon-black"></i> Collapse all</a></li>
			                          <li><a href="#" onclick="mappingUI.sourceSchema.performAction('resetView'); return false;"><i class="icon-retweet icon-black"></i> Reset view</a></li>
			                    	</ul>
								</li>
								<li class="divider-vertical"></li>
						    </ul>
					    </div>
					    <div class="pull-right">
						    <ul class="nav">
						    	<li class="divider-vertical"></li>
						    	<li class="dropdown">
			                        <a data-toggle="dropdown" style="min-width: 150px;" class="brand dropdown-toggle" href="#"><b class="caret"></b> ${util:limitStringSize(mapping.target.name, 25)}</a>
			                        <ul class="dropdown-menu">
			                          <li><a href="#" onclick="mappingUI.targetSchema.performAction('expandAll'); return false;"><i class="icon-resize-full icon-black"></i> Expand all</a></li>
			                          <li><a href="#" onclick="mappingUI.targetSchema.performAction('collapseAll'); return false;"><i class="icon-resize-small icon-black"></i> Collapse all</a></li>
			                          <li><a href="#" onclick="mappingUI.targetSchema.performAction('resetView'); return false;"><i class="icon-retweet icon-black"></i> Reset view</a></li>
			                    	</ul>
								</li>
						    </ul>
					    </div>
					    <div id="mapping-dynamic-button-area" style="width: 300px; margin-left: auto; margin-right: auto;"></div>
				    </div>
			    </div>
				<div id="mapping_canvas_container">
					<input id="mapping_id" type="hidden" value="${mapping.id}" />
					<canvas style="position: relative;" id="canvas" width="800" height="600" tabindex="0"></canvas>	
				</div>
			</div>
		</div>
	</div>
</div>