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
				<li><a href='<s:url value="/" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.title" /></a></li>
				<li><a href='<s:url value="/schema" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_short" /></a></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_editor" /></li>
			</ul>
			<input type="hidden" id="schema-id" value="${schema.id}" />
			<div id="main-content">
				<div class="row">
					<div class="col-md-6 col-lg-7">
						<div class="clearfix">
							<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_editor" /> <small>${schema.label}</small>&nbsp;</h2>		
							<div class="pull-left schema-editor-buttons">
								<button type="button" onclick="schemaEditor.schema.performAction('expandAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-full"></span> <s:message code="~eu.dariah.de.minfba.schereg.schemas.button.expand_all" /></button>
				      			<button type="button" onclick="schemaEditor.schema.performAction('collapseAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-small"></span> <s:message code="~eu.dariah.de.minfba.schereg.schemas.button.collapse_all" /></button>
				      			<button type="button" onclick="schemaEditor.triggerUploadFile('${schema.id}'); return false;" class="btn btn-default btn-sm pull-left" ><span class='glyphicon glyphicon-edit'></span> <s:message code="~eu.dariah.de.minfba.schereg.schemas.button.import" /></button>
							</div>
						</div>
						<div class="clearfix">
							<div id="schema-editor-container">
								<canvas id="schema-editor-canvas"></canvas>
							</div>
						</div>
					</div>
					<div class="col-md-6 col-lg-5 details-container schema-editor-context" role="tabpanel">
						<ul class="nav nav-tabs" role="tablist">
							<li role="presentation" id="tab-element-activity" class="active"><a href="#element-activity" aria-controls="element-activity" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.common.activity" /></a></li>
							<li role="presentation" id="tab-element-metadata" class="hide"><a href="#element-metadata" aria-controls="element-metadata" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.schemas.model.schema" /></a></li>
						</ul>
						<div class="tab-content">
							<div role="tabpanel" class="tab-pane active" id="element-activity">
								...
							</div>
							<div role="tabpanel" class="tab-pane" id="element-metadata">
								<div id="schema-editor-dynamic-buttons" class="pull-right"></div>
								<div id="schema-editor-context-info" class="clearfix"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>