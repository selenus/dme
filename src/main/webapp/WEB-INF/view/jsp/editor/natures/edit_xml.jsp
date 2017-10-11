<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="xmlNature" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~ Edit xml nature</h3>	
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-3 control-label" for="function_name">~Namespaces:</label>
			<div class="col-sm-8">
				<table id="edit-nature-namespaces" style="width: 100%">
					<tbody style="display: block; border: 1px solid #CCC; padding: 2px; max-height: 300px; overflow-y: auto; width: 100%">
						<c:if test="${xmlNature.namespaces!=null}">
							<c:forEach items="${xmlNature.namespaces}" var="xmlNs" varStatus="status">
								<tr class="edit-nature-namespace-row">
									<td width="100px;" style="min-width: 100px;"><input type="text" class="form-control input-sm edit-nature-prefix" value="${xmlNs.prefix}"></td>
									<td width="99%;"><input type="text" class="form-control input-sm edit-nature-url" value="${xmlNs.url}"></td>
									<td width="10px;"><a href="#" class="btn btn-link"><i class="fa fa-minus-circle fa-color-warning" aria-hidden="true" onclick="$(this).closest('tr').remove(); return false;"></i></a></td>
								</tr>
							</c:forEach>
						</c:if>
						<tr class="edit-nature-namespace-row">
							<td width="100px;"><input type="text" class="form-control input-sm edit-nature-prefix"></td>
							<td><input type="text" class="form-control input-sm edit-nature-url"></td>
							<td width="10px;"><a href="#" class="btn btn-link"><i class="fa fa-minus-circle fa-color-warning" aria-hidden="true" onclick="$(this).closest('tr').remove(); return false;"></i></a></td>
						</tr>
						<tr id="edit-nature-namespace-placeholder" class="edit-nature-namespace-row" style="display: none;">
							<td width="100px;"><input type="text" class="form-control input-sm edit-nature-prefix"></td>
							<td><input type="text" class="form-control input-sm edit-nature-url"></td>
							<td width="10px;"><a href="#" class="btn btn-link"><i class="fa fa-minus-circle fa-color-warning" aria-hidden="true" onclick="$(this).closest('tr').remove(); return false;"></i></a></td>
						</tr>
					</tbody>
				</table>
				<a href="#" class="btn btn-link" onclick="$('#edit-nature-namespace-placeholder').clone().removeProp('id').appendTo('#edit-nature-namespaces').show().find('input')[0].focus();"><i class="fa fa-plus" aria-hidden="true"></i> ~Add namespace</a>
			</div>
		</div>
		<legend>Advanced options:</legend>
		<div class="form-group">
			<label class="col-sm-3 control-label" for="function_name">~Root selector:</label>
			<div class="col-sm-8">
				<sf:input path="recordPath" class="form-control" />
				<div class="alert alert-warning alert-sm" role="alert" style="margin-top: 10px;">~ hint break parse</div>
			</div>
		</div>		
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~de.unibamberg.minf.common.link.save" /></button>
		</div>
	</div>
</sf:form>