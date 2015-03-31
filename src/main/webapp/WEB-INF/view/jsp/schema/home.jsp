<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<tiles:importAttribute name="fluidLayout" />

<div class="jumbotron jumbotron-small">
	 <div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
		<div class="row">
			<div class="xs-hidden sm-visible col-sm-3 col-lg-2 col-sm-offset-1">
				<div class="pull-right dariah-flower-white-45"><s:message code="~eu.dariah.de.minfba.schereg.title" /></div>
			</div>
			<div class="col-sm-6 col-lg-7 col-sm-offset-1">
				<h1><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_short" /></h1>
			</div>
		</div>
	</div>
</div>
<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<!-- Notifications -->
		<div id="notifications-area" class="col-sm-10 col-sm-offset-1"></div>
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li><a href='<s:url value="/" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.title" /></a></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_short" /></li>
			</ul>
			<div id="main-content">
				<div class="row">
					<div class="col-md-7">
						<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title" /></h2>
						<div id="data-details-container">
							<div class="pull-right">
								<div class="data-table-filter">
									<input type="text" class="form-control" placeholder='<s:message code="~eu.dariah.de.minfba.common.view.common.filter"/>'>
								</div>
								<div class="data-table-count">
									<select class="form-control">
									  <option>10</option>
									  <option>25</option>
									  <option>50</option>
									  <option>100</option>
									  <option><s:message code="~eu.dariah.de.minfba.common.view.common.all"/></option>
									</select>
								</div>
								<button id="btn-add-schema" class="btn btn-default btn-sm">
									<span class="glyphicon glyphicon-plus"></span> <s:message code="~eu.dariah.de.minfba.schereg.schemas.button.add" />
								</button> 								
							</div>
							<table id="schema-table" class="table table-striped table-bordered table-condensed">
								<thead>
									<tr>
										<th></th> <!-- Status -->
										<th><s:message code="~eu.dariah.de.minfba.schereg.schemas.model.label" /></th>
										<th><s:message code="~eu.dariah.de.minfba.schereg.schemas.model.type" /></th>
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
					<div class="col-md-5 data-details-container" role="tabpanel">
						<ul class="nav nav-tabs" role="tablist">
							<li role="presentation" id="tab-schema-activity" class="active"><a href="#schema-activity" aria-controls="schema-activity" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.common.activity" /></a></li>
							<li role="presentation" id="tab-schema-metadata" class="hide"><a href="#schema-metadata" aria-controls="schema-metadata" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.schemas.model.schema" /></a></li>
							<li role="presentation" id="tab-schema-elements" class="hide"><a href="#schema-elements" aria-controls="schema-elements" role="tab" data-toggle="tab"><s:message code="~eu.dariah.de.minfba.schereg.schemas.model.elements" /></a></li>
						</ul>
						<div class="tab-content">
							<div role="tabpanel" class="tab-pane active" id="schema-activity">...</div>
							<div role="tabpanel" class="tab-pane" id="schema-metadata">...</div>
							<div role="tabpanel" class="tab-pane" id="schema-elements">...</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>