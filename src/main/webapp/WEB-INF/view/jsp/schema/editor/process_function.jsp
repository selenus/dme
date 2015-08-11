<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~Progress</h3>	
	</div>
	<div class="form-content">
		<div class="panel-group" id="accordion-validate-function" role="tablist" aria-multiselectable="true">
			<div id="function-parsing" class="panel panel-default">
				<div class="panel-heading" role="tab" id="heading-function-parsing">
					<h4 class="panel-title"> 
						<span class="mini-loader grammar-loading hide"></span>
						<span class="glyphicon glyphicon-console function-waiting" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-ok function-ok hide" aria-hidden="true"></span>
						<span class="glyphicon glyphicon-exclamation-sign function-error hide" aria-hidden="true"></span>
						 ~Parsing function
					</h4>
				</div>
				<div id="collapse-function-parsing" class="panel-collapse" role="tabpanel" aria-labelledby="heading-function-parsing">
					<div class="panel-body">
						<div id="function-svg" class="grammar-sample-svg">
							<div class="grammar-sample-svg-container"></div>
							<div class="grammar-svg-controls">
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
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset">~Close</button>
		</div>
	</div>
</div>