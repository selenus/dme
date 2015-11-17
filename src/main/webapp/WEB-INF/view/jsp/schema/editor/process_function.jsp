<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.function.process.title" /></h3>	
	</div>
	<div class="form-content">
		<div class="panel-group" id="accordion-validate-function" role="tablist" aria-multiselectable="true">
			<div id="function-parsing" class="panel panel-default">
				<div class="panel-heading" role="tab" id="heading-function-parsing">
					<h4 class="panel-title"> 
						<span class="mini-loader function-loading"></span>
						<span class="glyphicon glyphicon-console function-waiting hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-ok function-ok hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-exclamation-sign function-error hide" aria-hidden="true"></span>
						 <s:message code="~eu.dariah.de.minfba.schereg.form.function.process.parsing_function" />
					</h4>
				</div>
				<div id="collapse-function-parsing" class="panel-collapse" role="tabpanel" aria-labelledby="heading-function-parsing">
					<div class="panel-body hide">
						<div id="function-alerts"></div>
						<div id="function-svg" class="outer-svg-container" style="height: 350px;">
							<div class="inner-svg-container"></div>
							<div class="svg-button-container">
								<button class="btn btn-link btn-sm btn-svg-zoomin"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
								<button class="btn btn-link btn-sm btn-svg-zoomout"><span class="glyphicon glyphicon-minus" aria-hidden="true"></span></button>
								<button class="btn btn-link btn-sm btn-svg-reset"><span class="glyphicon glyphicon-repeat" aria-hidden="true"></span></button>
								<button class="btn btn-link btn-sm btn-svg-newwindow"><span class="glyphicon glyphicon-new-window" aria-hidden="true"></span></button>
							</div>
						</div>
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