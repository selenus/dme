<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 class="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.function.process.title" /></h3>	
	</div>
	<div class="form-content">
		<div class="panel-group accordion-validate-function" role="tablist" aria-multiselectable="true">
			<div class="function-parsing panel panel-default">
				<div class="panel-heading heading-function-parsing" role="tab">
					<h4 class="panel-title"> 
						<span class="mini-loader function-loading"></span>
						<span class="glyphicon glyphicon-console function-waiting hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-ok function-ok hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-exclamation-sign function-error hide" aria-hidden="true"></span>
						 <s:message code="~de.unibamberg.minf.dme.form.function.process.parsing_function" />
					</h4>
				</div>
				<div class="panel-collapse collapse-function-parsing" role="tabpanel" aria-labelledby="heading-function-parsing">
					<div class="panel-body hide">
						<div class="function-alerts"></div>
						<div class="function-svg outer-svg-container" style="height: 350px;">
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
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.close" /></button>
		</div>
	</div>
</div>