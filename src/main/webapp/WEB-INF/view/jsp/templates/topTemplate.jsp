<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div class="navbar navbar-inverse navbar-static-top navbar-dariah" id="top">
	<div class="navbar-inner">
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span1"></div>
				<div class="span10">
					<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</a>
					<!-- <a class="brand" href="<s:url value='/' />"><img src="<s:url value="/resources/img/dariah_white.png" />" height="35" width="121" alt="DARIAH-DE" /></a> -->
					<div class="nav-collapse collapse">
						<ul class="nav pull-right">									
							<li class="dropdown">
								<a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-globe icon-white"></i> 
							    	<s:message code="~de.dariah.common.view.language" arguments="${pageContext.response.locale}" /> <span class="caret"></span>
							    </a>
								<ul class="dropdown-menu">
									<c:forEach items="${_LANGUAGES}" var="_LANGUAGE">
										<li><a href="?lang=${_LANGUAGE.key}"><img src="<s:url value="/resources/img/flag_${_LANGUAGE.key}.png" />" height="32" width="32" alt="English" /> ${_LANGUAGE.value}</a></li>
									</c:forEach>
								</ul>
							</li>
							<li>
								<c:choose>
									<c:when test="${__auth==true}">
										<li><a href="<s:url value='/saml/logout' />" ><i class="icon-signout"></i> Logout</a></li>
									</c:when>
									<c:otherwise>
										<li><a href="<s:url value='/saml/login' />" ><i class="icon-signin"></i> Login</a></li>
									</c:otherwise>						
								</c:choose>
							
								<!-- <a class="pull-right" href="#">
									<i class="icon-signin"></i> <s:message code="~de.dariah.common.view.login" />
								</a> -->
							</li>	
						</ul>
						<ul class="nav">
							<li><a href="<s:url value='/' />" class="brand">DARIAH-DE</a></li>
							<li <c:choose><c:when test="${section=='admin' || section=='crawling' || section=='help' || section=='auth'}"> class="active dropdown"</c:when><c:otherwise> class="dropdown"</c:otherwise></c:choose>>
								<a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-white icon-star"></i> <s:message code="~myRegistry.title" /> <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li class="nav-header">Personal Settings</li>
									<li><a href="<s:url value='/user/profile'/>"> My Profile</a></li>
									<c:if test="${__authLevel >= __roles['ROLE_USER'].level}">
										<li><a href="<s:url value='#'/>"> My Projects</a></li>
										<li><a href="<s:url value='#'/>"> My Schemas</a></li>
										<li><a href="<s:url value='#'/>"> My Crosswalks</a></li>
									</c:if>
									<!--<c:if test="${__authLevel >= __roles['ROLE_CONTENTADMIN'].level}">-->
										<li class="nav-header">Users, Roles and Privileges</li>
										<li><a href="<s:url value='/auth/management' />"> Authorization Management</a></li>
									<!--</c:if>-->
										<li><a href="<s:url value='/auth/saml/admin' />"> SAML Metadata Management</a></li>
									
									<c:if test="${__authLevel >= __roles['ROLE_ADMINISTRATOR'].level}">
									<li class="nav-header">Administration</li>
										<li><a href="<s:url value='/admin/log'/>"> <s:message code="~page.top.navigation.log" /></a></li>
										<li><a href="<s:url value='/monitoring'/>"> <s:message code="~page.top.navigation.monitoring" /></a></li>
										<li><a href="<s:url value='/admin/userlog'/>"> <s:message code="~page.top.navigation.userlog" /></a></li>
									</c:if>
									<li class="divider"></li>
									<li><a href="<s:url value='#' />"> Help</a></li>
								</ul>
							</li>
							<li class="dropdown<c:if test="${section=='search'}"> active</c:if>">
								<a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-white icon-search"></i> <s:message code="~de.dariah.genericsearch.view.search.title" /> <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="http://dev3.dariah.eu/search/simple"> <s:message code="~de.dariah.genericsearch.view.search.simple.title" /></a></li>
									<li><a href="http://dev3.dariah.eu/search/extended"> <s:message code="~de.dariah.genericsearch.view.search.extended.title" /> </a></li>
								</ul>
							</li>
							<li <c:if test="${section=='schema'}"> class="active"</c:if>><a href="<s:url value='/schema' />"><s:message code="schemaRegistry.title" /></a></li>
							<li <c:if test="${section=='mapping'}"> class="active"</c:if>><a href="<s:url value='/mapping' />"><s:message code="crosswalkRegistry.title" /></a></li>
						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>