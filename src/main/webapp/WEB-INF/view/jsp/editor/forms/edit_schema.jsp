<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="datamodelImpl" class="form-horizontal" >
	<div class="form-header">
		<c:choose>
			<c:when test="${datamodelImpl.id!=null && datamodelImpl.id!=''}">
				<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.schema.edit" /></h3>
			</c:when>
			<c:otherwise>
				<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.schema.create" /></h3>
			</c:otherwise>
		</c:choose>
		<sf:hidden path="id"/>
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="control-label col-sm-3" for="datamodelImpl_name"><s:message code="~de.unibamberg.minf.dme.model.schema.label" />:</label>
			<div class="col-sm-9">
				<sf:input path="name" class="form-control" id="datamodelImpl_name" />
				<sf:errors path="name" cssClass="error" />
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="datamodelImpl_id"><s:message code="~de.unibamberg.minf.common.model.id" />:</label>
			<div class="col-sm-9">
				<div class="input-group">
			      <input type="text" class="form-control" id="datamodelImpl_updateId" name="updateId" value="${datamodelImpl.id}" disabled="disabled" placeholder="<s:message code='~de.unibamberg.minf.common.view.new_id_onsave' />" />
			      <span class="input-group-btn">
			        <button class="btn btn-default" onclick="$('#datamodelImpl_updateId').removeProp('disabled'); $('#id-rename-hint').removeClass('hide'); $('#datamodelImpl_updateId').focus();" type="button"><s:message code="~de.unibamberg.minf.common.link.edit" /></button>
			      </span>
			    </div>
				<div id="id-rename-hint" class="alert alert-sm alert-warning alert-persist hide" style="margin-top: 10px;"><i class="fa fa-exclamation-triangle fa-color-warning" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.notification.id_rename_hint" /></div>
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="datamodelImpl_description"><s:message code="~de.unibamberg.minf.dme.model.schema.description" />:</label>
			<div class="col-sm-9">
				<sf:textarea path="description" class="form-control" rows="4" id="datamodelImpl_description" />
			</div>
		</div>
		<div class="form-group">
			<div class="col-sm-offset-3 col-sm-9">
				<label>
					<input type="checkbox" name="readOnly" id="readOnly"<c:if test="${readOnly}"> checked="checked"</c:if>> <s:message code="~de.unibamberg.minf.common.model.readonly" />
				</label>
			</div>
		</div>	
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button class="btn btn-primary start form-btn-submit" type="submit"><s:message code="~de.unibamberg.minf.common.link.save" /></button>
		</div>
	</div>
</sf:form>
