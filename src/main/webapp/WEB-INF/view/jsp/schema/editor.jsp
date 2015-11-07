<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<tiles:importAttribute name="fluidLayout" />

<div class="jumbotron jumbotron-small">
	 <div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
		<div class="row">
			<!-- Notifications -->
			<div id="notifications-area" class="col-sm-10 col-sm-offset-1"></div>
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
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li><a href='<s:url value="/" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.title" /></a></li>
				<li><a href='<s:url value="/schema" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.view.home.title_short" /></a></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.view.editor.title" /></li>
			</ul>
			<input type="hidden" id="schema-id" value="${schema.id}" />
			<input type="hidden" id="schema-write" value="${schema.write}" />
			<input type="hidden" id="schema-own" value="${schema.own}" />
			<div id="main-content">
				<div class="row">
					<div class="col-xs-12">
						<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.view.editor.title" /> 
						<small>${schema.pojo.label}
							<c:if test="${!schema.own && !schema.write}"> <span class="glyphicon glyphicon-lock"></span></c:if>
						</small>&nbsp;</h2>		
					</div>
					<div class="col-xs-12">
						<div class="hide editor-layout-container">
						
							<!-- South: Logging pane -->
							<div class="layout-south layout-pane">
								<ul id="schema-editor-log" class="log"></ul>
							</div>
							
							<!-- West: Sample transformation -->
							<div id="schema-editor-sample-pane" class="layout-west layout-pane">
								<c:set var="currentSampleCount" value="${session.sampleOutput==null ? 0 : fn:length(session.sampleOutput)}"/>
								<input type="hidden" id="currentSampleCount" value="${currentSampleCount}">
								<input type="hidden" id="currentSampleIndex" value="${session.selectedOutputIndex==null ? 0 : session.selectedOutputIndex}">
								
								<div class="ui-pane-title">
									<h4><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.title" /></h4>
								</div>
								<h5>~Sessions</h5>									
								<div class="ui-pane-subcontainer button-bar">
									<button type="button" onclick="sessions.saveSession(schemaEditor.schemaId);" class="btn btn-default btn-sm">~ Save session</button>
									<button type="button" onclick="sessions.loadSession(schemaEditor.schemaId);" class="btn btn-default btn-sm">~ Load session</button>
									<button type="button" onclick="schemaEditor.sample_resetSession(); return false;" class="btn btn-default btn-sm"><s:message code="~eu.dariah.de.minfba.common.link.reset" /></button>
								</div>
								<div id="schema-editor-sample-container">
									<h5>~Sample</h5>
									<button type="button" onclick="schemaEditor.sample_applyAndExecute(); return false;" class="pull-right btn btn-primary btn-sm"><s:message code="~eu.dariah.de.minfba.schereg.editor.actions.execute" /></button>
									<ul class="nav nav-tabs" role="tablist">
										<li role="presentation"<c:if test="${currentSampleCount==0}"> class="active"</c:if>>
											<a href="#schema-sample-input-container" aria-controls="schema-sample-input-container" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.input" /></a>
										</li>
										<c:choose>
											<c:when test="${currentSampleCount>0}">
												<li role="presentation" class="active"><a href="#schema-sample-output-container" aria-controls="schema-sample-output-container" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.output" /><span class="badge-c"> <span class="badge">${currentSampleCount}</span></span></a></li>
											</c:when>
											<c:otherwise>
												<li role="presentation" class="disabled"><a href="#schema-sample-output-container" aria-controls="schema-sample-output-container" role="tab"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.output" /></a></li>
											</c:otherwise>
										</c:choose>
									</ul>
									<div class="tab-content">
										<div role="tabpanel" class="tab-pane <c:if test="${currentSampleCount==0}"> active</c:if>" id="schema-sample-input-container">
											<c:choose>
												<c:when test="${session.sampleInput!=null && session.sampleInput!=''}">
													<input type="hidden" id="sample-set" value="true">
													<textarea id="schema-sample-textarea" class="form-control height-sized-element" placeholder="<s:message code="~eu.dariah.de.minfba.schereg.editor.sample.placeholder_set" />" rows="3"></textarea>
												</c:when>
												<c:otherwise>
													<input type="hidden" id="sample-set" value="false">
													<textarea id="schema-sample-textarea" class="form-control height-sized-element" placeholder="<s:message code="~eu.dariah.de.minfba.schereg.editor.sample.placeholder" />" rows="3"></textarea>
												</c:otherwise>
											</c:choose>
										</div>
										<div role="tabpanel" class="tab-pane <c:if test="${currentSampleCount>0}"> active</c:if>" id="schema-sample-output-container">
											<div class="button-bar">
												<span class="schema-sample-output-counter"><c:if test="${currentSampleCount>0}">${session.selectedOutputIndex} / ${currentSampleCount}</c:if></span>
												<button id="btn-sample-prev-resource" type="button" onclick="schemaEditor.sample_getPrevResource(); return false;" class="btn btn-default btn-sm<c:if test="${currentSampleCount>0}"> disabled</c:if>"><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span></button>
												<button id="btn-sample-next-resource" type="button" onclick="schemaEditor.sample_getNextResource(); return false;" class="btn btn-default btn-sm<c:if test="${currentSampleCount>0}"> disabled</c:if>"><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span></button>
											</div>
											<div id="schema-sample-output-resource" class="height-sized-element">
											
											</div>
										</div>
									</div>
								</div>									
							</div>
							
							<!-- Center: Model -->
							<div class="layout-center layout-pane" style="padding-bottom: 0;">
								<div class="ui-pane-title">
									<h4><s:message code="~eu.dariah.de.minfba.schereg.editor.element_model" /></h4>
								</div>
								<!-- <div class="button-bar ui-pane-subcontainer ">
									<button type="button" onclick="schemaEditor.schema.expandAll(); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-full"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.expand_all" /></button>
					      			<button type="button" onclick="schemaEditor.schema.collapseAll(); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-small"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.collapse_all" /></button>
					      			<button type="button" onclick="schemaEditor.schema.resetView(); schemaEditor.reload(); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-refresh"></span> <s:message code="~eu.dariah.de.minfba.common.link.reload" /></button>		
								</div> -->
								<div class="editor-container ui-pane-subcontainer height-sized-element">
									<canvas id="schema-editor-canvas"></canvas>
								</div>
							</div>
							
							<!-- East: Context details -->
							<div id="schema-editor-detail-pane" class="layout-east layout-pane">
								<div id="schema-context-container">
									<div class="ui-pane-title">
										<h4><s:message code="~eu.dariah.de.minfba.schereg.editor.schema_details" /></h4>
									</div>
									<c:if test="${schema.own || schema.write}">
										<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.actions" /></h5>
										<div id="schema-context-buttons" class="ui-pane-subcontainer button-bar">
											<button type="button" onclick="schemaEditor.exportSchema(); return false;" class="btn btn-default btn-sm"><span class='glyphicon glyphicon-cloud-download'></span> <s:message code="~eu.dariah.de.minfba.schereg.button.export" /></button>
											<button type="button" onclick="schemaEditor.triggerUploadFile('${schema.id}'); return false;" class="btn btn-default btn-sm"><span class='glyphicon glyphicon-cloud-upload'></span> <s:message code="~eu.dariah.de.minfba.schereg.button.import" /></button>
											<button type="button" onclick="schemaEditor.createRoot(); return false;" class="btn btn-default btn-sm"><span class='glyphicon glyphicon-add'></span> ~ Create root element</button>
											<button type="button" onclick="return false;" class="btn btn-danger btn-sm"><span class='glyphicon glyphicon-trash'></span> </button>
										</div>
									</c:if>
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.history" /></h5>
									<div class="ui-pane-subcontainer" id="schema-context-activities"></div>
								</div>
								<div id="schema-element-context-container" class="hide">
									<div class="ui-pane-title">
										<h4><s:message code="~eu.dariah.de.minfba.schereg.editor.element_details" /></h4>
									</div>
									<c:if test="${schema.own || schema.write}">
										<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.actions" /></h5>
										<div id="schema-element-context-buttons" class="ui-pane-subcontainer"></div>
									</c:if>
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.details" /></h5>
									<div id="schema-element-context-info" class="clearfix ui-pane-subcontainer"></div>
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.history" /></h5>
									<div id="schema-element-context-activities">...</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
