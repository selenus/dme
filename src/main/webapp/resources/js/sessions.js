var sessions;
$(document).ready(function() {
	sessions = new SessionHandler();
});

var SessionHandler = function() {
	__translator.addTranslations([
								"~eu.dariah.de.minfba.schereg.dialog.confirm_delete_session.head",
								"~eu.dariah.de.minfba.schereg.dialog.confirm_delete_session.body"
		]);
	//__translator.getTranslations();
	this.pathname = __util.getBaseUrl() + "sessions";
};

SessionHandler.prototype.newSession = function(entityId, callback) {
	var _this = this;
	$.ajax({
	    url: _this.pathname + "/async/newSession",
	    data: { entityId: entityId },
	    type: "GET",
	    success: function(data) {
	    	if (data.success) { 
	    		_this.handleCallback(callback, data);
	    	}
	    }, 
	    error: __util.processServerError
	});
};

SessionHandler.prototype.deleteSession = function(entityId, callback) {
	var _this = this;
	bootbox.confirm({
		title: __translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete_session.head"),
		message: __translator.translate("~eu.dariah.de.minfba.schereg.dialog.confirm_delete_session.body"),
		callback: function(result) {
			if(result) {
				$.ajax({
				    url: _this.pathname + "/async/deleteSession",
				    data: { entityId: entityId },
				    type: "GET",
				    success: function(data) {
				    	if (data.success) { 
				    		_this.handleCallback(callback, data);
				    	}
				    }, 
				    error: __util.processServerError
				});
			}
		}
	});
};

SessionHandler.prototype.loadSession = function(entityId, callback) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "load-session-" + entityId;
	
	modalFormHandler = new ModalFormHandler({
		formFullUrl: _this.pathname + "/form/loadSession?entityId=" + entityId,
		identifier: form_identifier,
		//additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		completeCallback: function() { window.location.reload(); }
	});
		
	modalFormHandler.show(form_identifier);
};

SessionHandler.prototype.saveSession = function(entityId, callback) {
	if (!__util.isLoggedIn()) {
		__util.showLoginNote();
		return;
	}
	
	var _this = this;
	var form_identifier = "save-session-" + entityId;
	
	modalFormHandler = new ModalFormHandler({
		formFullUrl: _this.pathname + "/form/saveSession?entityId=" + entityId,
		identifier: form_identifier,
		//additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		//completeCallback: function() { window.location.reload(); }
	});
		
	modalFormHandler.show(form_identifier);
};

SessionHandler.prototype.handleCallback = function(callback, data) {
	if (callback != undefined && typeof callback == 'function') {
		callback(data);
	}	
};