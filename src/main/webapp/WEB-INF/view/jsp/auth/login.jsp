<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<div id="content">
	<div id="login_form_wrapper">
		<h1><s:message code="${form_title}" /></h1>
		<c:if test="${not empty error}">
			<div id="login_error"><s:message code="${error}" /></div>
		</c:if>
		<form id="content_login_form" action="<s:url value="/auth_security_check" />" method="post" >
			<table>
				<tr>
					<td><label for="j_username"><s:message code="auth.form.username" /></label></td>
					<td><input id="j_username" name="j_username" type="text" /></td>
				</tr>
				<tr>
					<td><label for="j_password"><s:message code="auth.form.password" /></label></td>
					<td><input id="j_password" name="j_password" type="password" /></td>
				</tr>
				<tr>
					<td></td>
					<td>
						<input id="remember_me" name="_spring_security_remember_me" type="checkbox" />
						<s:message code="auth.form.remember_me" />
					</td>
				</tr>
				<tr>
					<td></td>
					<td><input type="submit" value="<s:message code="auth.form.login" />"/></td>
				</tr>
			</table>						
		</form>
	</div>
</div>