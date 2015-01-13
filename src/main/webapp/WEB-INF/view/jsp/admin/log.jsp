<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<div id="content">
	<h2>Administration - Log</h2>
	<form>
		<input id="auto_refresh" name="auto_refresh" type="checkbox" checked="checked" />&nbsp;Auto-Refresh
		<span id="refresh_message"></span>
	</form>
	<div id="contentInner">
		<div>	
			<table class="table table-bordered table-striped model-list" style="table-layout: fixed;">
			<thead>
				<tr>
					<th style="width: 80px;">Level</th>
					<th style="width: 160px;">Timestamp</th>
					<th>Message</th>
				</tr>
				</thead>
				<tbody>
					<%@ include file="incl/log_row.jsp" %>
				</tbody>
			</table>
		</div>
	</div>
</div>