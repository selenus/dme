<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div role="tabpanel" class="tab-pane <c:if test="${currentSampleCount==0}"> active</c:if>" id="sample-input-container">
	<c:set var="sampleInputSet" value="${session.sampleInput!=null && session.sampleInput!=''}" />
	<c:set var="sampleInputDisplayed" value="${inputSet && sampleInputOversize==false}" />
	
	<div id="sample-input-textarea-container">
		
		<input type="hidden" id="sample-set" value="${sampleInputSet}">
		
		<div id="sample-input-inputtypes">
		  <label><input type="radio" name="inputType" id="inputTypeXml" value="XML" checked>XML</label>
		  <label><input type="radio" name="inputType" id="inputTypeCsv" value="CSV">CSV</label>
		  <label><input type="radio" name="inputType" id="inputTypeText" value="TEXT">Text</label>
		</div>
		
		<div id="sample-input-textarea-placeholder" onclick="editor.handleEnterTextarea(); return false;" class="codearea height-sized-element<c:if test="${sampleInputDisplayed}"> hide</c:if>">
		
			<c:choose>
				<c:when test="${!sampleInputSet}">
					<p><s:message code="~de.unibamberg.minf.dme.editor.sample.placeholder" /></p>
				</c:when>
				<c:otherwise>
					<c:set var="showlink"><a onclick="editor.loadSampleInput(); return false;"><i class="fa fa-eye" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.editor.sample.placeholder_set.show" /></a></c:set>
					<c:set var="downlink"><a onclick="editor.downloadSampleInput(); return false;"><i class="fa fa-download" aria-hidden="true"></i> <s:message code="~de.unibamberg.minf.dme.editor.sample.placeholder_set.download" /></a></c:set>
					<p><s:message code="~de.unibamberg.minf.dme.editor.sample.placeholder_set" arguments="${showlink}, ${downlink}" /></p>				
				</c:otherwise>
			</c:choose>										
		</div>
	
		<c:choose>
			<c:when test="${sampleInputDisplayed}">
				<textarea id="sample-input-textarea" class="sample-textarea form-control height-sized-element" rows="3">${session.sampleInput}</textarea>
			</c:when>
			<c:otherwise>
				<textarea id="sample-input-textarea" class="sample-textarea form-control height-sized-element hide" rows="3"></textarea>
			</c:otherwise>
		</c:choose>
	</div>
	
</div>