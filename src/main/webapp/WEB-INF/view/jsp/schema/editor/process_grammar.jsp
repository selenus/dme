<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~Progress</h3>	
	</div>
	<div class="form-content">
		<div id="grammar-uploading" class="alert">
			<span class="mini-loader grammar-loading hide"></span>
			<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
			 ~Uploading grammar
		</div>
		<div id="grammar-parsing" class="alert">
			<span class="mini-loader grammar-loading hide"></span>
			<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
			 ~Generating Java parser/lexer source code from grammar rules
		</div>
		<div id="grammar-compiling" class="alert">
			<span class="mini-loader grammar-loading hide"></span>
			<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
			 ~Compiling executable Java bytecode 
		</div>		
		<div id="grammar-sandboxing" class="alert">
			<span class="mini-loader grammar-loading hide"></span>
			<span class="glyphicon glyphicon-console grammar-waiting" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-ok grammar-ok hide" aria-hidden="true"></span>
			<span class="glyphicon glyphicon-exclamation-sign grammar-error hide" aria-hidden="true"></span>
			 ~Preparing transformation sandbox for grammar
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.ok" /></button>
		</div>
	</div>
</div>