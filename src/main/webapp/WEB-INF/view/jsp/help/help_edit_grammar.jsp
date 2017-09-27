<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.grammar.help.title" /></h3>	
	</div>
	<div class="form-content">
		<div class="clearfix">
			<div class="alert alert-sm alert-info" role="alert"><s:message code="~de.unibamberg.minf.dme.form.grammar.help.hint.samples" /></div>
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.close" /></button>
		</div>
	</div>
</div>