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
				<h1><s:message code="~eu.dariah.de.minfba.schereg.view.schema_editor" /></h1>
			</div>
		</div>
	</div>
</div>
<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li><a href='<s:url value="/" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.title" /></a></li>
				<li><a href='<s:url value="/registry" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.registry.title" /></a></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.view.schema_editor" /></li>
			</ul>
			<input type="hidden" id="schema-id" value="${schema.id}" />
			<input type="hidden" id="schema-write" value="${schema.write}" />
			<input type="hidden" id="schema-own" value="${schema.own}" />
			<div id="main-content">
				<div class="row">
					<div class="col-xs-12">
						<h2 class="pull-left">
						<small><s:message code="~eu.dariah.de.minfba.schereg.model.schema.schema" />:</small>&nbsp;${schema.pojo.label}
							<c:if test="${!schema.own && !schema.write}"> <small></small></c:if>
						&nbsp;</h2>

						<c:if test="${schema.draft}"><span class="label label-warning"><s:message code="~eu.dariah.de.minfba.common.model.draft" /></span></c:if>
						<c:if test="${schema.readOnly}"><span class="label label-info"><s:message code="~eu.dariah.de.minfba.common.model.readonly" /></span></c:if>
						
						<div class="pull-right">
							<c:choose>
								<c:when test="${schema.own || schema.write}">
									<button type="button" onclick="editor.triggerEditSchema(); return false;" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-edit"></span> <s:message code="~eu.dariah.de.minfba.common.link.edit" /></button>
									
									<c:if test="${schema.draft}">
										<button type="button" onclick="editor.triggerPublish(); return false;" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-export"></span> <s:message code="~eu.dariah.de.minfba.common.link.publish" /></button>
									</c:if>
									
									<c:if test="${!mapped}">
										<button type="button" onclick="editor.triggerDeleteSchema(); return false;" class="btn btn-sm btn-danger"><span class="glyphicon glyphicon-trash"></span> <s:message code="~eu.dariah.de.minfba.common.link.delete" /></button>
									</c:if>
								</c:when>
								<c:otherwise>
									<span class="glyphicon glyphicon-lock"></span>
								</c:otherwise>
							</c:choose>
						</div>
					</div>
					<div class="col-xs-12">
						<div class="hide editor-layout-container">
						
							<!-- South: Logging pane -->
							<div class="layout-south layout-pane">
								<ul id="schema-editor-log" class="log"></ul>
							</div>
							
							<!-- West: Sample transformation -->
							<div class="layout-west layout-pane editor-sample-pane">
								<c:set var="currentSampleCount" value="${session.sampleOutput==null ? 0 : fn:length(session.sampleOutput)}"/>
								<input type="hidden" id="currentSampleCount" value="${currentSampleCount}">
								<input type="hidden" id="currentSampleIndex" value="${session.selectedOutputIndex==null ? 0 : session.selectedOutputIndex}">
								
								<div class="ui-pane-title">
									<h4><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.title" />
										<span class="pull-right"><span class="glyphicon glyphicon-info-sign glyphicon-color-info" data-toggle="tooltip" data-placement="top" title="<s:message code="~eu.dariah.de.minfba.schereg.editor.hint.sessions" />" aria-hidden="true"></span></span>
									</h4>
								</div>
								<div style="background-color: white; overflow: hidden;">
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.sessions" /></h5>
								</div>									
								<div class="ui-pane-subcontainer button-bar">
									<button type="button" onclick="sessions.saveSession(editor.schema.id);" class="btn btn-default btn-sm"><s:message code="~eu.dariah.de.minfba.schereg.button.save_session" /></button>
									<button type="button" onclick="sessions.loadSession(editor.schema.id);" class="btn btn-default btn-sm"><s:message code="~eu.dariah.de.minfba.schereg.button.load_session" /></button>
									<button type="button" onclick="editor.resetSampleSession(); return false;" class="btn btn-default btn-sm"><s:message code="~eu.dariah.de.minfba.common.link.reset" /></button>
								</div>
								<div class="editor-sample-container">
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.data" /></h5>
									<button type="button" onclick="editor.applyAndExecuteSample(); return false;" class="pull-right btn btn-primary btn-sm"><s:message code="~eu.dariah.de.minfba.schereg.editor.actions.execute" /></button>
									<ul class="nav nav-tabs" role="tablist">
										<li role="presentation"<c:if test="${currentSampleCount==0}"> class="active"</c:if>>
											<a href="#sample-input-container" aria-controls="sample-input-container" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.input" /></a>
										</li>
										<c:choose>
											<c:when test="${currentSampleCount>0}">
												<li role="presentation" class="active"><a href="#sample-output-container" aria-controls="sample-output-container" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.output" /><span class="badge-c"> <span class="badge">${currentSampleCount}</span></span></a></li>
											</c:when>
											<c:otherwise>
												<li role="presentation" class="disabled"><a href="#sample-output-container" aria-controls="sample-output-container" role="tab"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.output" /></a></li>
											</c:otherwise>
										</c:choose>
									</ul>
									<div class="tab-content">
										<div role="tabpanel" class="tab-pane <c:if test="${currentSampleCount==0}"> active</c:if>" id="sample-input-container">
											<c:choose>
												<c:when test="${session.sampleInput!=null && session.sampleInput!=''}">
													<input type="hidden" id="sample-set" value="true">
													<textarea class="sample-textarea form-control height-sized-element" placeholder="<s:message code="~eu.dariah.de.minfba.schereg.editor.sample.placeholder_set" />" rows="3"></textarea>
												</c:when>
												<c:otherwise>
													<input type="hidden" id="sample-set" value="false">
													<textarea class="sample-textarea form-control height-sized-element" placeholder="<s:message code="~eu.dariah.de.minfba.schereg.editor.sample.placeholder" />" rows="3"></textarea>
												</c:otherwise>
											</c:choose>
										</div>
										<div role="tabpanel" class="tab-pane <c:if test="${currentSampleCount>0}"> active</c:if>" id="sample-output-container">
											<div class="button-bar">
												<span class="sample-output-counter"><c:if test="${currentSampleCount>0}">${session.selectedOutputIndex + 1} / ${currentSampleCount}</c:if></span>
												<button type="button" onclick="editor.getPrevSampleResource(); return false;" class="btn-sample-prev-resource btn btn-default btn-sm<c:if test="${currentSampleCount>0}"> disabled</c:if>"><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span></button>
												<button type="button" onclick="editor.getNextSampleResource(); return false;" class="btn-sample-next-resource btn btn-default btn-sm<c:if test="${currentSampleCount>0}"> disabled</c:if>"><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span></button>
											</div>
											<div class="sample-output-resource height-sized-element">
											
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
									<div class="panel-group ui-pane-subcontainer" id="schema-context-buttons-accordion" role="tablist" aria-multiselectable="true">
										<div class="panel">
									    	<div class="panel-heading" role="tab" id="schema-context-buttons-heading">
									      		<h5 class="panel-title">
											        <a role="button" data-toggle="collapse" data-parent="#schema-context-buttons-accordion" href="#schema-context-buttons-collapse" aria-expanded="true" aria-controls="schema-context-buttons-collapse">
											          <s:message code="~eu.dariah.de.minfba.schereg.editor.actions" />											          
											          <span class="pull-right"><span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span> <span class="glyphicon glyphicon-info-sign glyphicon-color-info" data-toggle="tooltip" data-placement="left" title="<s:message code="~eu.dariah.de.minfba.schereg.editor.hint.context_menu" />" aria-hidden="true"></span></span>
											        </a>
									      		</h5>
									    	</div>
									    	<div id="schema-context-buttons-collapse" class="panel-collapse collapse" role="tabpanel" aria-labelledby="schema-context-buttons-heading">
									      		<div class="panel-body" id="schema-context-buttons"></div>
									    	</div>
									  	</div>
									</div>
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.history" /></h5>
									<div class="ui-pane-subcontainer" id="schema-context-activities"></div>
								</div>
								<div id="schema-element-context-container" class="hide">
									<div class="ui-pane-title">
										<h4><s:message code="~eu.dariah.de.minfba.schereg.editor.element_details" /></h4>
									</div>
									<div class="panel-group ui-pane-subcontainer" id="schema-element-context-buttons-accordion" role="tablist" aria-multiselectable="true">
										<div class="panel">
									    	<div class="panel-heading" role="tab" id="schema-element-context-buttons-heading">
									      		<h5 class="panel-title">
											        <a role="button" data-toggle="collapse" data-parent="#schema-element-context-buttons-accordion" href="#schema-element-context-buttons-collapse" aria-expanded="true" aria-controls="schema-element-context-buttons-collapse">
											          <s:message code="~eu.dariah.de.minfba.schereg.editor.actions" />											          
											          <span class="pull-right"><span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span> <span class="glyphicon glyphicon-info-sign glyphicon-color-info" data-toggle="tooltip" data-placement="left" title="<s:message code="~eu.dariah.de.minfba.schereg.editor.hint.context_menu" />" aria-hidden="true"></span></span>
											        </a>
									      		</h5>
									    	</div>
									    	<div id="schema-element-context-buttons-collapse" class="panel-collapse collapse" role="tabpanel" aria-labelledby="schema-element-context-buttons-heading">
									      		<div class="panel-body" id="schema-element-context-buttons"></div>
									    	</div>
									  	</div>
									</div>
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.details" /></h5>
									<div id="schema-element-context-info" class="clearfix ui-pane-subcontainer"></div>
									<h5><s:message code="~eu.dariah.de.minfba.schereg.editor.history" /></h5>
									<div id="schema-element-context-activities"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
