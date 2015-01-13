<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<tiles:useAttribute id="fluidLayout" name="fluidLayout" classname="java.lang.String" />
<c:set value="/auth/saml/admin" var="pathPrefix" />

<div class="row<c:if test="${fluidLayout}">-fluid</c:if>">
	<div class="span10 offset1 content-wrapper no-margin">
		<div id="content" class="primary-area">
			<ul class="breadcrumb">
			   	<li><a href="<c:url value="/"/>">Start</a> <span class="divider">/</span></li>
			   	<li class="active">Metadata</li>
			</ul>
			<h1>Metadata</h1>
			<p>
				<a class="btn btn-primary" href="<c:url value="${pathPrefix}/refresh"/>"><i class="icon-refresh icon-white"></i> Refresh metadata</a>
				<a class="btn" href="<c:url value="${pathPrefix}/generate"/>"><i class="icon-plus icon-black"></i> Generate new SP</a>
			</p>
			
			<h3>Default hosted service provider</h3>
			<p>
			    <c:forEach var="entity" items="${hostedSP}">
			        <a href="<c:url value="${pathPrefix}/display"><c:param name="entityId" value="${hostedSP}"/></c:url>">
			            <c:out value="${hostedSP}"/></a>
			    </c:forEach>
			    <c:if test="${empty spList}"> - </c:if>
			    <br/>
			    <small><i>Default service provider is available without selection of alias.</i></small>
			</p>
			
			<h3>Service providers</h3>
			<p>
			   <c:forEach var="entity" items="${spList}">
			        <a href="<c:url value="${pathPrefix}/display"><c:param name="entityId" value="${entity}"/></c:url>">
			            <c:out value="${entity}"/></a><br/>
			    </c:forEach>
			    <c:if test="${empty spList}"> - </c:if>
			</p>
			
			<h3>Identity providers</h3>
			<p>
			    <c:forEach var="entity" items="${idpList}">
			        <a href="<c:url value="${pathPrefix}/display"><c:param name="entityId" value="${entity}"/></c:url>">
			            <c:out value="${entity}"/></a><br/>
			    </c:forEach>
			    <c:if test="${empty idpList}"> - </c:if>
			</p>
			
			<h3>Metadata providers</h3>
			<p>
			    <c:forEach var="entity" items="${metadata}" varStatus="status">
			        <a href="<c:url value="${pathPrefix}/provider"><c:param name="providerIndex" value="${status.index}"/></c:url>">
			            <c:out value="${entity}"/></a><br/>
			    </c:forEach>
			</p>
		</div>
	</div>
</div>