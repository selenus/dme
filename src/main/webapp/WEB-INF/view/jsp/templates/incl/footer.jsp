<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<footer role="contentinfo" class="footer">
	<span>&copy; <fmt:formatDate value="${date}" pattern="yyyy" /> DARIAH-DE</span>
	<ul class="pull-right inline">
		<li><a href="#"><s:message code="~de.unibamberg.minf.common.link.privacy" /></a></li>
		<li><a href="#"><s:message code="~de.unibamberg.minf.common.link.legal_information" /></a></li>
		<li><a href="#"><s:message code="~de.unibamberg.minf.common.link.contact" /></a></li>
	</ul>
</footer>