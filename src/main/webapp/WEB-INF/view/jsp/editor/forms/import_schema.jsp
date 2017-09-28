<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<form method="POST" action="${saveUrl}" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.button.import" /></h3>
		<input type="hidden" name="schemaId" value="${schema.id}">
		<c:if test="${elementId!=null}">
			<input type="hidden" name="elementId" id="elementId" value="${elementId}" />
		</c:if>
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-4 control-label" for="schema_source"><s:message code="~de.unibamberg.minf.dme.model.schema.source" />:</label>
			<div class="col-sm-8">
				<input type="hidden" name="file.id" id="file.id" />
				<input id="schema_source" type="file" name="file" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-4 control-label" for="schema_root"><s:message code="~de.unibamberg.minf.dme.model.schema.root_element" />:</label>
			<div class="col-sm-8">
				<!--<select id="schema_root" name="schema_root" class="form-control" disabled="disabled"></select>-->
				<input type="text" class="form-control typeahead" id="schema_root" disabled="disabled" />
				<input type="hidden" name="schema_root_qn" id="schema_root_qn" />
				<input type="hidden" name="schema_root_type" id="schema_root_type" />
			</div>
		</div>
		<div id="importer-options" class="form-group hide">
			<label class="col-sm-4 control-label" for="schema_root"><s:message code="~de.unibamberg.minf.dme.notification.import.importer" />:</label>
			<div class="col-sm-8">
				<p class="form-control-static"><span id="importer-type"></span> (<span id="importer-subtype"></span>)</p>
			</div>
			<div class="col-sm-offset-4 col-sm-8 hide" id="importer-keep-ids">
				<div class="checkbox">
					<label> <input type="checkbox" value="true" name="keep-imported-ids" onclick="$('#id-keep-ids-hint').removeClass('hide'); $(window).trigger('resize');"> <s:message code="~de.unibamberg.minf.dme.notification.import.keep_ids" />
					</label>
				</div>
				<div id="id-keep-ids-hint" class="alert alert-sm alert-warning alert-persist hide" style="margin-top: 10px;"><i class="fa fa-exclamation-triangle fa-color-warning" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.notification.import.keep_ids_hint" /></div>
			</div>
			
		</div>
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button id="btn-submit-schema-elements" class="btn btn-primary start form-btn-submit" disabled="disabled" type="submit"><s:message code="~de.unibamberg.minf.common.link.save" /></button>
		</div>
	</div>
</form>
