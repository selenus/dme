<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<tiles:importAttribute name="fluidLayout" />

<div class="jumbotron jumbotron-small">
	 <div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
		<div class="row">
			<!-- Notifications -->
			<div id="notifications-area" class="col-sm-10 col-sm-offset-1"></div>
			<div class="xs-hidden sm-visible col-sm-3 col-lg-2 col-sm-offset-1">
				<div class="pull-right dariah-flower-white-45"><s:message code="~eu.dariah.de.minfba.schereg.title_short" /></div>
			</div>
			<div class="col-sm-6 col-lg-7 col-sm-offset-1">
				<h1><s:message code="~eu.dariah.de.minfba.schereg.vocabulary.title" /></h1>
			</div>
		</div>
	</div>
</div>
<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li><a href='<s:url value="/" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.title" /></a></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.vocabulary.title" /></li>
			</ul>
			<div id="main-content">
				<div class="row">
					<div id="vocabulary-table-container" class="col-lg-6">
						<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.vocabulary.title" />&nbsp;</h2>
						<div class="pull-right">
							<button id="btn-add-vocabulary" class="btn btn-default btn-sm pull-left">
								<span class="glyphicon glyphicon-plus"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.add_vocabulary" />
							</button>
							<div class="data-table-filter">
								<input type="text" class="form-control input-sm" placeholder='<s:message code="~eu.dariah.de.minfba.common.link.filter"/>'>
							</div>
							<div class="data-table-count">
								<select class="form-control input-sm">
								  <option>10</option>
								  <option>25</option>
								  <option>50</option>
								  <option>100</option>
								  <option><s:message code="~eu.dariah.de.minfba.common.link.all"/></option>
								</select>
							</div>					
						</div>
						<div class="clearfix">
							<table id="vocabulary-table" class="table table-striped table-bordered table-condensed">
								<thead>
									<tr>
										<th></th> <!-- Status -->
										<th><s:message code="~eu.dariah.de.minfba.schereg.vocabulary.model.label" /></th>
										<th></th> <!-- Actions -->
									</tr>
								</thead>
								<tbody>
								<tr>
									<td colspan="3" align="center"><s:message code="~eu.dariah.de.minfba.common.view.no_data_fetched_yet" /></td>
								</tr>
								</tbody>
							</table>
						</div>
					</div>
					<div id="vocabulary-item-table-container" class="col-lg-6">
						<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.vocabulary.item.title" />&nbsp;</h2>
						<div id="vocabulary-item-table-hide" style="clear: both;">
							<s:message code="~eu.dariah.de.minfba.schereg.vocabulary.item.none_selected" />
						</div>
						<div id="vocabulary-item-table-display" class="hide">
							<div class="pull-right">
								<button id="btn-add-mapping" onclick="vocabularyTable.itemTable.triggerAdd();" class="btn btn-default btn-sm pull-left">
									<span class="glyphicon glyphicon-plus"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.add_vocabulary_item" />
								</button>
								<div class="data-table-filter">
									<input type="text" class="form-control input-sm" placeholder='<s:message code="~eu.dariah.de.minfba.common.link.filter"/>'>
								</div>
								<div class="data-table-count">
									<select class="form-control input-sm">
									  <option>10</option>
									  <option>25</option>
									  <option>50</option>
									  <option>100</option>
									  <option><s:message code="~eu.dariah.de.minfba.common.link.all"/></option>
									</select>
								</div>					
							</div>
							<div class="clearfix">
								<table id="vocabulary-item-table" class="table table-striped table-bordered table-condensed">
									<thead>
										<tr>
											<th></th> <!-- Status -->
											<th><s:message code="~eu.dariah.de.minfba.schereg.vocabulary.item.model.id" /></th>
											<th></th> <!-- Actions -->
										</tr>
									</thead>
									<tbody>
									<tr>
										<td colspan="3" align="center"><s:message code="~eu.dariah.de.minfba.common.view.no_data_fetched_yet" /></td>
									</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>