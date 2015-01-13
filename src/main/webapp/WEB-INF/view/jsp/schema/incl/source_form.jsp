<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div>
<ul class="nav nav-tabs fileupload-selection-tab"> 
	<li class="active"><a data-toggle="tab" href="#fileupload-local"><s:message code="fileinput.label.local" /></a></li>
	<li><a data-toggle="tab" href="#fileupload-web"><s:message code="fileinput.label.web" /></a></li>
</ul>
<div class="tab-content fileupload-selection-tabContent">
	<div id="fileupload-local" class="tab-pane fade in active">
		<div class="fileupload-buttonbar">
			<span class="btn btn-primary fileinput-button"> <i class="icon-plus icon-white"></i> <span><s:message code="fileinput.button.upload" /></span><input type="file" name="file" /></span>
		</div>
		<div class="fileupload-progress hide">
			<div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
				<div class="bar" style="width: 0%;"></div>
			</div>
			<div class="progress-extended">&nbsp;</div>
		</div>
		<div class="fileupload-files" role="presentation" data-toggle="modal-gallery" data-target="#modal-gallery"></div>
	</div>
	<div id="fileupload-web" class="tab-pane fade">
		<p>Web sources are currently not yet supported.</p>
	</div>
</div>
</div>