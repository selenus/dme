<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<form method="POST" action="${saveUrl}" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.nature.add" /></h3>	
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-3 control-label" for="add-nature-selector"><s:message code ="~de.unibamberg.minf.common.model.type" />:</label>
			<div class="col-sm-8">
				<select id="add-nature-selector" name="n" class="form-control">
				  <c:forEach items="${natures}" var="nature">
				  	<option value="<s:message code="${nature.name}" />"><s:message code="~${nature.name}.display_label" /></option>
				  </c:forEach>
				</select>
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-3 control-label" for="add-nature-selector"><s:message code="~de.unibamberg.minf.dme.model.schema.elements" />:</label>
			<div class="col-sm-8">
				<div class="radio">
				  <label>
				    <input type="radio" name="autocreate" value="false" checked="checked" onchange="$('#terminals-autocreate-options').hide();">
				    <s:message code="~de.unibamberg.minf.dme.form.nature.terminals.no_autocreate" />
				  </label>
				</div>
				<div class="radio">
				  <label>
				    <input type="radio" name="autocreate" value="true" onchange="$('#terminals-autocreate-options').show();">
				    <s:message code="~de.unibamberg.minf.dme.form.nature.terminals.autocreate" />
				  </label>
				</div>
			</div>
		</div>
		<div id="terminals-autocreate-options" class="form-group" style="display: none;">
			<label class="col-sm-3 control-label" for="add-nature-selector"><s:message code="~de.unibamberg.minf.dme.form.nature.terminals.names" />:</label>
			<div class="col-sm-8">
				<div class="radio">
				  <label>
				    <input type="radio" name="element-naming" value="unchanged" checked="checked">
				    <s:message code="~de.unibamberg.minf.dme.form.nature.terminals.label_unchanged" />
				  </label>
				</div>
				<div class="radio">
				  <label>
				    <input type="radio" name="element-naming" value="first_lower">
				    <s:message code="~de.unibamberg.minf.dme.form.nature.terminals.label_first_lower" />
				  </label>
				</div>
				<div class="radio">
				  <label>
				    <input type="radio" name="element-naming" value="all_upper">
				    <s:message code="~de.unibamberg.minf.dme.form.nature.terminals.label_all_upper" />
				  </label>
				</div>
				<div class="radio">
				  <label>
				    <input type="radio" name="element-naming" value="all_lower">
				    <s:message code="~de.unibamberg.minf.dme.form.nature.terminals.label_all_lower" />
				  </label>
				</div>
			</div>
		</div>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~de.unibamberg.minf.common.link.add" /></button>
		</div>
	</div>
</form>