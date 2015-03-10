<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tiles:importAttribute name="fluidLayout" />

<div class="jumbotron">
	 <div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
		<div class="row">
			<div class="col-xs-12">
				<h1>Blu</h1>
				<p>bla</p>
			</div>
		</div>
	</div>
</div>
<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
	<div class="row">
		<div id="main-content-wrapper" class="col-xs-12">
			<ul class="breadcrumb">
				<li>Lorem ipsum dolor</li>
				<li class="active">sit amet</li>
			</ul>
			<div id="main-content">
				<h1>sit amet</h1>
				<p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>
			</div>
		</div>
	</div>
</div>