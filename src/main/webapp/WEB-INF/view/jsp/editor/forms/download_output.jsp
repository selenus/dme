<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.download.title" /></h3>
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-4 control-label" for="data_model"><s:message code="~eu.dariah.de.minfba.schereg.model.datamodel" /></label>
			<div class="col-sm-8">
				<c:choose>
					<c:when test="${targetModel!=null}">
						<div class="radio">
							<label>
						    	<input type="radio" name="download-model-radios" id="download-data-radio1" value="source" checked>
						    	<s:message code="~eu.dariah.de.minfba.schereg.model.datamodel.source_model" />: ${sourceModel}
						  	</label>
						</div>
						<div class="radio">
							<label>
						    	<input type="radio" name="download-model-radios" id="download-data-radio2" value="target">
						    	<s:message code="~eu.dariah.de.minfba.schereg.model.datamodel.target_model" />: ${targetModel}
							</label>
						</div>
					</c:when>
					<c:otherwise>
						<label class="control-label">
						    ${sourceModel}
						</label>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
		
		<c:choose>
			<c:when test="${datasetCount>0}">
				<div class="form-group">
					<label class="col-sm-4 control-label" for="data_model"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.download.set" />:</label>
					<div class="col-sm-8">
						<div class="radio">
							<label>
						    	<input type="radio" name="download-data-radios" id="download-data-radio1" value="single" checked>
						    	<s:message code="~eu.dariah.de.minfba.schereg.editor.sample.download.set.single" />: 
						    	<a href="#" onclick="editor.getPrevSampleResource(); return false;" class="btn-sample-prev-resource <c:if test="${datasetCurrent==0}"> disabled</c:if>"><i class="fa fa-chevron-left" aria-hidden="true"></i></a>	
						    	<span class="sample-output-counter">${datasetCurrent+1} / ${datasetCount}</span>
						    	<a href="#" onclick="editor.getNextSampleResource(); return false;" class="btn-sample-next-resource <c:if test="${datasetCurrent==datasetCount-1}"> disabled</c:if>"><i class="fa fa-chevron-right" aria-hidden="true"></i></a>	
						  	</label>
						</div>
						<c:if test="${datasetCount>0}">
							<div class="radio">
								<label>
							    	<input type="radio" name="download-data-radios" id="download-data-radio2" value="all">
							    	<s:message code="~eu.dariah.de.minfba.schereg.editor.sample.download.set.all" arguments="${datasetCount}" />
								</label>
							</div>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="col-sm-4 control-label" for="data_model"><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.download.format" />:</label>
					<div class="col-sm-8">
						<div class="radio">
							<label>
						    	<input type="radio" name="download-format-radios" id="download-format-radio1" value="xml" checked>
						    	XML
							</label>
						</div>
						<div class="radio">
							<label>
						    	<input type="radio" name="download-format-radios" id="download-format-radio2" value="json">
						    	JSON
							</label>
						</div>
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-offset-4 col-sm-8">
						<button onclick="editor.createDownload(); return false;" class="btn btn-primary"><s:message code="~eu.dariah.de.minfba.common.link.create_download_link" /></button>
					</div>
				</div>
				<div class="form-group">
					<div id="download-link-container" class="col-sm-offset-4 col-sm-8"></div>
				</div>
			</c:when>
			<c:otherwise>
				<div class="alert alert-warning"><strong><s:message code="~eu.dariah.de.minfba.schereg.editor.sample.download.no_data.head" /></strong> <s:message code="~eu.dariah.de.minfba.schereg.editor.sample.download.no_data.body" /></div>
			</c:otherwise>
		</c:choose>
	
		
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.close" /></button>
		</div>
	</div>
</div>
