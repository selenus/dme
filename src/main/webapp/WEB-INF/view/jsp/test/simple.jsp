<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<tiles:importAttribute name="fluidLayout" />

<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<div id="main-content-wrapper" class="col-xs-12 col-sm-10 col-sm-offset-1">
			<ul class="breadcrumb">
				<li>Hauptebene</li>
				<li class="active">Subebene</li>
			</ul>
			<div id="main-content">
				<h1>Überschrift 1</h1>
				<p>...</p>
				<h2>Überschrift 2</h2>
				<p><s:message code="~testtext" /> </p>
			</div>
		</div>
	</div>
</div>