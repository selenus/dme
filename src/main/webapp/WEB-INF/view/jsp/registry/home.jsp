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
				<div class="pull-right dariah-flower-white-45"><s:message code="~eu.dariah.de.minfba.schereg.title" /></div>
			</div>
			<div class="col-sm-6 col-lg-7 col-sm-offset-1">
				<h1><s:message code="~eu.dariah.de.minfba.schereg.registry.title" /></h1>
			</div>
		</div>
	</div>
</div>
<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<div id="main-content-wrapper" class="col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li><a href='<s:url value="/" />' target="_self"><s:message code="~eu.dariah.de.minfba.schereg.title" /></a></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.registry.title" /></li>
			</ul>
			<div id="main-content">
				<div class="row">
					<div id="schema-table-container" class="col-lg-5">
						<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.model.schema.title" />&nbsp;</h2>
						<button id="btn-add-schema" class="btn btn-default btn-sm">
							<span class="glyphicon glyphicon-plus"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.add_schema" />
						</button>
						<div class="pull-right">
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
							<table id="schema-table" class="table table-striped table-bordered table-condensed">
								<thead>
									<tr>
										<th></th> <!-- Status -->
										<th><s:message code="~eu.dariah.de.minfba.schereg.model.schema.label" /></th>
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
					<div id="mapping-table-container" class="col-lg-7">
						<h2 class="pull-left"><s:message code="~eu.dariah.de.minfba.schereg.model.mapping.title" />&nbsp;</h2>
						<button id="btn-add-mapping" class="btn btn-default btn-sm">
							<span class="glyphicon glyphicon-plus"></span> <s:message code="~eu.dariah.de.minfba.schereg.button.add_mapping" />
						</button>
						<div class="pull-right">
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
							<table id="mapping-table" class="table table-striped table-bordered table-condensed">
								<thead>
									<tr>
										<th></th> <!-- Status -->
										<th><s:message code="~eu.dariah.de.minfba.schereg.model.mapping.source" /></th>
										<th><s:message code="~eu.dariah.de.minfba.schereg.model.mapping.target" /></th>
										<th></th> <!-- Actions -->
									</tr>
								</thead>
								<tbody>
								<tr>
									<td colspan="4" align="center"><s:message code="~eu.dariah.de.minfba.common.view.no_data_fetched_yet" /></td>
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