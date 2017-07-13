<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 class="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.show_processed_svg" /></h3>	
	</div>
	<div class="form-content">
		<div class="maximized-svg-container">
			<div class="inner-svg-container"></div>
			<div class="svg-button-container">
				<button class="btn btn-link btn-sm btn-svg-zoomin"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
				<button class="btn btn-link btn-sm btn-svg-zoomout"><span class="glyphicon glyphicon-minus" aria-hidden="true"></span></button>
				<button class="btn btn-link btn-sm btn-svg-reset"><span class="glyphicon glyphicon-repeat" aria-hidden="true"></span></button>
			</div>
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.close" /></button>
		</div>
	</div>
</div>