<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<tiles:importAttribute name="fluidLayout" />

<div class="jumbotron jumbotron-small">
	 <div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
		<div class="row">
			<div class="xs-hidden sm-visible col-sm-3 col-lg-2 col-sm-offset-1">
				<div class="pull-right dariah-flower-white-45"><s:message code="~eu.dariah.de.minfba.schereg.title" /></div>
			</div>
			<div class="col-sm-6 col-lg-7 col-sm-offset-1">
				<h1><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_short" /></h1>
			</div>
		</div>
	</div>
</div>
<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<!-- Notifications -->
		<div id="notifications-area" class="col-sm-10 col-sm-offset-1"></div>
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li><s:message code="~eu.dariah.de.minfba.schereg.title" /></li>
				<li><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_short" /></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_editor" /></li>
			</ul>
			<input type="hidden" id="schema-id" value="${schema.id}" />
			<div id="main-content">
				<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_editor" /> <small>${schema.label}</small></h2>
				<div class="row">
					<div id="schema-editor" class="col-xs-12" style="height: 600px;">
						<canvas style="position: relative;" id="canvas"></canvas>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>