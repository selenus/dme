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
				<li><s:message code="~eu.dariah.de.minfba.schereg.title" /></li>
				<li class="active"><s:message code="~eu.dariah.de.minfba.schereg.schemas.title_short" /></li>
			</ul>
			<div id="main-content">
				<button id="btn-add-schema" class="btn btn-default btn-sm pull-right">
					<span class="glyphicon glyphicon-plus"></span> <s:message code="~eu.dariah.de.minfba.schereg.schemas.button.add" />
				</button>
				<h2><s:message code="~eu.dariah.de.minfba.schereg.schemas.title" /></h2>
				<div class="row">
					<div class="col-md-7">
						<table id="schema-table" class="table table-striped table-bordered table-condensed">
							<thead>
								<tr>
									<th></th> <!-- Status -->
									<th><s:message code="~eu.dariah.de.minfba.schereg.schemas.model.label" /></th>
									<th><s:message code="~eu.dariah.de.minfba.schereg.schemas.model.type" /></th>
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
					<div class="col-md-5 data-details-container" role="tabpanel">
						<!-- Nav tabs -->
						<ul class="nav nav-tabs" role="tablist">
							<li role="presentation" id="tab-schema-activity" class="active"><a href="#schema-activity" aria-controls="schema-activity" role="tab" data-toggle="tab">Activity</a></li>
							<li role="presentation" id="tab-schema-metadata" class="hide"><a href="#schema-metadata" aria-controls="schema-metadata" role="tab" data-toggle="tab">Metadata</a></li>
							<li role="presentation" id="tab-schema-elements" class="hide"><a href="#schema-elements" aria-controls="schema-elements" role="tab" data-toggle="tab">Elements</a></li>
						</ul>

						<!-- Tab panes -->
						<div class="tab-content">
							<div role="tabpanel" class="tab-pane active" id="schema-activity">Activity</div>
							<div role="tabpanel" class="tab-pane" id="schema-metadata">Metadata</div>
							<div role="tabpanel" class="tab-pane" id="schema-elements">Elements</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>