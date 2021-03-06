<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
				<div class="pull-right dariah-flower-white-45"><s:message code="~de.unibamberg.minf.dme.title_short" /></div>
			</div>
			<div class="col-sm-6 col-lg-7 col-sm-offset-1">
				<h1><s:message code="~de.unibamberg.minf.dme.view.mapping_editor" /></h1>
			</div>
		</div>
	</div>
</div>
<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li><a href='<s:url value="/" />' target="_self"><s:message code="~de.unibamberg.minf.dme.title" /></a></li>
				<li><a href='<s:url value="/registry" />' target="_self"><s:message code="~de.unibamberg.minf.dme.registry.title" /></a></li>
				<li class="active"><s:message code="~de.unibamberg.minf.dme.view.mapping_editor" /></li>
			</ul>
			<input type="hidden" id="mapping-id" value="${mapping.id}" />
			<input type="hidden" id="source-id" value="${source.id}" />
			<input type="hidden" id="target-id" value="${target.id}" />
			<input type="hidden" id="mapping-write" value="${mapping.write}" />
			<input type="hidden" id="mapping-own" value="${mapping.own}" />
			<div id="main-content">
				<div class="row">
					<div class="col-xs-12">
						<h2 class="pull-left"><small><s:message code="~de.unibamberg.minf.dme.model.mapping.source" />:</small>
						${source.pojo.name} <small><s:message code="~de.unibamberg.minf.dme.model.mapping.target" />:</small> ${target.pojo.name} 
							<c:if test="${!mapping.own && !mapping.write}"></c:if>
						&nbsp;</h2>
						
						<c:if test="${mapping.draft}"><span class="label label-warning"><s:message code="~de.unibamberg.minf.common.model.draft" /></span></c:if>
						<c:if test="${mapping.readOnly}"><span class="label label-info"><s:message code="~de.unibamberg.minf.common.model.readonly" /></span></c:if>
												
						
						<div class="pull-right">
							<c:choose>
								<c:when test="${mapping.own || mapping.write}">
									<button type="button" onclick="editor.triggerEdit(); return false;" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-edit"></span> <s:message code="~de.unibamberg.minf.common.link.edit" /></button>
									<button type="button" onclick="editor.triggerDelete(); return false;" class="btn btn-sm btn-danger"><span class="glyphicon glyphicon-trash"></span> <s:message code="~de.unibamberg.minf.common.link.delete" /></button>
									<c:if test="${mapping.draft}">
										<button type="button" onclick="editor.triggerPublish(); return false;" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-export"></span> <s:message code="~de.unibamberg.minf.common.link.publish" /></button>
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
							
							<!-- South: Session log -->
							<div class="layout-south layout-pane">
								<ul id="mapping-editor-log" class="log"></ul>
							</div>
							
							<!-- West: Sample transformation -->
							<div class="editor-sample-pane layout-west layout-pane">
								<c:set var="currentSampleCount" value="${session.sampleOutput==null ? 0 : fn:length(session.sampleOutput)}"/>
								<input type="hidden" id="currentSampleCount" value="${currentSampleCount}">
								<input type="hidden" id="currentSampleIndex" value="${session.selectedOutputIndex==null ? 0 : session.selectedOutputIndex}">
								
								<div class="ui-pane-title">
									<h4><s:message code="~de.unibamberg.minf.dme.editor.sample.title" />
										<span class="pull-right"><span class="glyphicon glyphicon-info-sign glyphicon-color-info" data-toggle="tooltip" data-placement="top" title="<s:message code="~de.unibamberg.minf.dme.editor.hint.sessions" />" aria-hidden="true"></span></span>
									</h4>
								</div>
							
								<div class="editor-sample-container">
									<!-- Single button -->
									<div class="btn-group pull-right" style="margin-bottom: 10px;">
									  <button type="button" onclick="editor.applyAndExecuteSample(); return false;" class="btn btn-primary btn-sm"><i class="fa fa-cog" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.editor.actions.execute"/> </button>
									  <button type="button" onclick="editor.uploadAndExecuteSample(); return false;" class="btn btn-default btn-sm"><i class="fa fa-upload" aria-hidden="true"></i></button>
									  <button type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><i class="fa fa-bars" aria-hidden="true"></i></button>
									  <ul style="z-index: 1001" class="dropdown-menu">
									    <li><a href="#" onclick="editor.downloadSampleInput(); return false;"><i class="fa fa-download" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.editor.actions.download_sample_file" /></a></li>
									    <li><a href="#" onclick="editor.downloadSampleOutput(); return false;"><i class="fa fa-download" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.editor.actions.download_sample_output" /></a></li>
									    <li role="separator" class="divider"></li>
									    <li><a href="#" onclick="editor.newSampleSession(); return false;"><i class="fa fa-plus" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.button.new_session" /></a></li>
									    <li><a href="#" onclick="sessions.saveSession(editor.getEntityId());"><i class="fa fa-floppy-o" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.button.save_session" /></a></li>
										<li><a href="#" onclick="sessions.loadSession(editor.getEntityId());"><i class="fa fa-folder-open-o" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.button.load_session" /></a></li>
										<li><a href="#" onclick="editor.deleteSampleSession(); return false;"><i class="fa fa-trash" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.button.delete_session" /></a></li>
									  </ul>
									</div>
									
									
									<ul class="nav nav-tabs" role="tablist">
										<li role="presentation"<c:if test="${currentSampleCount==0}"> class="active"</c:if>>
											<a href="#sample-input-container" aria-controls="sample-input-container" role="tab" data-toggle="tab"><s:message code="~de.unibamberg.minf.dme.editor.sample.input" /></a>
										</li>
										<c:choose>
											<c:when test="${currentSampleCount>0}">
												<li role="presentation" class="active"><a href="#sample-output-container" aria-controls="sample-output-container" role="tab" data-toggle="tab"><s:message code="~de.unibamberg.minf.dme.editor.sample.output" /><span class="badge-c"> <span class="badge">${currentSampleCount}</span></span></a></li>
											</c:when>
											<c:otherwise>
												<li role="presentation" class="disabled"><a href="#sample-output-container" aria-controls="sample-output-container" role="tab"><s:message code="~de.unibamberg.minf.dme.editor.sample.output" /></a></li>
											</c:otherwise>
										</c:choose>
									</ul>
									<div class="tab-content">
										<%@ include file="include/sample_input_container.jsp" %>
										<div role="tabpanel" class="tab-pane <c:if test="${currentSampleCount>0}"> active</c:if>" id="sample-output-container">
											<div class="button-bar">
												<div class="pull-left">
													<button type="button" onclick="editor.showSampleResourceSource(); return false;" class="btn btn-info btn-sample-source btn-sm"><s:message code="~de.unibamberg.minf.dme.model.mapping.source" /></button>
													<button type="button" onclick="editor.showSampleResourceTarget(); return false;" class="btn btn-default btn-sample-target btn-sm"><s:message code="~de.unibamberg.minf.dme.model.mapping.target" /></button>
												</div>
												<span class="sample-output-counter"><c:if test="${currentSampleCount>0}">${session.selectedOutputIndex + 1} / ${currentSampleCount}</c:if></span>
												<button type="button" onclick="editor.getPrevSampleResource(); return false;" class="btn btn-default btn-sample-prev-resource btn-sm<c:if test="${currentSampleCount>0}"> disabled</c:if>"><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span></button>
												<button type="button" onclick="editor.getNextSampleResource(); return false;" class="btn btn-default btn-sample-next-resource btn-sm<c:if test="${currentSampleCount>0}"> disabled</c:if>"><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span></button>
											</div>
											<div class="height-sized-element">
												<div class="sample-output-resource"></div>
												<div class="sample-transformed-resource hide"></div>
											</div>
										</div>
									</div>
								</div>									
							</div>
							
							<!-- Center: Mapping canvas -->
							<div class="layout-center layout-pane" style="padding-bottom: 0;">
								<div class="ui-pane-title">
									<h4><s:message code="~de.unibamberg.minf.dme.editor.element_model" /></h4>
								</div>
								<!-- <div class="button-bar ui-pane-subcontainer ">
									<button type="button" onclick="schemaEditor.schema.performAction('expandAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-full"></span> <s:message code="~de.unibamberg.minf.dme.button.expand_all" /></button>
					      			<button type="button" onclick="schemaEditor.schema.performAction('collapseAll'); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-resize-small"></span> <s:message code="~de.unibamberg.minf.dme.button.collapse_all" /></button>
					      			<button type="button" onclick="schemaEditor.schema.performAction('resetView'); schemaEditor.reload(); return false;" class="btn btn-default btn-sm pull-left"><span class="glyphicon glyphicon-refresh"></span> <s:message code="~de.unibamberg.minf.common.link.reload" /></button>		
								</div> -->
								<div class="editor-container ui-pane-subcontainer height-sized-element">
									<canvas id="mapping-editor-canvas"></canvas>
								</div>
							</div>
							
							<!-- East: Properties -->
							<div class="layout-east layout-pane">
								<div id="mapping-context-container">
									<div class="ui-pane-title">
										<h4><s:message code="~de.unibamberg.minf.dme.editor.mapping_details" /></h4>
									</div>
									<div class="panel-group ui-pane-subcontainer" id="mapping-context-buttons-accordion" role="tablist" aria-multiselectable="true">
										<div class="panel">
									    	<div class="panel-heading" role="tab" id="mapping-context-buttons-heading">
									      		<h5 class="panel-title">
											        <a role="button" data-toggle="collapse" data-parent="#mapping-context-buttons-accordion" href="#mapping-context-buttons-collapse" aria-expanded="true" aria-controls="mapping-context-buttons-collapse">
											          <s:message code="~de.unibamberg.minf.dme.editor.actions" />											          
											          <span class="pull-right"><span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span> <span class="glyphicon glyphicon-info-sign glyphicon-color-info" data-toggle="tooltip" data-placement="left" title="<s:message code="~de.unibamberg.minf.dme.editor.hint.context_menu" />" aria-hidden="true"></span></span>
											        </a>
									      		</h5>
									    	</div>
									    	<div id="mapping-context-buttons-collapse" class="panel-collapse collapse" role="tabpanel" aria-labelledby="mapping-context-buttons-heading">
									      		<div class="panel-body" id="mapping-context-buttons"></div>
									    	</div>
									  	</div>
									</div>
									<h5><s:message code="~de.unibamberg.minf.dme.editor.history" /></h5>
									<div class="ui-pane-subcontainer" id="mapping-context-activities"></div>
								</div>
								<div id="mapped-concept-context-container" class="hide">
									<div class="ui-pane-title">
										<h4><s:message code="~de.unibamberg.minf.dme.editor.mapped_concept_details" /></h4>
									</div>
									<div class="panel-group ui-pane-subcontainer" id="mapped-concept-context-buttons-accordion" role="tablist" aria-multiselectable="true">
										<div class="panel">
									    	<div class="panel-heading" role="tab" id="mapped-concept-context-buttons-heading">
									      		<h5 class="panel-title">
											        <a role="button" data-toggle="collapse" data-parent="#mapped-concept-context-buttons-accordion" href="#mapped-concept-context-buttons-collapse" aria-expanded="true" aria-controls="mapped-concept-context-buttons-collapse">
											          <s:message code="~de.unibamberg.minf.dme.editor.actions" />											          
											          <span class="pull-right"><span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span> <span class="glyphicon glyphicon-info-sign glyphicon-color-info" data-toggle="tooltip" data-placement="left" title="<s:message code="~de.unibamberg.minf.dme.editor.hint.context_menu" />" aria-hidden="true"></span></span>
											        </a>
									      		</h5>
									    	</div>
									    	<div id="mapped-concept-context-buttons-collapse" class="panel-collapse collapse" role="tabpanel" aria-labelledby="mapped-concept-context-buttons-heading">
									      		<div class="panel-body" id="mapped-concept-context-buttons"></div>
									    	</div>
									  	</div>
									</div>
									<h5><s:message code="~de.unibamberg.minf.dme.editor.details" /></h5>
									<div id="mapped-concept-context-info" class="clearfix ui-pane-subcontainer"></div>
									<h5><s:message code="~de.unibamberg.minf.dme.editor.history" /></h5>
									<div id="mapped-concept-context-activities"></div>
								</div>
								<div id="schema-element-context-container" class="hide">
									<div class="ui-pane-title">
										<h4><s:message code="~de.unibamberg.minf.dme.editor.element_details" /></h4>
									</div>
									<div class="panel-group ui-pane-subcontainer" id="schema-element-context-buttons-accordion" role="tablist" aria-multiselectable="true">
										<div class="panel">
									    	<div class="panel-heading" role="tab" id="schema-element-context-buttons-heading">
									      		<h5 class="panel-title">
											        <a role="button" data-toggle="collapse" data-parent="#schema-element-context-buttons-accordion" href="#schema-element-context-buttons-collapse" aria-expanded="true" aria-controls="schema-element-context-buttons-collapse">
											          <s:message code="~de.unibamberg.minf.dme.editor.actions" />											          
											          <span class="pull-right"><span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span> <span class="glyphicon glyphicon-info-sign glyphicon-color-info" data-toggle="tooltip" data-placement="left" title="<s:message code="~de.unibamberg.minf.dme.editor.hint.context_menu" />" aria-hidden="true"></span></span>
											        </a>
									      		</h5>
									    	</div>
									    	<div id="schema-element-context-buttons-collapse" class="panel-collapse collapse" role="tabpanel" aria-labelledby="schema-element-context-buttons-heading">
									      		<div class="panel-body" id="schema-element-context-buttons"></div>
									    	</div>
									  	</div>
									</div>
									<h5><s:message code="~de.unibamberg.minf.dme.editor.details" /></h5>
									<div id="schema-element-context-info" class="clearfix ui-pane-subcontainer"></div>
									<h5><s:message code="~de.unibamberg.minf.dme.editor.history" /></h5>
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
