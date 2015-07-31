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
					<div class="col-xs-12">
						<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.view.editor.title" /> <small>${schema.label}</small>&nbsp;</h2>		
					</div>
					<div class="col-xs-12">
						<div id="schema-editor-layout-container" class="hide">
							<div class="ui-layout-center">
								<div class="ui-pane-title">
									<h4>~Element structure</h4>
								</div>
								
								<div class="schema-editor-buttons clearfix">
									<button type="button" onclick="schemaEditor.schema.performAction('expandAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-full"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.expand_all" /></button>
					      			<button type="button" onclick="schemaEditor.schema.performAction('collapseAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-small"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.collapse_all" /></button>
					      			<button type="button" onclick="schemaEditor.schema.performAction('resetView'); schemaEditor.reload(); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-refresh"></span> <s:message code="~eu.dariah.de.minfba.common.link.reload" /></button>		
								</div>
								<div id="schema-editor-container">
									<canvas id="schema-editor-canvas"></canvas>
								</div>
							</div>
							
							<div class="ui-layout-east">
								<div class="ui-pane-title">
									<h4>~Details and Actions</h4>
								</div>
								<div id="schema-context-container">
									<h5>~Schema actions</h5>
									<div id="schema-context-buttons" class="ui-pane-subcontainer">
										<button type="button" onclick="schemaEditor.triggerUploadFile('${schema.id}'); return false;" class="btn btn-default btn-sm"><span class='glyphicon glyphicon-cloud-upload'></span> <s:message code="~eu.dariah.de.minfba.schereg.button.import" /></button>
										<button type="button" onclick="return false;" class="btn btn-danger btn-sm"><span class='glyphicon glyphicon-trash'></span> </button>
									</div>
									<h5>~Schema activity</h5>
									<div class="ui-pane-subcontainer">...</div>
								</div>
								<div id="schema-element-context-container" class="hide">
									<h5>~Actions</h5>
									<div id="schema-element-context-buttons" class="ui-pane-subcontainer"></div>
									<h5>~Details</h5>
									<div id="schema-element-context-info" class="clearfix ui-pane-subcontainer"></div>
									<h5>~Activity</h5>
									<div>...</div>
								</div>
							</div>
							
							<div class="ui-layout-west">
								<div class="ui-pane-title">
									<h4>Sample input</h4>
								</div>
								<div class="alert alert-info alert-sm">
									~Provide any exemplary content here in order to perform live schema transformation on that data
								</div>
								<textarea id="schema-sample-textarea" class="form-control" placeholder="~ Enter sample data" rows="3"></textarea>
								
								<button type="button" onclick="return false;" class="btn btn-info btn-sm pull-right"><span class="glyphicon glyphicon-resize-full"></span> ~Apply</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>