<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="util" uri="/WEB-INF/taglib/util.tld" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<s:url value="/admin/auth/edit" var="edit_link" />
<s:url value="/admin/auth/new" var="new_link" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 main-content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
			   	<li><a href="<c:url value="/"/>">Start</a> <span class="divider">/</span></li>
			   	<li class="active">Authorization Management</li>
			</ul>
			<h1>Authorization Management</h1>
			<div>
				<div class="navbar">
					<div class="navbar-inner">
						<a class="brand" href="#">Options</a>
						<a id="btn-new-rolemapping" href="${new_link}" class="btn">
							<i class="icon-globe icon-black"></i> Assign group
						</a>
					</div>
				</div>
				
				<c:forEach items="${hashedMappings}" var="mappings">
					<h3>${mappings.key.authority}</h3>
					<p style="font-style: italic;">${mappings.key.description}</p>
					<div>
						<c:if test="${fn:length(mappings.value)>0}">
							<table class="table table-bordered table-striped table-hover" style="table-layout: fixed;">
								<thead>
									<tr>
										<th style="width: 70px;"></th>
										<th>Endpoint</th>
										<th>Name</th>
										<th style="width: 170px;">Expires</th>
										<th style="width: 50px;">Actions</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach items="${mappings.value}" var="mapping">
										<c:set value="${util:isBeforeNow(mapping.expires,false)}" var="expired" />
									
										<tr id="model_id_${mapping.id}" <c:choose>
												<c:when test="${mapping.active==false||expired==true}">class="warning"</c:when>
												<c:otherwise>class="success"</c:otherwise>
											</c:choose>>
											<td>
												<c:choose>
													<c:when test="${util:isBeforeNow(mapping.expires,false)==true || mapping.active==false}">
														<c:if test="${util:isBeforeNow(mapping.expires,false)==true}">
															<span class="label label-warning">Expired</span>
														</c:if>
														<c:if test="${mapping.active==false}">
															<span class="label label-warning">Inactive</span>
														</c:if>
													</c:when>
													<c:otherwise>
														<span class="label label-success">Ok</span>
													</c:otherwise>
												</c:choose>
											</td>
											<td>${mapping.endpoint}</td>
											<td>${mapping.name}</td>
											<td><joda:format value="${mapping.expires}" style="SM" /></td>
											<td>
												<a href="${edit_link}?id=${mapping.id}"><i class="icon-edit icon-black"></i> </a>
												<a href="#" onclick="if(confirm('Delete this role mapping?')){ deleteObject(${mapping.id}); return false;}"><i class="icon-remove icon-black"></i> </a>
											</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</c:if>
					</div>
					<hr />
				</c:forEach>
			</div>	
		</div>
	</div>
</div>