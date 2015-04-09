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
				<h1><s:message code="~eu.dariah.de.minfba.schereg.view.home.title_short" /></h1>
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
				<li><a href='<s:url value="/schema" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.view.home.title_short" /></a></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.view.editor.title" /></li>
			</ul>
			<input type="hidden" id="schema-id" value="${schema.id}" />
			<div id="main-content">
				<div class="row">
					<div class="col-md-6">
						<div class="clearfix">
							<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.view.editor.title" /> <small>${schema.label}</small>&nbsp;</h2>		
							<div class="pull-left schema-editor-buttons">
								<button type="button" onclick="schemaEditor.schema.performAction('expandAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-full"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.expand_all" /></button>
				      			<button type="button" onclick="schemaEditor.schema.performAction('collapseAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-small"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.collapse_all" /></button>

								<div class="btn-group pull-left">
									<button type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
										<s:message code="~eu.dariah.de.minfba.common.link.actions" /> <span class="caret"></span>
									</button>
									<ul id="schema-editor-dynamic-buttons-0" class="dropdown-menu" role="menu">
										<li><a href="#" onclick="schemaEditor.triggerUploadFile('${schema.id}'); return false;"><span class='glyphicon glyphicon-cloud-upload'></span> <s:message code="~eu.dariah.de.minfba.schereg.button.import" /></a></li>
										<li><a href="#" onclick="schemaEditor.reload(); return false;"><span class='glyphicon glyphicon-refresh'></span> <s:message code="~eu.dariah.de.minfba.common.link.reload" /></a></li>
									</ul>
								</div>    			
				      			
							</div>
						</div>
						<div class="clearfix">
							<div id="schema-editor-container">
								<canvas id="schema-editor-canvas"></canvas>
							</div>
						</div>
					</div>
					<div class="col-md-6 details-container schema-editor-context" role="tabpanel">
						<ul class="nav nav-tabs" role="tablist">
							<li role="presentation" id="tab-element-activity" class="active"><a href="#element-activity" aria-controls="element-activity" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.common.link.activity" /></a></li>
							<li role="presentation" id="tab-element-metadata" class="hide"><a href="#element-metadata" aria-controls="element-metadata" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.model.element.element" /></a></li>
						</ul>
						<div class="tab-content">
							<div role="tabpanel" class="tab-pane active" id="element-activity">
								...
							</div>
							<div role="tabpanel" class="tab-pane" id="element-metadata">
								<div class="row">
									<div id="schema-editor-dynamic-buttons-1" class="col-xs-9 col-md-8 col-xs-offset-3 col-md-offset-4 tab-buttons"></div>
								</div>
								<div id="schema-editor-context-info" class="clearfix"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>