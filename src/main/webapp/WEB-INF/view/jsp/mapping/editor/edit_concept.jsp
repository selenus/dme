<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>



<div>
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.grammar.edit" /> <small><span class="glyphicon glyphicon-info-sign help-sign" onclick="grammarEditor.showHelp(); return false;" aria-hidden="true"></span></small></h3>	
		
	</div>
	<div class="form-content row" style="padding-bottom: 0px;">
		<div class="col-md-7" style="border-right: 1px solid #E5E5E5;">
			<div class="legend"><strong>1</strong> <s:message code="~eu.dariah.de.minfba.schereg.form.grammar.legend.edit_function" /></div>
			<div class="form-group">
				<label id="label-parser-grammar" class="control-label" for="grammarContainer_parserGrammar"><s:message code="~eu.dariah.de.minfba.schereg.model.grammar.parser_grammar" />:</label>
				<div>
					
				</div>
			</div>	
			<div class="form-footer">
				<div class="controls">
					<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.close" /></button>
				</div>
			</div>
		</div>
		<div class="col-md-5" style="border-left: 1px solid #E5E5E5; margin-left: -1px;">
			<div class="legend"><strong>2</strong> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.execute" /></div>
			<div class="non-passthrough-only">
				<div class="form-group">
					<label class="control-label" for="grammar-sample-input"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.input" />:</label>
					<div>
						<textarea id="grammar-sample-input" rows="6" class="form-control codearea">${elementSample}</textarea>
					</div>
				</div>
				<div class="clearfix">
					<button id="btn-parse-sample" class="btn btn-warning btn-sm pull-right disabled" onclick="grammarEditor.parseSample(); return false;"><span class="glyphicon glyphicon-play" aria-hidden="true"></span> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.process_input" /></button>
				</div>
			</div>
			<div class="passthrough-only">
			</div>
			<div class="legend"><strong>3</strong> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.transformation_result" /></div>
			<div class="non-passthrough-only">
				<div id="grammar-parse-alerts">
					<div class="alert alert-sm alert-info"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.notice.hint_sample" /></div>
				</div>
				<div id="grammar-sample-svg-embedded" class="outer-svg-container hide">
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