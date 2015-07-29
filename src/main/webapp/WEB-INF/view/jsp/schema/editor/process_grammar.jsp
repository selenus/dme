<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~Progress</h3>	
	</div>
	<div class="form-content">
		<div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
			<div id="grammar-uploading" class="panel panel-default">
				<div class="panel-heading" role="tab" id="heading-grammar-uploading">
					<h4 class="panel-title">
						<a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapse-grammar-uploading" aria-expanded="true" aria-controls="collapse-grammar-uploading"> 
							<span class="mini-loader grammar-loading hide"></span>
							<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
							 ~Uploading grammar
						</a>
					</h4>
				</div>
				<div id="collapse-grammar-uploading" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading-grammar-uploading">
					<div class="panel-body">
						...
					</div>
				</div>
			</div>
			<div id="grammar-parsing" class="panel panel-default">
				<div class="panel-heading" role="tab" id="heading-grammar-parsing">
					<h4 class="panel-title">
						<a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapse-grammar-parsing" aria-expanded="true" aria-controls="collapse-grammar-parsing"> 
							<span class="mini-loader grammar-loading hide"></span>
							<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
							 ~Generating Java parser/lexer source code from grammar rules
						</a>
					</h4>
				</div>
				<div id="collapse-grammar-parsing" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading-grammar-parsing">
					<div class="panel-body">
						...
					</div>
				</div>
			</div>
			<div id="grammar-compiling" class="panel panel-default">
				<div class="panel-heading" role="tab" id="heading-grammar-compiling">
					<h4 class="panel-title">
						<a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapse-grammar-compiling" aria-expanded="true" aria-controls="collapse-grammar-compiling"> 
							<span class="mini-loader grammar-loading hide"></span>
							<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
							 ~Compiling executable Java bytecode
						</a>
					</h4>
				</div>
				<div id="collapse-grammar-compiling" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading-grammar-compiling">
					<div class="panel-body">
						...
					</div>
				</div>
			</div>
			<div id="grammar-sandboxing" class="panel panel-default">
				<div class="panel-heading" role="tab" id="heading-grammar-sandboxing">
					<h4 class="panel-title">
						<a role="button" data-toggle="collapse" data-parent="#accordion" href="#collapse-grammar-sandboxing" aria-expanded="true" aria-controls="collapse-grammar-sandboxing"> 
							<span class="mini-loader grammar-loading hide"></span>
							<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
							<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
							 ~Preparing transformation sandbox for grammar
						</a>
					</h4>
				</div>
				<div id="collapse-grammar-sandboxing" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading-grammar-sandboxing">
					<div class="panel-body">
						...
					</div>
				</div>
			</div>
		</div>

	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.ok" /></button>
		</div>
	</div>
</div>