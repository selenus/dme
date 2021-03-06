<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tpl" tagdir="/WEB-INF/tags" %>

<header role="banner" class="navbar navbar-default navbar-static-top <c:if test="${navbarInverse==true}">navbar-inverse</c:if>">
	<div class="container<c:if test="${fluidLayout==true}">-fluid</c:if>">
		<div class="row">
			<div class="col-sm-11 col-sm-offset-1">
		    	<div class="navbar-header">
		      		<button data-target=".bs-navbar-collapse" data-toggle="collapse" type="button" class="navbar-toggle">
		        		<span class="sr-only">Toggle navigation</span>
		        		<span class="icon-bar"></span>
		        		<span class="icon-bar"></span>
		        		<span class="icon-bar"></span>
		      		</button>
		      		<a class="navbar-brand" href="../">DARIAH-DE</a>
		    	</div>
		    	<nav role="navigation" class="collapse navbar-collapse bs-navbar-collapse">
		    		<!-- Main top navigation built from configuration -->
		    		<ul class="nav navbar-nav">
		    			<c:forEach items="${_nav.rootItems}" var="_navItem">
		    				<c:choose>
				    			<c:when test="${_navItem.subItems!=null && fn:length(_navItem.subItems)>0}">
					    			<li class="dropdown <c:if test="${_navItem.active || _navItem.childActive}"> active</c:if>">					    			
					    				<a aria-expanded="false" role="button" data-toggle="dropdown" class="dropdown-toggle" href="#">
											<c:if test="${_navItem.glyphicon!=null && fn:length(_navItem.glyphicon)>0}">
												<span class="${_navItem.glyphicon}"></span>&nbsp;
											</c:if>
											<s:message code="${_navItem.displayCode}" />
											<span class="caret"></span>
										</a>
										<ul role="menu" class="dropdown-menu">
											<tpl:topNav navItem="${_navItem}" />
										</ul>
					    			</li>
				    			</c:when>
				    			<c:otherwise>
				    				<li<c:if test="${_navItem.active || _navItem.childActive}"> class="active"</c:if>>
				    					<a href="<s:url value='${_navItem.linkUrl}'/>">
				    						<c:if test="${_navItem.glyphicon!=null && fn:length(_navItem.glyphicon)>0}">
												<span class="${_navItem.glyphicon}"></span>&nbsp;
											</c:if>
				    						<s:message code="${_navItem.displayCode}" />
				    					</a>
				    				</li>
				    			</c:otherwise>
			    			</c:choose>
						</c:forEach>
		    		</ul>
		    
		    		<!-- Elements for language selection and login/logout -->
					<ul class="nav navbar-nav navbar-right">
						<li class="dropdown">
							<a aria-expanded="false" role="button" data-toggle="dropdown" class="dropdown-toggle" href="#">
								<span class="glyphicon glyphicon-globe"></span> Language <span class="caret"></span>
							</a>
							<ul role="menu" class="dropdown-menu">
								<c:forEach items="${_LANGUAGES}" var="_LANGUAGE">
			    					<li role="presentation">
			    						<a href="?lang=${_LANGUAGE.key}">
			    							<img src="<s:url value="/resources/img/flags/flag_${_LANGUAGE.key}.png" />" height="32" width="32" alt="${_LANGUAGE.value}" /> ${_LANGUAGE.value}
			    						</a>
			    					</li>
								</c:forEach>
							</ul>
						</li>
						
						<c:set var="currentUrl" value="${requestScope['javax.servlet.forward.request_uri']}" />
						<li id="login"<c:if test="${_auth!=null && _auth.auth==true}"> style="display: none;"</c:if>><a href="<s:url value='/login?url=${currentUrl}' />" ><span class="glyphicon glyphicon-log-in"></span>&nbsp;Login</a></li>
					<li id="logout"<c:if test="${_auth==null || _auth.auth==false}"> style="display: none;"</c:if>><a href="<s:url value='/logout?url=${currentUrl}' />" ><span class="glyphicon glyphicon-log-out"></span>&nbsp;Logout</a></li>		
					</ul>
		    	</nav>
			</div>
		</div>
		<input id="currentUrl" type="hidden" value="${requestScope['javax.servlet.forward.request_uri']}" />
		<input id="baseUrl" type="hidden" value="<s:url value="/" />" />
		<input id="baseUrl2" type="hidden" value="<s:url value="/{}" />" />
	</div>
</header>