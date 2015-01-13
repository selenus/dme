<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="showSearchBar" name="showSearchBar" classname="java.lang.String" />
<s:url value="/search/simple" var="simple_search_url" />
<s:url value="/search/extended/switch" var="extended_search_url" />

<c:choose>
	<c:when test="${showSearchBar==true}">
		<div class="row-fluid searchbar-dariah hidden-phone">
			<div class="span10 offset1">
				<div class="pull-right" style="overflow: hidden;">
					<div>
						<a href="${extended_search_url}" style="padding: 0 0 0 10px" type="submit" class="btn btn-link">
							<span style="font-size: 85%"><s:message code="~de.dariah.genericsearch.view.search.extended.title" /></span>
						</a>
					</div>
					<div class="input-append">
						<form class="form-inline" method="post" action="${simple_search_url}" >
							<input name="expression" id="expression" type="text">
							<button class="btn btn-primary"><i class="icon-search icon-white"></i></button>
						</form>
					</div>
				</div>
			</div>
		</div>
	</c:when>
	<c:otherwise>
		<div style="height: 70px;"></div>
	</c:otherwise>
</c:choose>