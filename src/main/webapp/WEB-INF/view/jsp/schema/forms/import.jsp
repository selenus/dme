<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div>
	<div class="fileupload-buttonbar">
		<span class="btn btn-primary fileinput-button"> <span class="glyphicon glyphicon-cloud-upload" aria-hidden="true"></span> ~fileinput.button.upload <input type="file" name="file" /></span>
	</div>
	<div class="fileupload-progress hide">
		<div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
			<div class="bar" style="width: 0%;"></div>
		</div>
		<div class="progress-extended">&nbsp;</div>
	</div>
	<div class="fileupload-files" role="presentation" data-toggle="modal-gallery" data-target="#modal-gallery"></div>
</div>