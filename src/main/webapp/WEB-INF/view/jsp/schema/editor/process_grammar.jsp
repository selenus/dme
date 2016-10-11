<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 class="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.grammar.process.title" /></h3>	
	</div>
	<div class="form-content">
		<div class="panel-group accordion-validate-grammar" role="tablist" aria-multiselectable="true">
			<div class="grammar-uploading panel panel-default">
				<div class="panel-heading heading-grammar-uploading" role="tab">
					<h4 class="panel-title"> 
						<span class="mini-loader grammar-loading hide"></span>
						<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
						 <s:message code="~eu.dariah.de.minfba.schereg.form.grammar.process.uploading" />
					</h4>
				</div>
				<div class="collapse-grammar-uploading panel-collapse collapse" role="tabpanel" aria-labelledby="heading-grammar-uploading">
					<div class="panel-body hide">
					</div>
				</div>
			</div>
			<div class="panel panel-default grammar-parsing">
				<div class="heading-grammar-parsing panel-heading" role="tab">
					<h4 class="panel-title"> 
						<span class="mini-loader grammar-loading hide"></span>
						<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
						 <s:message code="~eu.dariah.de.minfba.schereg.form.grammar.process.java_creation" />
					</h4>
				</div>
				<div class="panel-collapse collapse collapse-grammar-parsing" role="tabpanel" aria-labelledby="heading-grammar-parsing">
					<div class="panel-body hide">
					</div>
				</div>
			</div>
			<div class="grammar-compiling panel panel-default">
				<div class="panel-heading heading-grammar-compiling" role="tab">
					<h4 class="panel-title">
						<span class="mini-loader grammar-loading hide"></span>
						<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
						 <s:message code="~eu.dariah.de.minfba.schereg.form.grammar.process.compiling" />
					</h4>
				</div>
				<div class="collapse-grammar-compiling panel-collapse collapse" role="tabpanel" aria-labelledby="heading-grammar-compiling">
					<div class="panel-body hide">
					</div>
				</div>
			</div>
			<div class="grammar-sandboxing panel panel-default">
				<div class="panel-heading heading-grammar-sandboxing" role="tab">
					<h4 class="panel-title">
						<span class="mini-loader grammar-loading hide"></span>
						<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
						 <s:message code="~eu.dariah.de.minfba.schereg.form.grammar.process.sandboxing" />
					</h4>
				</div>
				<div class="panel-collapse collapse collapse-grammar-sandboxing" role="tabpanel" aria-labelledby="heading-grammar-sandboxing">
					<div class="panel-body hide">
					</div>
				</div>
			</div>
		</div>

	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.close" /></button>
		</div>
	</div>
</div>