<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="util" uri="/WEB-INF/taglib/util.tld" %>

<div class="modal hide fade">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h3>DARIAH Schema Registry - Change Log</h3>
	</div>
	<div class="modal-body" style="font-family: 'Courier New', Courier, monospace; font-size: 12px;">
		<c:import url="/resources/change_log.txt" var="changelog" />
		${util:escapeText(changelog)}
	</div>
	<div class="modal-footer">
		<button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Close</button>
	</div>
</div>