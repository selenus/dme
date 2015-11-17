<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.grammar.help.title" /></h3>	
	</div>
	<div class="form-content">
		<div class="clearfix">
			<div class="alert alert-sm alert-info" role="alert"><s:message code="~eu.dariah.de.minfba.schereg.form.grammar.help.hint.samples" /></div>
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.close" /></button>
		</div>
	</div>
</div>