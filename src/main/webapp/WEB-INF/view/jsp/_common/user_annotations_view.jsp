<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<div class="modal-header">
	<button aria-hidden="true" data-dismiss="modal" class="close" type="button">×</button>
	<h3 id="form-header-title">Comments and Actions</h3>
	<input type="hidden" value="865758" name="id" id="id">
</div>
<div class="modal-body">
	<%@ include file="incl/user_annotations.jsp" %>
</div>
<div class="modal-footer">
	<button type="reset" class="btn cancel form-btn-cancel" data-dismiss="modal" aria-hidden="true">
		<i class="icon-ban-circle icon-black"></i><span> Schließen</span>
	</button>
	<button type="submit" class="btn btn-primary start form-btn-submit">
		<i class="icon-upload icon-white"></i><span> Kommentar hinzufügen</span>
	</button>
</div>
